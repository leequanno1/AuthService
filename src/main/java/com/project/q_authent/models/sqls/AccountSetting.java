package com.project.q_authent.models.sqls;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "account_settings", indexes = {
        @Index(name = "account_settings_index_0", columnList = "account_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountSetting {

    @Id
    @Column(name = "setting_id", nullable = false, unique = true)
    private String settingId;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "setting_values", columnDefinition = "TEXT")
    private String settingValues;

    @Column(name = "created_at")
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    @Column(name = "updated_at")
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());

    @Column(name = "del_flag")
    private Boolean delFlag = false;
}
