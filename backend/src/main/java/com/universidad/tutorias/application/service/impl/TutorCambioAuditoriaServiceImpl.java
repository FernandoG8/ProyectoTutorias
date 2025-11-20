package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.AlumnoSimpleDTO;
import com.universidad.tutorias.application.dto.TutorCambioAuditoriaDTO;
import com.universidad.tutorias.application.dto.TutorSimpleDTO;
import com.universidad.tutorias.application.service.TutorCambioAuditoriaService;
import com.universidad.tutorias.domain.entity.Asignacion;
import com.universidad.tutorias.domain.entity.Tutor;
import com.universidad.tutorias.domain.entity.TutorCambioAuditoria;
import com.universidad.tutorias.domain.repository.TutorCambioAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de auditoría de cambios de tutor.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TutorCambioAuditoriaServiceImpl implements TutorCambioAuditoriaService {

    private final TutorCambioAuditoriaRepository tutorCambioRepository;

    @Override
    public TutorCambioAuditoriaDTO registrarCambio(
            Asignacion asignacion,
            Tutor tutorNuevo,
            String usuarioResponsable,
            String motivo) {
        
        return registrarCambioDetallado(
                asignacion,
                tutorNuevo,
                usuarioResponsable,
                motivo,
                "MANUAL",
                null);
    }

    @Override
    public TutorCambioAuditoriaDTO registrarCambioDetallado(
            Asignacion asignacion,
            Tutor tutorNuevo,
            String usuarioResponsable,
            String motivo,
            String tipoCambio,
            String notas) {
        
        try {
            Tutor tutorAnterior = asignacion.getTutor();
            
            log.info("Registrando cambio de tutor para alumno {} ({}): {} → {}",
                    asignacion.getAlumno().getMatricula(),
                    asignacion.getAlumno().getNombre(),
                    tutorAnterior != null ? tutorAnterior.getNombre() : "NINGUNO",
                    tutorNuevo.getNombre());
            
            TutorCambioAuditoria cambio = TutorCambioAuditoria.builder()
                    .asignacion(asignacion)
                    .tutorAnterior(tutorAnterior)
                    .tutorNuevo(tutorNuevo)
                    .usuarioResponsable(usuarioResponsable)
                    .motivo(motivo)
                    .tipoCambio(tipoCambio != null ? tipoCambio : "MANUAL")
                    .notas(notas)
                    .build();
            
            TutorCambioAuditoria cambioGuardado = tutorCambioRepository.save(cambio);
            
            log.info("Cambio de tutor registrado con ID: {}", cambioGuardado.getId());
            
            return convertirADTO(cambioGuardado);
            
        } catch (Exception e) {
            log.error("Error al registrar cambio de tutor", e);
            throw new RuntimeException("Error al registrar cambio de tutor: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutorCambioAuditoriaDTO> obtenerHistorialPorAsignacion(Long asignacionId) {
        log.debug("Obteniendo historial de cambios para asignación ID: {}", asignacionId);
        
        List<TutorCambioAuditoria> cambios = tutorCambioRepository.findByAsignacionId(asignacionId);
        
        return cambios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutorCambioAuditoriaDTO> obtenerHistorialPorAlumno(Long alumnoId) {
        log.debug("Obteniendo historial de cambios para alumno ID: {}", alumnoId);
        
        List<TutorCambioAuditoria> cambios = tutorCambioRepository.findByAlumnoId(alumnoId);
        
        return cambios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutorCambioAuditoriaDTO> obtenerAsignacionesPorTutor(Long tutorId) {
        log.debug("Obteniendo asignaciones para tutor ID: {}", tutorId);
        
        List<TutorCambioAuditoria> cambios = tutorCambioRepository.findByTutorNuevoId(tutorId);
        
        return cambios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutorCambioAuditoriaDTO> obtenerRemocionesPorTutor(Long tutorId) {
        log.debug("Obteniendo remociones para tutor ID: {}", tutorId);
        
        List<TutorCambioAuditoria> cambios = tutorCambioRepository.findByTutorAnteriorId(tutorId);
        
        return cambios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutorCambioAuditoriaDTO> obtenerCambiosPorFecha(LocalDateTime inicio, LocalDateTime fin) {
        log.debug("Obteniendo cambios entre {} y {}", inicio, fin);
        
        List<TutorCambioAuditoria> cambios = tutorCambioRepository.findByFechaRango(inicio, fin);
        
        return cambios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutorCambioAuditoriaDTO> obtenerCambiosPorUsuario(String usuario) {
        log.debug("Obteniendo cambios realizados por usuario: {}", usuario);
        
        List<TutorCambioAuditoria> cambios = tutorCambioRepository.findByUsuarioResponsable(usuario);
        
        return cambios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TutorCambioAuditoriaDTO> obtenerCambiosPorTipo(String tipoCambio) {
        log.debug("Obteniendo cambios de tipo: {}", tipoCambio);
        
        List<TutorCambioAuditoria> cambios = tutorCambioRepository.findByTipoCambio(tipoCambio);
        
        return cambios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte TutorCambioAuditoria entity a DTO.
     */
    private TutorCambioAuditoriaDTO convertirADTO(TutorCambioAuditoria cambio) {
        if (cambio == null) {
            return null;
        }
        
        // Construir DTO de alumno
        AlumnoSimpleDTO alumnoDto = null;
        if (cambio.getAsignacion() != null && cambio.getAsignacion().getAlumno() != null) {
            alumnoDto = AlumnoSimpleDTO.builder()
                    .id(cambio.getAsignacion().getAlumno().getId())
                    .matricula(cambio.getAsignacion().getAlumno().getMatricula())
                    .nombre(cambio.getAsignacion().getAlumno().getNombre())
                    .carrera(cambio.getAsignacion().getAlumno().getCarrera())
                    .build();
        }
        
        // Construir DTO de tutor anterior
        TutorSimpleDTO tutorAnteriorDto = null;
        if (cambio.getTutorAnterior() != null) {
            tutorAnteriorDto = TutorSimpleDTO.builder()
                    .id(cambio.getTutorAnterior().getId())
                    .nombre(cambio.getTutorAnterior().getNombre())
                    .carrera(cambio.getTutorAnterior().getCarrera())
                    .cargaActual(cambio.getTutorAnterior().getCargaActual())
                    .capacidadMax(cambio.getTutorAnterior().getCapacidadMax())
                    .build();
        }
        
        // Construir DTO de tutor nuevo
        TutorSimpleDTO tutorNuevoDto = null;
        if (cambio.getTutorNuevo() != null) {
            tutorNuevoDto = TutorSimpleDTO.builder()
                    .id(cambio.getTutorNuevo().getId())
                    .nombre(cambio.getTutorNuevo().getNombre())
                    .carrera(cambio.getTutorNuevo().getCarrera())
                    .cargaActual(cambio.getTutorNuevo().getCargaActual())
                    .capacidadMax(cambio.getTutorNuevo().getCapacidadMax())
                    .build();
        }
        
        // Construir DTO del cambio
        return TutorCambioAuditoriaDTO.builder()
                .auditoriaId(cambio.getId())
                .asignacionId(cambio.getAsignacion() != null ? cambio.getAsignacion().getId() : null)
                .alumno(alumnoDto)
                .tutorAnterior(tutorAnteriorDto)
                .tutorNuevo(tutorNuevoDto)
                .usuarioResponsable(cambio.getUsuarioResponsable())
                .fechaHoraCambio(cambio.getFechaHoraCambio())
                .motivo(cambio.getMotivo())
                .tipoCambio(cambio.getTipoCambio())
                .notas(cambio.getNotas())
                .build();
    }

}
