package com.universidad.tutorias.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TutorUpdateDTO {
    @NotBlank
    private String nombre;

    @NotBlank
    private String carrera;

    @NotNull
    @Min(1)
    private Integer capacidadMax;

    private String areaAtencion;
    private String letraEdificio;

    @NotNull
    private Boolean activo;
}
