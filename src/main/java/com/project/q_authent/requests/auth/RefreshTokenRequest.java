package com.project.q_authent.requests.auth;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Refresh token
 * @since 1.00
 * @author leequanno1
 */
@Data
@Getter
@Setter
public class RefreshTokenRequest {
    private String refreshToken;
}
