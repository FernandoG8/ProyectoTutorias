package com.universidad.tutorias.application.service;

import com.universidad.tutorias.application.dto.AlumnoExcelDTO;
import com.universidad.tutorias.application.dto.ResultadoValidacion;

import java.util.List;

public interface AlumnoValidadorService {
    /**
     * Valida una lista de alumnos del Excel
     * @param alumnos lista de AlumnoExcelDTO
     * @param procesoId ID del proceso actual
     * @return ResultadoValidacion con válidos y errores
     */
    ResultadoValidacion validarAlumnos(List<AlumnoExcelDTO> alumnos, Long procesoId);
}