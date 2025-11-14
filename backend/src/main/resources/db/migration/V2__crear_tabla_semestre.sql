-- =====================================================
-- MIGRACIÓN: Implementar tabla de semestres
-- Versión: V2
-- Base de datos: MySQL
-- Datos: SOLO DE PRUEBA - Estructura limpia desde cero
-- =====================================================

-- PASO 1: Crear tabla semestre
CREATE TABLE IF NOT EXISTS semestres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(15) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_semestre_fechas CHECK (fecha_fin > fecha_inicio),
    CONSTRAINT uk_semestre_codigo UNIQUE (codigo),
    INDEX idx_semestre_activo (activo),
    INDEX idx_semestre_codigo (codigo),
    INDEX idx_semestre_fechas (fecha_inicio, fecha_fin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- PASO 2: Agregar columna id_semestre a asignaciones
ALTER TABLE asignaciones
ADD COLUMN id_semestre BIGINT NULL AFTER id,
ADD INDEX idx_asignacion_semestre (id_semestre);

-- PASO 3: Crear semestres basados en datos existentes
-- Primero, obtenemos los semestres únicos de la tabla asignaciones
INSERT IGNORE INTO semestres (codigo, nombre, fecha_inicio, fecha_fin, activo)
SELECT DISTINCT
    a.semestre_academico AS codigo,
    CONCAT('Semestre ',
           CASE
               WHEN a.semestre_academico LIKE '%-F1' THEN 'Agosto'
               WHEN a.semestre_academico LIKE '%-F2' THEN 'Enero'
               ELSE 'Periodo'
           END,
           ' ',
           LEFT(a.semestre_academico, 4),
           ' - ',
           CASE
               WHEN a.semestre_academico LIKE '%-F1' THEN 'Enero'
               WHEN a.semestre_academico LIKE '%-F2' THEN 'Julio'
               ELSE ''
           END,
           ' ',
           SUBSTRING(a.semestre_academico, 6, 4)
    ) AS nombre,
    DATE(CONCAT(
        LEFT(a.semestre_academico, 4),
        '-',
        CASE
            WHEN a.semestre_academico LIKE '%-F1' THEN '08-01'
            WHEN a.semestre_academico LIKE '%-F2' THEN '01-01'
            ELSE '01-01'
        END
    )) AS fecha_inicio,
    CASE
        WHEN a.semestre_academico LIKE '%-F1' THEN DATE(CONCAT(SUBSTRING(a.semestre_academico, 6, 4), '-01-31'))
        WHEN a.semestre_academico LIKE '%-F2' THEN DATE(CONCAT(SUBSTRING(a.semestre_academico, 6, 4), '-07-31'))
        ELSE DATE_ADD(DATE(CONCAT(LEFT(a.semestre_academico, 4), '-01-01')), INTERVAL 6 MONTH)
    END AS fecha_fin,
    (a.semestre_academico = (SELECT MAX(semestre_academico) FROM asignaciones WHERE semestre_academico IS NOT NULL AND semestre_academico != '')) AS activo
FROM asignaciones a
WHERE a.semestre_academico IS NOT NULL
    AND a.semestre_academico != ''
GROUP BY a.semestre_academico
ORDER BY a.semestre_academico DESC;

-- PASO 4: Poblar id_semestre basado en semestre_academico
UPDATE asignaciones a
INNER JOIN semestres s ON a.semestre_academico = s.codigo
SET a.id_semestre = s.id
WHERE a.semestre_academico IS NOT NULL;

-- PASO 5: Verificar integridad - Si hay registros sin id_semestre, el script falla
SET @count_sin_semestre = (
    SELECT COUNT(*)
    FROM asignaciones
    WHERE id_semestre IS NULL
        AND semestre_academico IS NOT NULL
        AND semestre_academico != ''
);

-- Verificar que no haya inconsistencias
SELECT CASE
    WHEN @count_sin_semestre > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'ERROR: Existen asignaciones sin id_semestre. Revisar datos antes de continuar.'
    ELSE 1
END;

-- PASO 6: Hacer id_semestre NOT NULL y agregar foreign key
ALTER TABLE asignaciones
    MODIFY COLUMN id_semestre BIGINT NOT NULL,
    ADD CONSTRAINT fk_asignacion_semestre
        FOREIGN KEY (id_semestre)
        REFERENCES semestres(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE;

-- PASO 7: Actualizar constraint de unicidad en asignaciones
-- Primero, eliminar el constraint viejo si existe
ALTER TABLE asignaciones
    DROP INDEX IF EXISTS uk_asignacion_unica;

-- Crear nuevo constraint con id_semestre
ALTER TABLE asignaciones
    ADD CONSTRAINT uk_asignacion_alumno_tutor_semestre
        UNIQUE (id_alumno, id_tutor, id_semestre);

-- PASO 8: Agregar id_semestre a procesos_asignacion
ALTER TABLE procesos_asignacion
ADD COLUMN id_semestre BIGINT NULL,
ADD INDEX idx_proceso_semestre (id_semestre),
ADD CONSTRAINT fk_proceso_semestre
    FOREIGN KEY (id_semestre)
    REFERENCES semestres(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- PASO 9: Crear el semestre activo si no existe (2025-2026-F1)
INSERT IGNORE INTO semestres (codigo, nombre, fecha_inicio, fecha_fin, activo)
VALUES ('2025-2026-F1', 'Semestre Agosto 2025 - Enero 2026', '2025-08-01', '2026-01-31', TRUE);

-- PASO 10: Verificar resultado de la migración
SELECT
    (SELECT COUNT(*) FROM semestres) AS total_semestres,
    (SELECT COUNT(*) FROM asignaciones WHERE id_semestre IS NOT NULL) AS asignaciones_con_semestre,
    (SELECT COUNT(*) FROM asignaciones WHERE id_semestre IS NULL) AS asignaciones_sin_semestre,
    (SELECT codigo FROM semestres WHERE activo = TRUE LIMIT 1) AS semestre_activo,
    'MIGRACIÓN COMPLETADA EXITOSAMENTE' AS estado;
