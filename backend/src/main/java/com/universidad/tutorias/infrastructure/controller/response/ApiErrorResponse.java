package com.universidad.tutorias.infrastructure.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp,
        List<FieldErrorDetail> fieldErrors,
        List<ExcelValidationErrorDetail> excelErrors
) {
    public static ApiErrorResponse error(int status, String error, String message, String path) {
        return new ApiErrorResponse(status, error, message, path, Instant.now(), null, null);
    }

    public static ApiErrorResponse error(int status, String error, String message, String path, List<FieldErrorDetail> fieldErrors) {
        return new ApiErrorResponse(status, error, message, path, Instant.now(), fieldErrors, null);
    }

    public static ApiErrorResponse excelError(int status, String error, String message, String path, List<ExcelValidationErrorDetail> excelErrors) {
        return new ApiErrorResponse(status, error, message, path, Instant.now(), null, excelErrors);
    }
}
