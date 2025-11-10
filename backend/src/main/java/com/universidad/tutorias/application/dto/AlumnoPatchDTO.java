package com.universidad.tutorias.application.dto;

import com.universidad.tutorias.domain.enums.EstadoAlumno;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlumnoPatchDTO {
    private String nombre;
    private String carrera;
    private Integer semestre;
    private EstadoAlumno estado;
    private Long tutorId;
}
