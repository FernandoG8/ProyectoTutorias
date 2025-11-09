// ============================================
// TUTOR CONTROLLER
// ============================================

package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.application.dto.TutorConAlumnosDTO;
import com.universidad.tutorias.application.dto.TutorCreateDTO;
import com.universidad.tutorias.application.dto.TutorResponseDTO;
import com.universidad.tutorias.application.dto.TutorUpdateDTO;
import com.universidad.tutorias.application.service.ReporteService;
import com.universidad.tutorias.application.service.TutorCrudService;
import com.universidad.tutorias.infrastructure.controller.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/tutores")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TutorController {

    private final ReporteService reporteService;
    private final TutorCrudService tutorCrudService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TutorResponseDTO>>> listarTutores(
            @RequestParam(required = false) String carrera,
            @RequestParam(required = false) Boolean disponibles,
            @RequestParam(required = false) Boolean sobrecargados,
            @RequestParam(required = false) Boolean activos) {
        log.info("Listando tutores. Carrera: {}, Disponibles: {}, Sobrecargados: {}, Activos: {}",
                carrera, disponibles, sobrecargados, activos);
        List<TutorResponseDTO> tutores = tutorCrudService.listarTutores(carrera, disponibles, sobrecargados, activos);
        return ResponseEntity.ok(ApiResponse.success(tutores));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TutorResponseDTO>> obtenerTutor(@PathVariable Long id) {
        TutorResponseDTO tutor = tutorCrudService.obtenerTutor(id);
        return ResponseEntity.ok(ApiResponse.success(tutor));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TutorResponseDTO>> crearTutor(@Valid @RequestBody TutorCreateDTO request) {
        log.info("Creando tutor {}", request.getNombre());
        TutorResponseDTO tutor = tutorCrudService.crearTutor(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.success(tutor, "Tutor creado correctamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TutorResponseDTO>> actualizarTutor(
            @PathVariable Long id,
            @Valid @RequestBody TutorUpdateDTO request) {
        log.info("Actualizando tutor {}", id);
        TutorResponseDTO tutor = tutorCrudService.actualizarTutor(id, request);
        return ResponseEntity.ok(ApiResponse.success(tutor, "Tutor actualizado correctamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarTutor(@PathVariable Long id) {
        log.info("Eliminando tutor {}", id);
        tutorCrudService.eliminarTutor(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Tutor eliminado correctamente"));
    }

    @GetMapping("/{tutorId}/alumnos")
    public ResponseEntity<ApiResponse<TutorConAlumnosDTO>> obtenerAlumnosDeTutor(
            @PathVariable Long tutorId,
            @RequestParam(required = false) String semestreAcademico) {

        log.info("Obteniendo alumnos del tutor {}, semestre: {}", tutorId, semestreAcademico);

        TutorConAlumnosDTO tutor = reporteService.obtenerAlumnosDeTutor(tutorId, semestreAcademico);

        return ResponseEntity.ok(ApiResponse.success(tutor));
    }
}