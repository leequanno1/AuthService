package com.project.q_authent.dtos;

import com.project.q_authent.models.sqls.AccountPolicy;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Data
@Setter
@Getter
public class AccountPolicyDTO {

    private String policyId;

    private String targetAccountId;

    private String rootId;

    private String creatorId;

    private String lastEditorId;

    private Boolean canView;

    private Boolean canDelete;

    private Boolean canCreate;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private Boolean delFlag;

    public AccountPolicyDTO(AccountPolicy accountPolicy) {

        policyId =  accountPolicy.getPolicyId();
        targetAccountId = accountPolicy.getAccount().getAccountId();
        rootId = accountPolicy.getRoot().getRootId();
        creatorId = accountPolicy.getCreator().getAccountId();
        lastEditorId = accountPolicy.getLastEditor().getAccountId();
        canView = accountPolicy.getCanView();
        canDelete = accountPolicy.getCanDelete();
        canCreate = accountPolicy.getCanCreate();
        createdAt = accountPolicy.getCreatedAt();
        updatedAt = accountPolicy.getUpdatedAt();
        delFlag =  accountPolicy.getDelFlag();
    }
}
