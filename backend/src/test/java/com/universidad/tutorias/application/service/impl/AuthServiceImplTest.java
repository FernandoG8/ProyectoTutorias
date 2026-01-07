package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.TestDataBuilder;
import com.universidad.tutorias.application.dto.auth.*;
import com.universidad.tutorias.domain.entity.RefreshToken;
import com.universidad.tutorias.domain.entity.Usuario;
import com.universidad.tutorias.domain.enums.RolUsuario;
import com.universidad.tutorias.domain.repository.RefreshTokenRepository;
import com.universidad.tutorias.domain.repository.UsuarioRepository;
import com.universidad.tutorias.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

/**
 * Test para AuthServiceImpl
 * Prueba los flujos de autenticación, registro, refresh y logout
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Tests")
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private Usuario usuario;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        usuario = TestDataBuilder.usuario()
                .id(1L)
                .username("test@example.com")
                .rol(RolUsuario.ROLE_COORDINADOR_TUTORIAS)
                .activo(true)
                .build();

        refreshToken = TestDataBuilder.refreshToken()
                .usuario(usuario)
                .expiryDate(Instant.now().plusSeconds(7 * 24 * 60 * 60))
                .build();
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("Login exitoso con credenciales válidas")
    void login_conCredencialesValidas_debeRetornarTokens() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuario, null, usuario.getAuthorities()
        );

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        doNothing().when(refreshTokenRepository).revokeAllByUsuario(usuario);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);
        when(jwtService.generateAccessToken(usuario)).thenReturn("access_token_test");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(3600L);

        // Act
        AuthTokensResult result = authService.login(request);

        // Assert
        assertNotNull(result);
        assertEquals("access_token_test", result.accessToken());
        assertEquals(3600L, result.accessTokenExpiresIn());
        assertNotNull(result.refreshToken());
        assertNotNull(result.userInfo());
        assertEquals("test@example.com", result.userInfo().username());

        verify(authenticationManager, times(1)).authenticate(any());
        verify(refreshTokenRepository, times(1)).revokeAllByUsuario(usuario);
        verify(jwtService, times(1)).generateAccessToken(usuario);
    }

    @Test
    @DisplayName("Login falla con credenciales inválidas")
    void login_conCredencialesInvalidas_debeLanzarExcepcion() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThrows(org.springframework.security.core.AuthenticationException.class, () -> authService.login(request));
        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    @DisplayName("Login revoca tokens anteriores del usuario")
    void login_debeRevocarTokensAnteriores() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuario, null, usuario.getAuthorities()
        );

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        doNothing().when(refreshTokenRepository).revokeAllByUsuario(usuario);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);
        when(jwtService.generateAccessToken(usuario)).thenReturn("access_token_test");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(3600L);

        // Act
        authService.login(request);

        // Assert
        verify(refreshTokenRepository, times(1)).revokeAllByUsuario(usuario);
    }

    // ==================== REGISTER TESTS ====================

    @Test
    @DisplayName("Registro exitoso con datos válidos")
    void register_conDatosValidos_debeCrearNuevoUsuario() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "newuser@example.com",
                "password123",
                RolUsuario.ROLE_COORDINADOR_TUTORIAS
        );

        when(usuarioRepository.existsByUsername(request.username())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded_password");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        RegisterResponse result = authService.register(request);

        // Assert
        assertNotNull(result);
        assertEquals(usuario.getId(), result.id());
        assertEquals("test@example.com", result.username());
        assertTrue(result.roles().contains("ROLE_COORDINADOR_TUTORIAS"));

        verify(usuarioRepository, times(1)).existsByUsername(request.username());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Registro falla si el usuario ya existe")
    void register_conUsuarioExistente_debeLanzarExcepcion() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "existing@example.com",
                "password123",
                RolUsuario.ROLE_COORDINADOR_TUTORIAS
        );

        when(usuarioRepository.existsByUsername(request.username())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registro encripta la contraseña correctamente")
    void register_debeEncriptarContraseña() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "newuser@example.com",
                "password123",
                RolUsuario.ROLE_COORDINADOR_TUTORIAS
        );

        when(usuarioRepository.existsByUsername(request.username())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        authService.register(request);

        // Assert
        verify(passwordEncoder, times(1)).encode("password123");
    }

    // ==================== REFRESH TESTS ====================

    @Test
    @DisplayName("Refresh exitoso con token válido")
    void refresh_conTokenValido_debeRetornarNuevosTokens() {
        // Arrange
        String refreshTokenValue = "valid_refresh_token";
        Instant futuro = Instant.now().plusSeconds(7 * 24 * 60 * 60);
        RefreshToken tokenValido = TestDataBuilder.refreshToken()
                .token(refreshTokenValue)
                .usuario(usuario)
                .expiryDate(futuro)
                .build();

        when(refreshTokenRepository.findByTokenAndRevokedFalse(refreshTokenValue))
                .thenReturn(Optional.of(tokenValido));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);
        when(jwtService.generateAccessToken(usuario)).thenReturn("new_access_token");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(3600L);
        when(jwtService.getRefreshTokenExpirationSeconds()).thenReturn(604800L);

        // Act
        AuthTokensResult result = authService.refresh(refreshTokenValue);

        // Assert
        assertNotNull(result);
        assertEquals("new_access_token", result.accessToken());
        assertNotNull(result.refreshToken());
        assertNotNull(result.userInfo());

        verify(refreshTokenRepository, times(1)).findByTokenAndRevokedFalse(refreshTokenValue);
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class)); // Para revocar el antiguo y crear el nuevo
    }

    @Test
    @DisplayName("Refresh falla si el token es nulo o vacío")
    void refresh_conTokenNulo_debeLanzarExcepcion() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authService.refresh(null));
        assertThrows(IllegalArgumentException.class, () -> authService.refresh(""));
        assertThrows(IllegalArgumentException.class, () -> authService.refresh("   "));
    }

    @Test
    @DisplayName("Refresh falla si el token no existe")
    void refresh_conTokenInexistente_debeLanzarExcepcion() {
        // Arrange
        String tokenInvalido = "nonexistent_token";

        when(refreshTokenRepository.findByTokenAndRevokedFalse(tokenInvalido))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authService.refresh(tokenInvalido));
    }

    @Test
    @DisplayName("Refresh falla si el token ha expirado")
    void refresh_conTokenExpirado_debeLanzarExcepcionYRevocar() {
        // Arrange
        String expiredToken = "expired_token";
        Instant pasado = Instant.now().minusSeconds(1000);
        RefreshToken tokenExpirado = TestDataBuilder.refreshToken()
                .token(expiredToken)
                .usuario(usuario)
                .expiryDate(pasado)
                .build();

        when(refreshTokenRepository.findByTokenAndRevokedFalse(expiredToken))
                .thenReturn(Optional.of(tokenExpirado));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(tokenExpirado);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authService.refresh(expiredToken));
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Refresh revoca el token antiguo al crear uno nuevo")
    void refresh_debeRevocarTokenAntiguoAlCrearNuevo() {
        // Arrange
        String refreshTokenValue = "valid_refresh_token";
        Instant futuro = Instant.now().plusSeconds(7 * 24 * 60 * 60);
        RefreshToken tokenValido = TestDataBuilder.refreshToken()
                .token(refreshTokenValue)
                .usuario(usuario)
                .expiryDate(futuro)
                .build();

        when(refreshTokenRepository.findByTokenAndRevokedFalse(refreshTokenValue))
                .thenReturn(Optional.of(tokenValido));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateAccessToken(usuario)).thenReturn("new_access_token");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(3600L);
        when(jwtService.getRefreshTokenExpirationSeconds()).thenReturn(604800L);

        // Act
        authService.refresh(refreshTokenValue);

        // Assert
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    // ==================== LOGOUT TESTS ====================

    @Test
    @DisplayName("Logout exitoso revoca el refresh token")
    void logout_conTokenValido_debeRevocarToken() {
        // Arrange
        String tokenValue = "valid_refresh_token";

        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        // Act
        authService.logout(tokenValue);

        // Assert
        verify(refreshTokenRepository, times(1)).findByToken(tokenValue);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Logout con token nulo no lanza excepción")
    void logout_conTokenNulo_noDebeHacerNada() {
        // Act & Assert - no debe lanzar excepción
        assertDoesNotThrow(() -> authService.logout(null));
        assertDoesNotThrow(() -> authService.logout(""));
        assertDoesNotThrow(() -> authService.logout("   "));

        verify(refreshTokenRepository, never()).findByToken(anyString());
    }

    @Test
    @DisplayName("Logout con token inexistente no lanza excepción")
    void logout_conTokenInexistente_noDebeHacerNada() {
        // Arrange
        String tokenInvalido = "nonexistent_token";

        when(refreshTokenRepository.findByToken(tokenInvalido)).thenReturn(Optional.empty());

        // Act & Assert
        assertDoesNotThrow(() -> authService.logout(tokenInvalido));
        verify(refreshTokenRepository, times(1)).findByToken(tokenInvalido);
        verify(refreshTokenRepository, never()).save(any());
    }

    // ==================== BUILD USER INFO TESTS ====================

    @Test
    @DisplayName("BuildUserInfo crea respuesta correcta con datos válidos")
    void buildUserInfo_conUsuarioValido_debeCrearRespuesta() {
        // Arrange
        Usuario usuarioTest = TestDataBuilder.usuario()
                .id(1L)
                .username("test@example.com")
                .rol(RolUsuario.ROLE_COORDINADOR_TUTORIAS)
                .build();

        // Act
        UserInfoResponse result = authService.buildUserInfo(usuarioTest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("test@example.com", result.username());
        assertTrue(result.roles().contains("ROLE_COORDINADOR_TUTORIAS"));
    }

    @Test
    @DisplayName("BuildUserInfo lanza excepción con usuario nulo")
    void buildUserInfo_conUsuarioNulo_debeLanzarExcepcion() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authService.buildUserInfo(null));
    }

    @Test
    @DisplayName("BuildUserInfo incluye todos los roles del usuario")
    void buildUserInfo_debeIncluirRoles() {
        // Arrange
        Usuario usuarioCoordinador = TestDataBuilder.usuario()
                .id(2L)
                .username("coordinador@example.com")
                .rol(RolUsuario.ROLE_SECRETARIO_ACADEMICO)
                .build();

        // Act
        UserInfoResponse result = authService.buildUserInfo(usuarioCoordinador);

        // Assert
        assertTrue(result.roles().contains("ROLE_SECRETARIO_ACADEMICO"));
        assertEquals(1, result.roles().size());
    }

    // ==================== INTEGRATION-LIKE TESTS ====================

    @Test
    @DisplayName("Flujo completo: Register -> Login -> Refresh -> Logout")
    void flujoCompleto_registroLoginRefreshLogout() {
        // Step 1: Register
        RegisterRequest registerRequest = new RegisterRequest(
                "newuser@example.com",
                "password123",
                RolUsuario.ROLE_COORDINADOR_TUTORIAS
        );

        when(usuarioRepository.existsByUsername(registerRequest.username())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("encoded_pass");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        RegisterResponse registerResponse = authService.register(registerRequest);
        assertNotNull(registerResponse);

        // Step 2: Login
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuario, null, usuario.getAuthorities()
        );

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        doNothing().when(refreshTokenRepository).revokeAllByUsuario(usuario);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);
        when(jwtService.generateAccessToken(usuario)).thenReturn("access_token");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(3600L);
        when(jwtService.getRefreshTokenExpirationSeconds()).thenReturn(604800L);

        AuthTokensResult loginResponse = authService.login(loginRequest);
        assertNotNull(loginResponse);

        // Step 3: Refresh
        String tokenForRefresh = loginResponse.refreshToken();
        when(refreshTokenRepository.findByTokenAndRevokedFalse(tokenForRefresh))
                .thenReturn(Optional.of(refreshToken));

        AuthTokensResult refreshResponse = authService.refresh(tokenForRefresh);
        assertNotNull(refreshResponse);

        // Step 4: Logout
        when(refreshTokenRepository.findByToken(refreshResponse.refreshToken()))
                .thenReturn(Optional.of(refreshToken));

        assertDoesNotThrow(() -> authService.logout(refreshResponse.refreshToken()));

        verify(usuarioRepository, times(1)).existsByUsername(registerRequest.username());
        verify(authenticationManager, times(1)).authenticate(any());
    }
}
