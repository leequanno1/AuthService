package com.project.q_authent.services.pool_policy_service;

import com.project.q_authent.constances.TableIdHeader;
import com.project.q_authent.dtos.AccountDTO;
import com.project.q_authent.dtos.UserPoolPolicyDTO;
import com.project.q_authent.exceptions.BadException;
import com.project.q_authent.exceptions.ErrorCode;
import com.project.q_authent.models.sqls.Account;
import com.project.q_authent.models.sqls.UserPool;
import com.project.q_authent.models.sqls.UserPoolPolicy;
import com.project.q_authent.repositories.AccountRepository;
import com.project.q_authent.repositories.UserPoolPolicyRepository;
import com.project.q_authent.repositories.UserPoolRepository;
import com.project.q_authent.requests.policy.PoolPolicyRequest;
import com.project.q_authent.utils.IDUtil;
import com.project.q_authent.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPoolPolicyService {

    private final UserPoolPolicyRepository userPoolPolicyRepository;

    private final AccountRepository accountRepository;

    private final UserPoolRepository userPoolRepository;

    /**
     * Create or update pool policy for target user's account.
     * If request has no policy id then update policy
     * @param request {@link PoolPolicyRequest}
     * @return OK
     */
    public String addOrUpdatePolicy(PoolPolicyRequest request) {

        UserPoolPolicy userPoolPolicy;
        Account attachAccount = accountRepository
                .findById(Objects.requireNonNull(SecurityUtils.getCurrentUserId()))
                .orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));
        Account targetAccount = accountRepository
                .findById(request.getTargetAccountId())
                .orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));
        UserPool userPool = userPoolRepository
                .findById(request.getPoolId())
                .orElseThrow(() -> new BadException(ErrorCode.POOL_NOT_FOUND));

        // check right to edit policy: only parent or root can edit policy
        if (!attachAccount.getAccountId().equals(targetAccount.getParentId())
                && !attachAccount.getAccountId().equals(targetAccount.getRootId()))
        {
            throw new BadException(ErrorCode.UNAUTHORIZED);
        }
        // check if policy exist
        if (!Objects.isNull(request.getPolicyId()) && !request.getPolicyId().isBlank()) {
            userPoolPolicy = userPoolPolicyRepository
                    .findById(request.getPolicyId())
                    .orElseThrow(() -> new BadException(ErrorCode.POLICY_NOT_FOUND));

            // update
            userPoolPolicy.setLastEditor(attachAccount);
            userPoolPolicy.setCanView(request.getCanView());
            userPoolPolicy.setCanEdit(request.getCanEdit());
            userPoolPolicy.setCanManage(request.getCanManage());
            userPoolPolicy.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        } else {
            // create new
            Account rootAccount;
            if (Objects.isNull(attachAccount.getRootId())) {
                rootAccount = attachAccount;
            } else {
                rootAccount = accountRepository
                        .findById(attachAccount.getRootId())
                        .orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));
            }

            userPoolPolicy = UserPoolPolicy
                    .builder()
                    .policyId(IDUtil.getID(TableIdHeader.POOL_POLICY_HEADER))
                    .root(rootAccount)
                    .creator(attachAccount)
                    .lastEditor(attachAccount)
                    .account(targetAccount)
                    .userPool(userPool)
                    .canView(request.getCanView())
                    .canEdit(request.getCanEdit())
                    .canManage(request.getCanManage())
                    .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                    .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                    .delFlag(false)
                    .build();
        }

        userPoolPolicyRepository.save(userPoolPolicy);

        return "OK";
    }

    /**
     * Find pool policy by target account ID and pool ID
     * @param targetId userId {@link String}
     * @param poolId poolId {@link String}
     * @return DTO of pool policy
     */
    public UserPoolPolicyDTO getPolicyByTargetId(String targetId, String poolId) {

        UserPoolPolicy userPoolPolicy = userPoolPolicyRepository
                .findByAccount_AccountIdAndUserPool_PoolId(targetId, poolId)
                .orElseThrow(() -> new BadException(ErrorCode.POOL_NOT_FOUND));

        return new UserPoolPolicyDTO(userPoolPolicy);
    }

    public List<UserPoolPolicyDTO> getPolicyByTargetId(String targetId) {

        List<UserPoolPolicy> userPoolPolicies = userPoolPolicyRepository
                .findAllByAccount_AccountId(targetId)
                .orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));

        return userPoolPolicies.stream().map(UserPoolPolicyDTO::new).collect(Collectors.toList());
    }

    public List<AccountDTO> getPolicyByParentAndPoolID(String parentId, String poolId) {
        List<UserPoolPolicy> userPoolPolicies = userPoolPolicyRepository
                .findAllByCreator_AccountIdAndUserPool_PoolId(parentId, poolId)
                .orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));

        return userPoolPolicies.stream().map((item) -> new AccountDTO(item.getAccount())).collect(Collectors.toList());
    }
}
