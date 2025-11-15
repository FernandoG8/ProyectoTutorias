package com.universidad.tutorias.domain.exception;

public class SemestreException extends RuntimeException {

    public SemestreException(String message) {
        super(message);
    }

    public SemestreException(String message, Throwable cause) {
        super(message, cause);
    }
}
