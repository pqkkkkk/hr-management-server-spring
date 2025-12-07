package org.pqkkkkk.hr_management_server.modules.request.controller.http.dto;

/**
 * Define the structure of API responses for Request module.
 */
public class Response {
    public record ApiError(
        String error
    ){}
    
    public record ApiResponse<T>(
        T data,
        boolean success,
        int statusCode,
        String message,
        ApiError error
    ){}
}
