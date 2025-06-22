package com.project.q_authent.responses.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class TokenResponse {
    private String refreshToken;
    private String accessToken;
}
