package com.project.q_authent.models.sqls;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pool_mail_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoolMailConfig {

    @Id
    @Column(name = "mail_config_id", nullable = false, length = 255)
    private String mailConfigId;

    @Column(name = "site_name", length = 255)
    private String siteName;

    @Column(name = "site_url", length = 255)
    private String siteUrl;

    @Column(name = "support_email", length = 255)
    private String supportEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id", nullable = false)
    private UserPool userPool;
}
