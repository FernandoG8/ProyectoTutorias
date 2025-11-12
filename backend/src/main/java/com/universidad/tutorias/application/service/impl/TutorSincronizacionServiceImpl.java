package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.service.TutorSincronizacionService;
import com.universidad.tutorias.domain.entity.Tutor;
import com.universidad.tutorias.domain.repository.AsignacionRepository;
import com.universidad.tutorias.domain.repository.TutorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TutorSincronizacionServiceImpl implements TutorSincronizacionService {

    private final TutorRepository tutorRepository;
    private final AsignacionRepository asignacionRepository;

    @Override
    @Transactional
    public void recalcularCargaTutor(Long tutorId) {
        Tutor tutor = tutorRepository.findByIdForUpdate(tutorId)
                .orElseThrow(() -> new EntityNotFoundException("Tutor no encontrado: " + tutorId));

        int cargaReal = asignacionRepository.countByTutorId(tutorId);
        int cargaAnterior = tutor.getCargaActual();
        tutor.sincronizarCarga(cargaReal);
        tutorRepository.save(tutor);

        if (cargaAnterior != cargaReal) {
            log.info("Tutor {} sincronizado de {} a {} asignaciones", tutor.getNombre(), cargaAnterior, cargaReal);
        }
    }

    @Override
    @Transactional
    public void recalcularCargaTodosLosTutores() {
        List<Long> ids = tutorRepository.findAllIds();
        ids.forEach(this::recalcularCargaTutor);
    }
}
