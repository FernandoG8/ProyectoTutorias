package com.universidad.tutorias.application.service;

import com.universidad.tutorias.application.dto.CambioTutorRequestDTO;
import com.universidad.tutorias.application.dto.CambioTutorResponseDTO;

public interface TutorReasignacionService {
    CambioTutorResponseDTO reasignarTutor(CambioTutorRequestDTO request);
}
