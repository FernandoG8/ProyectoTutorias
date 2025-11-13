package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.AlumnoCreateDTO;
import com.universidad.tutorias.application.dto.AlumnoPatchDTO;
import com.universidad.tutorias.application.dto.AlumnoResponseDTO;
import com.universidad.tutorias.application.dto.AlumnoUpdateDTO;
import com.universidad.tutorias.application.dto.TutorSimpleDTO;
import com.universidad.tutorias.application.service.AlumnoCrudService;
import com.universidad.tutorias.application.service.TutorSincronizacionService;
import com.universidad.tutorias.domain.entity.Alumno;
import com.universidad.tutorias.domain.entity.Asignacion;
import com.universidad.tutorias.domain.entity.Tutor;
import com.universidad.tutorias.domain.enums.EstadoAlumno;
import com.universidad.tutorias.domain.enums.TipoAsignacion;
import com.universidad.tutorias.domain.repository.AlumnoRepository;
import com.universidad.tutorias.domain.repository.AsignacionRepository;
import com.universidad.tutorias.domain.repository.TutorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlumnoCrudServiceImpl implements AlumnoCrudService {

    private final AlumnoRepository alumnoRepository;
    private final TutorRepository tutorRepository;
    private final AsignacionRepository asignacionRepository;
    private final TutorSincronizacionService tutorSincronizacionService;

    @Override
    @Transactional(readOnly = true)
    public Page<AlumnoResponseDTO> listarAlumnos(EstadoAlumno estado, String carrera, Integer semestre, Pageable pageable) {
        Specification<Alumno> specification = Specification.where(null);

        if (estado != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("estado"), estado));
        }
        if (carrera != null) {
            specification = specification.and((root, query, cb) -> cb.equal(cb.lower(root.get("carrera")), carrera.toLowerCase()));
        }
        if (semestre != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("semestre"), semestre));
        }

        Page<Alumno> alumnos = alumnoRepository.findAll(specification, pageable);
        return alumnos.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AlumnoResponseDTO obtenerAlumno(Long id) {
        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado con ID: " + id));
        return mapToResponse(alumno);
    }

    @Override
    @Transactional
    public AlumnoResponseDTO crearAlumno(AlumnoCreateDTO request) {
        if (alumnoRepository.existsByMatricula(request.getMatricula())) {
            throw new IllegalArgumentException("Ya existe un alumno con la matrícula proporcionada");
        }

        Alumno alumno = new Alumno();
        alumno.setMatricula(request.getMatricula().trim().toUpperCase());
        alumno.setNombre(request.getNombre());
        alumno.setCarrera(request.getCarrera());
        alumno.setSemestre(request.getSemestre());
        alumno.setEstado(request.getEstado() != null ? request.getEstado() : EstadoAlumno.ACTIVO);

        Alumno guardado = alumnoRepository.save(alumno);

        if (request.getTutorId() != null) {
            Tutor tutor = tutorRepository.findByIdForUpdate(request.getTutorId())
                    .orElseThrow(() -> new EntityNotFoundException("Tutor no encontrado con ID: " + request.getTutorId()));
            tutorSincronizacionService.recalcularCargaTutor(tutor.getId());
            Tutor finalTutor = tutor;
            tutor = tutorRepository.findByIdForUpdate(tutor.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Tutor no encontrado con ID: " + finalTutor.getId()));
            validarCapacidadTutor(tutor);

            guardado.setTutorActual(tutor);
            guardado = alumnoRepository.save(guardado);

            Asignacion asignacion = new Asignacion();
            asignacion.setAlumno(guardado);
            asignacion.setTutor(tutor);
            asignacion.setTipoAsignacion(TipoAsignacion.INICIAL);
            asignacionRepository.save(asignacion);

            tutor.incrementarCarga();
            tutorRepository.save(tutor);
        }

        log.info("Alumno creado con ID {}", guardado.getId());
        return mapToResponse(guardado);
    }

    @Override
    @Transactional
    public AlumnoResponseDTO reemplazarAlumno(Long id, AlumnoUpdateDTO request) {
        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado con ID: " + id));

        if (request.getTutorId() != null && !Objects.equals(obtenerTutorId(alumno), request.getTutorId())) {
            throw new IllegalArgumentException("Use el endpoint de cambio de tutor para reasignar un alumno");
        }

        alumno.setNombre(request.getNombre());
        alumno.setCarrera(request.getCarrera());
        alumno.setSemestre(request.getSemestre());
        alumno.setEstado(request.getEstado());

        Alumno actualizado = alumnoRepository.save(alumno);
        return mapToResponse(actualizado);
    }

    @Override
    @Transactional
    public AlumnoResponseDTO actualizarAlumnoParcial(Long id, AlumnoPatchDTO request) {
        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado con ID: " + id));

        if (request.getTutorId() != null && !Objects.equals(obtenerTutorId(alumno), request.getTutorId())) {
            throw new IllegalArgumentException("Use el endpoint de cambio de tutor para reasignar un alumno");
        }

        if (request.getNombre() != null) {
            alumno.setNombre(request.getNombre());
        }
        if (request.getCarrera() != null) {
            alumno.setCarrera(request.getCarrera());
        }
        if (request.getSemestre() != null) {
            alumno.setSemestre(request.getSemestre());
        }
        if (request.getEstado() != null) {
            alumno.setEstado(request.getEstado());
        }

        Alumno actualizado = alumnoRepository.save(alumno);
        return mapToResponse(actualizado);
    }

    @Override
    @Transactional
    public void eliminarAlumno(Long id) {
        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado con ID: " + id));

        Tutor tutor = alumno.getTutorActual();
        if (tutor != null) {
            Tutor tutorBloqueado = tutorRepository.findByIdForUpdate(tutor.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Tutor no encontrado con ID: " + tutor.getId()));
            tutorSincronizacionService.recalcularCargaTutor(tutorBloqueado.getId());
            tutorBloqueado = tutorRepository.findByIdForUpdate(tutorBloqueado.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Tutor no encontrado con ID: " + tutor.getId()));
            tutorBloqueado.decrementarCarga();
            tutorRepository.save(tutorBloqueado);
        }

        alumnoRepository.delete(alumno);
        log.info("Alumno eliminado con ID {}", id);
    }

    private void validarCapacidadTutor(Tutor tutor) {
        if (!tutor.tieneCapacidadDisponible()) {
            throw new IllegalArgumentException("El tutor seleccionado no tiene capacidad disponible");
        }
    }

    private Long obtenerTutorId(Alumno alumno) {
        return alumno.getTutorActual() != null ? alumno.getTutorActual().getId() : null;
    }

    private AlumnoResponseDTO mapToResponse(Alumno alumno) {
        TutorSimpleDTO tutorDTO = null;
        if (alumno.getTutorActual() != null) {
            Tutor tutor = alumno.getTutorActual();
            tutorDTO = TutorSimpleDTO.builder()
                    .id(tutor.getId())
                    .nombre(tutor.getNombre())
                    .carrera(tutor.getCarrera())
                    .build();
        }

        return AlumnoResponseDTO.builder()
                .id(alumno.getId())
                .matricula(alumno.getMatricula())
                .nombre(alumno.getNombre())
                .carrera(alumno.getCarrera())
                .semestre(alumno.getSemestre())
                .estado(alumno.getEstado())
                .tutor(tutorDTO)
                .cambiosTutor(alumno.getContadorCambiosTutor())
                .build();
    }
}
