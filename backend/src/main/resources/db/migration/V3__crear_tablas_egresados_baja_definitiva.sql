CREATE TABLE IF NOT EXISTS alumnos_egresados (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_alumno BIGINT NOT NULL,
    matricula VARCHAR(20) NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    carrera VARCHAR(100) NOT NULL,
    semestre INT NOT NULL,
    fecha_egreso TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_alumno_egresado UNIQUE (id_alumno),
    CONSTRAINT fk_egresado_alumno FOREIGN KEY (id_alumno) REFERENCES alumnos(id)
);

CREATE TABLE IF NOT EXISTS alumnos_baja_definitiva (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_alumno BIGINT NOT NULL,
    matricula VARCHAR(20) NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    carrera VARCHAR(100) NOT NULL,
    semestre INT NOT NULL,
    fecha_baja TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_alumno_baja_definitiva UNIQUE (id_alumno),
    CONSTRAINT fk_baja_alumno FOREIGN KEY (id_alumno) REFERENCES alumnos(id)
);
