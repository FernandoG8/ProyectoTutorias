// ============================================
// REINGRESO SERVICE IMPLEMENTATION
// ============================================

package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.AlumnoExcelDTO;
import com.universidad.tutorias.application.service.AuditoriaService;
import com.universidad.tutorias.application.service.ReingresoService;
import com.universidad.tutorias.domain.entity.Alumno;
import com.universidad.tutorias.domain.entity.AlumnoInactivo;
import com.universidad.tutorias.domain.entity.Asignacion;
import com.universidad.tutorias.domain.entity.Tutor;
import com.universidad.tutorias.domain.enums.EstadoAlumno;
import com.universidad.tutorias.domain.enums.SeveridadAlerta;
import com.universidad.tutorias.domain.enums.TipoAccion;
import com.universidad.tutorias.domain.enums.TipoAsignacion;
import com.universidad.tutorias.domain.entity.AlertaProceso;
import com.universidad.tutorias.domain.entity.ProcesoAsignacion;
import com.universidad.tutorias.domain.enums.TipoAlerta;
import com.universidad.tutorias.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReingresoServiceImpl implements ReingresoService {

    private final AlumnoRepository alumnoRepository;
    private final AlumnoInactivoRepository alumnoInactivoRepository;
    private final TutorRepository tutorRepository;
    private final AsignacionRepository asignacionRepository;
    private final AlertaProcesoRepository alertaRepository;
    private final AuditoriaService auditoriaService;

    @Override
    @Transactional
    public List<Alumno> procesarReingresos(List<AlumnoExcelDTO> alumnosReingreso, Long procesoId) {

        log.info("Procesando reingresos para {} alumnos", alumnosReingreso.size());

        List<Alumno> noAsignados = new ArrayList<>();
        int reingresosExitosos = 0;

        for (AlumnoExcelDTO alumnoDTO : alumnosReingreso) {
            try {
                Optional<Alumno> alumnoOpt = alumnoRepository.findByMatricula(
                        alumnoDTO.getMatricula().toUpperCase().trim()
                );

                if (!alumnoOpt.isPresent()) {
                    continue; // No es un reingreso, es nuevo
                }

                Alumno alumno = alumnoOpt.get();

                if (alumno.getEstado() == EstadoAlumno.ACTIVO) {
                    continue; // Ya está activo
                }

                // Buscar registro de inactividad
                Optional<AlumnoInactivo> inactivoOpt = alumnoInactivoRepository.findByAlumnoId(alumno.getId());

                if (!inactivoOpt.isPresent()) {
                    continue;
                }

                AlumnoInactivo inactivo = inactivoOpt.get();

                // Solo procesar MOVILIDAD y BAJA_TEMPORAL
                if (!inactivo.debePreservarTutor()) {
                    log.debug("Alumno {} no debe preservar tutor (motivo: {}), se omite",
                            alumno.getMatricula(), inactivo.getMotivoInactividad());
                    continue;
                }

                // Intentar asignar al tutor preservado
                if (inactivo.getTutorPreservado() != null) {
                    Tutor tutor = tutorRepository.findById(inactivo.getTutorPreservado().getId())
                            .orElse(null);

                    if (tutor != null && tutor.tieneCapacidadDisponible()) {
                        // Asignación exitosa
                        alumno.setEstado(EstadoAlumno.ACTIVO);
                        alumno.setTutorActual(tutor);
                        alumnoRepository.save(alumno);

                        // Crear asignación de reingreso
                        Asignacion asignacion = new Asignacion();
                        asignacion.setAlumno(alumno);
                        asignacion.setTutor(tutor);
                        asignacion.setTipoAsignacion(TipoAsignacion.REINGRESO);
                        asignacionRepository.save(asignacion);

                        // Actualizar carga del tutor
                        tutor.incrementarCarga();
                        tutorRepository.save(tutor);

                        // Eliminar registro de inactivo
                        alumnoInactivoRepository.delete(inactivo);

                        auditoriaService.registrarLog(
                                procesoId,
                                TipoAccion.ASIGNACION,
                                "ALUMNO",
                                alumno.getId(),
                                String.format("Reingreso: Alumno %s reasignado a su tutor anterior %s",
                                        alumno.getMatricula(), tutor.getNombre()),
                                String.format("{\"estado\":\"INACTIVO\",\"motivo\":\"%s\"}",
                                        inactivo.getMotivoInactividad()),
                                String.format("{\"estado\":\"ACTIVO\",\"tutor_id\":%d}", tutor.getId()),
                                "SISTEMA"
                        );

                        reingresosExitosos++;
                        log.info("Reingreso exitoso para alumno {}", alumno.getMatricula());

                    } else {
                        // No hay capacidad
                        noAsignados.add(alumno);

                        // Crear alerta
                        ProcesoAsignacion proceso = new ProcesoAsignacion();
                        proceso.setId(procesoId);

                        AlertaProceso alerta = AlertaProceso.builder()
                                .proceso(proceso)
                                .tipo(TipoAlerta.CAPACIDAD_EXCEDIDA)
                                .severidad(SeveridadAlerta.WARNING)
                                .descripcion(String.format(
                                        "Alumno de reingreso %s no pudo ser asignado a su tutor anterior %s por falta de capacidad (Carga: %d/%d)",
                                        alumno.getMatricula(),
                                        tutor != null ? tutor.getNombre() : "N/A",
                                        tutor != null ? tutor.getCargaActual() : 0,
                                        tutor != null ? tutor.getCapacidadMax() : 0
                                ))
                                .alumno(alumno)
                                .tutor(tutor)
                                .resuelta(false)
                                .build();

                        alertaRepository.save(alerta);

                        log.warn("Alumno {} requiere asignación manual (tutor sin capacidad)",
                                alumno.getMatricula());
                    }
                }

            } catch (Exception e) {
                log.error("Error al procesar reingreso de alumno {}: {}",
                        alumnoDTO.getMatricula(), e.getMessage(), e);
            }
        }

        log.info("Reingresos procesados. Exitosos: {}, Pendientes de asignación manual: {}",
                reingresosExitosos, noAsignados.size());

        return noAsignados;
    }
}