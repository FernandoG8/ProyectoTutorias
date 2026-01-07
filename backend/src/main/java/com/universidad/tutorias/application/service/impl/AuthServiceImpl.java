package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.auth.*;
import com.universidad.tutorias.application.service.AuthService;
import com.universidad.tutorias.domain.entity.RefreshToken;
import com.universidad.tutorias.domain.entity.Usuario;
import com.universidad.tutorias.domain.enums.RolUsuario;
import com.universidad.tutorias.domain.repository.RefreshTokenRepository;
import com.universidad.tutorias.domain.repository.UsuarioRepository;
import com.universidad.tutorias.infrastructure.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthTokensResult login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            Usuario usuario = (Usuario) authentication.getPrincipal();

            log.info("Usuario {} autenticado correctamente", usuario.getUsername());

            refreshTokenRepository.revokeAllByUsuario(usuario);
            RefreshToken refreshToken = crearRefreshToken(usuario);

            String accessToken = jwtService.generateAccessToken(usuario);
            UserInfoResponse userInfo = buildUserInfo(usuario);

            return new AuthTokensResult(
                    accessToken,
                    jwtService.getAccessTokenExpirationSeconds(),
                    refreshToken.getToken(),
                    refreshToken.getExpiryDate(),
                    userInfo
            );
        } catch (AuthenticationException ex) {
            log.warn("Error de autenticación para el usuario {}", request.username());
            throw ex;
        }
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        RolUsuario rol = request.role();

        Usuario usuario = Usuario.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .rol(rol)
                .activo(true)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);

        log.info("Usuario {} registrado con rol {}", guardado.getUsername(), guardado.getRol());

        return new RegisterResponse(
                guardado.getId(),
                guardado.getUsername(),
                List.of(guardado.getRol().name())
        );
    }

    @Override
    @Transactional
    public AuthTokensResult refresh(String refreshTokenValue) {
        if (!StringUtils.hasText(refreshTokenValue)) {
            throw new IllegalArgumentException("El token de refresco es obligatorio");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndRevokedFalse(refreshTokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Token de refresco inválido"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new IllegalArgumentException("El token de refresco ha expirado");
        }

        Usuario usuario = refreshToken.getUsuario();

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        RefreshToken nuevoRefreshToken = crearRefreshToken(usuario);
        String accessToken = jwtService.generateAccessToken(usuario);
        UserInfoResponse userInfo = buildUserInfo(usuario);

        return new AuthTokensResult(
                accessToken,
                jwtService.getAccessTokenExpirationSeconds(),
                nuevoRefreshToken.getToken(),
                nuevoRefreshToken.getExpiryDate(),
                userInfo
        );
    }

    @Override
    @Transactional
    public void logout(String refreshTokenValue) {
        if (!StringUtils.hasText(refreshTokenValue)) {
            return;
        }

        refreshTokenRepository.findByToken(refreshTokenValue).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.info("Token de refresco revocado para el usuario {}", token.getUsuario().getUsername());
        });
    }

    @Override
    public UserInfoResponse buildUserInfo(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("No se encontró información del usuario autenticado");
        }
        return new UserInfoResponse(
                usuario.getId(),
                usuario.getUsername(),
                List.of(usuario.getRol().name())
        );
    }

    private RefreshToken crearRefreshToken(Usuario usuario) {
        String token = generarTokenSeguro();
        Instant expiracion = Instant.now().plusSeconds(jwtService.getRefreshTokenExpirationSeconds());

        RefreshToken refreshToken = RefreshToken.builder()
                .usuario(usuario)
                .token(token)
                .expiryDate(expiracion)
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    private String generarTokenSeguro() {
        byte[] randomBytes = new byte[64];
        RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes) + "." + UUID.randomUUID();
    }
}
