package com.project.q_authent.exceptions;

import com.project.q_authent.responses.JsonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BadException.class)
    ResponseEntity<JsonResponse<String>> handleAppException(BadException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatusCode()).body(JsonResponse.error(errorCode));
    }
}
