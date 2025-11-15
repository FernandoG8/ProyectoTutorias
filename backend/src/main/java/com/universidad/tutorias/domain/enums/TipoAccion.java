package com.universidad.tutorias.domain.enums;

public enum TipoAccion {
    CARGA_ARCHIVO("Carga de Archivo"),
    MARCADO_INACTIVOS("Marcado de Inactivos"),
    LIBERACION_CUPOS("Liberación de Cupos"),
    ASIGNACION("Asignación de Tutor"),
    CAMBIO_TUTOR("Cambio de Tutor"),
    RESOLUCION_MOTIVO("Resolución de Motivo");

    private final String descripcion;

    TipoAccion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
