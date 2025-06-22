package com.project.q_authent.requests.auth;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String displayName;
}
