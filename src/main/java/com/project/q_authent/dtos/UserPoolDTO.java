package com.project.q_authent.dtos;

import com.project.q_authent.models.sqls.UserPool;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * User pool DTO for showing as list, minimize data showed
 * Last updated at 2025/07/26
 * @author leequanno1
 * @since 1.00
 */
@Data
@Getter
@Setter
public class UserPoolDTO {
    private String poolId;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private Boolean delFlag;

    private String poolName;

    public UserPoolDTO(UserPool userPool) {
        this.poolId = userPool.getPoolId();
        this.createdAt = userPool.getCreatedAt();
        this.updatedAt = userPool.getUpdatedAt();
        this.delFlag = userPool.getDelFlag();
        this.poolName = userPool.getPoolName();
    }
}
