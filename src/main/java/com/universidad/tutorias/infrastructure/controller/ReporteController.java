// ============================================
// REPORTE CONTROLLER
// ============================================

package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.application.dto.ReporteCarreraDTO;
import com.universidad.tutorias.application.service.ReporteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/por-carrera")
    public ResponseEntity<ReporteCarreraDTO> generarReportePorCarrera(
            @RequestParam String semestreAcademico,
            @RequestParam(required = false) String carrera) {

        log.info("Generando reporte por carrera. Semestre: {}, Carrera: {}", semestreAcademico, carrera);

        ReporteCarreraDTO reporte = reporteService.generarReportePorCarrera(semestreAcademico, carrera);

        return ResponseEntity.ok(reporte);
    }
}