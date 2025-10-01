package com.project.q_authent.models.sqls;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "pool_oauth2_register")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoolOAuth2Register {

    @Id
    @Column(name = "oauth2_register_id", nullable = false)
    private String oAuth2RegisterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id", nullable = false)
    private UserPool userPool;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oauth2_cd", nullable = false)
    private OAuth2Agent oAuth2Agent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_edit_account_id", nullable = false)
    private Account lastEditAccount;

    @Column(name = "client_id", nullable = false, length = 191)
    private String clientId;

    @Column(name = "client_secret", nullable = false, length = 512)
    private String clientSecret;

    @Column(name = "redirect_uri", nullable = false, length = 2048)
    private String redirectUri;

    @Column(name = "optional_data", length = 5000)
    private String optionalData;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

}
