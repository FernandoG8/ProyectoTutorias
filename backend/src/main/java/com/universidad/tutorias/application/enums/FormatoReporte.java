package com.universidad.tutorias.application.enums;

import java.util.Arrays;

public enum FormatoReporte {
    EXCEL,
    PDF;

    public static FormatoReporte from(String valor) {
        if (valor == null || valor.isBlank()) {
            return EXCEL;
        }

        return Arrays.stream(values())
                .filter(formato -> formato.name().equalsIgnoreCase(valor))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Formato de reporte no soportado: " + valor));
    }
}
