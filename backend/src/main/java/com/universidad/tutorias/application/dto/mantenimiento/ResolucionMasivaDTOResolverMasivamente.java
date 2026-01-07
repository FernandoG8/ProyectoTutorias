package com.universidad.tutorias.application.dto.mantenimiento;

import com.universidad.tutorias.application.dto.ResolucionMotivoResultDTO;
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
public class ResolucionMasivaDTOResolverMasivamente {
    private int totalProcesados;
    private int exitosos;
    private int fallidos;
    private List<ResolucionMotivoResultDTO> resultados = new ArrayList<>();
    private List<ErrorResolucionDTO> errores = new ArrayList<>();
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ErrorResolucionDTO {
    private Long alumnoInactivoId;
    private String alumnoMatricula;
    private String error;
}
