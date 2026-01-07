-- Postgres-compatible version of V6__add_google_refresh_columns
ALTER TABLE usuarios_google_link
    ADD COLUMN IF NOT EXISTS google_refresh_token_enc VARCHAR(512),
    ADD COLUMN IF NOT EXISTS google_refresh_token_iv VARCHAR(64),
    ADD COLUMN IF NOT EXISTS token_updated_at TIMESTAMP;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes WHERE indexname = 'uk_google_usuario'
    ) THEN
        ALTER TABLE usuarios_google_link
            ADD CONSTRAINT uk_google_usuario UNIQUE (usuario_id);
    END IF;
END$$;
