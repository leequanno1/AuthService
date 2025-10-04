package com.project.q_authent.dtos;

import com.project.q_authent.models.sqls.Account;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Data
@Getter
@Setter
public class AccountDTO {

    private String accountId;

    private String username;

    private String email;

    private String displayName;

    private String avatar;

    private Boolean active;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private Boolean delFlag;

    private String rootId;

    private String parentId;

    public AccountDTO(Account account) {
        accountId = account.getAccountId();
        username = account.getUsername();
        email = account.getEmail();
        displayName = account.getDisplayName();
        avatar = account.getAvatar();
        active = account.getActive();
        createdAt = account.getCreatedAt();
        updatedAt = account.getUpdatedAt();
        delFlag = account.getDelFlag();
        rootId = account.getRootId();
        parentId = account.getParentId();
    }
}
