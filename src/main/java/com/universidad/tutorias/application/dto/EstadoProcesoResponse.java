package com.universidad.tutorias.application.dto;

import com.universidad.tutorias.domain.enums.EstadoProceso;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoProcesoResponse {
    private Long procesoId;
    private EstadoProceso estado;
    private ProgresoDTO progreso;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Long tiempoTranscurridoMs;
}