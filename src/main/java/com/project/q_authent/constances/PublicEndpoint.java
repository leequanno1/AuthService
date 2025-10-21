package com.project.q_authent.constances;

/**
 * End point for public access
 * Last update 2025/07/26
 * @since 1.00
 * @author leequanno1
 */
public class PublicEndpoint {
    public static final String[] endpoints = {
            "/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api/auth/*",
            "/api/account/send-code/**",
            "/api/account/validate-code",
            "/api/account/reset-forgot-password",
            "/api/account/check-existed/**",
    };

}
