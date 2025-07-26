package com.project.q_authent.dtos;

import com.project.q_authent.models.sqls.UserPool;
import com.project.q_authent.utils.JsonUtils;
import com.project.q_authent.utils.RSAKeyUtils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.util.List;

/**
 * User pool DTO full for show as detail
 * Last updated at 2025/07/26
 * @since 1.00
 * @author leequanno1
 */
@Data
@Getter
@Setter
public class UserPoolDTOFull {
    private String poolId;

    private String accountId;

    private List<String> userFields;

    private List<String> authorizeFields;

    private String poolKey;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private Boolean delFlag;

    private String publicAccessKey;

    private String publicRefreshKey;

    private String poolName;

    private Boolean emailVerify;

    private List<String> roleLevels;

    public UserPoolDTOFull(UserPool userPool) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.poolId = userPool.getPoolId();
        this.accountId = userPool.getAccount().getAccountId();
        this.userFields = JsonUtils.fromJson(userPool.getUserFields());                     // transform to list
        this.authorizeFields = JsonUtils.fromJson(userPool.getAuthorizeFields());           // transform to list
        this.poolKey = userPool.getPoolKey();
        this.createdAt = userPool.getCreatedAt();
        this.updatedAt = userPool.getUpdatedAt();
        this.delFlag = userPool.getDelFlag();
        this.poolName = userPool.getPoolName();
        this.publicAccessKey = RSAKeyUtils.genPublicKey(userPool.getPrivateAccessKey());    // gen public key
        this.publicRefreshKey = RSAKeyUtils.genPublicKey(userPool.getPrivateRefreshKey());  // gen public key
        this.emailVerify = userPool.getEmailVerify();
        this.roleLevels = JsonUtils.fromJson(userPool.getRoleLevels());                     // transform to list
    }
}
