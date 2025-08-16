package com.project.q_authent.models.nosqls;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.q_authent.constances.TableIdHeader;
import com.project.q_authent.utils.IDUtil;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;

@Document(collation = "users")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
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

    private String roleLevel;
    @Builder.Default
    private Boolean isValidated = false;

    private String poolId;
    @Builder.Default
    private Timestamp createdAtTimestamp = new Timestamp(System.currentTimeMillis());
    @Builder.Default
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
    @Builder.Default
    private Boolean delFlag = false;
}
