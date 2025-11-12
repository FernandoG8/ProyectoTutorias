package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.domain.entity.Tutor;
import com.universidad.tutorias.domain.repository.AsignacionRepository;
import com.universidad.tutorias.domain.repository.TutorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TutorSincronizacionServiceImplTest {

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private AsignacionRepository asignacionRepository;

    @InjectMocks
    private TutorSincronizacionServiceImpl tutorSincronizacionService;

    @Test
    void recalcularCargaTutorActualizaCargaConConteoReal() {
        Tutor tutor = new Tutor();
        tutor.setId(1L);
        tutor.setNombre("Tutor Test");
        tutor.setCargaActual(5);
        tutor.setCapacidadMax(10);

        when(tutorRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(tutor));
        when(asignacionRepository.countByTutorId(1L)).thenReturn(3);

        tutorSincronizacionService.recalcularCargaTutor(1L);

        verify(asignacionRepository).countByTutorId(1L);
        verify(tutorRepository).findByIdForUpdate(1L);
        verify(tutorRepository).save(tutor);
        assertThat(tutor.getCargaActual()).isEqualTo(3);
    }

    @Test
    void recalcularCargaTodosLosTutoresInvocaProcesoPorCadaTutor() {
        when(tutorRepository.findAllIds()).thenReturn(List.of(1L, 2L));

        Tutor tutor1 = new Tutor();
        tutor1.setId(1L);
        tutor1.setCapacidadMax(5);
        tutor1.setCargaActual(4);

        Tutor tutor2 = new Tutor();
        tutor2.setId(2L);
        tutor2.setCapacidadMax(5);
        tutor2.setCargaActual(1);

        when(tutorRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(tutor1));
        when(tutorRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(tutor2));
        when(asignacionRepository.countByTutorId(1L)).thenReturn(2);
        when(asignacionRepository.countByTutorId(2L)).thenReturn(4);

        tutorSincronizacionService.recalcularCargaTodosLosTutores();

        verify(tutorRepository).findAllIds();
        verify(asignacionRepository).countByTutorId(1L);
        verify(asignacionRepository).countByTutorId(2L);
        verify(tutorRepository, times(2)).save(any(Tutor.class));
    }
}
