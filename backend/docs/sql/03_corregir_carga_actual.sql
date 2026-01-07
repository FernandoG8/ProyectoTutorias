-- Recalcula la carga actual de cada tutor basándose en asignaciones activas
UPDATE tutores t
SET carga_actual = (
    SELECT COUNT(*)
    FROM asignaciones a
    WHERE a.id_tutor = t.id
);
