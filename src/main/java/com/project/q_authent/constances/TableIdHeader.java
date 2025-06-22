package com.project.q_authent.constances;

import lombok.Getter;

@Getter
public enum TableIdHeader {

    ACCOUNT_HEADER("ACC"),
    ACCOUNT_SETTING_HEADER("STG"),
    USER_POOL_HEADER("UPL"),
    USER_HEADER("USR");

    private final String value;

    TableIdHeader(String value) {
        this.value = value;
    }
}