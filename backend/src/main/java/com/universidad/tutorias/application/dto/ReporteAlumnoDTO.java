package com.universidad.tutorias.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteAlumnoDTO {

    private String matricula;
    private String nombreAlumno;
    private String carrera;
    private Integer semestre;
    private String periodo;
    private String tutor;
    private String areaAtencion;
    private String edificio;
}
