package com.universidad.tutorias.domain.enums;

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

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esEstadoFinal() {
        return this == COMPLETADO || this == FALLIDO;
    }
}