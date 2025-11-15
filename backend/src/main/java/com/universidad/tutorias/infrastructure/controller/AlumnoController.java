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
     * Lista alumnos del semestre activo
     * Si no se especifica semestre, obtiene automáticamente el semestre activo
     *
     * @param page página (default: 1)
     * @param limit registros por página (default: 20)
     * @param estado filtro por estado (ACTIVO, INACTIVO)
     * @param carrera filtro por carrera
     * @param semestreId filtro por semestre específico (opcional, usa semestre activo por defecto)
     * @param soloActivo si es true, solo muestra alumnos activos en el semestre activo (default: false)
     * @return lista paginada de alumnos
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AlumnoResponseDTO>>> listarAlumnos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) EstadoAlumno estado,
            @RequestParam(required = false) String carrera,
            @RequestParam(required = false) Integer semestreId,
            @RequestParam(defaultValue = "false") boolean soloActivo) {

        log.info("Listando alumnos - page: {}, limit: {}, estado: {}, carrera: {}, semestreId: {}, soloActivo: {}",
                page, limit, estado, carrera, semestreId, soloActivo);

        try {
            // Si no se especifica semestre, obtener el semestre activo
            Integer semestreAFiltrar = semestreId;
            String semestreNombre = null;

            if (semestreAFiltrar == null) {
                // Obtener el semestre activo
                Optional<com.universidad.tutorias.domain.entity.Semestre> semestreActivo =
                        semestreService.obtenerSemestreActivo();

                if (semestreActivo.isPresent()) {
                    semestreAFiltrar = semestreActivo.get().getId().intValue();
                    semestreNombre = semestreActivo.get().getCodigo();
                    log.info("Utilizando semestre activo: {} (ID: {})", semestreNombre, semestreAFiltrar);
                } else {
                    log.warn("No hay semestre activo configurado en el sistema");
                    // Si no hay semestre activo, no filtrar por semestre
                    // El usuario verá todos los alumnos sin importar semestre
                }
            }

            // Si soloActivo está en true, forzar estado = ACTIVO
            EstadoAlumno estadoAFiltrar = estado;
            if (soloActivo && estadoAFiltrar == null) {
                estadoAFiltrar = EstadoAlumno.ACTIVO;
                log.info("Filtrando solo alumnos ACTIVOS");
            }

            int pageIndex = Math.max(page - 1, 0);
            Pageable pageable = PageRequest.of(pageIndex, limit);

            // Realizar búsqueda
            Page<AlumnoResponseDTO> resultado = alumnoCrudService.listarAlumnos(
                    estadoAFiltrar, carrera, semestreAFiltrar, pageable
            );

            PagedResponse<AlumnoResponseDTO> data = new PagedResponse<>(
                    resultado.getContent(),
                    page,
                    limit,
                    resultado.getTotalElements(),
                    resultado.getTotalPages()
            );

            String mensaje = semestreNombre != null
                    ? String.format("Alumnos del semestre %s", semestreNombre)
                    : "Alumnos";

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
                    semestre.getId().intValue(),
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
}
