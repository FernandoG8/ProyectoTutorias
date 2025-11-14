package com.universidad.tutorias.application.service;

import com.universidad.tutorias.domain.entity.Alumno;

import java.util.List;

public interface InactivacionService {
    /**
     * Marca alumnos como inactivos y registra en tabla correspondiente
     * @param alumnos lista de alumnos a inactivar
     * @param procesoId ID del proceso actual
     * @param semestreId ID del semestre académico
     */
    void marcarInactivos(List<Alumno> alumnos, Long procesoId, Long semestreId);

    /**
     * Libera cupos de tutores por alumnos inactivos
     * @param procesoId ID del proceso actual
     */
    void liberarCupos(Long procesoId);
}