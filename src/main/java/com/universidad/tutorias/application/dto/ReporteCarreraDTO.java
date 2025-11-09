package com.universidad.tutorias.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteCarreraDTO {
    private String semestre;
    private LocalDateTime fechaGeneracion;
    private List<CarreraResumenDTO> carreras;
}