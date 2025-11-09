package com.universidad.tutorias.domain.enums;

import lombok.Getter;

@Getter
public enum TipoAlerta {
    ERROR_FORMATO("Error de Formato"),
    ASIGNACION_CRUZADA("Asignación Cruzada"),
    CAPACIDAD_EXCEDIDA("Capacidad Excedida"),
    SIN_TUTOR_DISPONIBLE("Sin Tutor Disponible");

    private final String descripcion;

    TipoAlerta(String descripcion) {
        this.descripcion = descripcion;
    }

}