package com.universidad.tutorias.infrastructure.security.oauth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    public GoogleAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository, String authorizationRequestBaseUri) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, authorizationRequestBaseUri);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
        return customize(req, request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request, clientRegistrationId);
        return customize(req, request);
    }

    private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest req, HttpServletRequest request) {
        if (req == null) {
            return null;
        }

        String flow = (String) request.getSession(true).getAttribute(OAuth2Flow.FLOW_ATTR);
        boolean driveConnect = OAuth2Flow.FLOW_DRIVE_CONNECT.equals(flow);

        // Solo en DRIVE_CONNECT pedimos offline + consent para forzar refresh_token y scopes de Drive
        Map<String, Object> extra = new HashMap<>(req.getAdditionalParameters());
        if (driveConnect) {
            extra.put("access_type", "offline");
            extra.put("prompt", "consent");
        }

        // Asegurar scope de Drive completo cuando se conecta Drive
        Set<String> scopes = new HashSet<>(req.getScopes());
        if (driveConnect) {
            scopes.add("https://www.googleapis.com/auth/drive");
        }

        log.info("OAuth2 auth request - flow={}, driveConnect={}, scopes={}", flow, driveConnect, scopes);

        return OAuth2AuthorizationRequest.from(req)
                .additionalParameters(extra)
                .scopes(scopes)
                .build();
    }
}
