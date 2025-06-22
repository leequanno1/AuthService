package com.project.q_authent.requests.userpools;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserPoolRequest {
    private String userFields;

    private String authorizeFields;

    private String poolName;
}
