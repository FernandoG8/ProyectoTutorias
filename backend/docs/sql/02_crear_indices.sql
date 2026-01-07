-- Índices auxiliares para sincronización y consultas frecuentes
CREATE INDEX idx_alumnos_tutor_actual ON alumnos(id_tutor_actual);
CREATE INDEX idx_asignaciones_tutor ON asignaciones(id_tutor);
CREATE INDEX idx_asignaciones_alumno ON asignaciones(id_alumno);
