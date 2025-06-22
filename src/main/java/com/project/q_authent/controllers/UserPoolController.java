package com.project.q_authent.controllers;

import com.project.q_authent.requests.userpools.UserPoolRequest;
import com.project.q_authent.responses.JsonResponse;
import com.project.q_authent.constances.AuthField;
import com.project.q_authent.services.pool_services.UserPoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/user-pool")
@RequiredArgsConstructor
public class UserPoolController {
    private final UserPoolService userPoolService;

    /**
     * Create new user pool
     */
    @PostMapping("/create")
    public JsonResponse<String> createNewUserPool(@RequestBody UserPoolRequest request) throws Exception {
        return JsonResponse.success(userPoolService.createNewUserPool(request));
    }

    @GetMapping("/get-auth-field")
    public JsonResponse<String[]> getAuthField(){
        return JsonResponse.success(AuthField.fields);
    }

}
