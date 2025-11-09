package com.universidad.tutorias.application.dto;

import com.universidad.tutorias.domain.enums.EstadoAlumno;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlumnoCreateDTO {
    @NotBlank
    private String matricula;

    @NotBlank
    private String nombre;

    @NotBlank
    private String carrera;

    @NotNull
    @Min(1)
    private Integer semestre;

    private EstadoAlumno estado;

    private Long tutorId;
}
