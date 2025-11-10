package com.project.q_authent.responses.authify;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class NeedCodeValidateResponse {
    private Boolean needActive;
    private Boolean needReset;
    private String userId;
    private String codeId;
}
