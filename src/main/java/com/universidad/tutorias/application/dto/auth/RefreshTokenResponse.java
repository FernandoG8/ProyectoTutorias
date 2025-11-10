package com.universidad.tutorias.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record RefreshTokenResponse(
        @Schema(description = "Nuevo token de acceso")
        String accessToken,

        @Schema(description = "Token de refresco renovado")
        String refreshToken,

        @Schema(example = "Bearer")
        String tokenType,

        @Schema(description = "Tiempo de expiración del token de acceso en segundos", example = "3600")
        long expiresIn,

        @Schema(description = "Roles asignados al usuario autenticado")
        List<String> roles
) {
}
