// ============================================
// GLOBAL EXCEPTION HANDLER
// ============================================

package com.universidad.tutorias.infrastructure.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        log.error("Entidad no encontrada", ex);

        ErrorResponse error = ErrorResponse.builder()
                .codigo("ENTIDAD_NO_ENCONTRADA")
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ExcelFormatoException.class)
    public ResponseEntity<ErrorResponse> handleExcelFormatoException(ExcelFormatoException ex) {
        log.error("Error de formato en archivo Excel", ex);

        ErrorResponse error = ErrorResponse.builder()
                .codigo("FORMATO_INVALIDO")
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ProcesoAsignacionException.class)
    public ResponseEntity<ErrorResponse> handleProcesoAsignacionException(ProcesoAsignacionException ex) {
        log.error("Error en proceso de asignación", ex);

        ErrorResponse error = ErrorResponse.builder()
                .codigo("ERROR_PROCESO_ASIGNACION")
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errores = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorResponse error = ErrorResponse.builder()
                .codigo("VALIDACION_FALLIDA")
                .mensaje("Errores de validación")
                .detalles(errores)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Argumento ilegal", ex);

        ErrorResponse error = ErrorResponse.builder()
                .codigo("ARGUMENTO_INVALIDO")
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Error general no controlado", ex);

        ErrorResponse error = ErrorResponse.builder()
                .codigo("ERROR_INTERNO")
                .mensaje("Error interno del servidor: " + ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        private String codigo;
        private String mensaje;
        private List<String> detalles;
        private LocalDateTime timestamp;
    }
}