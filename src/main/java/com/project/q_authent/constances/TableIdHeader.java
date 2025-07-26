package com.project.q_authent.constances;

import lombok.Getter;

/**
 * Begin name for table id
 * Last update 2025/07/26
 * @since 1.00
 * @author leequanno1
 */
@Getter
public enum TableIdHeader {

    ACCOUNT_HEADER("ACC"),
    ACCOUNT_SETTING_HEADER("STG"),
    USER_POOL_HEADER("UPL"),
    USER_POLICY_HEADER("UPC"),
    POOL_POLICY_HEADER("PPC"),
    USER_HEADER("USR");

    private final String value;

    TableIdHeader(String value) {
        this.value = value;
    }
}