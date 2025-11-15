package com.universidad.tutorias.application.service;

import com.universidad.tutorias.application.dto.ResolucionMotivoResultDTO;
import com.universidad.tutorias.domain.entity.Alumno;
import com.universidad.tutorias.domain.enums.MotivoInactividad;

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

    /**
     * Cambia el motivo de inactividad de un alumno inactivo
     * Detecta automáticamente si el tutor preservado tiene capacidad disponible
     * @param alumnoInactivoId ID del registro de alumno inactivo
     * @param motivoInactividad nuevo motivo de inactividad
     * @return información del resultado del cambio (sugerencias si es necesario)
     */
    ResolucionMotivoResultDTO cambiarMotivoInactividad(Long alumnoInactivoId, MotivoInactividad motivoInactividad);
}