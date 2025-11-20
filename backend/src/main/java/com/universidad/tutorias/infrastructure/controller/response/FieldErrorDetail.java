package com.universidad.tutorias.infrastructure.controller.response;

public record FieldErrorDetail(
        String field,
        String message
) {}
