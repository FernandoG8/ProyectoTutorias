package com.universidad.tutorias.domain.enums;

import lombok.Getter;

@Getter
public enum TipoAsignacion {
    INICIAL("Asignación Inicial"),
    REASIGNACION("Reasignación"),
    REINGRESO("Reingreso");

    private final String descripcion;

    TipoAsignacion(String descripcion) {
        this.descripcion = descripcion;
    }

}