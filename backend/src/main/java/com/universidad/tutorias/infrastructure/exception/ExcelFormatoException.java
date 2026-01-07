

package com.universidad.tutorias.infrastructure.exception;

public class ExcelFormatoException extends RuntimeException {
    public ExcelFormatoException(String mensaje) {
        super(mensaje);
    }

    public ExcelFormatoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}