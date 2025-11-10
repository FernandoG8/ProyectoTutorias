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
public class TutorConAlumnosDTO {
    private Long id;
    private String nombre;
    private Integer cargaActual;
    private Integer capacidadMax;
    private String areaAtencion;
    private String letraEdificio;
    private List<AlumnoDetalleDTO> alumnos;
}