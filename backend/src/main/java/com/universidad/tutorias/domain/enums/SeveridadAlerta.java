package com.universidad.tutorias.domain.enums;

public enum SeveridadAlerta {
    CRITICO("Crítico"),
    ERROR("Error"),
    WARNING("Advertencia"),
    INFO("Información");

    private final String descripcion;

    SeveridadAlerta(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}