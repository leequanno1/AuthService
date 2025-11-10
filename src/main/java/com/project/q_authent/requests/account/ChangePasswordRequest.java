package com.project.q_authent.requests.account;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ChangePasswordRequest {

    private String accessToken;

    private String oldPassword;

    private String newPassword;
}
