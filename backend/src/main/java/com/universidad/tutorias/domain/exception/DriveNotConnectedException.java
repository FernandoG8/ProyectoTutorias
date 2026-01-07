package com.universidad.tutorias.domain.exception;

public class DriveNotConnectedException extends RuntimeException {
    private final String code;

    public DriveNotConnectedException(String message) {
        super(message);
        this.code = "DRIVE_NOT_CONNECTED";
    }

    public String getCode() {
        return code;
    }
}
