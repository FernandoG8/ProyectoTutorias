// ============================================
// DASHBOARD CONTROLLER
// ============================================

package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.application.service.SemestreService;
import com.universidad.tutorias.domain.entity.Semestre;
import com.universidad.tutorias.domain.repository.*;
import com.universidad.tutorias.domain.entity.Asignacion;
import com.universidad.tutorias.domain.entity.Alumno;
import com.universidad.tutorias.domain.enums.EstadoAlumno;
import com.universidad.tutorias.infrastructure.controller.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Dashboard", description = "Estadísticas y métricas del sistema")
public class DashboardController {

    private final SemestreService semestreService;
    private final AsignacionRepository asignacionRepository;
    private final AlumnoRepository alumnoRepository;
    private final TutorRepository tutorRepository;
    private final ProcesoAsignacionRepository procesoRepository;

    // ============================================
    // ESTADÍSTICAS GENERALES
    // ============================================

    @GetMapping("/estadisticas")
    @Operation(
        summary = "Obtener estadísticas generales",
        description = "Obtiene las estadísticas del semestre activo (o del semestre especificado)"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerEstadisticas(
        @Parameter(description = "ID del semestre (opcional, por defecto usa el activo)")
        @RequestParam(required = false) Long semestreId
    ) {
        log.info("GET /api/dashboard/estadisticas - semestreId: {}", semestreId);

        // Si no se especifica semestre, usar el activo
        if (semestreId == null) {
            Optional<Semestre> semestreActivo = semestreService.obtenerSemestreActivo();

            if (semestreActivo.isEmpty()) {
                return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.success(null, "No hay semestre activo configurado"));
            }

            semestreId = semestreActivo.get().getId();
        }

        Semestre semestre = semestreService.obtenerPorId(semestreId);

        // Obtener estadísticas
        List<Asignacion> asignaciones = asignacionRepository.findBySemestreId(semestreId);
        List<Alumno> alumnosActivos = alumnoRepository.findByEstadoWithTutor(EstadoAlumno.ACTIVO);

        int totalAlumnos = alumnosActivos.size();
        int alumnosConTutor = (int) alumnosActivos.stream()
            .filter(a -> a.getTutorActual() != null)
            .count();

        int totalTutores = (int) asignaciones.stream()
            .map(a -> a.getTutor().getId())
            .distinct()
            .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("semestre", semestre.getCodigo());
        stats.put("semestre_id", semestreId);
        stats.put("total_alumnos", totalAlumnos);
        stats.put("alumnos_con_tutor", alumnosConTutor);
        stats.put("alumnos_sin_tutor", totalAlumnos - alumnosConTutor);
        stats.put("total_tutores", totalTutores);
        stats.put("total_asignaciones", asignaciones.size());
        stats.put("promedio_alumnos_por_tutor",
            totalTutores > 0 ? (double) totalAlumnos / totalTutores : 0.0);
        stats.put("porcentaje_cobertura",
            totalAlumnos > 0 ? (double) alumnosConTutor / totalAlumnos * 100 : 0.0);

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // ============================================
    // DISTRIBUCIÓN DE ALUMNOS POR TUTOR
    // ============================================

    @GetMapping("/distribucion-tutores")
    @Operation(
        summary = "Obtener distribución de alumnos por tutor",
        description = "Obtiene la distribución de alumnos asignados a cada tutor"
    )
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> obtenerDistribucionTutores(
        @RequestParam(required = false) Long semestreId
    ) {
        log.info("GET /api/dashboard/distribucion-tutores - semestreId: {}", semestreId);

        if (semestreId == null) {
            Optional<Semestre> semestreActivo = semestreService.obtenerSemestreActivo();
            if (semestreActivo.isEmpty()) {
                return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.success(null, "No hay semestre activo configurado"));
            }
            semestreId = semestreActivo.get().getId();
        }

        List<Asignacion> asignaciones = asignacionRepository.findBySemestreId(semestreId);

        // Agrupar por tutor
        Map<Long, List<Asignacion>> porTutor = asignaciones.stream()
            .collect(Collectors.groupingBy(a -> a.getTutor().getId()));

        List<Map<String, Object>> distribucion = new ArrayList<>();

        for (Map.Entry<Long, List<Asignacion>> entry : porTutor.entrySet()) {
            String tutorNombre = entry.getValue().get(0).getTutor().getNombre();
            String tutorCarrera = entry.getValue().get(0).getTutor().getCarrera();
            int capacidad = entry.getValue().get(0).getTutor().getCapacidadMax();
            int asignados = entry.getValue().size();

            Map<String, Object> data = new HashMap<>();
            data.put("tutor_id", entry.getKey());
            data.put("tutor_nombre", tutorNombre);
            data.put("tutor_carrera", tutorCarrera);
            data.put("alumnos_asignados", asignados);
            data.put("capacidad_max", capacidad);
            data.put("carga_utilizada", (double) asignados / capacidad * 100);

            distribucion.add(data);
        }

        // Ordenar por cantidad de alumnos descendente
        distribucion.sort((a, b) ->
            Integer.compare((int)b.get("alumnos_asignados"), (int)a.get("alumnos_asignados"))
        );

        return ResponseEntity.ok(ApiResponse.success(distribucion));
    }

