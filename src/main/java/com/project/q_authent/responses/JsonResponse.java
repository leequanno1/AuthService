package com.project.q_authent.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.q_authent.exceptions.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonResponse<T> {
    int code;
    String message;
    T result;

    public JsonResponse(int code, String message, T result) {
        this.code = code;
        this.message = message;
        this.result = result;
    }

    public static <T> JsonResponse<T> success(T result) {
        return new JsonResponse<>(200, null, result);
    }

    public static <T> JsonResponse<T> success(String message) {
        return new JsonResponse<>(200, message, null);
    }

    public static <T> JsonResponse<T> success(String message, T result) {
        return new JsonResponse<>(200, message, result);
    }

    public static <T> JsonResponse<T> error(ErrorCode errorCode) {
        return new JsonResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> JsonResponse<T> error(Exception errorCode) {
        return new JsonResponse<>(500, errorCode.getMessage(), null);
    }

}