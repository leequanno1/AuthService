package com.project.q_authent.requests.authify;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AuthifyNMAuthRequest {

    private String username;

    private String email;

    private String phoneNumber;

    private String telCountryCode;

    private String password;

    private String lastName;

    private String firstName;

    private String avatarImg;

    private String backgroundImg;

    private String displayName;

    private Boolean gender;
}
