package com.universidad.tutorias.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record RefreshTokenRequest(
        @Schema(description = "Token de refresco emitido previamente")
        String refreshToken
) {
}
