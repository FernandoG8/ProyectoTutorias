// ============================================
// ASIGNACION CONTROLLER
// ============================================

package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.application.dto.*;
import com.universidad.tutorias.application.service.ProcesoOrchestrator;
import com.universidad.tutorias.domain.entity.ProcesoAsignacion;
import com.universidad.tutorias.domain.entity.AlertaProceso;
import com.universidad.tutorias.domain.enums.EstadoProceso;
import com.universidad.tutorias.domain.enums.SeveridadAlerta;
import com.universidad.tutorias.domain.repository.AlertaProcesoRepository;
import com.universidad.tutorias.domain.repository.ProcesoAsignacionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/asignaciones")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AsignacionController {

    private final ProcesoOrchestrator orchestrator;
    private final ProcesoAsignacionRepository procesoRepository;
    private final AlertaProcesoRepository alertaRepository;

    @PostMapping("/iniciar")
    public ResponseEntity<IniciarProcesoResponse> iniciarProceso(
            @Valid @ModelAttribute IniciarProcesoRequest request) {

        log.info("Iniciando proceso de asignación para semestre {} por usuario {}",
                request.getSemestreAcademico(), request.getUsuario());

        try {
            CompletableFuture<Long> futuro = orchestrator.ejecutarProcesoCompleto(
                    request.getArchivo(),
                    request.getSemestreAcademico(),
                    request.getUsuario()
            );

            // Esperar un momento para obtener el ID del proceso
            try {
                Long procesoId = futuro.get(3, TimeUnit.SECONDS);

                return ResponseEntity.ok(IniciarProcesoResponse.builder()
                        .procesoId(procesoId)
                        .estado(EstadoProceso.INICIADO)
                        .mensaje("Proceso de asignación iniciado correctamente")
                        .timestamp(LocalDateTime.now())
                        .build());

            } catch (TimeoutException e) {
                // El proceso sigue corriendo en background
                log.info("Proceso iniciado en segundo plano");
                return ResponseEntity.accepted()
                        .body(IniciarProcesoResponse.builder()
                                .mensaje("Proceso iniciado en segundo plano. Consulte el estado posteriormente.")
                                .timestamp(LocalDateTime.now())
                                .build());
            }

        } catch (Exception e) {
            log.error("Error al iniciar proceso", e);
            return ResponseEntity.internalServerError()
                    .body(IniciarProcesoResponse.builder()
                            .mensaje("Error al iniciar el proceso: " + e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<EstadoProcesoResponse> consultarEstado(@PathVariable Long procesoId) {

        log.debug("Consultando estado del proceso {}", procesoId);

        ProcesoAsignacion proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new EntityNotFoundException("Proceso no encontrado con ID: " + procesoId));

        // Calcular porcentaje (asumiendo total esperado basado en procesados)
        int totalEsperado = proceso.getTotalAlumnosProcesados();
        int porcentaje = 0;

        if (proceso.getEstado() == EstadoProceso.COMPLETADO || proceso.getEstado() == EstadoProceso.FALLIDO) {
            porcentaje = 100;
        } else if (totalEsperado > 0) {
            porcentaje = proceso.getPorcentajeProgreso(totalEsperado);
        }

        ProgresoDTO progreso = ProgresoDTO.builder()
                .porcentaje(porcentaje)
                .alumnosProcesados(proceso.getTotalAlumnosProcesados())
                .alumnosAsignados(proceso.getTotalAlumnosAsignados())
                .errores(proceso.getTotalErrores())
                .warnings(proceso.getTotalWarnings())
                .build();

        EstadoProcesoResponse response = EstadoProcesoResponse.builder()
                .procesoId(proceso.getId())
                .estado(proceso.getEstado())
                .progreso(progreso)
                .fechaInicio(proceso.getFechaInicio())
                .fechaFin(proceso.getFechaFin())
                .tiempoTranscurridoMs(proceso.getTiempoTranscurridoMs())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/proceso/{procesoId}/alertas")
    public ResponseEntity<Map<String, Object>> obtenerAlertas(
            @PathVariable Long procesoId,
            @RequestParam(required = false) SeveridadAlerta severidad,
            @RequestParam(required = false) Boolean resuelta) {

        log.info("Obteniendo alertas del proceso {}. Severidad: {}, Resuelta: {}",
                procesoId, severidad, resuelta);

        List<AlertaProceso> alertas = alertaRepository.findByProcesoId(procesoId);

        // Filtrar por severidad si se especifica
        if (severidad != null) {
            alertas = alertas.stream()
                    .filter(a -> a.getSeveridad() == severidad)
                    .collect(Collectors.toList());
        }

        // Filtrar por resuelta si se especifica
        if (resuelta != null) {
            alertas = alertas.stream()
                    .filter(a -> a.getResuelta().equals(resuelta))
                    .collect(Collectors.toList());
        }

        List<AlertaDTO> alertasDTO = alertas.stream()
                .map(this::convertirAlertaDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("proceso_id", procesoId);
        response.put("total_alertas", alertasDTO.size());
        response.put("alertas", alertasDTO);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/procesos")
    public ResponseEntity<List<Map<String, Object>>> listarProcesos() {
        log.info("Listando todos los procesos");

        List<ProcesoAsignacion> procesos = procesoRepository.findAllOrderByFechaDesc();

        List<Map<String, Object>> response = procesos.stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId());
                    map.put("estado", p.getEstado());
                    map.put("archivo_origen", p.getArchivoOrigen());
                    map.put("usuario_ejecutor", p.getUsuarioEjecutor());
                    map.put("fecha_inicio", p.getFechaInicio());
                    map.put("fecha_fin", p.getFechaFin());
                    map.put("total_procesados", p.getTotalAlumnosProcesados());
                    map.put("total_asignados", p.getTotalAlumnosAsignados());
                    map.put("total_errores", p.getTotalErrores());
                    map.put("total_warnings", p.getTotalWarnings());
                    map.put("tiempo_ms", p.getTiempoTranscurridoMs());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    private AlertaDTO convertirAlertaDTO(AlertaProceso alerta) {
        AlumnoSimpleDTO alumnoDTO = null;
        if (alerta.getAlumno() != null) {
            alumnoDTO = AlumnoSimpleDTO.builder()
                    .id(alerta.getAlumno().getId())
                    .matricula(alerta.getAlumno().getMatricula())
                    .nombre(alerta.getAlumno().getNombre())
                    .carrera(alerta.getAlumno().getCarrera())
                    .build();
        }

        TutorSimpleDTO tutorDTO = null;
        if (alerta.getTutor() != null) {
            tutorDTO = TutorSimpleDTO.builder()
                    .id(alerta.getTutor().getId())
                    .nombre(alerta.getTutor().getNombre())
                    .carrera(alerta.getTutor().getCarrera())
                    .build();
        }

        return AlertaDTO.builder()
                .id(alerta.getId())
                .tipo(alerta.getTipo())
                .severidad(alerta.getSeveridad())
                .descripcion(alerta.getDescripcion())
                .alumno(alumnoDTO)
                .tutor(tutorDTO)
                .resuelta(alerta.getResuelta())
                .fechaCreacion(alerta.getFechaCreacion())
                .build();
    }
}