    // ============================================
    // ÚLTIMOS PROCESOS
    // ============================================

    @GetMapping("/procesos-recientes")
    @Operation(
        summary = "Obtener procesos recientes",
        description = "Obtiene los últimos procesos de asignación ejecutados"
    )
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> obtenerProcesosRecientes(
        @Parameter(description = "Cantidad de procesos a retornar")
        @RequestParam(defaultValue = "5") int limit
    ) {
        log.info("GET /api/dashboard/procesos-recientes - limit: {}", limit);

        List<Map<String, Object>> procesos = procesoRepository.findAllOrderByFechaDesc()
            .stream()
            .limit(limit)
            .map(p -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", p.getId());
                map.put("estado", p.getEstado());
                map.put("archivo", p.getArchivoOrigen());
                map.put("usuario", p.getUsuarioEjecutor());
                map.put("fecha_inicio", p.getFechaInicio());
                map.put("fecha_fin", p.getFechaFin());
                map.put("total_procesados", p.getTotalAlumnosProcesados());
                map.put("total_asignados", p.getTotalAlumnosAsignados());
                map.put("total_errores", p.getTotalErrores());
                Long tiempoMs = p.getTiempoTranscurridoMs();
                map.put("tiempo_segundos",
                    tiempoMs != null ? tiempoMs / 1000.0 : 0.0);
                return map;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(procesos));
    }

    // ============================================
    // ESTADO DEL SEMESTRE
    // ============================================

    @GetMapping("/semestre-activo")
    @Operation(
        summary = "Obtener información del semestre activo",
        description = "Obtiene los detalles del semestre académico actualmente activo"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerSemestreActivo() {
        log.info("GET /api/dashboard/semestre-activo");

        Optional<Semestre> semestreActivo = semestreService.obtenerSemestreActivo();

        if (semestreActivo.isEmpty()) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.success(null, "No hay semestre activo configurado"));
        }

        Semestre semestre = semestreActivo.get();

        Map<String, Object> response = new HashMap<>();
        response.put("id", semestre.getId());
        response.put("codigo", semestre.getCodigo());
        response.put("nombre", semestre.getNombre());
        response.put("fecha_inicio", semestre.getFechaInicio());
        response.put("fecha_fin", semestre.getFechaFin());
        response.put("activo", semestre.getActivo());
        response.put("fecha_creacion", semestre.getFechaCreacion());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ============================================
    // HEALTH CHECK
    // ============================================

    @GetMapping("/health")
    @Operation(
        summary = "Health check del dashboard",
        description = "Verifica que el sistema esté funcionando correctamente"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        log.debug("GET /api/dashboard/health");

        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", new Date());
        health.put("semestre_activo_existe", semestreService.obtenerSemestreActivo().isPresent());

        return ResponseEntity.ok(ApiResponse.success(health));
    }
}
