package com.universidad.tutorias.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record RegisterResponse(
        @Schema(description = "Identificador del nuevo usuario", example = "7")
        Long id,

        @Schema(example = "secretario1")
        String username,

        @Schema(description = "Roles asignados al usuario creado")
        List<String> roles
) {
}
