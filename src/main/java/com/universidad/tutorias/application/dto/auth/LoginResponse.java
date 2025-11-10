package com.universidad.tutorias.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record LoginResponse(
        @Schema(description = "Token JWT de acceso")
        String accessToken,

        @Schema(description = "Token de refresco persistido")
        String refreshToken,

        @Schema(example = "Bearer")
        String tokenType,

        @Schema(description = "Tiempo de expiración en segundos del token de acceso", example = "3600")
        long expiresIn,

        @Schema(description = "Roles asignados al usuario autenticado")
        List<String> roles
) {
}
