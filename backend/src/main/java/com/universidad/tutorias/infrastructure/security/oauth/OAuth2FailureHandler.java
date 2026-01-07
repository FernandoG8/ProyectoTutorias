package com.universidad.tutorias.infrastructure.security.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final OAuth2Properties oAuth2Properties;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.warn("Falló la autenticación OAuth2: {}", exception.getMessage());
        String redirect = Optional.ofNullable(oAuth2Properties.getFailureRedirect())
                .filter(StringUtils::hasText)
                .orElse("/login?error=oauth2");
        getRedirectStrategy().sendRedirect(request, response, redirect);
    }
}
