package com.universidad.tutorias.domain.enums;

import lombok.Getter;

@Getter
public enum EstadoProceso {
    INICIADO("Iniciado"),
    COMPARANDO("Comparando Alumnos"),
    LIBERANDO_CUPOS("Liberando Cupos"),
    ASIGNANDO("Asignando Tutores"),
    COMPLETADO("Completado"),
    FALLIDO("Fallido");

    private final String descripcion;

    EstadoProceso(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean esEstadoFinal() {
        return this == COMPLETADO || this == FALLIDO;
    }
}