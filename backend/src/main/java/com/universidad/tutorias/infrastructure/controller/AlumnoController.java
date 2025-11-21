package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.application.dto.AlumnoCreateDTO;
import com.universidad.tutorias.application.dto.AlumnoPatchDTO;
import com.universidad.tutorias.application.dto.AlumnoResponseDTO;
import com.universidad.tutorias.application.dto.AlumnoUpdateDTO;
import com.universidad.tutorias.application.service.AlumnoCrudService;
import com.universidad.tutorias.application.service.SemestreService;
import com.universidad.tutorias.domain.enums.EstadoAlumno;
import com.universidad.tutorias.infrastructure.controller.response.ApiResponse;
import com.universidad.tutorias.infrastructure.controller.response.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/alumnos")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AlumnoController {

    private final AlumnoCrudService alumnoCrudService;
    private final SemestreService semestreService;

    /**
     * Lista TODOS los alumnos con filtros opcionales
     *
     * IMPORTANTE: Por defecto muestra alumnos del semestre académico activo
     * Los filtros son OPCIONALES - si no se especifican, no se aplican
     *
     * @param page página (default: 1)
     * @param limit registros por página (default: 20)
     * @param estado filtro por estado (ACTIVO, INACTIVO) - OPCIONAL
     * @param carrera filtro por carrera - OPCIONAL
     * @param semestreId filtro por ID de semestre académico (ej: ID de "2025-2026-F1") - POR DEFECTO: semestre activo
     * @param semestre filtro por semestre cursante del alumno (1, 2, 3, etc.) - OPCIONAL
     * @param soloActivo DEPRECATED - usar estado=ACTIVO en su lugar (default: false)
     * @return lista paginada de alumnos
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AlumnoResponseDTO>>> listarAlumnos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) EstadoAlumno estado,
            @RequestParam(required = false) String carrera,
            @RequestParam(required = false) Long semestreId,
            @RequestParam(required = false) String semestreCodigo,
            @RequestParam(required = false) Integer semestre,
            @RequestParam(defaultValue = "false") boolean soloActivo) {

        log.info("Listando alumnos - page: {}, limit: {}, estado: {}, carrera: {}, semestreId: {}, semestreCodigo: {}, semestre: {}, soloActivo: {}",
                page, limit, estado, carrera, semestreId, semestreCodigo, semestre, soloActivo);

        try {
            // Si soloActivo está en true, forzar estado = ACTIVO (retrocompatibilidad)
            EstadoAlumno estadoAFiltrar = estado;
            if (soloActivo && estadoAFiltrar == null) {
                estadoAFiltrar = EstadoAlumno.ACTIVO;
                log.info("Filtrando solo alumnos ACTIVOS (parámetro soloActivo deprecated)");
            }

            Long semestreAcademicoId = resolveSemestreId(semestreId, semestreCodigo);
            String semestreCodigoUsado = resolveSemestreCodigo(semestreId, semestreCodigo, semestreAcademicoId);

            if (semestreAcademicoId == null) {
                log.warn("No hay semestre activo configurado y no se proporcionó semestreId/semestreCodigo");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.success(null, "No hay semestre activo configurado en el sistema"));
            }

            int pageIndex = Math.max(page - 1, 0);
            Pageable pageable = PageRequest.of(pageIndex, limit);

            // Realizar búsqueda
            // Parámetro "semestre" es el semestre cursante del alumno (1, 2, 3, etc.)
            // NO el ID del semestre académico
            Page<AlumnoResponseDTO> resultado = alumnoCrudService.listarAlumnos(
                    estadoAFiltrar, carrera, semestre, semestreAcademicoId, pageable
            );

            PagedResponse<AlumnoResponseDTO> data = new PagedResponse<>(
                    resultado.getContent(),
                    page,
                    limit,
                    resultado.getTotalElements(),
                    resultado.getTotalPages()
            );

            // Mensaje descriptivo basado en los filtros aplicados
            String mensaje = semestreCodigoUsado != null
                    ? String.format("Alumnos del semestre %s", semestreCodigoUsado)
                    : "Alumnos del semestre activo no configurado";

            return ResponseEntity.ok(ApiResponse.success(data, mensaje));

        } catch (Exception e) {
            log.error("Error listando alumnos", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.success(null, "Error: " + e.getMessage()));
        }
    }

    /**
     * NUEVO ENDPOINT: Lista SOLO alumnos activos del semestre actual
     * Alias simplificado para obtener lista actualizada de estudiantes
     */
    @GetMapping("/semestre-actual")
    public ResponseEntity<ApiResponse<PagedResponse<AlumnoResponseDTO>>> listarAlumnosSemestreActual(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String carrera) {

        log.info("Listando alumnos del semestre actual - page: {}, limit: {}, carrera: {}",
                page, limit, carrera);

        try {
            // Obtener semestre activo
            Optional<com.universidad.tutorias.domain.entity.Semestre> semestreActivo =
                    semestreService.obtenerSemestreActivo();

            if (semestreActivo.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.success(null,
                                "No hay semestre activo configurado en el sistema"));
            }

            com.universidad.tutorias.domain.entity.Semestre semestre = semestreActivo.get();
            log.info("Utilizando semestre activo: {} (ID: {})", semestre.getCodigo(), semestre.getId());

            int pageIndex = Math.max(page - 1, 0);
            Pageable pageable = PageRequest.of(pageIndex, limit);

            // Obtener solo alumnos ACTIVOS del semestre actual
            Page<AlumnoResponseDTO> resultado = alumnoCrudService.listarAlumnos(
                    EstadoAlumno.ACTIVO, // Solo alumnos ACTIVOS
                    carrera,
                    null,
                    semestre.getId(),
                    pageable
            );

            PagedResponse<AlumnoResponseDTO> data = new PagedResponse<>(
                    resultado.getContent(),
                    page,
                    limit,
                    resultado.getTotalElements(),
                    resultado.getTotalPages()
            );

            return ResponseEntity.ok(ApiResponse.success(data,
                    String.format("Alumnos activos del semestre %s - Total: %d",
                            semestre.getCodigo(), resultado.getTotalElements())));

        } catch (Exception e) {
            log.error("Error listando alumnos del semestre actual", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.success(null, "Error: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlumnoResponseDTO>> obtenerAlumno(@PathVariable Long id) {
        AlumnoResponseDTO alumno = alumnoCrudService.obtenerAlumno(id);
        return ResponseEntity.ok(ApiResponse.success(alumno));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AlumnoResponseDTO>> crearAlumno(@Valid @RequestBody AlumnoCreateDTO request) {
        log.info("Creando alumno con matrícula {}", request.getMatricula());
        AlumnoResponseDTO alumno = alumnoCrudService.crearAlumno(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(alumno, "Alumno creado correctamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AlumnoResponseDTO>> reemplazarAlumno(
            @PathVariable Long id,
            @Valid @RequestBody AlumnoUpdateDTO request) {
        log.info("Actualizando alumno {} mediante PUT", id);
        AlumnoResponseDTO alumno = alumnoCrudService.reemplazarAlumno(id, request);
        return ResponseEntity.ok(ApiResponse.success(alumno, "Alumno actualizado correctamente"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AlumnoResponseDTO>> actualizarParcialmente(
            @PathVariable Long id,
            @Valid @RequestBody AlumnoPatchDTO request) {
        log.info("Actualizando parcialmente alumno {}", id);
        AlumnoResponseDTO alumno = alumnoCrudService.actualizarAlumnoParcial(id, request);
        return ResponseEntity.ok(ApiResponse.success(alumno, "Alumno actualizado correctamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarAlumno(@PathVariable Long id) {
        log.info("Eliminando alumno {}", id);
        alumnoCrudService.eliminarAlumno(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Alumno eliminado correctamente"));
    }

    private Long resolveSemestreId(Long semestreId, String semestreCodigo) {
        if (semestreId != null) {
            semestreService.obtenerPorId(semestreId);
            return semestreId;
        }

        if (semestreCodigo != null && !semestreCodigo.isBlank()) {
            com.universidad.tutorias.domain.entity.Semestre semestre =
                    semestreService.obtenerPorCodigo(semestreCodigo.trim().toUpperCase());
            return semestre.getId();
        }

        return semestreService.obtenerSemestreActivo()
                .map(com.universidad.tutorias.domain.entity.Semestre::getId)
                .orElse(null);
    }

    private String resolveSemestreCodigo(Long semestreId, String semestreCodigo, Long resolvedId) {
        if (semestreCodigo != null && !semestreCodigo.isBlank()) {
            return semestreCodigo.trim().toUpperCase();
        }

        if (semestreId != null) {
            return semestreService.obtenerPorId(semestreId).getCodigo();
        }

        if (resolvedId != null) {
            return semestreService.obtenerSemestreActivo()
                    .map(com.universidad.tutorias.domain.entity.Semestre::getCodigo)
                    .orElse(null);
        }

        return null;
    }
}
