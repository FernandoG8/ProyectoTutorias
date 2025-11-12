-- Asegura unicidad de matrículas y combinaciones alumno-tutor-semestre
ALTER TABLE alumnos
    ADD CONSTRAINT uk_alumno_matricula UNIQUE (matricula);

ALTER TABLE asignaciones
    ADD CONSTRAINT uk_asignacion_unica UNIQUE (id_alumno, id_tutor, semestre_academico);
