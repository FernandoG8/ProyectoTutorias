package com.universidad.tutorias.application.service;

import com.universidad.tutorias.application.dto.TutorCreateDTO;
import com.universidad.tutorias.application.dto.TutorResponseDTO;
import com.universidad.tutorias.application.dto.TutorUpdateDTO;

import java.util.List;

public interface TutorCrudService {
    List<TutorResponseDTO> listarTutores(String carrera, Boolean disponibles, Boolean sobrecargados, Boolean activos);

    TutorResponseDTO obtenerTutor(Long id);

    TutorResponseDTO crearTutor(TutorCreateDTO request);

    TutorResponseDTO actualizarTutor(Long id, TutorUpdateDTO request);

    void eliminarTutor(Long id);
}
