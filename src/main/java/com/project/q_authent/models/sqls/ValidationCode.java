package com.project.q_authent.models.sqls;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "validation_code")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationCode {

    @Id
    @Column(name = "validated_code_id", nullable = false)
    private String validatedCodeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_account_id", nullable = false)
    private Account targetAccount;

    @Column(name = "code_value", nullable = false)
    private Integer codeValue;

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed;

    @Column(name = "expire_time", nullable = false)
    private Timestamp expireTime;
}
