package com.universidad.tutorias.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlumnoSimpleDTO {
    private Long id;
    private String matricula;
    private String nombre;
    private String carrera;
}