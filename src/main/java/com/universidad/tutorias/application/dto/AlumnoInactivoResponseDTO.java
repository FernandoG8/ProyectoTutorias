package com.universidad.tutorias.application.dto;

import com.universidad.tutorias.domain.enums.MotivoInactividad;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class AlumnoInactivoResponseDTO {
    Long id;
    Long alumnoId;
    String nombre;
    String matricula;
    String carrera;
    Integer semestre;
    MotivoInactividad motivo;
    LocalDateTime fechaDeteccion;
    Boolean cupoLiberado;
}
