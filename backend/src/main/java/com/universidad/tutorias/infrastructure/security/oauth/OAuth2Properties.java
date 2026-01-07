package com.universidad.tutorias.infrastructure.security.oauth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.oauth2")
public class OAuth2Properties {

    /**
     * URL a la que se redirige tras un login exitoso.
     */
    private String successRedirect;

    /**
     * URL a la que se redirige tras un login fallido.
     */
    private String failureRedirect;
}
