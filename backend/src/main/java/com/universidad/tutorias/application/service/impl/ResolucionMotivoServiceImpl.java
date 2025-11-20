// ============================================
// RESOLUCION MOTIVO SERVICE IMPLEMENTATION
// ============================================

package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.service.AuditoriaService;
import com.universidad.tutorias.application.service.ResolucionMotivoService;
import com.universidad.tutorias.application.service.TutorSincronizacionService;
import com.universidad.tutorias.domain.entity.Alumno;
import com.universidad.tutorias.domain.entity.AlumnoInactivo;
import com.universidad.tutorias.domain.entity.AlumnoBajaDefinitiva;
import com.universidad.tutorias.domain.entity.AlumnoEgresado;
import com.universidad.tutorias.domain.entity.Tutor;
import com.universidad.tutorias.domain.enums.MotivoInactividad;
import com.universidad.tutorias.domain.enums.TipoAccion;
import com.universidad.tutorias.domain.repository.AlumnoInactivoRepository;
import com.universidad.tutorias.domain.repository.TutorRepository;
import com.universidad.tutorias.domain.repository.AlumnoEgresadoRepository;
import com.universidad.tutorias.domain.repository.AlumnoBajaDefinitivaRepository;
import com.universidad.tutorias.domain.repository.AsignacionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResolucionMotivoServiceImpl implements ResolucionMotivoService {

    private final AlumnoInactivoRepository alumnoInactivoRepository;
    private final TutorRepository tutorRepository;
    private final AuditoriaService auditoriaService;
    private final TutorSincronizacionService tutorSincronizacionService;
    private final AlumnoEgresadoRepository alumnoEgresadoRepository;
    private final AlumnoBajaDefinitivaRepository alumnoBajaDefinitivaRepository;
    private final AsignacionRepository asignacionRepository;

    @Override
    @Transactional
    public AlumnoInactivo asignarMotivo(Long alumnoInactivoId, MotivoInactividad motivo, String usuario) {

        log.info("Asignando motivo {} a alumno inactivo ID: {}", motivo, alumnoInactivoId);

        AlumnoInactivo alumnoInactivo = alumnoInactivoRepository.findById(alumnoInactivoId)
                .orElseThrow(() -> new EntityNotFoundException("Alumno inactivo no encontrado con ID: " + alumnoInactivoId));

        Alumno alumno = alumnoInactivo.getAlumno();
        MotivoInactividad motivoAnterior = alumnoInactivo.getMotivoInactividad();

        alumnoInactivo.setMotivoInactividad(motivo);
        alumnoInactivo.setFechaResolucion(LocalDateTime.now());

        // Si es MOVILIDAD o BAJA_TEMPORAL, restaurar cupo del tutor
        if (motivo == MotivoInactividad.MOVILIDAD || motivo == MotivoInactividad.BAJA_TEMPORAL) {

            alumnoInactivo.setCupoLiberado(false);

            if (alumnoInactivo.getTutorPreservado() != null) {
                Tutor tutor = tutorSincronizacionService.sincronizarYBloquearTutor(
                        alumnoInactivo.getTutorPreservado().getId());

                int cargaAntes = tutor.getCargaActual();
                if (!tutor.tieneCapacidadDisponible()) {
                    log.warn("Tutor {} (ID: {}) no tiene capacidad para restaurar cupo del alumno {} (carga: {}/{})",
                            tutor.getNombre(), tutor.getId(), alumno.getMatricula(), cargaAntes, tutor.getCapacidadMax());
                } else {
                    tutor.incrementarCarga();
                    tutorRepository.save(tutor);

                    log.info("Cupo restaurado para tutor {} (carga: {} -> {})",
                            tutor.getNombre(), cargaAntes, tutor.getCargaActual());

                    auditoriaService.registrarLog(
                            null,
                            TipoAccion.RESOLUCION_MOTIVO,
                            "TUTOR",
                            tutor.getId(),
                            String.format("Cupo restaurado para alumno con motivo %s", motivo),
                            String.format("{\"carga_actual\":%d}", cargaAntes),
                            String.format("{\"carga_actual\":%d}", tutor.getCargaActual()),
                            usuario
                    );
                }
            }
        }

        // Si es EGRESADO o BAJA DEFINITIVA, almacenar en tabla histórica y limpiar vínculos con tutor actual
        if (motivo == MotivoInactividad.EGRESADO) {
            registrarEgresado(alumno);
            alumnoInactivo.setCupoLiberado(false); // evitar que un proceso de liberación posterior descuente nuevamente
            desasignarAlumno(alumno);
        } else if (motivo == MotivoInactividad.BAJA_DEFINITIVA) {
            registrarBajaDefinitiva(alumno);
            alumnoInactivo.setCupoLiberado(false);
            desasignarAlumno(alumno);
        }

        AlumnoInactivo actualizado = alumnoInactivoRepository.save(alumnoInactivo);

        auditoriaService.registrarLog(
                null,
                TipoAccion.RESOLUCION_MOTIVO,
                "ALUMNO_INACTIVO",
                alumnoInactivo.getId(),
                String.format("Motivo de inactividad actualizado de %s a %s para alumno %s",
                        motivoAnterior, motivo, alumnoInactivo.getAlumno().getMatricula()),
                String.format("{\"motivo\":\"%s\",\"cupo_liberado\":%b}", motivoAnterior, true),
                String.format("{\"motivo\":\"%s\",\"cupo_liberado\":%b}", motivo, alumnoInactivo.getCupoLiberado()),
                usuario
        );

        log.info("Motivo asignado exitosamente para alumno {}: {}",
                alumnoInactivo.getAlumno().getMatricula(), motivo);
        return actualizado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumnoInactivo> obtenerPendientesDeResolucion(String carrera, Integer semestre, MotivoInactividad motivo) {
        log.info("Obteniendo alumnos inactivos pendientes de resolución. Carrera: {}, Semestre: {}, Motivo: {}",
                carrera, semestre, motivo);
        List<AlumnoInactivo> pendientes = alumnoInactivoRepository.findPendientesDeResolucion(carrera, semestre, motivo);
        log.info("Encontrados {} alumnos pendientes de resolución", pendientes.size());
        return pendientes;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumnoInactivo> listarInactivos(String carrera, Integer semestre, MotivoInactividad motivo) {
        log.info("Listando alumnos inactivos. Carrera: {}, Semestre: {}, Motivo: {}", carrera, semestre, motivo);
        return alumnoInactivoRepository.findAllWithFilters(carrera, semestre, motivo);
    }

    private void registrarEgresado(Alumno alumno) {
        if (alumno == null || alumno.getId() == null) {
            return;
        }
        if (alumnoEgresadoRepository.existsByAlumnoId(alumno.getId())) {
            return;
        }
        alumnoEgresadoRepository.save(
                AlumnoEgresado.builder()
                        .alumno(alumno)
                        .matricula(alumno.getMatricula())
                        .nombre(alumno.getNombre())
                        .carrera(alumno.getCarrera())
                        .semestre(alumno.getSemestre())
                        .build()
        );
        log.info("Alumno {} registrado como egresado", alumno.getMatricula());
    }

    private void registrarBajaDefinitiva(Alumno alumno) {
        if (alumno == null || alumno.getId() == null) {
            return;
        }
        if (alumnoBajaDefinitivaRepository.existsByAlumnoId(alumno.getId())) {
            return;
        }
        alumnoBajaDefinitivaRepository.save(
                AlumnoBajaDefinitiva.builder()
                        .alumno(alumno)
                        .matricula(alumno.getMatricula())
                        .nombre(alumno.getNombre())
                        .carrera(alumno.getCarrera())
                        .semestre(alumno.getSemestre())
                        .build()
        );
        log.info("Alumno {} registrado como baja definitiva", alumno.getMatricula());
    }

    /**
     * Elimina asignaciones del alumno y ajusta cargas de tutores afectados con bloqueo y recálculo.
     */
    private void desasignarAlumno(Alumno alumno) {
        alumno.setTutorActual(null);

        var asignaciones = asignacionRepository.findByAlumnoId(alumno.getId());
        if (asignaciones.isEmpty()) {
            return;
        }

        // Agrupar por tutor para actualizar cargas con un solo acceso por tutor
        Map<Long, Long> cargaPorTutor = asignaciones.stream()
                .filter(a -> a.getTutor() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        a -> a.getTutor().getId(),
                        java.util.stream.Collectors.counting()
                ));

        cargaPorTutor.forEach((tutorId, cantidad) -> {
            Tutor tutor = tutorSincronizacionService.sincronizarYBloquearTutor(tutorId);
            int nuevaCarga = Math.max(0, tutor.getCargaActual() - cantidad.intValue());
            tutor.sincronizarCarga(nuevaCarga);
            tutorRepository.save(tutor);
            log.info("Ajustada carga del tutor {} a {} tras desasignar {} alumnos", tutor.getNombre(), nuevaCarga, cantidad);
        });

        asignacionRepository.deleteAll(asignaciones);
        log.info("Asignaciones eliminadas para alumno {}", alumno.getMatricula());
    }
}
