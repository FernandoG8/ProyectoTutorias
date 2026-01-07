-- Postgres-compatible version of V2__crear_tabla_semestre
CREATE TABLE IF NOT EXISTS semestres (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(15) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_semestre_fechas CHECK (fecha_fin > fecha_inicio),
    CONSTRAINT uk_semestre_codigo UNIQUE (codigo)
);

CREATE INDEX IF NOT EXISTS idx_semestre_activo ON semestres(activo);
CREATE INDEX IF NOT EXISTS idx_semestre_codigo ON semestres(codigo);
CREATE INDEX IF NOT EXISTS idx_semestre_fechas ON semestres(fecha_inicio, fecha_fin);

-- asignaciones: add id_semestre
ALTER TABLE asignaciones
    ADD COLUMN IF NOT EXISTS id_semestre BIGINT;

CREATE INDEX IF NOT EXISTS idx_asignacion_semestre ON asignaciones(id_semestre);

-- populate from legacy semestre_academico
INSERT INTO semestres (codigo, nombre, fecha_inicio, fecha_fin, activo)
SELECT DISTINCT
    a.semestre_academico AS codigo,
    CONCAT('Semestre ',
           CASE WHEN a.semestre_academico LIKE '%-F1' THEN 'Agosto'
                WHEN a.semestre_academico LIKE '%-F2' THEN 'Enero'
                ELSE 'Periodo' END,
           ' ', LEFT(a.semestre_academico, 4), ' - ',
           CASE WHEN a.semestre_academico LIKE '%-F1' THEN 'Enero'
                WHEN a.semestre_academico LIKE '%-F2' THEN 'Julio'
                ELSE '' END,
           ' ', SUBSTRING(a.semestre_academico, 6, 4)
    ) AS nombre,
    TO_DATE(
        LEFT(a.semestre_academico, 4) || '-' ||
        CASE WHEN a.semestre_academico LIKE '%-F1' THEN '08-01'
             WHEN a.semestre_academico LIKE '%-F2' THEN '01-01'
             ELSE '01-01' END,
        'YYYY-MM-DD'
    ) AS fecha_inicio,
    CASE
        WHEN a.semestre_academico LIKE '%-F1' THEN TO_DATE(SUBSTRING(a.semestre_academico, 6, 4) || '-01-31', 'YYYY-MM-DD')
        WHEN a.semestre_academico LIKE '%-F2' THEN TO_DATE(SUBSTRING(a.semestre_academico, 6, 4) || '-07-31', 'YYYY-MM-DD')
        ELSE TO_DATE(LEFT(a.semestre_academico, 4) || '-01-01', 'YYYY-MM-DD') + INTERVAL '6 months'
    END AS fecha_fin,
    FALSE AS activo
FROM asignaciones a
WHERE a.semestre_academico IS NOT NULL AND a.semestre_academico != ''
ON CONFLICT (codigo) DO NOTHING;

-- set id_semestre
UPDATE asignaciones a
SET id_semestre = s.id
FROM semestres s
WHERE a.semestre_academico = s.codigo;

-- enforce NOT NULL and FK
ALTER TABLE asignaciones
    ALTER COLUMN id_semestre SET NOT NULL;

ALTER TABLE asignaciones
    ADD CONSTRAINT fk_asignacion_semestre
        FOREIGN KEY (id_semestre)
        REFERENCES semestres(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE;

-- unique constraint adjusted
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'uk_asignacion_unica') THEN
        EXECUTE 'DROP INDEX uk_asignacion_unica';
    END IF;
END$$;

ALTER TABLE asignaciones
    ADD CONSTRAINT uk_asignacion_alumno_tutor_semestre
        UNIQUE (id_alumno, id_tutor, id_semestre);

-- procesos_asignacion link
ALTER TABLE procesos_asignacion
    ADD COLUMN IF NOT EXISTS id_semestre BIGINT;

CREATE INDEX IF NOT EXISTS idx_proceso_semestre ON procesos_asignacion(id_semestre);

ALTER TABLE procesos_asignacion
    ADD CONSTRAINT fk_proceso_semestre
        FOREIGN KEY (id_semestre)
        REFERENCES semestres(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE;

-- default active semester
INSERT INTO semestres (codigo, nombre, fecha_inicio, fecha_fin, activo)
VALUES ('2025-2026-F1', 'Semestre Agosto 2025 - Enero 2026', '2025-08-01', '2026-01-31', TRUE)
ON CONFLICT (codigo) DO NOTHING;
