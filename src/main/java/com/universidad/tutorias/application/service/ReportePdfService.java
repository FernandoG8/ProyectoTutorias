package com.universidad.tutorias.application.service;

import com.universidad.tutorias.application.dto.ReporteAlumnoDTO;

import java.util.List;

public interface ReportePdfService {

    byte[] generarReporteCarrera(String codigoCarrera, String periodo, List<ReporteAlumnoDTO> alumnos);
}
