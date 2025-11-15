package com.universidad.tutorias.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgresoDTO {
    private Integer porcentaje;
    private Integer alumnosProcesados;
    private Integer alumnosAsignados;
    private Integer errores;
    private Integer warnings;
}