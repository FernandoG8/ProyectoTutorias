// ============================================
// TUTOR CONTROLLER
// ============================================

package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.application.dto.TutorConAlumnosDTO;
import com.universidad.tutorias.application.service.ReporteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tutores")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TutorController {

    private final ReporteService reporteService;

    @GetMapping("/{tutorId}/alumnos")
    public ResponseEntity<TutorConAlumnosDTO> obtenerAlumnosDeTutor(
            @PathVariable Long tutorId,
            @RequestParam(required = false) String semestreAcademico) {

        log.info("Obteniendo alumnos del tutor {}, semestre: {}", tutorId, semestreAcademico);

        TutorConAlumnosDTO tutor = reporteService.obtenerAlumnosDeTutor(tutorId, semestreAcademico);

        return ResponseEntity.ok(tutor);
    }
}