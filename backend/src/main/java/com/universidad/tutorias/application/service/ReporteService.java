package com.universidad.tutorias.application.service;

import com.universidad.tutorias.application.dto.ReporteCarreraDTO;
import com.universidad.tutorias.application.dto.TutorConAlumnosDTO;

public interface ReporteService {
    /**
     * Genera reporte de asignaciones por carrera
     * @param semestreAcademico semestre a consultar
     * @param carrera carrera específica (opcional)
     * @return ReporteCarreraDTO
     */
    ReporteCarreraDTO generarReportePorCarrera(String semestreAcademico, String carrera);

    /**
     * Obtiene lista de alumnos de un tutor
     * @param tutorId ID del tutor
     * @param semestreAcademico semestre a consultar (opcional)
     * @return TutorConAlumnosDTO
     */
    TutorConAlumnosDTO obtenerAlumnosDeTutor(Long tutorId, String semestreAcademico);
}