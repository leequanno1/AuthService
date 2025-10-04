package com.project.q_authent.requests.policy;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PoolPolicyRequest {

    private String policyId;

    private String targetAccountId;

    private String poolId;

    private Boolean canView;

    private Boolean canEdit;

    private Boolean canManage;

}
