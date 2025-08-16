package com.project.q_authent.models.sqls;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "user_pools", indexes = {
        @Index(name = "user_pools_index_0", columnList = "pool_key"),
        @Index(name = "user_pools_index_1", columnList = "account_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPool {

    @Id
    @Column(name = "pool_id", nullable = false, unique = true)
    private String poolId;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "user_fields", nullable = false, length = 1000)
    private String userFields;

    @Column(name = "authorize_fields", nullable = false, length = 1000)
    private String authorizeFields;

    /**
     * encode before storage
     * decode when sign
     * AES-GCM algorithm
     */
    @Column(name = "pool_key", nullable = false)
    private String poolKey;

    @Column(name = "created_at")
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    @Column(name = "updated_at")
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());

    @Column(name = "del_flag")
    private Boolean delFlag = false;

    /**
     * encode before storage
     * decode when sign
     * AES-GCM algorithm
     */
    @Column(name = "private_access_key", length = 660)
    private String privateAccessKey;

    /**
     * encode before storage
     * decode when sign
     * AES-GCM algorithm
     */
    @Column(name = "private_refresh_key", length = 660)
    private String privateRefreshKey;

    @Column(name = "pool_name")
    private String poolName;

    @Column(name = "email_verify")
    private Boolean emailVerify = false;

    @Column(name = "role_levels")
    private String roleLevels;

    @Column(name = "access_expired_minutes")
    private Integer accessExpiredMinutes = 15;

    @Column(name = "refresh_expired_days")
    private Integer refreshExpiredDays = 7;
}
