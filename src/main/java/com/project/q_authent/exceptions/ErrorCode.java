package com.project.q_authent.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

    SOMETHING_WRONG(9998, "Something went wrong", HttpStatus.BAD_REQUEST),

    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),

    // Code 1*** : Auth
    INVALID_KEY(1000, "Uncategorized exception", HttpStatus.BAD_REQUEST),

    USER_NOT_FOUND(1001, "User is not found", HttpStatus.NOT_FOUND),

    WRONG_PASSWORD(1002, "Invalid credentials", HttpStatus.BAD_REQUEST),

    USER_EXISTED(1003, "User existed", HttpStatus.BAD_REQUEST),

    EMAIL_USED(1004, "Email is already used", HttpStatus.BAD_REQUEST),

    ACCOUNT_UNACTIVATED(1005, "Account unactivated", HttpStatus.UNAUTHORIZED),

    INVALID_TOKEN(1006, "Invalid token", HttpStatus.UNAUTHORIZED),

    OLD_REFRESH_TOKEN(1007, "Refresh token have been renew", HttpStatus.UNAUTHORIZED),

    UNAUTHORIZED(1008, "Unauthorized", HttpStatus.UNAUTHORIZED),

    VALIDATION_CODE_NOT_FOUND(1009, "Validation code not found", HttpStatus.NOT_FOUND),

    VALIDATION_CODE_EXPIRED(10010, "Validation code expired", HttpStatus.BAD_REQUEST),

    INVALID_PASSWORD(10011, "Invalid password", HttpStatus.BAD_REQUEST),

    GROUP_NOT_FOUND(10012, "Group not found", HttpStatus.BAD_REQUEST),

    // Code 2*** : UserPool
    FIELD_NEEDED(2000, "Email verify cant turn on", HttpStatus.BAD_REQUEST),

    POOL_NOT_FOUND(2001, "User pool is not found", HttpStatus.NOT_FOUND),

    POOL_NAME_EXISTED(2002, "User pool name existed", HttpStatus.BAD_REQUEST ),

    // Code 3*** : Policy
    POLICY_NOT_FOUND(3000, "Policy is not found", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    // if err key wrong return code 1000
    public static ErrorCode getError(String errKey) {
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        try {
            errorCode = ErrorCode.valueOf(errKey);
        } catch (IllegalArgumentException ignored) {

        }
        return errorCode;
    }
}
