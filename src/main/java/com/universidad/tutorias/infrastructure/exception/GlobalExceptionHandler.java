// ============================================
// GLOBAL EXCEPTION HANDLER
// ============================================

package com.universidad.tutorias.infrastructure.exception;

import com.universidad.tutorias.infrastructure.controller.response.ApiErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        log.error("Entidad no encontrada", ex);

        ApiErrorResponse error = ApiErrorResponse.error("ENTIDAD_NO_ENCONTRADA", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ExcelFormatoException.class)
    public ResponseEntity<ApiErrorResponse> handleExcelFormatoException(ExcelFormatoException ex) {
        log.error("Error de formato en archivo Excel", ex);

        ApiErrorResponse error = ApiErrorResponse.error("FORMATO_INVALIDO", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ProcesoAsignacionException.class)
    public ResponseEntity<ApiErrorResponse> handleProcesoAsignacionException(ProcesoAsignacionException ex) {
        log.error("Error en proceso de asignación", ex);

        ApiErrorResponse error = ApiErrorResponse.error("ERROR_PROCESO_ASIGNACION", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errores = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ApiErrorResponse error = ApiErrorResponse.error("VALIDACION_FALLIDA", "Errores de validación", errores);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Argumento ilegal", ex);

        ApiErrorResponse error = ApiErrorResponse.error("ARGUMENTO_INVALIDO", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex) {
        log.error("Error general no controlado", ex);

        ApiErrorResponse error = ApiErrorResponse.error("ERROR_INTERNO", "Error interno del servidor: " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}