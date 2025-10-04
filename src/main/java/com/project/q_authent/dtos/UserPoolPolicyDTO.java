package com.project.q_authent.dtos;

import com.project.q_authent.models.sqls.UserPoolPolicy;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Data
@Getter
@Setter
public class UserPoolPolicyDTO {

    private String policyId;

    private String targetId;

    private String rootId;

    private String creatorId;

    private String userPoolId;

    private String lastEditId;

    private Timestamp lastEditTime;

    private Boolean canView;

    private Boolean canEdit;

    private Boolean canManage;

    /**
     * Primary mapper
     * @param userPoolPolicy {@link UserPoolPolicy}
     */
    public UserPoolPolicyDTO(UserPoolPolicy userPoolPolicy) {
        policyId = userPoolPolicy.getPolicyId();
        targetId = userPoolPolicy.getAccount().getAccountId();
        rootId = userPoolPolicy.getRoot().getAccountId();
        creatorId = userPoolPolicy.getCreator().getAccountId();
        lastEditId = userPoolPolicy.getLastEditor().getAccountId();
        userPoolId = userPoolPolicy.getUserPool().getPoolId();
        lastEditTime = userPoolPolicy.getUpdatedAt();
        canView = userPoolPolicy.getCanView();
        canEdit = userPoolPolicy.getCanEdit();
        canManage = userPoolPolicy.getCanManage();
    }

}
