package com.project.q_authent.requests.authify;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AuthifyActiveUserRequest {

//    private String userId;

    private String activeCode;
}
