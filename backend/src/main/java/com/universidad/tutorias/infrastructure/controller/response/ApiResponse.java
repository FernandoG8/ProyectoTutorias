package com.universidad.tutorias.infrastructure.controller.response;

import java.time.LocalDateTime;

public record ApiResponse<T>(
        String status,
        T data,
        String message,
        LocalDateTime timestamp
) {

    public static <T> ApiResponse<T> success(T data) {
        return success(data, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>("success", data, message, LocalDateTime.now());
    }
}
