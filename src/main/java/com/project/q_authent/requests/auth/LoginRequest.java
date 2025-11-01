package com.project.q_authent.requests.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Login request
 * @since 1.00
 * @author leequanno1
 */
@Data
@Getter
@Setter
@AllArgsConstructor
public class LoginRequest {
    private String rootId;
    private String username;
    private String password;
}
