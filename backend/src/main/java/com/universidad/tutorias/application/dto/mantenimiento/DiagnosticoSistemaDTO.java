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
public class DiagnosticoSistemaDTO {
    private int totalTutores;
    private int tutoresSincronizados;
    private int tutoresConDiscrepancia;
    private int totalAlumnos;
    private int alumnosActivos;
    private int alumnosInactivos;
    private int inactivosSinMotivo;
    private int inactivosConMotivo;
    private int asignacionesTotales;
    private int asignacionesActivas;
    private int asignacionesInactivas;
    private List<String> anomaliasDetectadas = new ArrayList<>();
    private String estadoGeneral; // SALUDABLE, ADVERTENCIA, CRÍTICO
}
