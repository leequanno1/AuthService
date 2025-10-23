package com.project.q_authent.services.pool_services;

import com.project.q_authent.constances.AuthField;
import com.project.q_authent.constances.TableIdHeader;
import com.project.q_authent.dtos.UserPoolDTO;
import com.project.q_authent.dtos.UserPoolDTOFull;
import com.project.q_authent.exceptions.BadException;
import com.project.q_authent.exceptions.ErrorCode;
import com.project.q_authent.models.sqls.Account;
import com.project.q_authent.models.sqls.UserPool;
import com.project.q_authent.repositories.AccountRepository;
import com.project.q_authent.repositories.UserPoolRepository;
import com.project.q_authent.repositories.UserRepository;
import com.project.q_authent.requests.userpools.UserPoolRequest;
import com.project.q_authent.utils.AESGCMUtils;
import com.project.q_authent.utils.IDUtil;
import com.project.q_authent.utils.JsonUtils;
import com.project.q_authent.utils.SecurityUtils;
import com.project.q_authent.utils.RandomKeyGenerator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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
    private final UserRepository userRepository;
    private final AESGCMUtils aesgcmUtils;

    /**
     * Create new user pool
     * @param request {@link UserPoolRequest}
     * @return String PoolID if success, otherwise throw BadException
     * @throws Exception key encode error
     * @since 1.00
     */
    public String createNewUserPool(UserPoolRequest request) throws Exception {
        String userId = Objects.requireNonNull(SecurityUtils.getCurrentUserId());
        Account account = accountRepository.findById(userId).orElseThrow(
                () -> new BadException(ErrorCode.USER_NOT_FOUND)
        );
        // Check if any pool has the same name
        if (!userPoolRepository.findAllByAccountAndPoolName(account, request.getPoolName()).isEmpty()) {
            throw new BadException(ErrorCode.POOL_NAME_EXISTED);
        }
        String newPoolID = IDUtil.getID(TableIdHeader.USER_POOL_HEADER);
        UserPool userPool = UserPool.builder()
                .poolId(newPoolID)
                .account(account)
                .userFields(JsonUtils.toJson(request.getUserFields()))
                .authorizeFields(JsonUtils.toJson(request.getAuthorizeFields()))
                .poolKey(aesgcmUtils.encrypt(RandomKeyGenerator.generateKeyBase64(128))) //encrypt key
                .privateAccessKey(aesgcmUtils.encrypt(RandomKeyGenerator.generateKeyBase64(512))) //encrypt key
                .privateRefreshKey(aesgcmUtils.encrypt(RandomKeyGenerator.generateKeyBase64(512))) //encrypt key
                .poolName(request.getPoolName())
                .roleLevels(JsonUtils.toJson(request.getRoleLevels()))
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .delFlag(false)
                .accessExpiredMinutes(request.getAccessExpiredMinutes())
                .refreshExpiredDays(request.getRefreshExpiredDays())
                .build();

        if (request.getEmailVerify() && !userPool.getUserFields().contains("email")) {
            throw new BadException(ErrorCode.FIELD_NEEDED);
        } else {
            userPool.setEmailVerify(request.getEmailVerify());
        }

        userPoolRepository.save(userPool);
        return newPoolID;
    }

    public String deleteUserPool(String poolId) {
        String accountId = Objects.requireNonNull(SecurityUtils.getCurrentUserId());
        UserPool userPool = userPoolRepository.findById(poolId)
                .orElseThrow(() -> new BadException(ErrorCode.POOL_NOT_FOUND));
        if(accountId.equals(userPool.getAccount().getAccountId())) {
            if(userPool.getDelFlag()) {
                userRepository.deleteByPoolId(userPool.getPoolId());
                userPoolRepository.delete(userPool);
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
        return new UserPoolDTOFull(userPool, aesgcmUtils);
    }

    /**
     * Update 2 edit available field {poolName, emailVerify}.
     * If emailVerify is true and userFields dont have "email" so add one
     * @param poolId {@link String}
     * @param poolName {@link String}
     * @param emailVerified {@link Boolean}
     * @return OK
     */
    public String updateUserPool(String poolId, String poolName, Boolean emailVerified) {
        String accountId = Objects.requireNonNull(SecurityUtils.getCurrentUserId());
        UserPool userPool = userPoolRepository.findById(poolId)
                .orElseThrow(() -> new BadException(ErrorCode.POOL_NOT_FOUND));
        if(!userPool.getAccount().getAccountId().equals(accountId)) {
            throw new BadException(ErrorCode.UNAUTHORIZED);
        }

        // TODO: check policy to verify user can edit
        // Only do if poolName not null or empty
        if (!Objects.isNull(poolName) && !poolName.isEmpty()) {
            userPool.setPoolName(poolName);
        }
        // Only do if emailVerified not null
        if (!Objects.isNull(emailVerified)) {
            userPool.setEmailVerify(emailVerified);
            List<String> authentFields = JsonUtils.fromJson(userPool.getAuthorizeFields());
            if (emailVerified) {
                List<String> userFields = JsonUtils.fromJson(userPool.getUserFields());
                if (!userFields.contains(AuthField.EMAIL)) {
                    userFields.add(AuthField.EMAIL);
                }
                if (!authentFields.contains(AuthField.EMAIL)) {
                    userFields.add(AuthField.EMAIL);
                }
            } else {
                authentFields.remove(AuthField.EMAIL);
            }
        }

        userPool.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        userPoolRepository.save(userPool);
        return "OK";
    }

    public String advanceUpdateUserPool(UserPoolRequest request) {

        // find pool with id
        UserPool userPool = userPoolRepository
                .findById(request.getPoolId())
                .orElseThrow(() -> new BadException(ErrorCode.POOL_NOT_FOUND));
        // TODO: check policy, or is root to verify user can edit
        // set pool properties
        userPool.setUserFields(JsonUtils.toJson(request.getUserFields()));
        userPool.setAuthorizeFields(JsonUtils.toJson(request.getAuthorizeFields()));
        userPool.setPoolName(request.getPoolName());
        userPool.setEmailVerify(request.getEmailVerify());
        userPool.setRoleLevels(JsonUtils.toJson(request.getRoleLevels()));
        userPool.setAccessExpiredMinutes(request.getAccessExpiredMinutes());
        userPool.setRefreshExpiredDays(request.getRefreshExpiredDays());
        userPool.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        userPoolRepository.save(userPool);

        return "OK";
    }
}
