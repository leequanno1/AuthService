package com.project.q_authent.requests.userpools;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * User pool request
 * @since 1.00
 * @author leequanno1
 */
@Data
@Getter
@Setter
public class UserPoolRequest {
    private String poolId;

    private String userFields;

    private String authorizeFields;

    private String poolName;

    private Boolean emailVerify;

    private String roleLevels;

    private int accessExpiredMinute;

    private int refreshExpiredDay;
}
