package com.universidad.tutorias.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record UserInfoResponse(
        @Schema(example = "1")
        Long id,

        @Schema(example = "coord_tutorias")
        String username,

        @Schema(description = "Roles asociados al usuario en sesión")
        List<String> roles
) {
}
