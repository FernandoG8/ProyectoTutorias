package com.universidad.tutorias.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response DTO para diagnóstico del sistema
 * Proporciona estadísticas completas y anomalías detectadas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosticoResponse {

    private int totalTutores;
    private int totalAlumnos;
    private int alumnosActivos;
    private int alumnosInactivos;
    private int inactivosSinMotivo;
    private int asignacionesTotales;
    private int tutoresConDiscrepancia;
    private List<String> anomalias;
    private String estadoGeneral; // SALUDABLE, ADVERTENCIA, CRÍTICO

    /**
     * Estado general del sistema
     */
    public enum EstadoSistema {
        SALUDABLE,
        ADVERTENCIA,
        CRITICO
    }
}
