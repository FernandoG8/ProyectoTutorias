package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.application.dto.auth.*;
import com.universidad.tutorias.application.service.AuthService;
import com.universidad.tutorias.domain.entity.Usuario;
import com.universidad.tutorias.infrastructure.controller.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemeType;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Gestión de autenticación y tokens JWT")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario", description = "Autentica credenciales y entrega tokens JWT")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Autenticación exitosa",
            content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Solicitud de login para usuario {}", request.username());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('COORDINADOR_TUTORIAS')")
    @Operation(summary = "Registrar usuario", description = "Permite al coordinador crear nuevos usuarios",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario creado",
            content = @Content(schema = @Schema(implementation = RegisterResponse.class)))
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token de acceso", description = "Genera un nuevo token de acceso usando el refresh token")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token renovado",
            content = @Content(schema = @Schema(implementation = RefreshTokenResponse.class)))
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    @Operation(summary = "Información del usuario autenticado", description = "Obtiene información del usuario actual",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Información del usuario",
            content = @Content(schema = @Schema(implementation = UserInfoResponse.class)))
    public ResponseEntity<ApiResponse<UserInfoResponse>> me(@AuthenticationPrincipal Usuario usuario) {
        UserInfoResponse response = authService.buildUserInfo(usuario);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
