package com.project.q_authent.controllers;

import com.project.q_authent.dtos.UserPoolPolicyDTO;
import com.project.q_authent.requests.policy.PoolPolicyRequest;
import com.project.q_authent.responses.JsonResponse;
import com.project.q_authent.services.pool_policy_service.UserPoolPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("/api/pool-policy")
@RequiredArgsConstructor
public class UserPoolPolicyController {

    private final UserPoolPolicyService userPoolPolicyService;

    /**
     * Create or update pool policy for target user's account
     * @param request {@link PoolPolicyRequest}
     * @return OK
     */
    @PostMapping("/attach")
    public JsonResponse<String> attachPolicy(@RequestBody PoolPolicyRequest request) {

        return JsonResponse.success(userPoolPolicyService.addOrUpdatePolicy(request));
    }

    /**
     * Find pool policy by target account ID and pool ID
     * @param targetId userId {@link String}
     * @param poolId poolId {@link String}
     * @return DTO of pool policy
     */
    @GetMapping("/get/{user-id}/{pool-id}")
    public JsonResponse<UserPoolPolicyDTO> getPolicyByTargetId(@PathVariable("user-id") String targetId, @PathVariable("pool-id") String poolId) {

        return JsonResponse.success(userPoolPolicyService.getPolicyByTargetId(targetId, poolId));
    }


}
