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

    /**
     * Nombre de la cookie que almacena el token de acceso.
     */
    private String accessCookieName = "tutorias_access_token";

    /**
     * Nombre de la cookie que almacena el token de refresco.
     */
    private String refreshCookieName = "tutorias_refresh_token";

    /**
     * Indica si las cookies deben marcarse como seguras. Debe ser true en entornos HTTPS.
     */
    private boolean cookieSecure = false;

    /**
     * Valor del atributo SameSite para las cookies de autenticación.
     */
    private String cookieSameSite = "Lax";

    /**
     * Dominio opcional para las cookies. Dejar vacío para el dominio actual.
     */
    private String cookieDomain;

    /**
     * Ruta compartida por las cookies de autenticación.
     */
    private String cookiePath = "/";
}
