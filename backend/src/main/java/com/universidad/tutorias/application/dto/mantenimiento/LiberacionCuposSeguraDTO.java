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
public class LiberacionCuposSeguraDTO {
    private boolean exitoso;
    private int cuposLiberados;
    private int tutoresActualizados;
    private List<CupoLiberadoDTO> detalles = new ArrayList<>();
    private List<String> advertencias = new ArrayList<>();
    private List<String> errores = new ArrayList<>();
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CupoLiberadoDTO {
    private Long tutorId;
    private String tutorNombre;
    private int alumnosLiberados;
    private int cargaAnterior;
    private int cargaNueva;
}
