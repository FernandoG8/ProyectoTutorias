// ============================================
// ASIGNACION CONTROLLER
// ============================================

package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.application.dto.*;
import com.universidad.tutorias.application.service.EjecucionAsignacionService;
import com.universidad.tutorias.application.service.ExcelValidacionYOrdenaService;
import com.universidad.tutorias.application.service.ProcesoOrchestrator;
import com.universidad.tutorias.application.service.SemestreService;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
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
    private final SemestreService semestreService;
    private final ExcelValidacionYOrdenaService excelValidacionService;
    private final EjecucionAsignacionService ejecucionAsignacionService;

    @PostMapping("/iniciar")
    public ResponseEntity<ApiResponse<IniciarProcesoResponse>> iniciarProceso(
            @Valid @ModelAttribute IniciarProcesoRequest request) {

        log.info("Iniciando proceso de asignación para semestre {} por usuario {}",
                request.getSemestreAcademico(), request.getUsuario());

        try {
            // Convertir código de semestre a ID
            Long semestreId = semestreService.convertirCodigoAId(request.getSemestreAcademico());

            CompletableFuture<Long> futuro = orchestrator.ejecutarProcesoCompleto(
                    request.getArchivo(),
                    semestreId,
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

    /**
     * Endpoint para validar, limpiar y ordenar un archivo Excel.
     *
     * RESPONSABILIDADES:
     * - Leer archivo Excel
     * - Validar estructura y datos (reportar TODOS los errores)
     * - Limpiar y normalizar datos
     * - Ordenar por semestre (mayores primero)
     * - Construir respuesta con errores o datos ordenados
     *
     * FLUJO:
     * 1. Si hay errores → GlobalExceptionHandler lanza ExcelValidationException (400)
     * 2. Si todo ok → return {status="OK", data=alumnosOrdenados[]}
     *
     * USADO POR:
     * - Frontend para validar Excel antes de ejecutar asignación
     * - No modifica BD, solo valida y ordena
     *
     * NEXT STEP:
     * - Si respuesta es OK, pasar data[] al endpoint POST /ejecutar
     *
     * Las excepciones son manejadas por GlobalExceptionHandler:
     * - ExcelFormatoException → 400 BAD_REQUEST
     * - ExcelValidationException → 400 BAD_REQUEST con lista de errores
     * - DomainValidationException → 400 BAD_REQUEST
     */
    @PostMapping("/validar-excel")
    public ResponseEntity<ApiResponse<ExcelValidacionResponse>> validarExcel(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("semestreId") Long semestreId) {

        log.info("Validando Excel para semestre ID: {}", semestreId);

        ExcelValidacionResponse respuesta = excelValidacionService.validarYProcesarExcel(archivo, semestreId);

        log.info("Validación exitosa: {} alumnos listos para asignación", respuesta.getTotalValidas());
        return ResponseEntity.ok(ApiResponse.success(respuesta, respuesta.getMessage()));
    }

    /**
     * Endpoint para ejecutar asignación con datos previamente validados.
     *
     * PRECONDICIONES:
     * - Datos DEBEN venir del endpoint POST /validar-excel (status="OK")
     * - Los alumnos DEBEN estar ordenados por semestre (mayores primero)
     * - TODOS los alumnos DEBEN tener validación completa
     * - semestreId DEBE corresponder a los alumnos en la lista
     *
     * RESPONSABILIDADES:
     * - Crear registros Asignacion con tipos: NUEVO_INGRESO o REINGRESO
     * - Actualizar cargas de tutores correctamente
     * - Registrar cambios en auditoría (logs_auditoria)
     * - Reportar errores sin detener (best-effort)
     * - Retornar estadísticas completas
     *
     * FLUJO:
     * 1. Validar precondiciones (semestreId, lista no vacía) → lanza DomainValidationException si falla
     * 2. Crear ProcesoAsignacion
     * 3. Invocar EjecucionAsignacionService.ejecutar()
     * 4. Retornar resultado con status (OK/PARTIAL/ERROR)
     *
     * NOTA:
     * - Los datos YA están validados, este endpoint es "lógica pura"
     * - No revalida Excel, confía en precondiciones
     * - Respeta el orden de semestres para liberar cupos
     * - GlobalExceptionHandler maneja todas las excepciones
     *
     * RESPUESTA:
     * - status: "OK" (todos), "PARTIAL" (algunos), "ERROR" (ninguno)
     * - totalAlumnos: Total a procesar
     * - alumnosAsignados: Exitosos
     * - alumnosConError: Fallidos
     * - duracionMs: Tiempo total
     * - erroresDetalle: Lista de errores (si hay)
     */
    @PostMapping("/ejecutar")
    public ResponseEntity<ApiResponse<EjecucionAsignacionResponse>> ejecutarAsignacion(
            @Valid @RequestBody EjecutarAsignacionRequest request) {

        log.info("Ejecutando asignación para {} alumnos en semestre {}",
                request.getAlumnosValidados().size(),
                request.getSemestreId());

        EjecucionAsignacionResponse respuesta = ejecucionAsignacionService.ejecutar(request);

        // Determinar HTTP status basado en respuesta
        HttpStatus httpStatus = "ERROR".equals(respuesta.getStatus()) ?
                HttpStatus.INTERNAL_SERVER_ERROR :
                HttpStatus.OK;

        log.info("Asignación completada con status: {}", respuesta.getStatus());

        return ResponseEntity
                .status(httpStatus)
                .body(ApiResponse.success(respuesta, respuesta.getMessage()));
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
