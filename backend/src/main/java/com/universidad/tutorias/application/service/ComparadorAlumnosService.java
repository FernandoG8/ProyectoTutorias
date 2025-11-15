package com.universidad.tutorias.application.service;

import com.universidad.tutorias.application.dto.AlumnoExcelDTO;
import com.universidad.tutorias.domain.entity.Alumno;

import java.util.List;

public interface ComparadorAlumnosService {
    /**
     * Compara alumnos del Excel con los activos en BD
     * @param alumnosExcel lista del archivo Excel
     * @param semestreId ID del semestre académico
     * @return lista de alumnos que deben marcarse inactivos
     */
    List<Alumno> identificarInactivos(List<AlumnoExcelDTO> alumnosExcel, Long semestreId);
}