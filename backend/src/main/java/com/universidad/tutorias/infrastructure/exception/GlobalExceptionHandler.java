// ============================================
// GLOBAL EXCEPTION HANDLER
// ============================================

package com.universidad.tutorias.infrastructure.exception;

import com.universidad.tutorias.domain.exception.*;
import com.universidad.tutorias.infrastructure.controller.response.ApiErrorResponse;
import jakarta.persistence.EntityNotFoundException;
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

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(Exception ex) {
        log.warn("Credenciales inválidas", ex);
        ApiErrorResponse error = ApiErrorResponse.error("CREDENCIALES_INVALIDAS", "Usuario o contraseña incorrectos");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Acceso denegado", ex);
        ApiErrorResponse error = ApiErrorResponse.error("ACCESO_DENEGADO", "No tiene permisos para realizar esta acción");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(AuthenticationException ex) {
        log.warn("Error de autenticación", ex);
        ApiErrorResponse error = ApiErrorResponse.error("AUTENTICACION_FALLIDA", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Argumento ilegal", ex);

        ApiErrorResponse error = ApiErrorResponse.error("ARGUMENTO_INVALIDO", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(SemestreNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleSemestreNotFound(SemestreNotFoundException ex) {
        log.warn("Semestre no encontrado: {}", ex.getMessage());
        ApiErrorResponse error = ApiErrorResponse.error("SEMESTRE_NO_ENCONTRADO", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(SemestreValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleSemestreValidation(SemestreValidationException ex) {
        log.warn("Error de validación de semestre: {}", ex.getMessage());
        ApiErrorResponse error = ApiErrorResponse.error("VALIDACION_SEMESTRE", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(SemestreException.class)
    public ResponseEntity<ApiErrorResponse> handleSemestreException(SemestreException ex) {
        log.warn("Error de semestre: {}", ex.getMessage());
        ApiErrorResponse error = ApiErrorResponse.error("ERROR_SEMESTRE", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(CapacidadExcedidaException.class)
    public ResponseEntity<ApiErrorResponse> handleCapacidadExcedida(CapacidadExcedidaException ex) {
        log.warn("Capacidad excedida: {}", ex.getMessage());
        ApiErrorResponse error = ApiErrorResponse.error("CAPACIDAD_EXCEDIDA",
                "No hay capacidad disponible: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DuplicadoException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicado(DuplicadoException ex) {
        log.warn("Recurso duplicado: {}", ex.getMessage());
        ApiErrorResponse error = ApiErrorResponse.error("RECURSO_DUPLICADO",
                "Ya existe un registro similar: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(SinTutorDisponibleException.class)
    public ResponseEntity<ApiErrorResponse> handleSinTutorDisponible(SinTutorDisponibleException ex) {
        log.warn("Sin tutor disponible: {}", ex.getMessage());
        ApiErrorResponse error = ApiErrorResponse.error("SIN_TUTOR_DISPONIBLE",
                "No hay tutores disponibles para esta asignación: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(AlumnoInactivoException.class)
    public ResponseEntity<ApiErrorResponse> handleAlumnoInactivo(AlumnoInactivoException ex) {
        log.warn("Alumno inactivo: {}", ex.getMessage());
        ApiErrorResponse error = ApiErrorResponse.error("ALUMNO_INACTIVO",
                "El alumno está inactivo y no puede realizar esta operación: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex) {
        log.error("Error general no controlado", ex);

        ApiErrorResponse error = ApiErrorResponse.error("ERROR_INTERNO", "Error interno del servidor: " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}