package com.project.q_authent.models.sqls;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "accounts", indexes = {
        @Index(name = "accounts_index_0", columnList = "username"),
        @Index(name = "accounts_index_1", columnList = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"userPools", "accountSettings"})
public class Account {

    @Id
    @Column(name = "account_id", nullable = false, unique = true)
    private String accountId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    private String avatar;

    @Column(nullable = false)
    private Boolean active = false;

    @Column(name = "created_at")
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    @Column(name = "updated_at")
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());

    @Column(name = "del_flag")
    private Boolean delFlag = false;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPool> userPools;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountSetting> accountSettings;

    @Column(name = "root_id")
    private String rootId;

    @Column(name = "parent_id")
    private String parentId;
}
