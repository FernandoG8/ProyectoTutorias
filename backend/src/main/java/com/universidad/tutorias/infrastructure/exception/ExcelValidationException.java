package com.universidad.tutorias.infrastructure.exception;

import com.universidad.tutorias.infrastructure.controller.response.ExcelValidationErrorDetail;
import java.util.List;

/**
 * Excepción específica para validación de archivos Excel.
 * Contiene una lista de errores de validación a nivel de fila/columna.
 * Retorna HTTP 400 BAD_REQUEST con estructura detallada de errores.
 */
public class ExcelValidationException extends RuntimeException {
    private final List<ExcelValidationErrorDetail> errors;

    public ExcelValidationException(String message, List<ExcelValidationErrorDetail> errors) {
        super(message);
        this.errors = errors;
    }

    public List<ExcelValidationErrorDetail> getErrors() {
        return errors;
    }
}
