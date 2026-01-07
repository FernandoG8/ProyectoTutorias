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
public class SincronizacionTutoresDTO {
    private int tutoresProcesados;
    private int tutoresCorregidos;
    private int totalCambios;
    private List<CambioTutorDTO> cambiosDetallados = new ArrayList<>();
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CambioTutorDTO {
    private Long tutorId;
    private String tutorNombre;
    private int cargaAnterior;
    private int cargaNueva;
    private int diferencia;
    private boolean esCorreccion;
}
