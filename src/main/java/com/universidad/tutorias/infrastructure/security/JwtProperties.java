package com.universidad.tutorias.infrastructure.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /**
     * Clave secreta usada para firmar el token.
     */
    private String secret;

    /**
     * Tiempo de expiración del token de acceso en segundos.
     */
    private long expiration;

    /**
     * Tiempo de expiración del token de refresco en segundos.
     */
    private long refreshExpiration;

    /**
     * Identificador del issuer del token.
     */
    private String issuer;
}
