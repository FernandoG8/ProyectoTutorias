package com.universidad.tutorias.infrastructure.exception;

public class ProcesoAsignacionException extends RuntimeException {
    public ProcesoAsignacionException(String mensaje) {
        super(mensaje);
    }

    public ProcesoAsignacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}