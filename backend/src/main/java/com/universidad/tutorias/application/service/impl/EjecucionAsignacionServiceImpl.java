package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.*;
import com.universidad.tutorias.application.service.*;
import com.universidad.tutorias.domain.entity.Alumno;
import com.universidad.tutorias.domain.entity.ProcesoAsignacion;
import com.universidad.tutorias.domain.entity.Semestre;
import com.universidad.tutorias.domain.enums.EstadoProceso;
import com.universidad.tutorias.domain.exception.DomainValidationException;
import com.universidad.tutorias.domain.repository.ProcesoAsignacionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación de EjecucionAsignacionService.
 *
 * RESPONSABILIDADES:
 * - Orquestar la ejecución de asignaciones
 * - Invocar AsignacionService.asignarAlumnos()
 * - Transformar ResultadoAsignacion → EjecucionAsignacionResponse
 * - Calcular estadísticas, timing, porcentajes
 * - Manejar errores de manera best-effort (continuar en caso de fallo)
 *
 * FLUJO:
 * 1. Validar precondiciones (semestreId, alumnos no vacío)
 * 2. Crear ProcesoAsignacion en BD
 * 3. Convertir AlumnoValidadoDTO → AlumnoExcelDTO
 * 4. Invocar AsignacionService.asignarAlumnos()
 * 5. Procesar resultado y construir respuesta
 * 6. Retornar EjecucionAsignacionResponse con status OK/PARTIAL/ERROR
 *
 * NO HACE:
 * - Validación de Excel (confía en precondiciones)
 * - Transformación de archivos
 * - Lógica de selección de tutores (delegada a AsignacionService)
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class EjecucionAsignacionServiceImpl implements EjecucionAsignacionService {

    private final AsignacionService asignacionService;
    private final ProcesoAsignacionRepository procesoRepository;
    private final SemestreService semestreService;
    private final ComparadorAlumnosService comparadorAlumnosService;
    private final InactivacionService inactivacionService;
    private final ReingresoService reingresoService;

    @Override
    public EjecucionAsignacionResponse ejecutar(EjecutarAsignacionRequest request) {

        final long inicioMs = System.currentTimeMillis();

        log.info("Iniciando ejecución de asignación: {} alumnos, semestre ID: {}",
                request.getAlumnosValidados().size(),
                request.getSemestreId());

        // ============================================
        // 1. VALIDAR PRECONDICIONES
        // ============================================

        if (request.getSemestreId() == null || request.getSemestreId() <= 0) {
            throw new DomainValidationException("semestreId inválido: " + request.getSemestreId());
        }

        if (request.getAlumnosValidados() == null || request.getAlumnosValidados().isEmpty()) {
            throw new DomainValidationException("Lista de alumnos validados vacía");
        }

        // Validar que el semestre existe
        Semestre semestre = semestreService.obtenerPorId(request.getSemestreId());
        // Si no existe, obtenerPorId lanza excepción que será capturada por GlobalExceptionHandler

        log.info("Semestre validado: {} ({})", semestre.getCodigo(), semestre.getNombre());

        try {

            // ============================================
            // 2. CREAR PROCESO ASIGNACION EN BD
            // ============================================

            ProcesoAsignacion proceso = new ProcesoAsignacion();
            proceso.setEstado(EstadoProceso.INICIADO);
            proceso.setFechaInicio(LocalDateTime.now());
            proceso.setArchivoOrigen("[ENDPOINT /ejecutar]");
            proceso.setUsuarioEjecutor("SISTEMA");
            proceso.setTotalAlumnosProcesados(0);
            proceso.setTotalAlumnosAsignados(0);
            proceso.setTotalErrores(0);
            proceso.setTotalWarnings(0);
            proceso.setSemestre(semestre);

            proceso = procesoRepository.save(proceso);

            log.info("Proceso creado con ID: {}", proceso.getId());

            // ============================================
            // 3. CONVERTIR AlumnoValidadoDTO → AlumnoExcelDTO
            // ============================================

            List<AlumnoExcelDTO> alumnosParaAsignar = request.getAlumnosValidados().stream()
                    .map(this::convertirAlumnoValidadoAExcel)
                    .collect(Collectors.toList());

            log.info("Convertidos {} alumnos a formato interno", alumnosParaAsignar.size());

            // ============================================
            // 4. MARCAR INACTIVOS + LIBERAR CUPOS + REINGRESOS
            // ============================================

            proceso.setEstado(EstadoProceso.COMPARANDO);
            procesoRepository.save(proceso);

            List<Alumno> alumnosAInactivar = comparadorAlumnosService.identificarInactivos(alumnosParaAsignar, semestre.getId());
            if (!alumnosAInactivar.isEmpty()) {
                inactivacionService.marcarInactivos(alumnosAInactivar, proceso.getId(), semestre.getId());
            }

            proceso.setEstado(EstadoProceso.LIBERANDO_CUPOS);
            procesoRepository.save(proceso);
            inactivacionService.liberarCupos(proceso.getId());

            List<Alumno> reingresosPendientes = reingresoService.procesarReingresos(
                    alumnosParaAsignar,
                    proceso.getId(),
                    semestre.getId());

            // ============================================
            // 5. INVOCAR AsignacionService
            // ============================================

            ResultadoAsignacion resultado;
            try {
                proceso.setEstado(EstadoProceso.ASIGNANDO);
                procesoRepository.save(proceso);

                resultado = asignacionService.asignarAlumnos(
                        alumnosParaAsignar,
                        proceso.getId(),
                        request.getSemestreId()
                );

                log.info("AsignacionService completado: {} procesados, {} exitosos, {} errores",
                        resultado.getTotalProcesados(),
                        resultado.getTotalAsignados(),
                        resultado.getTotalErrores());

            } catch (Exception e) {
                log.error("Error fatal en AsignacionService", e);
                return construirRespuestaError(
                        request,
                        inicioMs,
                        "Error fatal durante asignación: " + e.getMessage()
                );
            }

            // ============================================
            // 6. PROCESAR RESULTADO Y CONSTRUIR RESPUESTA
            // ============================================

            long duracionMs = System.currentTimeMillis() - inicioMs;

            EjecucionAsignacionResponse respuesta = EjecucionAsignacionResponse.builder()
                    .status(determinarStatus(resultado))
                    .message(construirMensaje(resultado))
                    .timestamp(LocalDateTime.now())
                    .totalAlumnos(resultado.getTotalProcesados())
                    .alumnosAsignados(resultado.getTotalAsignados())
                    .alumnosConError(resultado.getTotalErrores())
                    .duracionMs(duracionMs)
                    .detalles(construirDetalles(resultado, duracionMs, alumnosAInactivar.size(), reingresosPendientes.size()))
                    .porcentajeExito(calcularPorcentajeExito(resultado))
                    .erroresDetalle(resultado.getErrores() != null ?
                            convertirErrores(resultado.getErrores()) : Collections.emptyList())
                    .build();

            // ============================================
            // 7. ACTUALIZAR ESTADO DEL PROCESO
            // ============================================

            proceso.setEstado(EstadoProceso.COMPLETADO);
            proceso.setFechaFin(LocalDateTime.now());
            proceso.setTotalAlumnosProcesados(resultado.getTotalProcesados());
            proceso.setTotalAlumnosAsignados(resultado.getTotalAsignados());
            proceso.setTotalErrores(resultado.getTotalErrores());
            proceso.setTotalWarnings(resultado.getAlertas() != null ?
                    (int) resultado.getAlertas().stream()
                            .filter(a -> a.getSeveridad().name().equals("WARNING"))
                            .count() : 0);

            procesoRepository.save(proceso);

            log.info("Ejecución completada en {}ms. Status: {}", duracionMs, respuesta.getStatus());

            return respuesta;

        } catch (Exception e) {
            log.error("Error inesperado en EjecucionAsignacionService", e);
            return construirRespuestaError(
                    request,
                    inicioMs,
                    "Error inesperado: " + e.getMessage()
            );
        }
    }

    // ============================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ============================================

    /**
     * Convierte AlumnoValidadoDTO a AlumnoExcelDTO para procesamiento interno.
     */
    private AlumnoExcelDTO convertirAlumnoValidadoAExcel(AlumnoValidadoDTO validado) {
        return AlumnoExcelDTO.builder()
                .fila(0) // No es relevante en este contexto
                .matricula(validado.getMatricula())
                .nombre(validado.getNombre())
                .carrera(validado.getCarrera())
                .semestre(validado.getSemestreNumerico())
                .build();
    }

    /**
     * Determina el status de la respuesta basado en el resultado de asignación.
     *
     * OK: Todos asignados exitosamente
     * PARTIAL: Algunos asignados, otros con error
     * ERROR: Ninguno asignado o error fatal
     */
    private String determinarStatus(ResultadoAsignacion resultado) {
        if (resultado.getTotalErrores() == 0 && resultado.getTotalAsignados() > 0) {
            return "OK";
        } else if (resultado.getTotalAsignados() > 0) {
            return "PARTIAL";
        } else {
            return "ERROR";
        }
    }

    /**
     * Construye el mensaje de respuesta para el usuario.
     */
    private String construirMensaje(ResultadoAsignacion resultado) {
        if (resultado.getTotalErrores() == 0) {
            return "Asignación completada exitosamente";
        } else if (resultado.getTotalAsignados() > 0) {
            return "Asignación completada parcialmente";
        } else {
            return "Asignación fallida";
        }
    }

    /**
     * Construye los detalles de la respuesta.
     */
    private String construirDetalles(ResultadoAsignacion resultado,
                                     long duracionMs,
                                     int inactivados,
                                     int reingresosPendientes) {
        if (resultado.getTotalErrores() == 0) {
            return String.format("%d alumnos asignados, %d inactivados, %d reingresos pendientes en %dms",
                    resultado.getTotalAsignados(), inactivados, reingresosPendientes, duracionMs);
        } else {
            return String.format("%d de %d alumnos asignados. %d errores, %d inactivados, %d reingresos pendientes en %dms",
                    resultado.getTotalAsignados(),
                    resultado.getTotalProcesados(),
                    resultado.getTotalErrores(),
                    inactivados,
                    reingresosPendientes,
                    duracionMs);
        }
    }

    /**
     * Calcula el porcentaje de éxito de la asignación.
     */
    private Double calcularPorcentajeExito(ResultadoAsignacion resultado) {
        if (resultado.getTotalProcesados() == 0) {
            return 0.0;
        }
        return (resultado.getTotalAsignados() * 100.0) / resultado.getTotalProcesados();
    }

    /**
     * Convierte ErrorAsignacionDTO del servicio a AsignacionErrorDTO para respuesta.
     */
    private List<AsignacionErrorDTO> convertirErrores(List<ErrorAsignacionDTO> erroresInterno) {
        if (erroresInterno == null || erroresInterno.isEmpty()) {
            return Collections.emptyList();
        }

        return erroresInterno.stream()
                .map(e -> AsignacionErrorDTO.builder()
                        .alumnoMatricula(e.getMatricula())
                        .alumnoNombre(e.getNombreAlumno())
                        .error(e.getMensajeError())
                        .raizCausa(e.getDetallesTecnicos())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Construye una respuesta de error.
     */
    private EjecucionAsignacionResponse construirRespuestaError(
            EjecutarAsignacionRequest request,
            long inicioMs,
            String mensajeError) {

        long duracionMs = System.currentTimeMillis() - inicioMs;

        return EjecucionAsignacionResponse.builder()
                .status("ERROR")
                .message("Error durante asignación")
                .timestamp(LocalDateTime.now())
                .totalAlumnos(request.getAlumnosValidados().size())
                .alumnosAsignados(0)
                .alumnosConError(request.getAlumnosValidados().size())
                .duracionMs(duracionMs)
                .detalles("Error: " + mensajeError)
                .porcentajeExito(0.0)
                .erroresDetalle(Collections.emptyList())
                .build();
    }

}
