package com.project.q_authent.requests.account;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class ValidateCodeRequest {

    private String accountId;

    private String code;
}
