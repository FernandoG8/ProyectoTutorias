package com.universidad.tutorias.domain.enums;

public enum TipoError {
    MATRICULA_INVALIDA("Matrícula Inválida"),
    CAMPO_VACIO("Campo Vacío"),
    CARRERA_INVALIDA("Carrera Inválida"),
    SEMESTRE_INVALIDO("Semestre Inválido"),
    MATRICULA_DUPLICADA("Matrícula Duplicada"),
    ASIGNACION_DUPLICADA("Asignación Duplicada"),
    CAPACIDAD_EXCEDIDA("Capacidad Excedida"),
    SIN_TUTOR_DISPONIBLE("Sin Tutor Disponible"),
    ERROR_SISTEMA("Error de Sistema");

    private final String descripcion;

    TipoError(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}