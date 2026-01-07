package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.application.dto.response.*;
import com.universidad.tutorias.application.service.InactivacionService;
import com.universidad.tutorias.application.service.TutorSincronizacionService;
import com.universidad.tutorias.domain.entity.Alumno;
import com.universidad.tutorias.domain.entity.AlumnoInactivo;
import com.universidad.tutorias.domain.enums.EstadoAlumno;
import com.universidad.tutorias.domain.enums.MotivoInactividad;
import com.universidad.tutorias.domain.repository.AlumnoInactivoRepository;
import com.universidad.tutorias.domain.repository.AlumnoRepository;
import com.universidad.tutorias.domain.repository.AsignacionRepository;
import com.universidad.tutorias.domain.repository.TutorRepository;
import com.universidad.tutorias.infrastructure.controller.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mantenimiento")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MantenimientoController {

    private final TutorSincronizacionService tutorSincronizacionService;
    private final InactivacionService inactivacionService;
    private final TutorRepository tutorRepository;
    private final AlumnoRepository alumnoRepository;
    private final AlumnoInactivoRepository alumnoInactivoRepository;
    private final AsignacionRepository asignacionRepository;

    /**
     * PASO 1: Diagnosticar el estado actual del sistema
     * Detecta anomalías y problemas antes de sincronizar
     */
    @GetMapping("/diagnostico")
    public ResponseEntity<ApiResponse<DiagnosticoResponse>> diagnosticar() {
        log.info("Ejecutando diagnóstico del sistema");

        try {
            // Estadísticas básicas
            int totalTutores = (int) tutorRepository.count();
            int totalAlumnos = (int) alumnoRepository.count();
            int alumnosActivos = (int) alumnoRepository.findAll().stream()
                    .filter(a -> a.getEstado() == EstadoAlumno.ACTIVO)
                    .count();
            int alumnosInactivos = totalAlumnos - alumnosActivos;

            List<AlumnoInactivo> inactivos = alumnoInactivoRepository.findAll();
            int inactivosSinMotivo = (int) inactivos.stream()
                    .filter(ai -> ai.getMotivoInactividad() == MotivoInactividad.SIN_DEFINIR)
                    .count();

            List<String> anomalias = new ArrayList<>();

            // Detectar tutores con discrepancia
            int tutoresConDiscrepancia = 0;
            for (var tutor : tutorRepository.findAll()) {
                int cargaReal = asignacionRepository.countByTutorId(tutor.getId());
                if (cargaReal != tutor.getCargaActual()) {
                    tutoresConDiscrepancia++;
                    anomalias.add(String.format("TUTOR %s: carga reportada=%d, real=%d",
                            tutor.getNombre(), tutor.getCargaActual(), cargaReal));
                }
                if (cargaReal > tutor.getCapacidadMax()) {
                    anomalias.add(String.format("TUTOR SOBRECARGADO: %s (%d/%d)",
                            tutor.getNombre(), cargaReal, tutor.getCapacidadMax()));
                }
            }

            String estadoGeneral = anomalias.isEmpty() ? "SALUDABLE" :
                    (anomalias.size() <= 3 ? "ADVERTENCIA" : "CRÍTICO");

            DiagnosticoResponse diagnostico = DiagnosticoResponse.builder()
                    .totalTutores(totalTutores)
                    .totalAlumnos(totalAlumnos)
                    .alumnosActivos(alumnosActivos)
                    .alumnosInactivos(alumnosInactivos)
                    .inactivosSinMotivo(inactivosSinMotivo)
                    .asignacionesTotales((int) asignacionRepository.count())
                    .tutoresConDiscrepancia(tutoresConDiscrepancia)
                    .anomalias(anomalias)
                    .estadoGeneral(estadoGeneral)
                    .build();

            return ResponseEntity.ok(ApiResponse.success(diagnostico,
                    String.format("Diagnóstico completado. Anomalías: %d", anomalias.size())));

        } catch (Exception e) {
            log.error("Error en diagnóstico", e);
            throw e;
        }
    }

    /**
     * PASO 2: Sincronizar carga de TODOS los tutores
     * Realiza recuento de asignaciones activas y corrige discrepancias
     */
    @PostMapping("/sincronizar-tutores")
    public ResponseEntity<ApiResponse<SincronizacionResponse>> sincronizarTutores() {
        log.info("Sincronizando carga de todos los tutores");

        try {
            List<Long> tutorIds = tutorRepository.findAllIds();
            List<SincronizacionResponse.CambioTutorSincronizacion> cambios = new ArrayList<>();

            for (Long tutorId : tutorIds) {
                var tutor = tutorRepository.findById(tutorId).orElse(null);
                if (tutor == null) continue;

                int cargaAnterior = tutor.getCargaActual();
                tutorSincronizacionService.recalcularCargaTutor(tutorId);

                var tutorActualizado = tutorRepository.findById(tutorId).orElse(null);
                if (tutorActualizado != null) {
                    int cargaNueva = tutorActualizado.getCargaActual();
                    if (cargaAnterior != cargaNueva) {
                        cambios.add(SincronizacionResponse.CambioTutorSincronizacion.builder()
                                .tutorId(tutorId)
                                .tutorNombre(tutor.getNombre())
                                .cargaAnterior(cargaAnterior)
                                .cargaNueva(cargaNueva)
                                .diferencia(cargaNueva - cargaAnterior)
                                .build());
                    }
                }
            }

            SincronizacionResponse resultado = SincronizacionResponse.builder()
                    .tutoresProcesados(tutorIds.size())
                    .tutoresCorregidos(cambios.size())
                    .cambiosDetallados(cambios)
                    .build();

            return ResponseEntity.ok(ApiResponse.success(resultado,
                    String.format("Sincronización completada. Tutores corregidos: %d", cambios.size())));

        } catch (Exception e) {
            log.error("Error sincronizando tutores", e);
            throw e;
        }
    }

    /**
     * PASO 3: Listar alumnos inactivos pendientes de resolución
     * Muestra los alumnos con motivo SIN_DEFINIR
     */
    @GetMapping("/pendientes-resolucion")
    public ResponseEntity<ApiResponse<List<PendienteResolucionResponse>>> listarPendientes() {
        log.info("Listando alumnos inactivos pendientes de resolución");

        try {
            List<AlumnoInactivo> inactivos = alumnoInactivoRepository.findAll().stream()
                    .filter(ai -> ai.getMotivoInactividad() == MotivoInactividad.SIN_DEFINIR)
                    .collect(Collectors.toList());

            List<PendienteResolucionResponse> pendientes = new ArrayList<>();

            for (AlumnoInactivo inactivo : inactivos) {
                PendienteResolucionResponse.PendienteResolucionResponseBuilder builder = PendienteResolucionResponse.builder()
                        .alumnoInactivoId(inactivo.getId())
                        .alumnoId(inactivo.getAlumno().getId())
                        .matricula(inactivo.getAlumno().getMatricula())
                        .nombre(inactivo.getAlumno().getNombre())
                        .carrera(inactivo.getAlumno().getCarrera());

                if (inactivo.getTutorPreservado() != null) {
                    builder.tutorId(inactivo.getTutorPreservado().getId())
                            .tutorNombre(inactivo.getTutorPreservado().getNombre())
                            .tutorActivo(inactivo.getTutorPreservado().getActivo())
                            .tutorCargaActual(inactivo.getTutorPreservado().getCargaActual())
                            .tutorCapacidadMax(inactivo.getTutorPreservado().getCapacidadMax())
                            .tutorCuposDisponibles(inactivo.getTutorPreservado().getCapacidadMax() -
                                    inactivo.getTutorPreservado().getCargaActual());
                } else {
                    builder.tutorNombre("N/A")
                            .tutorActivo(false)
                            .tutorCuposDisponibles(0);
                }

                pendientes.add(builder.build());
            }

            return ResponseEntity.ok(ApiResponse.success(pendientes,
                    String.format("Encontrados %d alumnos pendientes", pendientes.size())));

        } catch (Exception e) {
            log.error("Error listando pendientes", e);
            throw e;
        }
    }

    /**
     * PASO 4: Resolver masivamente motivos de inactivos
     * Acepta mapa de {alumnoInactivoId -> motivo}
     */
    @PostMapping("/resolver-masivamente")
    public ResponseEntity<ApiResponse<ResolucionMasivaResponse>> resolverMasivamente(
            @RequestBody Map<Long, String> resoluciones) {
        log.info("Resolviendo {} inactivos masivamente", resoluciones.size());

        List<ResolucionMasivaResponse.ResolucionExitosa> exitosos = new ArrayList<>();
        List<ResolucionMasivaResponse.ResolucionFallida> fallidos = new ArrayList<>();

        try {
            for (Map.Entry<Long, String> entry : resoluciones.entrySet()) {
                try {
                    MotivoInactividad motivo = MotivoInactividad.valueOf(entry.getValue());
                    var resolucion = inactivacionService.cambiarMotivoInactividad(
                            entry.getKey(),
                            motivo
                    );

                    if (resolucion.isExitoso()) {
                        exitosos.add(ResolucionMasivaResponse.ResolucionExitosa.builder()
                                .alumnoInactivoId(entry.getKey())
                                .motivo(entry.getValue())
                                .exitoso(true)
                                .build());
                    } else {
                        fallidos.add(ResolucionMasivaResponse.ResolucionFallida.builder()
                                .alumnoInactivoId(entry.getKey())
                                .razon(resolucion.getMensaje())
                                .build());
                    }

                } catch (IllegalArgumentException e) {
                    fallidos.add(ResolucionMasivaResponse.ResolucionFallida.builder()
                            .alumnoInactivoId(entry.getKey())
                            .razon("Motivo inválido: " + entry.getValue())
                            .build());
                }
            }

            ResolucionMasivaResponse resultado = ResolucionMasivaResponse.builder()
                    .totalProcesados(resoluciones.size())
                    .exitosos(exitosos.size())
                    .fallidos(fallidos.size())
                    .detalleExitosos(exitosos)
                    .detalleFallidos(fallidos)
                    .build();

            return ResponseEntity.ok(ApiResponse.success(resultado,
                    String.format("Resueltos: %d, Fallidos: %d", exitosos.size(), fallidos.size())));

        } catch (Exception e) {
            log.error("Error resolviendo masivamente", e);
            throw e;
        }
    }

    /**
     * PASO 5: Liberar cupos de forma segura
     * Valida integridad antes de liberar
     */
    @PostMapping("/liberar-cupos-seguro")
    public ResponseEntity<ApiResponse<LiberacionCuposResponse>> liberarCuposSeguro(
            @RequestParam(required = false) Long procesoId) {
        log.info("Liberando cupos de forma segura");

        try {
            // Validar integridad primero
            List<String> errores = new ArrayList<>();
            List<String> advertencias = new ArrayList<>();

            // Verificar asignaciones de inactivos
            long asignacionesInactivas = asignacionRepository.findAll().stream()
                    .filter(a -> a.getAlumno().getEstado() == EstadoAlumno.INACTIVO)
                    .count();

            if (asignacionesInactivas > 0) {
                advertencias.add(String.format("Hay %d asignaciones de alumnos inactivos", asignacionesInactivas));
            }

            // Sincronizar primero
            int tutoresCorregidos = 0;
            for (var tutor : tutorRepository.findAll()) {
                int cargaAnterior = tutor.getCargaActual();
                tutorSincronizacionService.recalcularCargaTutor(tutor.getId());
                var tutorActualizado = tutorRepository.findById(tutor.getId()).orElse(null);
                if (tutorActualizado != null && tutorActualizado.getCargaActual() != cargaAnterior) {
                    tutoresCorregidos++;
                }
            }

            if (tutoresCorregidos > 0) {
                advertencias.add(String.format("Se sincronizaron %d tutores antes de liberar", tutoresCorregidos));
            }

            // Liberar cupos
            inactivacionService.liberarCupos(procesoId != null ? procesoId : 0L);

            // Contar cupos liberados
            int cuposLiberados = (int) alumnoInactivoRepository.findAll().stream()
                    .filter(ai -> !Boolean.TRUE.equals(ai.getCupoLiberado()))
                    .count();

            LiberacionCuposResponse resultado = LiberacionCuposResponse.builder()
                    .exitoso(true)
                    .cuposLiberados(cuposLiberados)
                    .tutoresSincronizados(tutoresCorregidos)
                    .advertencias(advertencias)
                    .errores(errores)
                    .build();

            return ResponseEntity.ok(ApiResponse.success(resultado,
                    String.format("Liberación completada. Cupos: %d", cuposLiberados)));

        } catch (Exception e) {
            log.error("Error liberando cupos", e);
            throw e;
        }
    }

    /**
     * PASO 0: Verificar integridad de datos
     * Detecta todas las anomalías posibles
     */
    @GetMapping("/validar-integridad")
    public ResponseEntity<ApiResponse<IntegridadResponse>> validarIntegridad() {
        log.info("Validando integridad de datos");

        try {
            List<IntegridadResponse.Anomalia> anomalias = new ArrayList<>();

            // Anomalía 1: Asignaciones de inactivos
            var asignacionesInactivas = asignacionRepository.findAll().stream()
                    .filter(a -> a.getAlumno().getEstado() == EstadoAlumno.INACTIVO)
                    .collect(Collectors.toList());

            if (!asignacionesInactivas.isEmpty()) {
                anomalias.add(IntegridadResponse.Anomalia.builder()
                        .tipo("ASIGNACION_FANTASMA")
                        .descripcion("Existen asignaciones de alumnos inactivos")
                        .cantidad(asignacionesInactivas.size())
                        .severidad("CRÍTICO")
                        .build());
            }

            // Anomalía 2: Alumnos activos sin tutor
            var alumnosSinTutor = alumnoRepository.findAll().stream()
                    .filter(a -> a.getEstado() == EstadoAlumno.ACTIVO && a.getTutorActual() == null)
                    .collect(Collectors.toList());

            if (!alumnosSinTutor.isEmpty()) {
                anomalias.add(IntegridadResponse.Anomalia.builder()
                        .tipo("ALUMNO_SIN_TUTOR")
                        .descripcion("Alumnos activos sin tutor asignado")
                        .cantidad(alumnosSinTutor.size())
                        .severidad("CRÍTICO")
                        .build());
            }

            // Anomalía 3: Tutores sobrecargados
            var tutoresSobrecargados = tutorRepository.findAll().stream()
                    .filter(t -> {
                        int cargaReal = asignacionRepository.countByTutorId(t.getId());
                        return cargaReal > t.getCapacidadMax();
                    })
                    .collect(Collectors.toList());

            if (!tutoresSobrecargados.isEmpty()) {
                anomalias.add(IntegridadResponse.Anomalia.builder()
                        .tipo("TUTOR_SOBRECARGADO")
                        .descripcion("Tutores con carga superior a capacidad máxima")
                        .cantidad(tutoresSobrecargados.size())
                        .severidad("CRÍTICO")
                        .build());
            }

            IntegridadResponse reporte = IntegridadResponse.builder()
                    .esIntegro(anomalias.isEmpty())
                    .totalAnomalias(anomalias.size())
                    .anomalias(anomalias)
                    .build();

            return ResponseEntity.ok(ApiResponse.success(reporte,
                    anomalias.isEmpty() ? "Sistema íntegro" : anomalias.size() + " anomalías detectadas"));

        } catch (Exception e) {
            log.error("Error validando integridad", e);
            throw e;
        }
    }
}
