package com.universidad.tutorias.infrastructure.controller.response;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        String status,
        String code,
        String message,
        List<String> details,
        LocalDateTime timestamp
) {
    public static ApiErrorResponse error(String code, String message) {
        return error(code, message, null);
    }

    public static ApiErrorResponse error(String code, String message, List<String> details) {
        return new ApiErrorResponse("error", code, message, details, LocalDateTime.now());
    }
}
