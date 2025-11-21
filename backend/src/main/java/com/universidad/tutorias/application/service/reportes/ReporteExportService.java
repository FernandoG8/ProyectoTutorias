package com.universidad.tutorias.application.service.reportes;

import com.universidad.tutorias.application.enums.FormatoReporte;
import com.universidad.tutorias.application.service.reportes.dto.ReporteArchivoDTO;
import com.universidad.tutorias.application.service.reportes.dto.ReporteContexto;
import com.universidad.tutorias.application.service.reportes.factory.ReporteStrategyFactory;
import com.universidad.tutorias.application.service.reportes.strategy.ReporteStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteExportService {

    private final ReporteStrategyFactory strategyFactory;
    private final ReporteConsultaService consultaService;

    public ReporteArchivoDTO generarReporteTutor(Long tutorId, String periodo, FormatoReporte formato) {
        ReporteContexto contexto = consultaService.prepararContextoTutor(tutorId, periodo);
        ReporteStrategy strategy = strategyFactory.get(formato);
        return strategy.generar(contexto);
    }

    public ReporteArchivoDTO generarReporteCarrera(String codigoCarrera, String periodo, FormatoReporte formato) {
        ReporteContexto contexto = consultaService.prepararContextoCarrera(codigoCarrera, periodo);
        ReporteStrategy strategy = strategyFactory.get(formato);
        return strategy.generar(contexto);
    }

    public List<ReporteArchivoDTO> generarReportesTodasLasCarreras(String periodo, FormatoReporte formato) {
        List<String> carreras = consultaService.obtenerCarrerasDisponibles(periodo);
        if (carreras.isEmpty()) {
            return List.of();
        }

        return carreras.stream()
                .sorted()
                .map(carrera -> generarReporteCarrera(carrera, periodo, formato))
                .collect(Collectors.toList());
    }

    public ReporteArchivoDTO generarExcelAlumnosPorTutor(Long tutorId, String periodo) {
        return generarReporteTutor(tutorId, periodo, FormatoReporte.EXCEL);
    }

    public List<ReporteArchivoDTO> generarReportesTodosLosTutores(String periodo, FormatoReporte formato) {
        List<Long> tutores = consultaService.obtenerTutoresDisponibles(periodo);
        if (tutores.isEmpty()) {
            return List.of();
        }

        return tutores.stream()
                .sorted()
                .map(tutorId -> generarReporteTutor(tutorId, periodo, formato))
                .collect(Collectors.toList());
    }
}
