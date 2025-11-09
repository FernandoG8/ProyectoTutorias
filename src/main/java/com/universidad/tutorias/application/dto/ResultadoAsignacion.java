package com.universidad.tutorias.application.dto;

import com.universidad.tutorias.domain.entity.AlertaProceso;
import com.universidad.tutorias.domain.entity.Asignacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoAsignacion {
    private int totalProcesados;
    private int totalAsignados;
    private int totalErrores;
    private List<AlertaProceso> alertas;
    private List<Asignacion> asignaciones;

    public boolean esExitoso() {
        return totalAsignados == totalProcesados;
    }
}