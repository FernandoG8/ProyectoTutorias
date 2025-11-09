package com.universidad.tutorias.application.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class CambioTutorResponseDTO {
    Long alumnoId;
    Long tutorAnteriorId;
    Long tutorNuevoId;
    String tutorAnteriorNombre;
    String tutorNuevoNombre;
    LocalDateTime fechaCambio;
    String motivo;
}
