package com.project.q_authent.models.nosqls;


import com.project.q_authent.constances.TableIdHeader;
import com.project.q_authent.utils.IDUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "active_code")
public class ActiveCode {

    @Id
    @Builder.Default
    private String codeId = IDUtil.getID(TableIdHeader.VALIDATION_CODE_HEADER);

    @Indexed
    private String userId;

    private String code;
}
