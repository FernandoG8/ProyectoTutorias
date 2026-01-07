package com.universidad.tutorias.domain.enums;

import lombok.Getter;

@Getter
public enum MotivoInactividad {
    SIN_DEFINIR("Sin Definir"),
    BAJA_TEMPORAL("Baja Temporal"),
    BAJA_DEFINITIVA("Baja Definitiva"),
    MOVILIDAD("Movilidad"),
    EGRESADO("Egresado");

    private final String descripcion;

    MotivoInactividad(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean debePreservarTutor() {
        return this == MOVILIDAD || this == BAJA_TEMPORAL;
    }
}
