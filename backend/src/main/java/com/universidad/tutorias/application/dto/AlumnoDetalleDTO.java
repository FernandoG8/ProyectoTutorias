package com.universidad.tutorias.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlumnoDetalleDTO {
    private Long id;
    private String matricula;
    private String nombre;
    private Integer semestre;
    private LocalDateTime fechaAsignacion;
}