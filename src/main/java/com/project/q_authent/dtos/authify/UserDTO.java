package com.project.q_authent.dtos.authify;

import com.project.q_authent.models.nosqls.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Data
public class UserDTO {

    private String userId;

    private String username;

    private String email;

    private String phoneNumber;

    private String telCountryCode;

    private String lastName;

    private String firstName;

    private String avatarImg;

    private String backgroundImg;

    private String displayName;

    private Boolean gender;

    private Boolean delFlag;

    private Date createdAt;

    private Date updatedAt;

    private Boolean isValidated;

    public UserDTO(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.telCountryCode = user.getTelCountryCode();
        this.lastName = user.getLastName();
        this.firstName = user.getFirstName();
        this.avatarImg = user.getAvatarImg();
        this.backgroundImg = user.getBackgroundImg();
        this.displayName = user.getDisplayName();
        this.gender = user.getGender();
        this.delFlag = user.getDelFlag();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.isValidated = user.getIsValidated();
    }

}
