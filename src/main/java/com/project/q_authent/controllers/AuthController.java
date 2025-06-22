package com.project.q_authent.controllers;

import com.project.q_authent.requests.auth.RegisterRequest;
import com.project.q_authent.requests.auth.LoginRequest;
import com.project.q_authent.requests.auth.RefreshTokenRequest;
import com.project.q_authent.responses.JsonResponse;
import com.project.q_authent.responses.auth.TokenResponse;
import com.project.q_authent.services.authent_services.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SecurityService securityService;

    @PostMapping("/register")
    public JsonResponse<String> register(@RequestBody RegisterRequest request) {
        return JsonResponse.success(securityService.register(request));
    }

    @PostMapping("/login")
    public JsonResponse<TokenResponse> login(@RequestBody LoginRequest request) {
        return JsonResponse.success(securityService.login(request.getUsername(), request.getPassword()));
    }

    @PostMapping("/refresh")
    public JsonResponse<TokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return JsonResponse.success(securityService.refreshToken(request.getRefreshToke()));
    }

    @GetMapping("/hello-word")
    public JsonResponse<String> hello(){
        return JsonResponse.success("Hello world");
    }
}
