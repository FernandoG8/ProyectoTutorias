package com.universidad.tutorias.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @Schema(description = "Token de refresco emitido previamente")
        @NotBlank(message = "El token de refresco es obligatorio")
        String refreshToken
) {
}
