package com.project.q_authent.dtos.authify;

import com.project.q_authent.models.nosqls.User;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Data
@NoArgsConstructor
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

    public static User toUser(UserDTO user) {
        return User.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .telCountryCode(user.getTelCountryCode())
                .lastName(user.getLastName())
                .firstName(user.getFirstName())
                .avatarImg(user.getAvatarImg())
                .backgroundImg(user.getBackgroundImg())
                .displayName(user.getDisplayName())
                .gender(user.getGender())
                .isValidated(Boolean.TRUE)
                .build();
    }
}
