package com.project.q_authent.services.account_policy_service;

import com.project.q_authent.constances.TableIdHeader;
import com.project.q_authent.dtos.AccountPolicyDTO;
import com.project.q_authent.exceptions.BadException;
import com.project.q_authent.exceptions.ErrorCode;
import com.project.q_authent.models.sqls.Account;
import com.project.q_authent.models.sqls.AccountPolicy;
import com.project.q_authent.repositories.AccountPolicyRepository;
import com.project.q_authent.repositories.AccountRepository;
import com.project.q_authent.requests.policy.AccountPolicyRequest;
import com.project.q_authent.utils.IDUtil;
import com.project.q_authent.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AccountPolicyService {

    private final AccountPolicyRepository accountPolicyRepository;

    private final AccountRepository accountRepository;

    /**
     * Create or update pool policy for target user's account.
     * If request has no policy id then update policy
     * @param request {@link AccountPolicyRequest}
     * @return OK
     */
    public String createOrUpdateAccountPolicy(AccountPolicyRequest request) {

        String currentAccId = Objects.requireNonNull(SecurityUtils.getCurrentUserId());
        Account attachAccount = accountRepository
                .findById(currentAccId)
                .orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));
        Account targetAccount = accountRepository
                .findById(request.getTargetAccountId())
                .orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));

        // check right to edit policy: only parent or root can edit policy
        if (!attachAccount.getAccountId().equals(targetAccount.getParentId())
                && !attachAccount.getAccountId().equals(targetAccount.getRootId())) {
            throw new BadException(ErrorCode.UNAUTHORIZED);
        }
        // check policy exist
        AccountPolicy accountPolicy;
        if (!Objects.isNull(request.getPolicyId()) && !request.getPolicyId().isBlank()) {
            //update
            accountPolicy = accountPolicyRepository
                    .findById(request.getPolicyId())
                    .orElseThrow(() -> new BadException(ErrorCode.POLICY_NOT_FOUND));

            accountPolicy.setLastEditor(attachAccount);
            accountPolicy.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
            accountPolicy.setCanCreate(request.getCanCreate());
            accountPolicy.setCanView(request.getCanView());
            accountPolicy.setCanDelete(request.getCanDelete());
        } else {
            //add
            Account rootAccount;
            if (targetAccount.getRootId().equals(currentAccId)) {
                rootAccount = attachAccount;
            } else {
                rootAccount = accountRepository
                        .findById(targetAccount.getRootId())
                        .orElseThrow(() -> new BadException(ErrorCode.USER_NOT_FOUND));
            }
            accountPolicy = AccountPolicy
                    .builder()
                    .policyId(IDUtil.getID(TableIdHeader.ACCOUNT_POLICY_HEADER))
                    .targetAccount(targetAccount)
                    .creator(attachAccount)
                    .root(rootAccount)
                    .lastEditor(attachAccount)
                    .canCreate(request.getCanCreate())
                    .canView(request.getCanView())
                    .canDelete(request.getCanDelete())
                    .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                    .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                    .delFlag(false)
                    .build();
        }

        accountPolicyRepository.save(accountPolicy);

        return "OK";
    }
    /**
     * Find account policy by target account ID and pool ID
     * @param targetId userId {@link String}
     * @return DTO of account policy
     */
    public AccountPolicyDTO getAccountPolicyByTargetId(String targetId) {

        AccountPolicy accountPolicy = accountPolicyRepository
                .findByTargetAccount_AccountId(targetId)
                .orElseThrow(() -> new BadException(ErrorCode.POLICY_NOT_FOUND));

        return new AccountPolicyDTO(accountPolicy);
    }
}
