package com.project.q_authent.controllers;

import com.project.q_authent.requests.account.ValidateCodeRequest;
import com.project.q_authent.requests.auth.RegisterRequest;
import com.project.q_authent.requests.auth.LoginRequest;
import com.project.q_authent.requests.auth.RefreshTokenRequest;
import com.project.q_authent.responses.JsonResponse;
import com.project.q_authent.responses.auth.TokenResponse;
import com.project.q_authent.services.authent_services.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller
 * Last update 2025/07/26
 * @since 1.00
 * @author leequanno1
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SecurityService securityService;

    /**
     * Handle registration for user using service
     * @param request RegisterRequest
     * @return JsonResponse<String> result
     * @since 1.00
     */
    @PostMapping("/register")
    public JsonResponse<String> register(@RequestBody RegisterRequest request) {
        return JsonResponse.success(securityService.register(request));
    }

    /**
     * Handle login for user using service
     * @param request {@link LoginRequest}
     * @return JsonResponse<TokenResponse> result
     * @since 1.00
     */
    @PostMapping("/login")
    public JsonResponse<TokenResponse> login(@RequestBody LoginRequest request) {
        return JsonResponse.success(securityService.login(request.getUsername(), request.getPassword()));
    }

    /**
     * Refresh to get new access token and refresh token
     * @param request {@link RefreshTokenRequest}
     * @return JsonResponse<TokenResponse> result
     * @since 1.00
     */
    @PostMapping("/refresh")
    public JsonResponse<TokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return JsonResponse.success(securityService.refreshToken(request.getRefreshToke()));
    }

    @GetMapping("/hello-word")
    public JsonResponse<String> hello(){
        return JsonResponse.success("Hello world");
    }

    @PostMapping("/active-user")
    public JsonResponse<TokenResponse> activeSubUser(@RequestBody ValidateCodeRequest request) {

        return JsonResponse.success(securityService.activeSubUser(request.getAccountId(), request.getCode()));
    }

}
