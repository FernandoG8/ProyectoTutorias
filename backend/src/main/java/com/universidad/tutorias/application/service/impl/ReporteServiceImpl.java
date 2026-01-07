// ============================================
// REPORTE SERVICE IMPLEMENTATION
// ============================================

package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.*;
import com.universidad.tutorias.application.service.ReporteService;
import com.universidad.tutorias.domain.entity.Asignacion;
import com.universidad.tutorias.domain.entity.Tutor;
import com.universidad.tutorias.domain.repository.AsignacionRepository;
import com.universidad.tutorias.domain.repository.TutorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private final AsignacionRepository asignacionRepository;
    private final TutorRepository tutorRepository;

    private static final List<String> TODAS_LAS_CARRERAS = Arrays.asList(
            "ICA", "IE", "IMECA", "IME", "ISC", "ITS"
    );

    @Override
    @Transactional(readOnly = true)
    public ReporteCarreraDTO generarReportePorCarrera(String semestreAcademico, String carrera) {

        log.info("Generando reporte por carrera. Semestre: {}, Carrera: {}", semestreAcademico, carrera);

        List<String> carreras = carrera != null
                ? Collections.singletonList(carrera.toUpperCase())
                : TODAS_LAS_CARRERAS;

        List<CarreraResumenDTO> resumenCarreras = new ArrayList<>();

        for (String carr : carreras) {
            List<Asignacion> asignaciones = asignacionRepository.findByCarreraAndSemestreString(carr, semestreAcademico);

            if (asignaciones.isEmpty()) {
                log.debug("No hay asignaciones para carrera {} en semestre {}", carr, semestreAcademico);
                continue;
            }

            // Agrupar por tutor
            Map<Tutor, List<Asignacion>> porTutor = asignaciones.stream()
                    .collect(Collectors.groupingBy(Asignacion::getTutor));

            List<TutorConAlumnosDTO> tutoresDTO = porTutor.entrySet().stream()
                    .map(entry -> {
                        Tutor tutor = entry.getKey();
                        List<Asignacion> asignacionesTutor = entry.getValue();

                        List<AlumnoDetalleDTO> alumnosDTO = asignacionesTutor.stream()
                                .map(a -> AlumnoDetalleDTO.builder()
                                        .id(a.getAlumno().getId())
                                        .matricula(a.getAlumno().getMatricula())
                                        .nombre(a.getAlumno().getNombre())
                                        .semestre(a.getAlumno().getSemestre())
                                        .fechaAsignacion(a.getFechaAsignacion())
                                        .build())
                                .sorted(Comparator.comparing(AlumnoDetalleDTO::getMatricula))
                                .collect(Collectors.toList());

                        return TutorConAlumnosDTO.builder()
                                .id(tutor.getId())
                                .nombre(tutor.getNombre())
                                .cargaActual(tutor.getCargaActual())
                                .capacidadMax(tutor.getCapacidadMax())
                                .areaAtencion(tutor.getAreaAtencion())
                                .letraEdificio(tutor.getLetraEdificio())
                                .alumnos(alumnosDTO)
                                .build();
                    })
                    .sorted(Comparator.comparing(TutorConAlumnosDTO::getNombre))
                    .collect(Collectors.toList());

            int totalAlumnos = asignaciones.size();
            int totalTutores = porTutor.size();

            CarreraResumenDTO resumen = CarreraResumenDTO.builder()
                    .carrera(carr)
                    .totalAlumnos(totalAlumnos)
                    .totalTutores(totalTutores)
                    .tutores(tutoresDTO)
                    .build();

            resumenCarreras.add(resumen);
        }

        log.info("Reporte generado para {} carreras", resumenCarreras.size());

        return ReporteCarreraDTO.builder()
                .semestre(semestreAcademico)
                .fechaGeneracion(LocalDateTime.now())
                .carreras(resumenCarreras)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TutorConAlumnosDTO obtenerAlumnosDeTutor(Long tutorId, String semestreAcademico) {

        log.info("Obteniendo alumnos del tutor ID: {}, Semestre: {}", tutorId, semestreAcademico);

        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new EntityNotFoundException("Tutor no encontrado con ID: " + tutorId));

        List<Asignacion> asignaciones;
        if (semestreAcademico != null && !semestreAcademico.trim().isEmpty()) {
            asignaciones = asignacionRepository.findByTutorAndSemestreString(tutorId, semestreAcademico);
        } else {
            // Obtener todas las asignaciones del tutor
            asignaciones = asignacionRepository.findByTutorIdWithDetalles(tutorId);
        }

        List<AlumnoDetalleDTO> alumnosDTO = asignaciones.stream()
                .map(a -> AlumnoDetalleDTO.builder()
                        .id(a.getAlumno().getId())
                        .matricula(a.getAlumno().getMatricula())
                        .nombre(a.getAlumno().getNombre())
                        .semestre(a.getAlumno().getSemestre())
                        .fechaAsignacion(a.getFechaAsignacion())
                        .build())
                .sorted(Comparator.comparing(AlumnoDetalleDTO::getMatricula))
                .collect(Collectors.toList());

        log.info("Encontrados {} alumnos para el tutor {}", alumnosDTO.size(), tutor.getNombre());

        return TutorConAlumnosDTO.builder()
                .id(tutor.getId())
                .nombre(tutor.getNombre())
                .cargaActual(tutor.getCargaActual())
                .capacidadMax(tutor.getCapacidadMax())
                .areaAtencion(tutor.getAreaAtencion())
                .letraEdificio(tutor.getLetraEdificio())
                .alumnos(alumnosDTO)
                .build();
    }
}