package com.universidad.tutorias.application.service;

import com.universidad.tutorias.application.dto.AlumnoExcelDTO;
import com.universidad.tutorias.application.dto.ResultadoAsignacion;

import java.util.List;

public interface AsignacionService {
    /**
     * Ejecuta el algoritmo de asignación principal
     * @param alumnosValidos lista de alumnos a asignar
     * @param procesoId ID del proceso actual
     * @param semestreAcademico semestre actual
     * @return ResultadoAsignacion con estadísticas
     */
    ResultadoAsignacion asignarAlumnos(List<AlumnoExcelDTO> alumnosValidos,
                                       Long procesoId,
                                       String semestreAcademico);
}