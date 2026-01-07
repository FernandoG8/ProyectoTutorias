package com.universidad.tutorias.infrastructure.security.oauth;

import com.universidad.tutorias.application.dto.auth.AuthTokensResult;
import com.universidad.tutorias.application.dto.auth.UserInfoResponse;
import com.universidad.tutorias.domain.entity.RefreshToken;
import com.universidad.tutorias.domain.entity.Usuario;
import com.universidad.tutorias.domain.entity.UsuarioGoogleLink;
import com.universidad.tutorias.domain.repository.RefreshTokenRepository;
import com.universidad.tutorias.domain.repository.UsuarioGoogleLinkRepository;
import com.universidad.tutorias.domain.repository.UsuarioRepository;
import com.universidad.tutorias.infrastructure.security.CryptoService;
import com.universidad.tutorias.infrastructure.security.JwtService;
import com.universidad.tutorias.infrastructure.security.TokenCookieService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final UsuarioGoogleLinkRepository usuarioGoogleLinkRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final TokenCookieService tokenCookieService;
    private final OAuth2Properties oAuth2Properties;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final CryptoService cryptoService;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        String flow = session != null ? (String) session.getAttribute(OAuth2Flow.FLOW_ATTR) : null;
        Long linkUserId = session != null ? (Long) session.getAttribute(OAuth2Flow.LINK_USER_ID_ATTR) : null;
        log.info("OAuth2 FLOW detected: {}", flow);

        // Logging detallado para diagnosticar ausencia de refresh_token
        OAuth2AuthorizedClient authorizedClient = null;
        OAuth2AuthenticationToken oauth2Token = authentication instanceof OAuth2AuthenticationToken ? (OAuth2AuthenticationToken) authentication : null;
        if (oauth2Token != null) {
            authorizedClient = authorizedClientService.loadAuthorizedClient(
                    oauth2Token.getAuthorizedClientRegistrationId(),
                    oauth2Token.getName()
            );
            log.info("========== DEBUG OAUTH2 TOKEN ==========");
            if (authorizedClient != null) {
                log.info("Client Registration ID: {}", authorizedClient.getClientRegistration().getRegistrationId());
                log.info("Access Token presente: {}", authorizedClient.getAccessToken() != null);
                if (authorizedClient.getAccessToken() != null && authorizedClient.getAccessToken().getTokenValue() != null) {
                    String at = authorizedClient.getAccessToken().getTokenValue();
                    log.info("Access Token value (primeros 20 chars): {}", at.substring(0, Math.min(20, at.length())));
                } else {
                    log.info("Access Token value: null");
                }
                log.info("Refresh Token presente: {}", authorizedClient.getRefreshToken() != null);
                if (authorizedClient.getRefreshToken() != null && authorizedClient.getRefreshToken().getTokenValue() != null) {
                    String rt = authorizedClient.getRefreshToken().getTokenValue();
                    log.info("Refresh Token value: EXISTE (longitud: {})", rt.length());
                } else {
                    log.info("Refresh Token value: NULL");
                }
                log.info("Scopes solicitados: {}", authorizedClient.getClientRegistration().getScopes());
                log.info("Principal Name: {}", authorizedClient.getPrincipalName());
            } else {
                log.info("No se pudo cargar OAuth2AuthorizedClient para logging detallado.");
            }
            if (session != null) {
                log.info("Session ID: {}", session.getId());
                log.info("Session OAUTH2_FLOW: {}", session.getAttribute(OAuth2Flow.FLOW_ATTR));
            }
            log.info("========================================");
        }

        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String sub = oAuth2User.getAttribute("sub");
            String email = oAuth2User.getAttribute("email");
            log.info("OAuth2 user: sub={}, email={}", sub, email);

            if (!StringUtils.hasText(sub)) {
                log.warn("No se recibió 'sub' desde el proveedor OAuth2");
                sendFailureRedirect(request, response, "missing_sub");
                return;
            }

            if (OAuth2Flow.FLOW_LINK.equals(flow)) {
                handleLinkFlow(linkUserId, sub, email, request, response, false, null);
            } else if (OAuth2Flow.FLOW_DRIVE_CONNECT.equals(flow)) {
                handleLinkFlow(linkUserId, sub, email, request, response, true, authorizedClient);
            } else {
                handleLoginFlow(sub, email, request, response);
            }
        } finally {
            if (session != null) {
                session.removeAttribute(OAuth2Flow.FLOW_ATTR);
                session.removeAttribute(OAuth2Flow.LINK_USER_ID_ATTR);
            }
        }
    }

    private void handleLinkFlow(Long linkUserId, String sub, String email, HttpServletRequest request, HttpServletResponse response, boolean expectRefreshToken, OAuth2AuthorizedClient authorizedClient) throws IOException {
        if (linkUserId == null) {
            log.warn("Link flow sin usuario en sesión");
            sendFailureRedirect(request, response, "invalid_flow");
            return;
        }

        Usuario usuario = usuarioRepository.findById(linkUserId).orElse(null);
        if (usuario == null || !usuario.isActivo()) {
            log.warn("Usuario no encontrado o inactivo para link: {}", linkUserId);
            sendFailureRedirect(request, response, "user_inactive");
            return;
        }

        Optional<UsuarioGoogleLink> linkBySub = usuarioGoogleLinkRepository.findByGoogleSub(sub);
        if (linkBySub.isPresent() && !linkBySub.get().getUsuario().getId().equals(linkUserId)) {
            log.warn("Cuenta Google ya vinculada a otro usuario. google_sub={}, userId={}", sub, linkBySub.get().getUsuario().getId());
            sendFailureRedirect(request, response, "already_linked");
            return;
        }

        Optional<UsuarioGoogleLink> existingForUser = usuarioGoogleLinkRepository.findByUsuarioId(linkUserId);
        if (existingForUser.isPresent()) {
            UsuarioGoogleLink link = existingForUser.get();
            if (!link.getGoogleSub().equals(sub)) {
                log.warn("Usuario {} ya tiene otro sub vinculado", linkUserId);
                sendFailureRedirect(request, response, "already_linked_user");
                return;
            }
            link.setGoogleEmail(email);
            if (expectRefreshToken) {
                try {
                    updateRefreshToken(link, authorizedClient, request, response);
                } catch (OAuth2AuthenticationException e) {
                    log.warn("Falló la actualización del refresh token de Google", e);
                    return;
                }
            }
            usuarioGoogleLinkRepository.save(link);
        } else {
            UsuarioGoogleLink nuevoLink = UsuarioGoogleLink.builder()
                    .usuario(usuario)
                    .googleSub(sub)
                    .googleEmail(email)
                    .build();
            if (expectRefreshToken) {
                try {
                    updateRefreshToken(nuevoLink, authorizedClient, request, response);
                } catch (OAuth2AuthenticationException e) {
                    log.warn("Falló el guardado del refresh token de Google", e);
                    return;
                }
            }
            usuarioGoogleLinkRepository.save(nuevoLink);
        }

        AuthTokensResult tokens = issueTokens(usuario);
        tokenCookieService.addAuthCookies(response, tokens);

        String redirectUrl = appendQuery(resolveSuccessRedirect(), "linked", "1");
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private void handleLoginFlow(String sub, String email, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Optional<UsuarioGoogleLink> link = usuarioGoogleLinkRepository.findByGoogleSub(sub);
        if (link.isEmpty()) {
            log.warn("Intento de login con Google sin vínculo previo. sub={}, email={}", sub, email);
            sendFailureRedirect(request, response, "not_authorized");
            return;
        }

        Usuario usuario = link.get().getUsuario();
        if (usuario == null || !usuario.isActivo()) {
            log.warn("Usuario vinculado inactivo o no encontrado. sub={}, userId={}", sub, usuario != null ? usuario.getId() : null);
            sendFailureRedirect(request, response, "user_inactive");
            return;
        }

        AuthTokensResult tokens = issueTokens(usuario);
        tokenCookieService.addAuthCookies(response, tokens);

        String redirectUrl = resolveSuccessRedirect();
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private AuthTokensResult issueTokens(Usuario usuario) {
        refreshTokenRepository.revokeAllByUsuario(usuario);
        RefreshToken refreshToken = crearRefreshToken(usuario);
        String accessToken = jwtService.generateAccessToken(usuario);

        return new AuthTokensResult(
                accessToken,
                jwtService.getAccessTokenExpirationSeconds(),
                refreshToken.getToken(),
                refreshToken.getExpiryDate(),
                buildUserInfo(usuario)
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

    private String resolveSuccessRedirect() {
        return Optional.ofNullable(oAuth2Properties.getSuccessRedirect())
                .filter(StringUtils::hasText)
                .orElse("/");
    }

    private String resolveFailureRedirect() {
        return Optional.ofNullable(oAuth2Properties.getFailureRedirect())
                .filter(StringUtils::hasText)
                .orElse("/login?error=oauth2");
    }

    private UserInfoResponse buildUserInfo(Usuario usuario) {
        return new UserInfoResponse(
            usuario.getId(),
            usuario.getUsername(),
            List.of(usuario.getRol().name())
        );
    }

    private void sendFailureRedirect(HttpServletRequest request, HttpServletResponse response, String code) throws IOException {
        String redirect = appendQuery(resolveFailureRedirect(), "code", code);
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, redirect);
    }

    private String appendQuery(String baseUrl, String key, String value) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam(key, value)
                .build(true)
                .toUriString();
    }

    private void updateRefreshToken(UsuarioGoogleLink link, OAuth2AuthorizedClient client, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (client == null) {
            client = loadAuthorizedClient();
        }
        if (client == null || client.getRefreshToken() == null) {
            log.warn("No se obtuvo refresh_token durante el flow de conexión a Drive");
            sendFailureRedirect(request, response, "missing_refresh_token");
            throw new OAuth2AuthenticationException("missing_refresh_token");
        }
        if (!cryptoService.isConfigured()) {
            log.error("app.crypto.key no configurada; no se puede cifrar refresh token de Google");
            sendFailureRedirect(request, response, "missing_crypto_key");
            throw new OAuth2AuthenticationException("missing_crypto_key");
        }
        String refreshToken = client.getRefreshToken().getTokenValue();
        log.info("Refresh token recibido y será cifrado (usuarioId={}, clientPrincipal={})", link.getUsuario().getId(), request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "null");
        CryptoService.EncryptionResult encryption = cryptoService.encrypt(refreshToken);
        link.setGoogleRefreshTokenEnc(encryption.cipherText());
        link.setGoogleRefreshTokenIv(encryption.iv());
        link.setTokenUpdatedAt(Instant.now().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
    }

    private OAuth2AuthorizedClient loadAuthorizedClient() {
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        return authorizedClientService.loadAuthorizedClient("google", auth.getName());
    }
}
