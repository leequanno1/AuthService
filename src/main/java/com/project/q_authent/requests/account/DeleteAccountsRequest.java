package com.project.q_authent.requests.account;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class DeleteAccountsRequest {

    private List<String> accountIds;

    private Boolean isDeleteSubAccounts;
}
