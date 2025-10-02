package com.project.q_authent.services.account_service;

import com.project.q_authent.constances.TableIdHeader;
import com.project.q_authent.exceptions.BadException;
import com.project.q_authent.exceptions.ErrorCode;
import com.project.q_authent.models.sqls.Account;
import com.project.q_authent.models.sqls.ValidationCode;
import com.project.q_authent.repositories.AccountRepository;
import com.project.q_authent.repositories.ValidationCodeRepository;
import com.project.q_authent.services.notificaton_service.EmailService;
import com.project.q_authent.utils.CodeGeneratorUtils;
import com.project.q_authent.utils.IDUtil;
import com.project.q_authent.utils.SecurityUtils;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Account Service
 * Last update 2025/10/02
 * @since 1.00
 * @author leequanno1
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    private final ValidationCodeRepository validationCodeRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    /**
     * Precess reset password when forgot pass
     * @param validationID validation Id {@link String}
     * @param newPassword new account password {@link String}
     * @return "OK" if success
     */
    public String resetForgotPassword(String validationID, String newPassword) {

        ValidationCode validationCode = validationCodeRepository
                .findById(validationID)
                .orElseThrow(() -> new BadException(ErrorCode.VALIDATION_CODE_NOT_FOUND));

        if (!validationCode.getIsUsed() || Objects.isNull(SecurityUtils.getCurrentUserId())) {
            throw new BadException(ErrorCode.SOMETHING_WRONG);
        }
        // save new password

        Account account = validationCode.getTargetAccount();
        account.setPassword(passwordEncoder.encode(newPassword));

        accountRepository.save(account);
        validationCodeRepository.delete(validationCode);
        return "OK";
    }

    /**
     *
     * @param accountID include accountID {@link String}
     * @param code and validationCode {@link String}
     * @return validationCodeID {@link String}
     */
    public String validationEmailCode(String accountID, String code) {

        // find validation code of account has lastest expired time
        ValidationCode validationCode = validationCodeRepository
                .findByTargetAccount_AccountIdOrderByExpireTimeDesc(accountID)
                .orElseThrow(() -> new BadException(ErrorCode.VALIDATION_CODE_NOT_FOUND));
        // check expired time and code value
        if (validationCode.getExpireTime().before(Timestamp.valueOf(LocalDateTime.now()))) {
            throw new BadException(ErrorCode.VALIDATION_CODE_EXPIRED);
        }

        if (!validationCode.getCodeValue().equals(Integer.parseInt(code))) {
            throw new BadException(ErrorCode.INVALID_TOKEN);
        }
        // set is_used = true
        validationCode.setIsUsed(true);
        validationCodeRepository.save(validationCode);

        return validationCode.getValidatedCodeId();
    }

    /**
     * Send code to account's email
     * @param emailOrUsername {@link String}
     * @return accountId {@link String}
     * @throws MessagingException ex
     */
    public String sendCode(String emailOrUsername) throws MessagingException {
        // get account by email or username
        Account account = accountRepository
                .findByEmailOrUsername(emailOrUsername, emailOrUsername)
                .orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));
        // delete all code from account, create code, save db
        validationCodeRepository.deleteAllByTargetAccount(account);
        ValidationCode validationCode = ValidationCode
                .builder()
                .validatedCodeId(IDUtil.getID(TableIdHeader.VALIDATION_CODE_HEADER))
                .targetAccount(account)
                .codeValue(CodeGeneratorUtils.generateCode())
                .isUsed(false)
                .expireTime(Timestamp.valueOf(LocalDateTime.now().plusMinutes(5)))
                .build();
        validationCodeRepository.save(validationCode);
        // send code to email
        emailService.sendValidationCode(account.getEmail(), validationCode.getCodeValue());
        // return account id
        return account.getAccountId();
    }

    /**
     *
     * @param oldPassword String
     * @param newPassword String
     * @return OK
     */
    public String changePassword(String oldPassword, String newPassword) {

        String accountId = SecurityUtils.getCurrentUserId();
        if (Objects.isNull(oldPassword) || Objects.isNull(newPassword) ||  Objects.isNull(accountId)) {
            throw new BadException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        Account account = accountRepository
                .findById(accountId)
                .orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));

        if (account.getPassword().equals(passwordEncoder.encode(oldPassword))) {
            account.setPassword(passwordEncoder.encode(newPassword));
            accountRepository.save(account);
        } else {
            throw new BadException(ErrorCode.INVALID_PASSWORD);
        }

        return "OK";
    }
}
