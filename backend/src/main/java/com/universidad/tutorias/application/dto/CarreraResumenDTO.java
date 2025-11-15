package com.universidad.tutorias.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarreraResumenDTO {
    private String carrera;
    private Integer totalAlumnos;
    private Integer totalTutores;
    private List<TutorConAlumnosDTO> tutores;
}