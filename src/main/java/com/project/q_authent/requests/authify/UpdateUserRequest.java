package com.project.q_authent.requests.authify;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UpdateUserRequest {

    private String accessToken;

    private String sessionId;

    private AuthifyNMAuthRequest userData;
}
