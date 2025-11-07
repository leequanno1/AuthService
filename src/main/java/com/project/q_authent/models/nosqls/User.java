package com.project.q_authent.models.nosqls;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.q_authent.constances.TableIdHeader;
import com.project.q_authent.utils.IDUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    @Builder.Default
    private String userId = IDUtil.getID(TableIdHeader.USER_HEADER);
    @Indexed
    private String username;
    @Indexed
    private String email;
    @Indexed
    private String phoneNumber;

    private String telCountryCode;

    private String password;

    private String lastName;

    private String firstName;

    private String avatarImg;

    private String backgroundImg;

    private String displayName;

    private Boolean gender;

    @Builder.Default
    private Boolean isValidated = false;
    @Indexed
    private String poolId;
    @Builder.Default
    private Date createdAt = Timestamp.valueOf(LocalDateTime.now());
    @Builder.Default
    private Date updatedAt = Timestamp.valueOf(LocalDateTime.now());
    @Builder.Default
    private Boolean delFlag = false;
}
