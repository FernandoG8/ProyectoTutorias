package com.universidad.tutorias.application.service;

import com.universidad.tutorias.domain.entity.AlumnoInactivo;
import com.universidad.tutorias.domain.enums.MotivoInactividad;

import java.util.List;

public interface ResolucionMotivoService {
    /**
     * Asigna motivo de inactividad a un alumno inactivo
     */
    void asignarMotivo(Long alumnoInactivoId, MotivoInactividad motivo, String usuario);

    /**
     * Obtiene lista de alumnos inactivos pendientes de resolución
     */
    List<AlumnoInactivo> obtenerPendientesDeResolucion();
}