package com.universidad.tutorias.application.service;

import com.universidad.tutorias.application.dto.ReporteArchivoDTO;
import com.universidad.tutorias.application.enums.FormatoReporte;

import java.util.List;

public interface ReporteExportService {

    ReporteArchivoDTO generarExcelAlumnosPorTutor(Long tutorId, String periodo);

    ReporteArchivoDTO generarReporteCarrera(String codigoCarrera, String periodo, FormatoReporte formato);

    List<ReporteArchivoDTO> generarReportesTodasLasCarreras(String periodo, FormatoReporte formato);
}
