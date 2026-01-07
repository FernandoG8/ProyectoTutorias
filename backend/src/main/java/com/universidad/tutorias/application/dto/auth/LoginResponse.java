package com.universidad.tutorias.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "Mensaje de confirmación del inicio de sesión")
        String mensaje
) {
}
