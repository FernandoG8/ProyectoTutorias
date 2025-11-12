package com.universidad.tutorias.application.dto;

import com.universidad.tutorias.domain.entity.AlertaProceso;
import com.universidad.tutorias.domain.entity.Asignacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoAsignacion {
    private int totalProcesados;
    private int totalAsignados;
    private int totalErrores;
    private List<ErrorAsignacionDTO> errores;
    private List<AlertaProceso> alertas;
    private List<Asignacion> asignaciones;
    private Map<String, Integer> estadisticas;

    public boolean esExitoso() {
        return totalAsignados == totalProcesados;
    }
}