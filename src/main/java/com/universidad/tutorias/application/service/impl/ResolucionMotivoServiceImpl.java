// ============================================
// RESOLUCION MOTIVO SERVICE IMPLEMENTATION
// ============================================

package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.service.AuditoriaService;
import com.universidad.tutorias.application.service.ResolucionMotivoService;
import com.universidad.tutorias.domain.entity.AlumnoInactivo;
import com.universidad.tutorias.domain.entity.Tutor;
import com.universidad.tutorias.domain.enums.MotivoInactividad;
import com.universidad.tutorias.domain.enums.TipoAccion;
import com.universidad.tutorias.domain.repository.AlumnoInactivoRepository;
import com.universidad.tutorias.domain.repository.TutorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResolucionMotivoServiceImpl implements ResolucionMotivoService {

    private final AlumnoInactivoRepository alumnoInactivoRepository;
    private final TutorRepository tutorRepository;
    private final AuditoriaService auditoriaService;

    @Override
    @Transactional
    public AlumnoInactivo asignarMotivo(Long alumnoInactivoId, MotivoInactividad motivo, String usuario) {

        log.info("Asignando motivo {} a alumno inactivo ID: {}", motivo, alumnoInactivoId);

        AlumnoInactivo alumnoInactivo = alumnoInactivoRepository.findById(alumnoInactivoId)
                .orElseThrow(() -> new EntityNotFoundException("Alumno inactivo no encontrado con ID: " + alumnoInactivoId));

        MotivoInactividad motivoAnterior = alumnoInactivo.getMotivoInactividad();

        alumnoInactivo.setMotivoInactividad(motivo);
        alumnoInactivo.setFechaResolucion(LocalDateTime.now());

        // Si es MOVILIDAD o BAJA_TEMPORAL, restaurar cupo del tutor
        if (motivo == MotivoInactividad.MOVILIDAD || motivo == MotivoInactividad.BAJA_TEMPORAL) {

            alumnoInactivo.setCupoLiberado(false);

            if (alumnoInactivo.getTutorPreservado() != null) {
                Tutor tutor = tutorRepository.findByIdForUpdate(alumnoInactivo.getTutorPreservado().getId())
                        .orElseThrow(() -> new EntityNotFoundException("Tutor no encontrado"));

                int cargaAntes = tutor.getCargaActual();
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
}