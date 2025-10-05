package com.project.q_authent.requests.policy;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AccountPolicyRequest {

    private String policyId;

    private String targetAccountId;

    private Boolean canCreate;

    private Boolean canView;

    private Boolean canDelete;
}
