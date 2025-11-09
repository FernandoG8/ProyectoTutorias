package com.universidad.tutorias.domain.enums;

public enum TipoError {
    MATRICULA_INVALIDA("Matrícula Inválida"),
    CAMPO_VACIO("Campo Vacío"),
    CARRERA_INVALIDA("Carrera Inválida"),
    SEMESTRE_INVALIDO("Semestre Inválido"),
    MATRICULA_DUPLICADA("Matrícula Duplicada");

    private final String descripcion;

    TipoError(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}