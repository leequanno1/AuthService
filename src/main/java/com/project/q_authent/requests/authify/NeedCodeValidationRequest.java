package com.project.q_authent.requests.authify;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class NeedCodeValidationRequest {
    private Boolean needActive;
    private Boolean needReset;
    private String userId;
    private String code;
    private String codeId;
}
