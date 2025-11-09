// ============================================
// ASIGNACION CONTROLLER
// ============================================

package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.application.dto.*;
import com.universidad.tutorias.application.service.ProcesoOrchestrator;
import com.universidad.tutorias.application.service.TutorReasignacionService;
import com.universidad.tutorias.domain.entity.AlertaProceso;
import com.universidad.tutorias.domain.entity.ProcesoAsignacion;
import com.universidad.tutorias.domain.enums.EstadoProceso;
import com.universidad.tutorias.domain.enums.SeveridadAlerta;
import com.universidad.tutorias.domain.repository.AlertaProcesoRepository;
import com.universidad.tutorias.domain.repository.ProcesoAsignacionRepository;
import com.universidad.tutorias.infrastructure.controller.response.ApiResponse;
import com.universidad.tutorias.infrastructure.exception.ProcesoAsignacionException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    private final TutorReasignacionService tutorReasignacionService;

    @PostMapping("/iniciar")
    public ResponseEntity<ApiResponse<IniciarProcesoResponse>> iniciarProceso(
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

                IniciarProcesoResponse response = IniciarProcesoResponse.builder()
                        .procesoId(procesoId)
                        .estado(EstadoProceso.INICIADO)
                        .mensaje("Proceso de asignación iniciado correctamente")
                        .timestamp(LocalDateTime.now())
                        .build();

                return ResponseEntity.ok(ApiResponse.success(response));

            } catch (TimeoutException e) {
                // El proceso sigue corriendo en background
                log.info("Proceso iniciado en segundo plano");
                IniciarProcesoResponse response = IniciarProcesoResponse.builder()
                        .mensaje("Proceso iniciado en segundo plano. Consulte el estado posteriormente.")
                        .timestamp(LocalDateTime.now())
                        .build();

                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(ApiResponse.success(response));
            }

        } catch (Exception e) {
            log.error("Error al iniciar proceso", e);
            throw new ProcesoAsignacionException("Error al iniciar el proceso: " + e.getMessage());
        }
    }

    @GetMapping("/proceso/{procesoId}")
    public ResponseEntity<ApiResponse<EstadoProcesoResponse>> consultarEstado(@PathVariable Long procesoId) {

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

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/proceso/{procesoId}/alertas")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerAlertas(
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

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/procesos")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listarProcesos() {
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

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/cambio-tutor")
    public ResponseEntity<ApiResponse<CambioTutorResponseDTO>> cambioManualTutor(
            @Valid @RequestBody CambioTutorRequestDTO request) {
        log.info("Solicitud de cambio de tutor para alumno {} de {} a {}", request.getAlumnoId(),
                request.getTutorOrigenId(), request.getTutorDestinoId());
        CambioTutorResponseDTO resultado = tutorReasignacionService.reasignarTutor(request);
        return ResponseEntity.ok(ApiResponse.success(resultado, "Reasignación de tutor completada"));
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