-- Postgres-compatible version of V7__crear_tabla_export_jobs
CREATE TABLE export_jobs (
    id              VARCHAR(36) PRIMARY KEY,
    status          VARCHAR(30)      NOT NULL,
    type            VARCHAR(50)      NOT NULL,
    created_by      VARCHAR(150)     NOT NULL,
    idempotency_key VARCHAR(100),
    created_at      TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at      TIMESTAMP,
    finished_at     TIMESTAMP,
    total           INT              DEFAULT 0,
    processed       INT              DEFAULT 0,
    success_count   INT              DEFAULT 0,
    fail_count      INT              DEFAULT 0,
    progress_pct    INT,
    message         VARCHAR(255),
    result_json     TEXT,
    error_json      TEXT,
    CONSTRAINT uq_export_jobs_idempo UNIQUE (created_by, type, idempotency_key)
);
