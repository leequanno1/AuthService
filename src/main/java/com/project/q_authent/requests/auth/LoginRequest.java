package com.project.q_authent.requests.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class LoginRequest {
    private String username;
    private String password;
}
