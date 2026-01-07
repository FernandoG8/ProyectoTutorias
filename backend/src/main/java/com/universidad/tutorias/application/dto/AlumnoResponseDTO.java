package com.universidad.tutorias.application.dto;

import com.universidad.tutorias.domain.enums.EstadoAlumno;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AlumnoResponseDTO {
    Long id;
    String matricula;
    String nombre;
    String carrera;
    Integer semestre;
    EstadoAlumno estado;
    TutorSimpleDTO tutor;
    Integer cambiosTutor;
}
