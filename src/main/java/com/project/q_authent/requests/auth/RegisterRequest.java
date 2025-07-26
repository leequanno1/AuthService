package com.project.q_authent.requests.auth;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Register request
 * @since 1.00
 * @author leequanno1
 */
@Getter
@Setter
@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String displayName;
}
