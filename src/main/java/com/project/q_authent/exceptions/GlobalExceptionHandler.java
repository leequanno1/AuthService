package com.project.q_authent.exceptions;

import com.project.q_authent.responses.JsonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BadException.class)
    ResponseEntity<JsonResponse<String>> handleBadException(BadException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatusCode()).body(JsonResponse.error(errorCode));
    }

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<JsonResponse<String>> handleException(Exception e) {
        return ResponseEntity.status(ErrorCode.UNCATEGORIZED_EXCEPTION.getStatusCode()).body(JsonResponse.error(e));
    }
}
