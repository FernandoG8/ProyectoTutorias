package com.universidad.tutorias.application.dto.drive;

import com.universidad.tutorias.application.enums.FormatoReporte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExportMasivoJobRequest {
    @NotBlank
    private String semestre;

    @NotNull
    private FormatoReporte formato;
}
