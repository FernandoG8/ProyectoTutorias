package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.TutorCreateDTO;
import com.universidad.tutorias.application.dto.TutorResponseDTO;
import com.universidad.tutorias.application.dto.TutorUpdateDTO;
import com.universidad.tutorias.application.service.TutorCrudService;
import com.universidad.tutorias.domain.entity.Tutor;
import com.universidad.tutorias.domain.repository.TutorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TutorCrudServiceImpl implements TutorCrudService {

    private final TutorRepository tutorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TutorResponseDTO> listarTutores(String carrera, Boolean disponibles, Boolean sobrecargados, Boolean activos) {
        Specification<Tutor> specification = Specification.where(null);

        if (carrera != null) {
            specification = specification.and((root, query, cb) -> cb.equal(cb.lower(root.get("carrera")), carrera.toLowerCase()));
        }
        if (activos != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("activo"), activos));
        }
        if (Boolean.TRUE.equals(disponibles)) {
            specification = specification.and((root, query, cb) -> cb.lt(root.get("cargaActual"), root.get("capacidadMax")));
        }
        if (Boolean.TRUE.equals(sobrecargados)) {
            specification = specification.and((root, query, cb) -> cb.greaterThan(root.get("cargaActual"), root.get("capacidadMax")));
        }

        List<Tutor> tutores = tutorRepository.findAll(specification);
        return tutores.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TutorResponseDTO obtenerTutor(Long id) {
        Tutor tutor = tutorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tutor no encontrado con ID: " + id));
        return mapToResponse(tutor);
    }

    @Override
    @Transactional
    public TutorResponseDTO crearTutor(TutorCreateDTO request) {
        Tutor tutor = new Tutor();
        tutor.setNombre(request.getNombre());
        tutor.setCarrera(request.getCarrera());
        tutor.setCapacidadMax(request.getCapacidadMax());
        tutor.setCargaActual(0);
        tutor.setAreaAtencion(request.getAreaAtencion());
        tutor.setLetraEdificio(request.getLetraEdificio());
        tutor.setActivo(request.getActivo() != null ? request.getActivo() : Boolean.TRUE);

        Tutor guardado = tutorRepository.save(tutor);
        log.info("Tutor creado con ID {}", guardado.getId());
        return mapToResponse(guardado);
    }

    @Override
    @Transactional
    public TutorResponseDTO actualizarTutor(Long id, TutorUpdateDTO request) {
        Tutor tutor = tutorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tutor no encontrado con ID: " + id));

        if (request.getCapacidadMax() < tutor.getCargaActual()) {
            throw new IllegalArgumentException("La capacidad máxima no puede ser menor a la carga actual del tutor");
        }

        tutor.setNombre(request.getNombre());
        tutor.setCarrera(request.getCarrera());
        tutor.setCapacidadMax(request.getCapacidadMax());
        tutor.setAreaAtencion(request.getAreaAtencion());
        tutor.setLetraEdificio(request.getLetraEdificio());
        tutor.setActivo(request.getActivo());

        Tutor actualizado = tutorRepository.save(tutor);
        return mapToResponse(actualizado);
    }

    @Override
    @Transactional
    public void eliminarTutor(Long id) {
        Tutor tutor = tutorRepository.findByIdWithAlumnos(id)
                .orElseThrow(() -> new EntityNotFoundException("Tutor no encontrado con ID: " + id));

        if (tutor.getAlumnosAsignados() != null && !tutor.getAlumnosAsignados().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar un tutor con alumnos asignados");
        }

        tutorRepository.delete(tutor);
        log.info("Tutor eliminado con ID {}", id);
    }

    private TutorResponseDTO mapToResponse(Tutor tutor) {
        int capacidadDisponible = tutor.getCapacidadMax() - tutor.getCargaActual();
        return TutorResponseDTO.builder()
                .id(tutor.getId())
                .nombre(tutor.getNombre())
                .carrera(tutor.getCarrera())
                .capacidadMax(tutor.getCapacidadMax())
                .cargaActual(tutor.getCargaActual())
                .capacidadDisponible(Math.max(0, capacidadDisponible))
                .areaAtencion(tutor.getAreaAtencion())
                .letraEdificio(tutor.getLetraEdificio())
                .activo(tutor.getActivo())
                .build();
    }
}
