package com.universidad.tutorias.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CambioTutorRequestDTO {
    @NotNull
    private Long alumnoId;

    @NotNull
    private Long tutorOrigenId;

    @NotNull
    private Long tutorDestinoId;

    @NotBlank
    private String motivo;

    @NotBlank
    private String usuario;
}
