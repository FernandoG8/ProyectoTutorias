package com.universidad.tutorias.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "coord_tutorias")
        @NotBlank(message = "El nombre de usuario es obligatorio")
        String username,

        @Schema(example = "123456")
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
