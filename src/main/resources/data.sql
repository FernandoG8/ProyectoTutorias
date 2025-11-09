
-- Insertar Matriz de Afinidad de Carreras
INSERT INTO matriz_afinidad_carreras (carrera_origen, carrera_compatible, prioridad) VALUES
-- Ingeniería Civil y Administración (ICA)
('ICA', 'ICA', 1)
    ON DUPLICATE KEY UPDATE prioridad = VALUES(prioridad);

INSERT INTO matriz_afinidad_carreras (carrera_origen, carrera_compatible, prioridad) VALUES
-- Ingeniería en Energía (IE)
('IE', 'IE', 1),
('IE', 'IME', 2),
('IE', 'IMECA', 3)
    ON DUPLICATE KEY UPDATE prioridad = VALUES(prioridad);

INSERT INTO matriz_afinidad_carreras (carrera_origen, carrera_compatible, prioridad) VALUES
-- Ingeniería en Mecatrónica (IMECA)
('IMECA', 'IMECA', 1),
('IMECA', 'IME', 2),
('IMECA', 'IE', 3)
    ON DUPLICATE KEY UPDATE prioridad = VALUES(prioridad);

INSERT INTO matriz_afinidad_carreras (carrera_origen, carrera_compatible, prioridad) VALUES
-- Ingeniería en Mecánica Eléctrica (IME)
('IME', 'IME', 1),
('IME', 'IMECA', 2),
('IME', 'IE', 2)
    ON DUPLICATE KEY UPDATE prioridad = VALUES(prioridad);

INSERT INTO matriz_afinidad_carreras (carrera_origen, carrera_compatible, prioridad) VALUES
-- Ingeniería en Sistemas Computacionales (ISC)
('ISC', 'ISC', 1),
('ISC', 'ITS', 2)
    ON DUPLICATE KEY UPDATE prioridad = VALUES(prioridad);

INSERT INTO matriz_afinidad_carreras (carrera_origen, carrera_compatible, prioridad) VALUES
-- Ingeniería en Tecnologías de Software (ITS)
('ITS', 'ITS', 1),
('ITS', 'ISC', 2)
    ON DUPLICATE KEY UPDATE prioridad = VALUES(prioridad);