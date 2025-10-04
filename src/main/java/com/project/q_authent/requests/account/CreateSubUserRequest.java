package com.project.q_authent.requests.account;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CreateSubUserRequest {

    private String username;

    private String password;

    private String email;
    
}
