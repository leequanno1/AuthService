package com.project.q_authent.dtos;

import com.project.q_authent.models.sqls.PoolMailConfig;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PoolMailConfigDTO {
    private String siteName;

    private String siteUrl;

    private String supportEmail;

    public PoolMailConfigDTO(PoolMailConfig poolMailConfig) {
        this.siteName = poolMailConfig.getSiteName();
        this.siteUrl = poolMailConfig.getSiteUrl();
        this.supportEmail = poolMailConfig.getSupportEmail();
    }
}
