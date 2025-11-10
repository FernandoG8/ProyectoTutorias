package com.universidad.tutorias.application.dto;

import com.universidad.tutorias.domain.entity.ErrorValidacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoValidacion {
    private List<AlumnoExcelDTO> alumnosValidos;
    private List<ErrorValidacion> errores;
    private boolean tieneErroresCriticos;

    public int getTotalValidos() {
        return alumnosValidos != null ? alumnosValidos.size() : 0;
    }

    public int getTotalErrores() {
        return errores != null ? errores.size() : 0;
    }
}