package org.pqkkkkk.hr_management_server.modules.profile.controller.http.advice;

import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response.ApiError;
import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * Global controller advice to handle exceptions across the application.
 */
@ControllerAdvice
@Slf4j
public class GlobalControllerAdvice {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ApiError error = new ApiError(
                ex.getBindingResult().getFieldErrors().stream()
                        .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                        .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                        .orElse("Validation error"));
        ApiResponse<Void> response = new ApiResponse<>(null, false, HttpStatus.BAD_REQUEST.value(),
                "Validation error", error);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(null, false, HttpStatus.BAD_REQUEST.value(),
                "Invalid argument: " + ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("An unexpected error occurred", ex);
        ApiResponse<Void> response = new ApiResponse<>(null, false, HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
