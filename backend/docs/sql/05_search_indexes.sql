-- Índices y ajustes de collation para búsquedas con ranking
CREATE INDEX idx_alumnos_matricula ON alumnos(matricula);
CREATE INDEX idx_alumnos_nombre ON alumnos(nombre);
CREATE INDEX idx_tutores_nombre ON tutores(nombre);

ALTER TABLE alumnos MODIFY nombre VARCHAR(255) COLLATE utf8mb4_unicode_ci;
ALTER TABLE tutores MODIFY nombre VARCHAR(255) COLLATE utf8mb4_unicode_ci;
