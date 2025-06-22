package com.project.q_authent.requests.auth;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RefreshTokenRequest {
    private String refreshToke;
}
