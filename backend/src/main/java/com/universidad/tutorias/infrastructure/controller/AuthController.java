package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.application.dto.auth.*;
import com.universidad.tutorias.application.service.AuthService;
import com.universidad.tutorias.domain.entity.Usuario;
import com.universidad.tutorias.infrastructure.controller.response.ApiResponse;
import com.universidad.tutorias.infrastructure.security.TokenCookieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Gestión de autenticación basada en cookies seguras")
@RequiredArgsConstructor
@Slf4j
@SecurityScheme(name = "cookieAuth", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.COOKIE, paramName = "tutorias_access_token")
public class AuthController {

    private final AuthService authService;
    private final TokenCookieService tokenCookieService;

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario", description = "Autentica credenciales y emite cookies de sesión seguras")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Autenticación exitosa",
            content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request,
                                                            HttpServletResponse response) {
        log.info("Solicitud de login para usuario {}", request.username());
        AuthTokensResult tokens = authService.login(request);
        tokenCookieService.addAuthCookies(response, tokens);
        LoginResponse body = new LoginResponse("Autenticación exitosa");
        return ResponseEntity.ok(ApiResponse.success(body, "Sesión iniciada correctamente"));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('COORDINADOR_TUTORIAS')")
    @Operation(summary = "Registrar usuario", description = "Permite al coordinador crear nuevos usuarios",
            security = {@SecurityRequirement(name = "cookieAuth")})
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
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(HttpServletRequest servletRequest,
                                                                     HttpServletResponse response,
                                                                     @RequestBody(required = false) RefreshTokenRequest request) {
        String refreshTokenValue = resolveRefreshToken(servletRequest, request);
        AuthTokensResult tokens = authService.refresh(refreshTokenValue);
        tokenCookieService.addAuthCookies(response, tokens);
        RefreshTokenResponse body = new RefreshTokenResponse("Token renovado correctamente");
        return ResponseEntity.ok(ApiResponse.success(body, "Token de acceso renovado correctamente"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Revoca el refresh token activo y limpia las cookies",
            security = {@SecurityRequirement(name = "cookieAuth")})
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest servletRequest,
                                                    HttpServletResponse response,
                                                    @RequestBody(required = false) RefreshTokenRequest request) {
        String refreshTokenValue = resolveRefreshToken(servletRequest, request);
        authService.logout(refreshTokenValue);
        tokenCookieService.clearAuthCookies(response);
        return ResponseEntity.ok(ApiResponse.success(null, "Sesión cerrada correctamente"));
    }

    @GetMapping("/me")
    @Operation(summary = "Información del usuario autenticado", description = "Obtiene información del usuario actual",
            security = {@SecurityRequirement(name = "cookieAuth")})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Información del usuario",
            content = @Content(schema = @Schema(implementation = UserInfoResponse.class)))
    public ResponseEntity<ApiResponse<UserInfoResponse>> me(@AuthenticationPrincipal Usuario usuario) {
        UserInfoResponse response = authService.buildUserInfo(usuario);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private String resolveRefreshToken(HttpServletRequest request, RefreshTokenRequest body) {
        if (body != null && StringUtils.hasText(body.refreshToken())) {
            return body.refreshToken();
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> tokenCookieService.getRefreshCookieName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
