package com.universidad.tutorias.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
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
    @JsonAlias({"usuario", "usuario_responsable"})
    private String usuarioResponsable;

    @JsonAlias({"semestreAcademico", "semestre_academico", "semestre"})
    private String semestreAcademico;
}
