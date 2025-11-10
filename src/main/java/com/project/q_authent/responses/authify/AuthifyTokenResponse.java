package com.project.q_authent.responses.authify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class AuthifyTokenResponse {

    private String accessToken;

    private String refreshToken;
}
