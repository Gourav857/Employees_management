package com.example.ems.exception;

import com.example.ems.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice // HIGH INDUSTRY FIX: Pure project ke exception pipeline ko intercept karega
public class GlobalExceptionHandler {

    // 1. Handle custom business runtime failures (e.g., "Email already registered!")
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        System.out.println("--- [GLOBAL EXCEPTION] Intercepted Runtime Error: " + ex.getMessage() + " ---");
        ErrorResponse errorObj = new ErrorResponse(ex.getMessage());
        return new ResponseEntity<>(errorObj, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 2. Fallback tracker for any unhandled core structural exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        System.out.println("--- [GLOBAL EXCEPTION] Intercepted Fatal System Error: " + ex.getMessage() + " ---");
        ErrorResponse errorObj = new ErrorResponse("Internal Server Security/Data Constraint Error");
        return new ResponseEntity<>(errorObj, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
