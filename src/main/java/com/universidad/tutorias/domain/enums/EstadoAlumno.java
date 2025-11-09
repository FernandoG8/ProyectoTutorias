package com.universidad.tutorias.domain.enums;

public enum EstadoAlumno {
    ACTIVO("Activo"),
    INACTIVO("Inactivo");

    private final String descripcion;

    EstadoAlumno(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}