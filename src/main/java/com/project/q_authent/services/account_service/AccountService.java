package com.project.q_authent.services.account_service;

import com.project.q_authent.constances.TableIdHeader;
import com.project.q_authent.dtos.AccountDTO;
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
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    @Transactional
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

    /**
     * Create sub user fron root user
     * @param username {@link String}
     * @param password {@link String}
     * @param email {@link String}
     * @return OK
     */
    public String createSubUser(String username, String password, String email) {

        String accountId = SecurityUtils.getCurrentUserId();

        if (Objects.isNull(accountId)) {
            throw new BadException(ErrorCode.UNAUTHORIZED);
        }

        Account parentAccount = accountRepository.findById(accountId).orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));
        String rootId = Objects.isNull(parentAccount.getRootId())? parentAccount.getAccountId() : parentAccount.getRootId();

        // check subuser exist
        if (accountRepository.countByRootIdAndUsernameOrEmail(rootId, username, email) != 0) {
            throw new BadException(ErrorCode.USER_EXISTED);
        }

        Account account = Account
                .builder()
                .accountId(IDUtil.getID(TableIdHeader.ACCOUNT_HEADER))
                .username(username)
                .displayName(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .active(false)
                .parentId(accountId)
                .rootId(rootId)
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .delFlag(false)
                .build();

        accountRepository.save(account);

        return  "OK";
    }

    /**
     * Show all sub-accounts of a parent account's ID.
     * First check current account is equal or higher level than the target parent.
     * Then show all sub accounts
     * @param parentId {@link String}
     * @return {@link List} of {@link AccountDTO}
     */
    public List<AccountDTO> getSubAccountByParentId(String parentId) {

        // check parent ship of parent ID
        // is root, is parent, is parent of parent,...
        Account targetParentAccount = accountRepository
                .findById(parentId)
                .orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));
        String currentUserId = Objects.requireNonNull(SecurityUtils.getCurrentUserId());
        boolean isHigherLevelParent = true;

        if (!currentUserId.equals(targetParentAccount.getAccountId())
                && !currentUserId.equals(targetParentAccount.getParentId())
                && !currentUserId.equals(targetParentAccount.getRootId())) {

            // loop to check if current account is a higher level parent
            Account checkedAccount = accountRepository.getReferenceById(targetParentAccount.getParentId());
            String rootId = checkedAccount.getRootId();
            while (!rootId.equals(checkedAccount.getParentId()) && !currentUserId.equals(checkedAccount.getParentId())) {
                checkedAccount = accountRepository.getReferenceById(checkedAccount.getParentId());
            }

            isHigherLevelParent = !rootId.equals(checkedAccount.getParentId());
        }

        if (!isHigherLevelParent) {
            throw new BadException(ErrorCode.UNAUTHORIZED);
        }

        // find all sub-account
        List<Account> subAccounts = accountRepository.findAllByParentIdAndDelFlag(parentId,false);
        return subAccounts.stream().map(AccountDTO::new).collect(Collectors.toList());
    }

    /**
     * Check exist account, throw exception if exist
     * @param rootId rootID
     * @param email email
     * @param username username
     * @return OK if have no account in root ID have email and username
     */
    public String checkAccountExists(String rootId, String email, String username) {

        if (!rootId.startsWith(TableIdHeader.ACCOUNT_HEADER.getValue())) {
            if (accountRepository.countAccountsByRootIdIsNullAndEmail(email) > 0) {
                throw new BadException(ErrorCode.EMAIL_USED);
            }

            if (accountRepository.countAccountsByRootIdIsNullAndUsername(username) > 0) {
                throw new BadException(ErrorCode.USER_EXISTED);
            }
        } else {
            if (accountRepository.countAccountsByRootIdAndEmail(rootId, email) > 0) {
                throw new BadException(ErrorCode.EMAIL_USED);
            }

            if (accountRepository.countAccountsByRootIdAndUsername(rootId, username) > 0) {
                throw new BadException(ErrorCode.USER_EXISTED);
            }
        }

        return "OK";
    }
}
