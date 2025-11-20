package com.universidad.tutorias.infrastructure.exception;

import com.universidad.tutorias.domain.exception.*;
import com.universidad.tutorias.infrastructure.controller.response.ApiErrorResponse;
import com.universidad.tutorias.infrastructure.controller.response.FieldErrorDetail;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Manejador centralizado de excepciones para toda la aplicación.
 * Convierte todas las excepciones en respuestas HTTP estandarizadas.
 *
 * Estructura de respuesta:
 * - status: código HTTP
 * - error: tipo de error (p.ej. "VALIDATION_ERROR", "NOT_FOUND", etc.)
 * - message: mensaje legible para el usuario
 * - path: ruta del request
 * - timestamp: fecha/hora de la excepción
 * - fieldErrors: errores de validación de campos (si aplica)
 * - excelErrors: errores de validación de Excel (si aplica)
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ===================== VALIDACIÓN (400 BAD_REQUEST) =====================

    /**
     * Maneja errores de validación de Bean Validation (@Valid, @NotNull, etc.)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<FieldErrorDetail> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> new FieldErrorDetail(err.getField(), err.getDefaultMessage()))
                .toList();

        ApiErrorResponse response = ApiErrorResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Hay errores de validación en la petición",
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Maneja errores de validación de restricciones JPA (@Column, etc.)
     */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<FieldErrorDetail> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(cv -> new FieldErrorDetail(
                        cv.getPropertyPath().toString(),
                        cv.getMessage()
                ))
                .toList();

        ApiErrorResponse response = ApiErrorResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Hay errores de validación en la petición",
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Maneja errores de validación de negocio/dominio
     */
    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainValidation(
            DomainValidationException ex,
            HttpServletRequest request
    ) {
        log.warn("Error de validación de dominio: {}", ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "DOMAIN_VALIDATION_ERROR",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Maneja errores de validación de archivos Excel
     */
    @ExceptionHandler(ExcelValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleExcelValidation(
            ExcelValidationException ex,
            HttpServletRequest request
    ) {
        log.warn("Error de validación de Excel: {} errores encontrados", ex.getErrors().size());

        ApiErrorResponse response = ApiErrorResponse.excelError(
                HttpStatus.BAD_REQUEST.value(),
                "EXCEL_VALIDATION_ERROR",
                "El archivo Excel contiene errores de validación",
                request.getRequestURI(),
                ex.getErrors()
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Maneja errores de formato de archivo Excel
     */
    @ExceptionHandler(ExcelFormatoException.class)
    public ResponseEntity<ApiErrorResponse> handleExcelFormatoException(
            ExcelFormatoException ex,
            HttpServletRequest request
    ) {
        log.error("Error de formato en archivo Excel", ex);

        ApiErrorResponse response = ApiErrorResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "EXCEL_FORMAT_ERROR",
                "El formato del archivo Excel no es válido: " + ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        log.warn("Argumento inválido: {}", ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_ARGUMENT",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    // ===================== SEMESTRE (400/404) =====================

    @ExceptionHandler(SemestreValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleSemestreValidation(
            SemestreValidationException ex,
            HttpServletRequest request
    ) {
        log.warn("Error de validación de semestre: {}", ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "SEMESTER_VALIDATION_ERROR",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(SemestreNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleSemestreNotFound(
            SemestreNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Semestre no encontrado: {}", ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.error(
                HttpStatus.NOT_FOUND.value(),
                "SEMESTER_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(SemestreException.class)
    public ResponseEntity<ApiErrorResponse> handleSemestreException(
            SemestreException ex,
            HttpServletRequest request
    ) {
        log.warn("Error de semestre: {}", ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "SEMESTER_ERROR",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    // ===================== NOT FOUND (404) =====================

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Entidad no encontrada: {}", ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.error(
                HttpStatus.NOT_FOUND.value(),
                "ENTITY_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // ===================== CONFLICT (409) =====================

    @ExceptionHandler(CapacidadExcedidaException.class)
    public ResponseEntity<ApiErrorResponse> handleCapacidadExcedida(
            CapacidadExcedidaException ex,
            HttpServletRequest request
    ) {
        log.warn("Capacidad excedida: {}", ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.error(
                HttpStatus.CONFLICT.value(),
                "CAPACITY_EXCEEDED",
                "No hay capacidad disponible: " + ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(DuplicadoException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicado(
            DuplicadoException ex,
            HttpServletRequest request
    ) {
        log.warn("Recurso duplicado: {}", ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.error(
                HttpStatus.CONFLICT.value(),
                "DUPLICATE_RESOURCE",
                "Ya existe un registro similar: " + ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(SinTutorDisponibleException.class)
    public ResponseEntity<ApiErrorResponse> handleSinTutorDisponible(
            SinTutorDisponibleException ex,
            HttpServletRequest request
    ) {
        log.warn("Sin tutor disponible: {}", ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.error(
                HttpStatus.CONFLICT.value(),
                "NO_AVAILABLE_TUTOR",
                "No hay tutores disponibles para esta asignación: " + ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // ===================== UNPROCESSABLE ENTITY (422) =====================

    @ExceptionHandler(AlumnoInactivoException.class)
    public ResponseEntity<ApiErrorResponse> handleAlumnoInactivo(
            AlumnoInactivoException ex,
            HttpServletRequest request
    ) {
        log.warn("Alumno inactivo: {}", ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.error(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "INACTIVE_STUDENT",
                "El alumno está inactivo y no puede realizar esta operación: " + ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    // ===================== UNAUTHORIZED (401) =====================

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(
            Exception ex,
            HttpServletRequest request
    ) {
        log.warn("Credenciales inválidas", ex);

        ApiErrorResponse response = ApiErrorResponse.error(
                HttpStatus.UNAUTHORIZED.value(),
                "INVALID_CREDENTIALS",
                "Usuario o contraseña incorrectos",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        log.warn("Error de autenticación", ex);

        ApiErrorResponse response = ApiErrorResponse.error(
                HttpStatus.UNAUTHORIZED.value(),
                "AUTHENTICATION_FAILED",
                "Error de autenticación: " + ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // ===================== FORBIDDEN (403) =====================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn("Acceso denegado", ex);

        ApiErrorResponse response = ApiErrorResponse.error(
                HttpStatus.FORBIDDEN.value(),
                "ACCESS_DENIED",
                "No tiene permisos para realizar esta acción",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // ===================== INTERNAL SERVER ERROR (500) =====================

    /**
     * Maneja SOLO errores realmente inesperados.
     * Los errores esperados (validaciones, reglas de negocio) deben tener su propio manejador.
     */
    @ExceptionHandler(ProcesoAsignacionException.class)
    public ResponseEntity<ApiErrorResponse> handleProcesoAsignacionException(
            ProcesoAsignacionException ex,
            HttpServletRequest request
    ) {
        log.error("Error en proceso de asignación", ex);

        ApiErrorResponse response = ApiErrorResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "ASSIGNMENT_PROCESS_ERROR",
                "Error en el proceso de asignación: " + ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Fallback para cualquier excepción no manejada específicamente.
     * Retorna 500 y registra en logs.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Error inesperado", ex);

        ApiErrorResponse response = ApiErrorResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "Ha ocurrido un error inesperado. Contacte al administrador.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
