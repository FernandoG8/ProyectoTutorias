
-- Insertar Tutores de Prueba
INSERT INTO tutores (nombre, carrera, capacidad_max, carga_actual, area_atencion, letra_edificio, activo, fecha_registro) VALUES
                                                                                                                              ('Dr. Juan Pérez', 'ITS', 30, 0, 'Desarrollo Web', 'A', true, NOW()),
                                                                                                                              ('Dra. María González', 'ITS', 25, 0, 'Inteligencia Artificial', 'A', true, NOW()),
                                                                                                                              ('Dr. Carlos Ramírez', 'ISC', 28, 0, 'Redes y Seguridad', 'B', true, NOW()),
                                                                                                                              ('Dra. Ana López', 'ISC', 30, 0, 'Bases de Datos', 'B', true, NOW()),
                                                                                                                              ('Dr. Roberto Martínez', 'IME', 25, 0, 'Sistemas Eléctricos', 'C', true, NOW()),
                                                                                                                              ('Dra. Laura Hernández', 'IMECA', 25, 0, 'Robótica', 'C', true, NOW()),
                                                                                                                              ('Dr. Fernando Silva', 'IE', 30, 0, 'Energías Renovables', 'D', true, NOW()),
                                                                                                                              ('Dra. Patricia Morales', 'ICA', 28, 0, 'Administración de Proyectos', 'E', true, NOW())
    ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);