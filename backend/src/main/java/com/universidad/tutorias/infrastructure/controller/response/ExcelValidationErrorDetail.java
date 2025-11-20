package com.universidad.tutorias.infrastructure.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExcelValidationErrorDetail(
        Integer rowNumber,
        String column,
        String value,
        String message
) {}
