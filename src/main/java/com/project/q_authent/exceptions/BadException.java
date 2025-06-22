package com.project.q_authent.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BadException extends RuntimeException{
    private ErrorCode errorCode;

    public BadException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
