CREATE TABLE IF NOT EXISTS alertas_proceso (
    id BIGSERIAL PRIMARY KEY,

    id_proceso BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    severidad VARCHAR(20) NOT NULL,
    descripcion TEXT NOT NULL,

    id_alumno BIGINT,
    id_tutor BIGINT,

    resuelta BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT now(),
    fecha_resolucion TIMESTAMP
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_proceso_alerta ON alertas_proceso (id_proceso);
CREATE INDEX IF NOT EXISTS idx_severidad ON alertas_proceso (severidad);
CREATE INDEX IF NOT EXISTS idx_resuelta ON alertas_proceso (resuelta);

-- Foreign keys
ALTER TABLE alertas_proceso
ADD CONSTRAINT fk_alerta_proceso
FOREIGN KEY (id_proceso) REFERENCES procesos_asignacion(id);

ALTER TABLE alertas_proceso
ADD CONSTRAINT fk_alerta_alumno
FOREIGN KEY (id_alumno) REFERENCES alumnos(id);

ALTER TABLE alertas_proceso
ADD CONSTRAINT fk_alerta_tutor
FOREIGN KEY (id_tutor) REFERENCES tutores(id);
