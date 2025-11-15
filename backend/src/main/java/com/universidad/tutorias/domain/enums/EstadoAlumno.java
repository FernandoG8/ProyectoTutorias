package com.universidad.tutorias.domain.enums;

import lombok.Getter;

@Getter
public enum EstadoAlumno {
    ACTIVO("Activo"),
    INACTIVO("Inactivo");

    private final String descripcion;

    EstadoAlumno(String descripcion) {
        this.descripcion = descripcion;
    }

}