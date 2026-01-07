-- Agrega columnas para almacenar el refresh token cifrado de Google Drive
-- y la marca de tiempo de actualización.
ALTER TABLE usuarios_google_link
    ADD COLUMN IF NOT EXISTS google_refresh_token_enc VARCHAR(512) NULL,
    ADD COLUMN IF NOT EXISTS google_refresh_token_iv VARCHAR(64) NULL,
    ADD COLUMN IF NOT EXISTS token_updated_at DATETIME NULL;

-- Asegura unicidad por usuario si no existía previamente.
ALTER TABLE usuarios_google_link
    ADD UNIQUE INDEX IF NOT EXISTS uk_google_usuario (usuario_id);
