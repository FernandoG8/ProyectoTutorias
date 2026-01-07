-- Postgres-compatible version of V5__crear_tabla_usuarios_google_link
CREATE TABLE IF NOT EXISTS usuarios_google_link (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    google_sub VARCHAR(64) NOT NULL,
    google_email VARCHAR(255),
    google_refresh_token_enc VARCHAR(512),
    google_refresh_token_iv VARCHAR(64),
    token_updated_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_google_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT uk_google_sub UNIQUE (google_sub),
    CONSTRAINT uk_google_usuario UNIQUE (usuario_id)
);
