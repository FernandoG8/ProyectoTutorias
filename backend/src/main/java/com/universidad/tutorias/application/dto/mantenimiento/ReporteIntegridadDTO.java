package com.universidad.tutorias.application.dto.mantenimiento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteIntegridadDTO {
    private boolean esIntegro;
    private int totalAnomalias;
    private List<AnomaliaDTO> anomalias = new ArrayList<>();
    private List<String> recomendaciones = new ArrayList<>();
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class AnomaliaDTO {
    private String tipo;
    private String descripcion;
    private List<Long> idsAfectados;
    private String severidad;
}
