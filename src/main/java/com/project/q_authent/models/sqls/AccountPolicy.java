package com.project.q_authent.models.sqls;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "account_policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountPolicy {

    @Id
    @Column(name = "policy_id", nullable = false, unique = true)
    private String policyId;

    @ManyToOne
    @JoinColumn(name = "target_account_id", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "root_id", nullable = false)
    private Account root;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private Account creator;

    @ManyToOne
    @JoinColumn(name = "last_edit_id", nullable = false)
    private Account lastEditor;

    @Column(name = "can_view")
    private Boolean canView;

    @Column(name = "can_delete")
    private Boolean canDelete;

    @Column(name = "can_create", nullable = false)
    private Boolean canCreate;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

    @Column(name = "del_flg", nullable = false)
    private Boolean delFlag;

}
