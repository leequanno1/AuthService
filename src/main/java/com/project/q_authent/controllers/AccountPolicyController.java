package com.project.q_authent.controllers;

import com.project.q_authent.dtos.AccountPolicyDTO;
import com.project.q_authent.requests.policy.AccountPolicyRequest;
import com.project.q_authent.responses.JsonResponse;
import com.project.q_authent.services.account_policy_service.AccountPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account-policy")
@RequiredArgsConstructor
public class AccountPolicyController {

    private final AccountPolicyService accountPolicyService;

    /**
     * Create or update pool policy for target user's account.
     * If request has no policy id then update policy
     * @param request {@link AccountPolicyRequest}
     * @return OK
     */
    @PostMapping("/attach")
    public JsonResponse<String> createOrUpdateAccountPolicy(@RequestBody AccountPolicyRequest request) {
        return JsonResponse.success(accountPolicyService.createOrUpdateAccountPolicy(request));
    }

    /**
     * Find account policy by target account ID and pool ID
     * @param targetId userId {@link String}
     * @return DTO of account policy
     */
    @GetMapping("/get/{id}")
    public JsonResponse<AccountPolicyDTO> getAccountPolicyByTargetId(@PathVariable("id") String targetId) {
        return JsonResponse.success(accountPolicyService.getAccountPolicyByTargetId(targetId));
    }

}
