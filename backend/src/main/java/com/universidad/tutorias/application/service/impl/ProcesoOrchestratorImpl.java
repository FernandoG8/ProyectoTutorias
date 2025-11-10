// ============================================
// PROCESO ORCHESTRATOR IMPLEMENTATION
// ============================================

package com.universidad.tutorias.application.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.universidad.tutorias.application.dto.AlumnoExcelDTO;
import com.universidad.tutorias.application.dto.ResultadoAsignacion;
import com.universidad.tutorias.application.dto.ResultadoValidacion;
import com.universidad.tutorias.application.service.*;
import com.universidad.tutorias.domain.entity.Alumno;
import com.universidad.tutorias.domain.entity.ProcesoAsignacion;
import com.universidad.tutorias.domain.enums.EstadoProceso;
import com.universidad.tutorias.domain.repository.ProcesoAsignacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcesoOrchestratorImpl implements ProcesoOrchestrator {

    private final ExcelReaderService excelReaderService;
    private final AlumnoValidadorService validadorService;
    private final ComparadorAlumnosService comparadorService;
    private final InactivacionService inactivacionService;
    private final AsignacionService asignacionService;
    private final ReingresoService reingresoService;
    private final ProcesoAsignacionRepository procesoRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Async("asignacionExecutor")
    @Transactional
    public CompletableFuture<Long> ejecutarProcesoCompleto(MultipartFile archivo,
                                                           String semestreAcademico,
                                                           String usuario) {

        log.info("===== INICIO PROCESO DE ASIGNACIÓN =====");
        log.info("Archivo: {}, Usuario: {}, Semestre: {}",
                archivo.getOriginalFilename(), usuario, semestreAcademico);

        // Crear registro de proceso
        ProcesoAsignacion proceso = new ProcesoAsignacion();
        proceso.setEstado(EstadoProceso.INICIADO);
        proceso.setArchivoOrigen(archivo.getOriginalFilename());
        proceso.setUsuarioEjecutor(usuario);
        proceso = procesoRepository.save(proceso);

        Long procesoId = proceso.getId();

        try {
            // FASE 1: Leer y validar archivo Excel
            log.info("[Proceso {}] FASE 1: Lectura de archivo Excel", procesoId);
            List<AlumnoExcelDTO> alumnosExcel = excelReaderService.leerArchivo(archivo);

            ResultadoValidacion validacion = validadorService.validarAlumnos(alumnosExcel, procesoId);
            log.info("[Proceso {}] Validación: {} válidos, {} errores",
                    procesoId, validacion.getTotalValidos(), validacion.getTotalErrores());

            // FASE 2: Comparar con BD y marcar inactivos
            log.info("[Proceso {}] FASE 2: Comparación y marcado de inactivos", procesoId);
            proceso.setEstado(EstadoProceso.COMPARANDO);
            procesoRepository.save(proceso);

            List<Alumno> alumnosAInactivar = comparadorService.identificarInactivos(alumnosExcel);

            if (!alumnosAInactivar.isEmpty()) {
                inactivacionService.marcarInactivos(alumnosAInactivar, procesoId);
            }

            // FASE 3: Liberar cupos
            log.info("[Proceso {}] FASE 3: Liberación de cupos", procesoId);
            proceso.setEstado(EstadoProceso.LIBERANDO_CUPOS);
            procesoRepository.save(proceso);

            inactivacionService.liberarCupos(procesoId);

            // FASE 4: Procesar reingresos (alumnos que estaban inactivos y vuelven)
            log.info("[Proceso {}] FASE 4: Procesamiento de reingresos", procesoId);
            List<Alumno> reingresosPendientes = reingresoService.procesarReingresos(
                    validacion.getAlumnosValidos(), procesoId
            );

            // FASE 5: Asignar alumnos nuevos
            log.info("[Proceso {}] FASE 5: Asignación de alumnos nuevos", procesoId);
            proceso.setEstado(EstadoProceso.ASIGNANDO);
            procesoRepository.save(proceso);

            ResultadoAsignacion resultado = asignacionService.asignarAlumnos(
                    validacion.getAlumnosValidos(),
                    procesoId,
                    semestreAcademico
            );

            // FINALIZACIÓN: Actualizar estado del proceso
            proceso.setEstado(EstadoProceso.COMPLETADO);
            proceso.setFechaFin(LocalDateTime.now());

            Map<String, Object> detalles = new HashMap<>();
            detalles.put("total_excel", alumnosExcel.size());
            detalles.put("validos", validacion.getTotalValidos());
            detalles.put("errores_validacion", validacion.getTotalErrores());
            detalles.put("inactivados", alumnosAInactivar.size());
            detalles.put("reingresos_pendientes", reingresosPendientes.size());
            detalles.put("procesados", resultado.getTotalProcesados());
            detalles.put("asignados", resultado.getTotalAsignados());
            detalles.put("errores_asignacion", resultado.getTotalErrores());
            detalles.put("tiempo_ms", proceso.getTiempoTranscurridoMs());

            try {
                proceso.setDetallesJson(objectMapper.writeValueAsString(detalles));
            } catch (JsonProcessingException e) {
                log.error("Error al serializar detalles", e);
            }

            procesoRepository.save(proceso);

            log.info("===== PROCESO COMPLETADO EXITOSAMENTE =====");
            log.info("Tiempo total: {} ms", proceso.getTiempoTranscurridoMs());
            log.info("Resumen: {} procesados, {} asignados, {} errores",
                    resultado.getTotalProcesados(), resultado.getTotalAsignados(), resultado.getTotalErrores());

            return CompletableFuture.completedFuture(procesoId);

        } catch (Exception e) {
            log.error("[Proceso {}] ERROR EN PROCESO DE ASIGNACIÓN", procesoId, e);

            proceso.setEstado(EstadoProceso.FALLIDO);
            proceso.setFechaFin(LocalDateTime.now());

            Map<String, Object> detalles = new HashMap<>();
            detalles.put("error", e.getMessage());
            detalles.put("tipo_error", e.getClass().getSimpleName());

            try {
                proceso.setDetallesJson(objectMapper.writeValueAsString(detalles));
            } catch (JsonProcessingException jsonEx) {
                log.error("Error al serializar detalles del error", jsonEx);
            }

            procesoRepository.save(proceso);

            return CompletableFuture.failedFuture(e);
        }
    }
}