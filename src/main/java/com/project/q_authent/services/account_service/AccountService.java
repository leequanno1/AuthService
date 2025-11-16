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
import java.util.ArrayList;
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
        emailService.sendValidationCode(account.getEmail(), "Login", validationCode.getCodeValue().toString());
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

        if (passwordEncoder.matches(oldPassword, account.getPassword())) {
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
     * @return sub-user ID
     */
    public String createSubUser(String username, String password, String email) {

        String accountId = SecurityUtils.getCurrentUserId();

        if (Objects.isNull(accountId)) {
            throw new BadException(ErrorCode.UNAUTHORIZED);
        }

        Account parentAccount = accountRepository.findById(accountId).orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));
        String rootId = Objects.isNull(parentAccount.getRootId())? parentAccount.getAccountId() : parentAccount.getRootId();

        if (!accountRepository.findAllByRootIdAndUsername(rootId, username).orElse(new ArrayList<>()).isEmpty()) {
            throw new BadException(ErrorCode.USER_EXISTED);
        }
        if (!accountRepository.findAllByRootIdAndEmail(rootId, email).orElse(new ArrayList<>()).isEmpty()) {
            throw new BadException(ErrorCode.EMAIL_USED);
        }

        String newAccountId = IDUtil.getID(TableIdHeader.ACCOUNT_HEADER);
        Account account = Account
                .builder()
                .accountId(newAccountId)
                .username(username)
                .displayName(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .active(true)
                .parentId(accountId)
                .rootId(rootId)
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .delFlag(false)
                .build();

        accountRepository.save(account);

        return  newAccountId;
    }

    /**
     * Show all subaccounts of a parent account's ID.
     * First check current account is equal or higher level than the target parent.
     * Then show all subaccounts
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

        // find all subaccount
        List<Account> subAccounts = accountRepository.findAllByParentIdAndDelFlag(parentId,false);
        return subAccounts.stream().map(AccountDTO::new).collect(Collectors.toList());
    }

    /**
     * Check exist account, throw exception if exist
     * @param rootId rootID
     * @param email email
     * @param username username
     * @return OK if there have no account in root ID have email and username
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

    public AccountDTO getRootAccount() {

        String accountId = Objects.requireNonNull(SecurityUtils.getCurrentUserId());
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));
        AccountDTO rootAccount;
        if (Objects.isNull(account.getRootId())) {
            rootAccount = new AccountDTO(account);
        } else {
            rootAccount = new AccountDTO(accountRepository.findById(account.getRootId()).orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND)));
        }
        return rootAccount;
    }

    /**
     * Logical delete accounts by ID
     * @param accountIds {@link List} account ID
     * @param isDeleteSubAccounts {@link Boolean} true if they want to logical delete all child's subaccounts
     * @return "OK" if success
     */
    @Transactional
    public String logicalDeleteAccounts(List<String> accountIds, Boolean isDeleteSubAccounts) {

        String prAccountID = Objects.requireNonNull(SecurityUtils.getCurrentUserId());
        Account prAccount = accountRepository.findById(prAccountID)
                .orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));

        // Get all subaccounts
        List<Account> subAccounts = accountRepository.findAllById(accountIds);
        for (Account subAccount : subAccounts) {
            if (Objects.isNull(subAccount.getParentId()) || !prAccount.getAccountId().equals(subAccount.getParentId())) {
                throw new BadException(ErrorCode.UNAUTHORIZED);
            }
            subAccount.setDelFlag(true);
        }
        accountRepository.saveAll(subAccounts);
        if (isDeleteSubAccounts) {
            subAccounts =  accountRepository.findAllByParentIdInAndDelFlag(subAccounts.stream().map(Account::getAccountId).collect(Collectors.toList()),false);
            while (!Objects.isNull(subAccounts) &&  !subAccounts.isEmpty()) {
                // logical delete
                for (Account subAccount : subAccounts) {
                    subAccount.setDelFlag(true);
                }
                accountRepository.saveAll(subAccounts);
                // find lower subaccounts
                subAccounts =  accountRepository.findAllByParentIdInAndDelFlag(subAccounts.stream().map(Account::getAccountId).collect(Collectors.toList()), false);
            }
        }

        return "OK";
    }

    /**
     * Get account by account id
     * @param accountId string
     * @return {@link AccountDTO}
     */
    public AccountDTO getAccountById(String accountId) {

        String requestAccID = SecurityUtils.getCurrentUserId();
        if (!isParentOrHigher(accountId, requestAccID)) {
            throw new BadException(ErrorCode.UNAUTHORIZED);
        }

        Account account = accountRepository.findById(accountId).orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));

        return new AccountDTO(account);
    }

    /**
     * Check a higherID account is parent or higher lvl than the lower account
     * @param lowerID {@link String} ID
     * @param higherID {@link String} ID
     * @return return true if higherID is actually higher, otherwise false
     */
    public boolean isParentOrHigher(String lowerID, String higherID) {
        Account lowerAccount = accountRepository.findById(lowerID).orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));
        // check if lw acc is root then return false
        if (Objects.isNull(lowerAccount.getRootId())) {
            return false;
        }

        Account higherAccount = accountRepository.findById(higherID).orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));

        while (!Objects.isNull(lowerAccount.getRootId())) {
            if(lowerAccount.getParentId().equals(higherAccount.getAccountId()) || lowerAccount.getRootId().equals(higherAccount.getAccountId())) {
                return true;
            } else {
                lowerAccount = accountRepository.findById(lowerAccount.getParentId()).orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));
            }
        }
        return false;
    }

    /**
     * Toggle account status by accountID and new status
     * @param accountId account ID
     * @param accStatus account status
     * @return OK
     */
    public String toggleStatus(String accountId, Boolean accStatus) {
        // check authority
        String crrAccountId = SecurityUtils.getCurrentUserId();
        if(!isParentOrHigher(accountId, crrAccountId)) {
            throw new BadException(ErrorCode.UNAUTHORIZED);
        }
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));
        account.setActive(accStatus);
        accountRepository.save(account);
        return "OK";
    }

    /**
     * Reset subaccount password
     * @param targetAccountId subaccount ID
     * @param newPassword new password
     * @return OK
     */
    public String resetSubAccountPassword(String targetAccountId, String newPassword) {
        String crrAccountId = SecurityUtils.getCurrentUserId();
        if(!isParentOrHigher(targetAccountId, crrAccountId)) {
            throw new BadException(ErrorCode.UNAUTHORIZED);
        }

        Account account = accountRepository.findById(targetAccountId).orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
        return "OK";
    }

    public String updateDisplayName(String displayName) {

        String accountId = Objects.requireNonNull(SecurityUtils.getCurrentUserId());
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));

        account.setDisplayName(displayName);
        accountRepository.save(account);
        return "OK";
    }
}
