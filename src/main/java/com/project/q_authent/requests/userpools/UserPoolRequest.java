package com.project.q_authent.requests.userpools;

import com.project.q_authent.dtos.RoleLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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

    private List<String> userFields;

    private List<String> authorizeFields;

    private String poolName;
    // Is it need email verify when sign up or not
    private Boolean emailVerify;

    private List<RoleLevel> roleLevels;

    private int accessExpiredMinute;

    private int refreshExpiredDay;
}
