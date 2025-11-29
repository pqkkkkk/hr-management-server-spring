package org.pqkkkkk.hr_management_server.shared;

import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response.ApiResponse;
import org.pqkkkkk.hr_management_server.modules.profile.controller.http.dto.Response.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiError error = new ApiError(ex.getClass().getSimpleName());
        ApiResponse<Object> response = new ApiResponse<>(
                null,
                false,
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                error
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        ApiError error = new ApiError(ex.getClass().getSimpleName());
        ApiResponse<Object> response = new ApiResponse<>(
                null,
                false,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An error occurred: " + ex.getMessage(),
                error
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

