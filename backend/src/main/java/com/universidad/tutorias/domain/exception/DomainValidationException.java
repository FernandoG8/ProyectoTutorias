package com.universidad.tutorias.domain.exception;

/**
 * Excepción para errores de validación de negocio/dominio.
 * No son errores de sistema, sino validaciones que se esperan que ocurran.
 * Retorna HTTP 400 BAD_REQUEST.
 */
public class DomainValidationException extends RuntimeException {

    public DomainValidationException(String message) {
        super(message);
    }

    public DomainValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
