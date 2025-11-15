package com.universidad.tutorias.infrastructure.security;

import com.universidad.tutorias.application.dto.auth.AuthTokensResult;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class TokenCookieService {

    private final JwtProperties jwtProperties;

    public String getAccessCookieName() {
        return jwtProperties.getAccessCookieName();
    }

    public String getRefreshCookieName() {
        return jwtProperties.getRefreshCookieName();
    }

    public void addAuthCookies(HttpServletResponse response, AuthTokensResult tokens) {
        ResponseCookie accessCookie = buildAccessTokenCookie(tokens.accessToken(), tokens.accessTokenExpiresIn());
        ResponseCookie refreshCookie = buildRefreshTokenCookie(tokens.refreshToken(), tokens.refreshTokenExpiry());
        addCookiesToResponse(response, accessCookie, refreshCookie);
    }

    public void clearAuthCookies(HttpServletResponse response) {
        ResponseCookie accessCookie = buildDeletionCookie(jwtProperties.getAccessCookieName());
        ResponseCookie refreshCookie = buildDeletionCookie(jwtProperties.getRefreshCookieName());
        addCookiesToResponse(response, accessCookie, refreshCookie);
    }

    private ResponseCookie buildAccessTokenCookie(String token, long expiresInSeconds) {
        Duration maxAge = Duration.ofSeconds(Math.max(expiresInSeconds, 0));
        return baseCookieBuilder(jwtProperties.getAccessCookieName(), token)
                .maxAge(maxAge)
                .build();
    }

    private ResponseCookie buildRefreshTokenCookie(String token, Instant expiry) {
        long seconds = Math.max(0, Duration.between(Instant.now(), expiry).getSeconds());
        Duration maxAge = Duration.ofSeconds(seconds);
        return baseCookieBuilder(jwtProperties.getRefreshCookieName(), token)
                .maxAge(maxAge)
                .build();
    }

    private ResponseCookie buildDeletionCookie(String name) {
        return baseCookieBuilder(name, "")
                .maxAge(Duration.ZERO)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookieBuilder(String name, String value) {
        // `ResponseCookie.Builder` no longer exists in Spring 6+, use `ResponseCookie.from(...)`
        // which returns a `ResponseCookieBuilder` and keeps compatibility with Spring Boot 3.
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(jwtProperties.isCookieSecure())
                .sameSite(jwtProperties.getCookieSameSite())
                .path(jwtProperties.getCookiePath());

        if (StringUtils.hasText(jwtProperties.getCookieDomain())) {
            builder.domain(jwtProperties.getCookieDomain());
        }

        return builder;
    }

    private void addCookiesToResponse(HttpServletResponse response, ResponseCookie... cookies) {
        for (ResponseCookie cookie : cookies) {
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
    }
}
