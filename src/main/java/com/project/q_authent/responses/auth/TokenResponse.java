package com.project.q_authent.responses.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Token response include access token and refresh token
 * @since 1.00
 * @author leequanno1
 */
@Data
@Getter
@Setter
@AllArgsConstructor
public class TokenResponse {
    private String refreshToken;
    private String accessToken;
}
