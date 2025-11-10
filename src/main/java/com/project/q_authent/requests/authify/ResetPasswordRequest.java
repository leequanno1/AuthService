package com.project.q_authent.requests.authify;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class ResetPasswordRequest {
    private String newPassword;
    private NeedCodeValidationRequest verifyInfo;
}
