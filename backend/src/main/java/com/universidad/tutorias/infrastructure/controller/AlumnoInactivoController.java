// ============================================
// ALUMNO INACTIVO CONTROLLER
// ============================================

package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.application.dto.AlumnoInactivoResponseDTO;
import com.universidad.tutorias.application.service.ResolucionMotivoService;
import com.universidad.tutorias.domain.entity.AlumnoInactivo;
import com.universidad.tutorias.domain.enums.MotivoInactividad;
import com.universidad.tutorias.infrastructure.controller.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alumnos-inactivos")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AlumnoInactivoController {

    private final ResolucionMotivoService resolucionService;

    @GetMapping("/pendientes")
    public ResponseEntity<ApiResponse<List<AlumnoInactivoResponseDTO>>> obtenerPendientes(
            @RequestParam(required = false) String carrera,
            @RequestParam(required = false) Integer semestre,
            @RequestParam(required = false) MotivoInactividad motivo) {
        log.info("Obteniendo alumnos inactivos pendientes. Carrera: {}, Semestre: {}, Motivo: {}",
                carrera, semestre, motivo);
        List<AlumnoInactivo> pendientes = resolucionService.obtenerPendientesDeResolucion(carrera, semestre, motivo);
        List<AlumnoInactivoResponseDTO> data = pendientes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlumnoInactivoResponseDTO>>> listarInactivos(
            @RequestParam(required = false) String carrera,
            @RequestParam(required = false) Integer semestre,
            @RequestParam(required = false) MotivoInactividad motivo) {
        log.info("Listando alumnos inactivos. Carrera: {}, Semestre: {}, Motivo: {}", carrera, semestre, motivo);
        List<AlumnoInactivo> inactivos = resolucionService.listarInactivos(carrera, semestre, motivo);
        List<AlumnoInactivoResponseDTO> data = inactivos.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PutMapping("/{alumnoInactivoId}/motivo")
    public ResponseEntity<ApiResponse<AlumnoInactivoResponseDTO>> asignarMotivo(
            @PathVariable Long alumnoInactivoId,
            @RequestParam MotivoInactividad motivo,
            @RequestParam String usuario) {

        log.info("Asignando motivo {} a alumno inactivo {} por usuario {}",
                motivo, alumnoInactivoId, usuario);

        AlumnoInactivo actualizado = resolucionService.asignarMotivo(alumnoInactivoId, motivo, usuario);
        AlumnoInactivoResponseDTO data = mapToResponse(actualizado);

        return ResponseEntity.ok(ApiResponse.success(data, "Motivo asignado correctamente"));
    }

    private AlumnoInactivoResponseDTO mapToResponse(AlumnoInactivo alumnoInactivo) {
        return AlumnoInactivoResponseDTO.builder()
                .id(alumnoInactivo.getId())
                .alumnoId(alumnoInactivo.getAlumno().getId())
                .nombre(alumnoInactivo.getAlumno().getNombre())
                .matricula(alumnoInactivo.getAlumno().getMatricula())
                .carrera(alumnoInactivo.getAlumno().getCarrera())
                .semestre(alumnoInactivo.getAlumno().getSemestre())
                .motivo(alumnoInactivo.getMotivoInactividad())
                .fechaDeteccion(alumnoInactivo.getFechaInactividad())
                .cupoLiberado(alumnoInactivo.getCupoLiberado())
                .build();
    }
}