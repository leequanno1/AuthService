package com.project.q_authent.controllers;

import com.project.q_authent.requests.authify.AuthifyActiveUserRequest;
import com.project.q_authent.requests.authify.AuthifyNMAuthRequest;
import com.project.q_authent.responses.JsonResponse;
import com.project.q_authent.services.authify_services.AuthifySecurityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authify")
@AllArgsConstructor
public class AuthifyController {

    private final AuthifySecurityService authifySecurityService;

    @PostMapping("/nm-sign-up")
    public JsonResponse<String> normalSignUp(@RequestBody AuthifyNMAuthRequest request, HttpServletResponse servletResponse) throws Exception {
        return JsonResponse.success(authifySecurityService.normalSignUp(request, servletResponse));
    }

    @PostMapping("/active-user")
    public JsonResponse<String> activeUser(@RequestBody AuthifyActiveUserRequest request, HttpServletRequest servletRequest) throws Exception {
        return JsonResponse.success(authifySecurityService.activeUser(servletRequest, request.getActiveCode()));
    }

    @PostMapping("/nm-login")
    public JsonResponse<String> normalLogin(@RequestBody AuthifyNMAuthRequest request, HttpServletResponse servletResponse) throws Exception {
        return JsonResponse.success(authifySecurityService.normalLogin(request, servletResponse));
    }

    @PostMapping("/validate")
    public JsonResponse<String> validate() throws Exception {
        return JsonResponse.success(authifySecurityService.validate());
    }

    @PostMapping("/refresh")
    public JsonResponse<String> refresh(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws Exception {
        return JsonResponse.success(authifySecurityService.refresh(servletRequest, servletResponse));
    }
}
