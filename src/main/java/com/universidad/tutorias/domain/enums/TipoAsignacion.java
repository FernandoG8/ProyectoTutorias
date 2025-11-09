package com.universidad.tutorias.domain.enums;

public enum TipoAsignacion {
    INICIAL("Asignación Inicial"),
    REASIGNACION("Reasignación"),
    REINGRESO("Reingreso");

    private final String descripcion;

    TipoAsignacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}