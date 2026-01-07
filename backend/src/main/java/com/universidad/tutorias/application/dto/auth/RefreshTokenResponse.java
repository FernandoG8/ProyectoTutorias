package com.universidad.tutorias.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record RefreshTokenResponse(
        @Schema(description = "Mensaje de confirmación de la renovación del token")
        String mensaje
) {
}
