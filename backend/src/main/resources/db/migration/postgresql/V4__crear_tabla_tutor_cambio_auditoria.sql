-- Postgres-compatible version of V4__crear_tabla_tutor_cambio_auditoria
CREATE TABLE IF NOT EXISTS tutor_cambio_auditoria (
    id BIGSERIAL PRIMARY KEY,
    id_asignacion BIGINT NOT NULL,
    id_tutor_anterior BIGINT,
    id_tutor_nuevo BIGINT NOT NULL,
    usuario_responsable VARCHAR(100),
    fecha_hora_cambio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo VARCHAR(500),
    tipo_cambio VARCHAR(50),
    notas TEXT,
    CONSTRAINT fk_tca_asignacion FOREIGN KEY (id_asignacion) REFERENCES asignaciones(id) ON DELETE CASCADE,
    CONSTRAINT fk_tca_tutor_anterior FOREIGN KEY (id_tutor_anterior) REFERENCES tutores(id) ON DELETE SET NULL,
    CONSTRAINT fk_tca_tutor_nuevo FOREIGN KEY (id_tutor_nuevo) REFERENCES tutores(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_asignacion_cambio ON tutor_cambio_auditoria(id_asignacion);
CREATE INDEX IF NOT EXISTS idx_fecha_cambio ON tutor_cambio_auditoria(fecha_hora_cambio);
CREATE INDEX IF NOT EXISTS idx_usuario_cambio ON tutor_cambio_auditoria(usuario_responsable);
CREATE INDEX IF NOT EXISTS idx_tutor_nuevo ON tutor_cambio_auditoria(id_tutor_nuevo);
CREATE INDEX IF NOT EXISTS idx_tutor_anterior ON tutor_cambio_auditoria(id_tutor_anterior);
