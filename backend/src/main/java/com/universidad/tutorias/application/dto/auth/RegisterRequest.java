package com.universidad.tutorias.application.dto.auth;

import com.universidad.tutorias.domain.enums.RolUsuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @Schema(example = "secretario1")
        @NotBlank(message = "El nombre de usuario es obligatorio")
        String username,

        @Schema(example = "123456")
        @NotBlank(message = "La contraseña es obligatoria")
        String password,

        @Schema(implementation = RolUsuario.class)
        @NotNull(message = "El rol es obligatorio")
        RolUsuario role
) {
}
