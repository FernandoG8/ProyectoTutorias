package com.universidad.tutorias.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO para validación de integridad del sistema
 * Detecta anomalías críticas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegridadResponse {

    private boolean esIntegro;
    private int totalAnomalias;
    private List<Anomalia> anomalias;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Anomalia {
        private String tipo; // ASIGNACION_FANTASMA, ALUMNO_SIN_TUTOR, TUTOR_SOBRECARGADO
        private String descripcion;
        private int cantidad;
        private String severidad; // CRÍTICO, ADVERTENCIA, INFORMACIÓN
    }
}
