package com.project.q_authent.services.pool_services;

import com.project.q_authent.constances.AuthField;
import com.project.q_authent.constances.TableIdHeader;
import com.project.q_authent.dtos.UserPoolDTOFull;
import com.project.q_authent.dtos.authify.UserDTO;
import com.project.q_authent.exceptions.BadException;
import com.project.q_authent.exceptions.ErrorCode;
import com.project.q_authent.models.nosqls.User;
import com.project.q_authent.models.sqls.Account;
import com.project.q_authent.models.sqls.UserPool;
import com.project.q_authent.models.sqls.UserPoolPolicy;
import com.project.q_authent.repositories.AccountRepository;
import com.project.q_authent.repositories.UserPoolPolicyRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final UserPoolPolicyRepository userPoolPolicyRepository;
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
        if (!userPoolRepository.findAllByAccountAndPoolNameAndDelFlag(account, request.getPoolName(), Boolean.FALSE).isEmpty()) {
            throw new BadException(ErrorCode.POOL_NAME_EXISTED);
        }
        String newPoolID = IDUtil.getID(TableIdHeader.USER_POOL_HEADER);
        UserPool userPool = UserPool.builder()
                .poolId(newPoolID)
                .account(account)
                .userFields(JsonUtils.toJson(request.getUserFields()))
                .authorizeFields(JsonUtils.toJson(request.getAuthorizeFields()))
                .poolKey(RandomKeyGenerator.generateKeyBase64(128)) //encrypt key
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
            userPoolPolicyRepository.deleteByUserPool_PoolId(userPool.getPoolId());
        } else {
            throw new BadException(ErrorCode.UNAUTHORIZED);
        }
        return "Ok";
    }

    public List<UserPoolDTOFull> getAllUserPool(boolean showDeleted) {
        String accountId = Objects.requireNonNull(SecurityUtils.getCurrentUserId());
        //Get account and get root ID
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));
        String rootId = Objects.isNull(account.getRootId()) ? account.getAccountId() : account.getRootId();
        List<UserPool> userPools;
        if(showDeleted) {
            userPools = userPoolRepository.findUserPoolsByAccount_AccountId(rootId)
                    .orElseThrow(() -> new BadException(ErrorCode.POOL_NOT_FOUND));
        } else {
            userPools = userPoolRepository.findUserPoolsByAccount_AccountIdAndDelFlag(rootId, false)
                    .orElseThrow(() -> new BadException(ErrorCode.POOL_NOT_FOUND));
        }
        return userPools.stream().map((item) -> {
            try {
                return new UserPoolDTOFull(item, aesgcmUtils);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).toList();
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

        String accountId = Objects.requireNonNull(SecurityUtils.getCurrentUserId());
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));
        // find pool with id
        UserPool userPool = userPoolRepository
                .findById(request.getPoolId())
                .orElseThrow(() -> new BadException(ErrorCode.POOL_NOT_FOUND));
        // TODO: check policy, or is root to verify user can edit
        // check root
        if (!Objects.isNull(account.getRootId())) {
            // check policy
            UserPoolPolicy plc = userPoolPolicyRepository
                    .findByAccount_AccountIdAndUserPool_PoolIdAndDelFlag(accountId, request.getPoolId(), false)
                    .orElseThrow(() -> new BadException(ErrorCode.POLICY_NOT_FOUND));
            if (!plc.getCanEdit()) {
                throw new BadException(ErrorCode.UNAUTHORIZED);
            }
        }
        // check name duplicate
        UserPool checkedPool = userPoolRepository.findUserPoolByAccount_AccountIdAndPoolNameAndDelFlag(userPool.getAccount().getAccountId(), request.getPoolName(), false).orElse(null);
        if (!Objects.isNull(checkedPool) && !checkedPool.getPoolId().equals(request.getPoolId())) {
            throw new BadException(ErrorCode.POOL_NAME_EXISTED);
        }

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

    @Transactional
    public String deleteUserPools(List<String> poolIds) {
        String accountId = Objects.requireNonNull(SecurityUtils.getCurrentUserId());
        List<UserPool> userPools = userPoolRepository.findAllByPoolIdIsIn(poolIds);
        for (UserPool userPool : userPools) {
            if(accountId.equals(userPool.getAccount().getAccountId())) {
                if(userPool.getDelFlag()) {
                    userRepository.deleteByPoolId(userPool.getPoolId());
                    userPoolRepository.delete(userPool);
                } else {
                    userPool.setDelFlag(true);
                    userPool.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                    userPoolRepository.save(userPool);
                }
                // delete referent policy
                userPoolPolicyRepository.deleteByUserPool_PoolId(userPool.getPoolId());
            } else {
                throw new BadException(ErrorCode.UNAUTHORIZED);
            }
        }
        return "Ok";
    }

    /**
     * look in pool policies for pool have account id parent ID equal request account id
     * @param accID acc ID
     * @return {@link List}
     */
    public List<UserPoolDTOFull> getByAttachedByAccID(String accID) {
        // get policies with target ID equal accID
        List<UserPoolPolicy> poolPolicies = userPoolPolicyRepository.findAllByAccount_AccountIdAndDelFlag(accID, false);

        if(Objects.isNull(poolPolicies) || poolPolicies.isEmpty()) {
            throw new BadException(ErrorCode.POOL_NOT_FOUND);
        }

        return poolPolicies.stream().map((plc) -> {
            try {
                return new UserPoolDTOFull(plc.getUserPool(), aesgcmUtils);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).toList();
    }

    /**
     * Get all users by poolId.
     * Check user is root or have authority to manage pool, otherwise throw.
     * @param poolId {@link String}
     * @return List UserDTO
     */
    public List<UserDTO> getAllUsers(String poolId) {

        String accountId = Objects.requireNonNull(SecurityUtils.getCurrentUserId());
        Account crAccount = accountRepository.findById(accountId).orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));

        // not root account
        if (Objects.isNull(crAccount.getRootId())) {
            // get policy to check authority
            UserPoolPolicy poolPolicy = userPoolPolicyRepository
                    .findByAccount_AccountIdAndUserPool_PoolIdAndDelFlag(crAccount.getAccountId(), poolId, Boolean.FALSE)
                    .orElse(null);
            if (Objects.isNull(poolPolicy) || !poolPolicy.getCanManage()) {
                throw new BadException(ErrorCode.UNAUTHORIZED);
            }
        }

        List<User> users = userRepository.findAllByPoolIdAndDelFlag(poolId, Boolean.FALSE);

        return users.stream().map(UserDTO::new).toList();
    }
}
