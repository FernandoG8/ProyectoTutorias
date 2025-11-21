package com.universidad.tutorias.domain.exception;

/**
 * Excepción para errores en el proceso de reasignación de tutores.
 * Casos de uso:
 * - Alumno ha alcanzado límite de 2 cambios permitidos
 * - Tutor destino no tiene capacidad disponible
 * - Alumno no está asignado al tutor origen
 * - Tutores origen y destino son iguales
 * - Semestre no existe
 *
 * Retorna HTTP 400 BAD_REQUEST (validación de reglas de negocio).
 */
public class ReasignacionTutorException extends RuntimeException {

    public ReasignacionTutorException(String message) {
        super(message);
    }

    public ReasignacionTutorException(String message, Throwable cause) {
        super(message, cause);
    }
}
