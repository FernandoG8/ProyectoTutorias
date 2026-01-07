package com.universidad.tutorias.infrastructure.service.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.universidad.tutorias.domain.entity.UsuarioGoogleLink;
import com.universidad.tutorias.infrastructure.security.CryptoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleTokenService {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final CryptoService cryptoService;

    public GoogleAccessToken refreshAccessToken(UsuarioGoogleLink link) {
        if (link == null || !StringUtils.hasText(link.getGoogleRefreshTokenEnc()) || !StringUtils.hasText(link.getGoogleRefreshTokenIv())) {
            throw new IllegalStateException("No hay refresh token almacenado para la cuenta de Google vinculada");
        }
        ClientRegistration google = clientRegistrationRepository.findByRegistrationId("google");
        if (google == null) {
            throw new IllegalStateException("No se encontró la configuración de cliente Google");
        }

        try {
            String refreshToken = cryptoService.decrypt(link.getGoogleRefreshTokenEnc(), link.getGoogleRefreshTokenIv());

            var transport = GoogleNetHttpTransport.newTrustedTransport();
            var jsonFactory = GsonFactory.getDefaultInstance();

            var tokenResponse = new GoogleRefreshTokenRequest(
                    transport,
                    jsonFactory,
                    refreshToken,
                    google.getClientId(),
                    google.getClientSecret()
            ).setGrantType("refresh_token").execute();

            return new GoogleAccessToken(
                    tokenResponse.getAccessToken(),
                    Instant.now().plusSeconds(tokenResponse.getExpiresInSeconds() != null ? tokenResponse.getExpiresInSeconds() : 3600)
            );
        } catch (Exception e) {
            log.error("Error renovando access token de Google", e);
            throw new IllegalStateException("No se pudo renovar el token de Google Drive");
        }
    }

    public record GoogleAccessToken(String accessToken, Instant expiresAt) {}
}
