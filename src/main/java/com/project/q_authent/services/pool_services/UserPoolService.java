package com.project.q_authent.services.pool_services;

import com.project.q_authent.constances.TableIdHeader;
import com.project.q_authent.dtos.UserPoolDTO;
import com.project.q_authent.dtos.UserPoolDTOFull;
import com.project.q_authent.exceptions.BadException;
import com.project.q_authent.exceptions.ErrorCode;
import com.project.q_authent.models.sqls.Account;
import com.project.q_authent.models.sqls.UserPool;
import com.project.q_authent.repositories.AccountRepository;
import com.project.q_authent.repositories.UserPoolRepository;
import com.project.q_authent.requests.userpools.UserPoolRequest;
import com.project.q_authent.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

/**
 * User pool service
 * Last updated at 2025/07/26
 * @since 1.00
 * @author leequanno1
 */
@Service
@RequiredArgsConstructor
public class UserPoolService {

    private final AccountRepository accountRepository;
    private final UserPoolRepository userPoolRepository;
    private final AESGCMUtils aesgcmUtils;

    /**
     * Create new user pool
     * @param request {@link UserPoolRequest}
     * @return String OK if success, otherwise throw BadException
     * @throws Exception key encode error
     * @since 1.00
     */
    public String createNewUserPool(UserPoolRequest request) throws Exception {
        String userId = Objects.requireNonNull(SecurityUtils.getCurrentUserId());
        Account account = accountRepository.findById(userId).orElseThrow(
                () -> new BadException(ErrorCode.USER_NOT_FOUND)
        );
        UserPool userPool = UserPool.builder()
                .poolId(IDUtil.getID(TableIdHeader.USER_POOL_HEADER))
                .account(account)
                .userFields(request.getUserFields())
                .authorizeFields(request.getAuthorizeFields())
                .poolKey(RandomKeyGenerator.generateKeyBase64(128)) // Raw key
                .privateAccessKey(aesgcmUtils.encrypt(RSAKeyUtils.generateRsaPrivateKeyBase64(512))) //encrypt key
                .privateRefreshKey(aesgcmUtils.encrypt(RSAKeyUtils.generateRsaPrivateKeyBase64(512))) //encrypt key
                .poolName(request.getPoolName())
                .roleLevels(request.getRoleLevels())
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .delFlag(false)
                .accessExpiredMinutes(request.getAccessExpiredMinute())
                .refreshExpiredDays(request.getRefreshExpiredDay())
                .build();

        if (request.getEmailVerify() && !userPool.getUserFields().contains("email")) {
            throw new BadException(ErrorCode.FIELD_NEEDED);
        } else {
            userPool.setEmailVerify(request.getEmailVerify());
        }

        userPoolRepository.save(userPool);
        return "Ok";
    }

    public String deleteUserPool(String poolId) {
        String accountId = Objects.requireNonNull(SecurityUtils.getCurrentUserId());
        UserPool userPool = userPoolRepository.findById(poolId)
                .orElseThrow(() -> new BadException(ErrorCode.POOL_NOT_FOUND));
        if(accountId.equals(userPool.getAccount().getAccountId())) {
            if(userPool.getDelFlag()) {
                userPoolRepository.delete(userPool);
                // TODO: also delete all users in user schema that have poolId
            } else {
                userPool.setDelFlag(true);
                userPool.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                userPoolRepository.save(userPool);
            }
        } else {
            throw new BadException(ErrorCode.UNAUTHORIZED);
        }
        return "Ok";
    }

    public List<UserPoolDTO> getAllUserPool(boolean showDeleted) {
        String accountId = Objects.requireNonNull(SecurityUtils.getCurrentUserId());
        List<UserPool> userPools;
        if(showDeleted) {
            userPools = userPoolRepository.findUserPoolsByAccount_AccountId(accountId)
                    .orElseThrow(() -> new BadException(ErrorCode.POOL_NOT_FOUND));
        } else {
            userPools = userPoolRepository.findUserPoolsByAccount_AccountIdAndDelFlag(accountId, false)
                    .orElseThrow(() -> new BadException(ErrorCode.POOL_NOT_FOUND));
        }
        return userPools.stream().map(UserPoolDTO::new).toList();
    }

    public UserPoolDTOFull getPoolDetail(String poolId) throws Exception {
        String accountId = Objects.requireNonNull(SecurityUtils.getCurrentUserId());
        UserPool userPool = userPoolRepository.findById(poolId)
                .orElseThrow(() -> new BadException(ErrorCode.POOL_NOT_FOUND));
        if(!userPool.getAccount().getAccountId().equals(accountId)) {
            throw new BadException(ErrorCode.UNAUTHORIZED);
        }
        userPool.setPrivateAccessKey(aesgcmUtils.decrypt(userPool.getPrivateAccessKey()));
        userPool.setPrivateRefreshKey(aesgcmUtils.decrypt(userPool.getPrivateRefreshKey()));
        return new UserPoolDTOFull(userPool);
    }

    public String updateUserPool(UserPoolRequest request) {
        String accountId = Objects.requireNonNull(SecurityUtils.getCurrentUserId());
        UserPool userPool = userPoolRepository.findById(request.getPoolId())
                .orElseThrow(() -> new BadException(ErrorCode.POOL_NOT_FOUND));
        if(!userPool.getAccount().getAccountId().equals(accountId)) {
            throw new BadException(ErrorCode.UNAUTHORIZED);
        }
        userPool.setPoolName(request.getPoolName());
        userPool.setEmailVerify(request.getEmailVerify());
        if (request.getEmailVerify()) {
            // TODO: handle add email field to auth field
            System.out.println("Do nothing");
        }
        userPool.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        userPoolRepository.save(userPool);
        return "Ok";
    }
}
