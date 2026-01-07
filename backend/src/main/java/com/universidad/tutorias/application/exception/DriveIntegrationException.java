package com.universidad.tutorias.application.exception;

public class DriveIntegrationException extends RuntimeException {

    private final String code;

    public DriveIntegrationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
