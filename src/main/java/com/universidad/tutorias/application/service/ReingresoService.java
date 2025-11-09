package com.universidad.tutorias.application.service;

import com.universidad.tutorias.application.dto.AlumnoExcelDTO;
import com.universidad.tutorias.domain.entity.Alumno;

import java.util.List;

public interface ReingresoService {
    /**
     * Procesa reingresos de alumnos con baja temporal o movilidad
     */
    List<Alumno> procesarReingresos(List<AlumnoExcelDTO> alumnosReingreso, Long procesoId);
}