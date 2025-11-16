package com.project.q_authent.requests.userpools;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MailConfigRequest {
    private String poolId;
    private String siteName;
    private String siteUrl;
    private String supportEmail;
}
