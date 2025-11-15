package com.universidad.tutorias.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorAsignacionDTO {
    private int filaExcel;
    private String matricula;
    private String nombreAlumno;
    private String carrera;
    private TipoErrorAsignacion tipoError;
    private String mensajeError;
    private String detallesTecnicos;
}
