package org.pqkkkkk.hr_management_server.modules.timesheet.controller.http.dto;

/**
 * Define the structure of API responses for Timesheet module
 */
public class Response {
    
    /**
     * API Error structure
     */
    public record ApiError(
        String error
    ) {}

    /**
     * Generic API Response wrapper
     */
    public record ApiResponse<T>(
        T data,
        boolean success,
        int statusCode,
        String message,
        ApiError error
    ) {
        /**
         * Create a successful response
         */
        public static <T> ApiResponse<T> success(T data) {
            return new ApiResponse<>(data, true, 200, "Success", null);
        }

        /**
         * Create a successful response with custom message
         */
        public static <T> ApiResponse<T> success(T data, String message) {
            return new ApiResponse<>(data, true, 200, message, null);
        }

        /**
         * Create an error response
         */
        public static <T> ApiResponse<T> error(String errorMessage, int statusCode) {
            return new ApiResponse<>(null, false, statusCode, null, new ApiError(errorMessage));
        }

        /**
         * Create a bad request error response
         */
        public static <T> ApiResponse<T> badRequest(String errorMessage) {
            return error(errorMessage, 400);
        }

        /**
         * Create a not found error response
         */
        public static <T> ApiResponse<T> notFound(String errorMessage) {
            return error(errorMessage, 404);
        }

        /**
         * Create an internal server error response
         */
        public static <T> ApiResponse<T> internalError(String errorMessage) {
            return error(errorMessage, 500);
        }
    }
}
