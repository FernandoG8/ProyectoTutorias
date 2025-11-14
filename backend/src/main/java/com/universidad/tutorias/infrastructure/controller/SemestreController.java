package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.application.dto.semestre.ActualizarSemestreDTO;
import com.universidad.tutorias.application.dto.semestre.CrearSemestreDTO;
import com.universidad.tutorias.application.dto.semestre.EstadisticasSemestreDTO;
import com.universidad.tutorias.application.dto.semestre.SemestreDTO;
import com.universidad.tutorias.application.service.SemestreService;
import com.universidad.tutorias.domain.entity.Semestre;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller para gestión de semestres.
 * Proporciona endpoints para CRUD, búsqueda, activación y estadísticas.
 */
@Slf4j
@RestController
@RequestMapping("/api/semestres")
@RequiredArgsConstructor
public class SemestreController {

    private final SemestreService semestreService;

    /**
     * POST /api/semestres
     * Crea un nuevo semestre.
     */
    @PostMapping
    public ResponseEntity<SemestreDTO> crearSemestre(@Valid @RequestBody CrearSemestreDTO dto) {
        log.info("POST /api/semestres - Creando semestre: {}", dto.getCodigo());

        Semestre semestre = semestreService.crearSemestre(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(convertirADTO(semestre));
    }

    /**
     * GET /api/semestres
     * Lista todos los semestres ordenados por fecha de inicio descendente.
     */
    @GetMapping
    public ResponseEntity<List<SemestreDTO>> listarTodos() {
        log.info("GET /api/semestres - Listando todos los semestres");

        List<SemestreDTO> semestres = semestreService.listarTodos()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(semestres);
    }

    /**
     * GET /api/semestres/ultimos?cantidad=5
     * Obtiene los últimos N semestres.
     */
    @GetMapping("/ultimos")
    public ResponseEntity<List<SemestreDTO>> listarUltimos(
            @RequestParam(defaultValue = "5") int cantidad) {

        log.info("GET /api/semestres/ultimos?cantidad={} - Listando últimos semestres", cantidad);

        List<SemestreDTO> semestres = semestreService.listarUltimos(cantidad)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(semestres);
    }

    /**
     * GET /api/semestres/activo
     * Obtiene el semestre activo actual (si existe).
     */
    @GetMapping("/activo")
    public ResponseEntity<?> obtenerSemestreActivo() {
        log.info("GET /api/semestres/activo - Buscando semestre activo");

        var semestreOpt = semestreService.obtenerSemestreActivo();
        if (semestreOpt.isPresent()) {
            return ResponseEntity.ok(convertirADTO(semestreOpt.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("mensaje", "No hay semestre activo en este momento"));
        }
    }

    /**
     * GET /api/semestres/{id}
     * Obtiene un semestre específico por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SemestreDTO> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/semestres/{} - Obteniendo semestre", id);

        Semestre semestre = semestreService.obtenerPorId(id);
        return ResponseEntity.ok(convertirADTO(semestre));
    }

    /**
     * GET /api/semestres/codigo/{codigo}
     * Obtiene un semestre por su código.
     */
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<SemestreDTO> obtenerPorCodigo(@PathVariable String codigo) {
        log.info("GET /api/semestres/codigo/{} - Obteniendo semestre", codigo);

        Semestre semestre = semestreService.obtenerPorCodigo(codigo);
        return ResponseEntity.ok(convertirADTO(semestre));
    }

    /**
     * PUT /api/semestres/{id}
     * Actualiza un semestre existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SemestreDTO> actualizarSemestre(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarSemestreDTO dto) {

        log.info("PUT /api/semestres/{} - Actualizando semestre", id);

        Semestre semestre = semestreService.actualizarSemestre(id, dto);
        return ResponseEntity.ok(convertirADTO(semestre));
    }

    /**
     * DELETE /api/semestres/{id}
     * Elimina un semestre (con validaciones previas).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarSemestre(@PathVariable Long id) {
        log.warn("DELETE /api/semestres/{} - Eliminando semestre", id);

        semestreService.eliminarSemestre(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * POST /api/semestres/{id}/activar
     * Activa un semestre (desactiva todos los demás).
     */
    @PostMapping("/{id}/activar")
    public ResponseEntity<SemestreDTO> activarSemestre(@PathVariable Long id) {
        log.info("POST /api/semestres/{}/activar - Activando semestre", id);

        semestreService.activarSemestre(id);
        Semestre semestre = semestreService.obtenerPorId(id);
        return ResponseEntity.ok(convertirADTO(semestre));
    }

    /**
     * POST /api/semestres/{id}/desactivar
     * Desactiva un semestre.
     */
    @PostMapping("/{id}/desactivar")
    public ResponseEntity<SemestreDTO> desactivarSemestre(@PathVariable Long id) {
        log.info("POST /api/semestres/{}/desactivar - Desactivando semestre", id);

        semestreService.desactivarSemestre(id);
        Semestre semestre = semestreService.obtenerPorId(id);
        return ResponseEntity.ok(convertirADTO(semestre));
    }

    /**
     * GET /api/semestres/{id}/estadisticas
     * Obtiene estadísticas detalladas del semestre.
     * Incluye: total de asignaciones, tutores activos, distribución por carrera, etc.
     */
    @GetMapping("/{id}/estadisticas")
    public ResponseEntity<EstadisticasSemestreDTO> obtenerEstadisticas(@PathVariable Long id) {
        log.info("GET /api/semestres/{}/estadisticas - Obteniendo estadísticas", id);

        EstadisticasSemestreDTO estadisticas = semestreService.obtenerEstadisticas(id);
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * GET /api/semestres/existe/codigo/{codigo}
     * Valida si existe un semestre con el código proporcionado.
     */
    @GetMapping("/existe/codigo/{codigo}")
    public ResponseEntity<?> verificarCodigoExistente(@PathVariable String codigo) {
        log.info("GET /api/semestres/existe/codigo/{} - Verificando existencia", codigo);

        boolean existe = semestreService.existeCodigo(codigo);
        return ResponseEntity.ok(
                java.util.Map.of("existe", existe)
        );
    }

    /**
     * Convierte entidad Semestre a DTO para respuestas.
     */
    private SemestreDTO convertirADTO(Semestre semestre) {
        return SemestreDTO.builder()
                .id(semestre.getId())
                .codigo(semestre.getCodigo())
                .nombre(semestre.getNombre())
                .fechaInicio(semestre.getFechaInicio())
                .fechaFin(semestre.getFechaFin())
                .activo(semestre.getActivo())
                .estaVigente(semestre.estaVigente())
                .fechaCreacion(semestre.getFechaCreacion())
                .build();
    }
}
