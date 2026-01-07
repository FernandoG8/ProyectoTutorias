package com.universidad.tutorias.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa un error específico durante la asignación de un alumno.
 *
 * Se usa en EjecucionAsignacionResponse.erroresDetalle[].
 *
 * Ejemplo:
 * {
 *   "alumnoId": 123,
 *   "alumnoMatricula": "A00123456",
 *   "alumnoNombre": "Juan Pérez García",
 *   "error": "Sin capacidad disponible",
 *   "raizCausa": "Tutor 456 no tiene cupos disponibles en semestre actual",
 *   "tutorIntentado": 456
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionErrorDTO {

    /**
     * ID del alumno que falló en asignación.
     */
    @JsonProperty("alumnoId")
    private Long alumnoId;

    /**
     * Matrícula del alumno (para referencia).
     */
    @JsonProperty("alumnoMatricula")
    private String alumnoMatricula;

    /**
     * Nombre del alumno (para referencia).
     */
    @JsonProperty("alumnoNombre")
    private String alumnoNombre;

    /**
     * Descripción del error.
     * Ejemplos:
     * - "Sin capacidad disponible"
     * - "Tutor no encontrado"
     * - "Alumno ya tiene asignación en semestre"
     * - "Semestre no válido"
     */
    @JsonProperty("error")
    private String error;

    /**
     * Causa raíz más detallada del error.
     * Ejemplos:
     * - "Tutor ID=456 no tiene cupos disponibles (carga=10/10)"
     * - "Semestre ID=999 no existe en base de datos"
     * - "Alumno ID=123 ya tiene asignación en semestre 2025-2025-FN"
     */
    @JsonProperty("raizCausa")
    private String raizCausa;

    /**
     * ID del tutor que se intentó asignar (si aplica).
     * Null si el error no está relacionado con tutor específico.
     */
    @JsonProperty("tutorIntentado")
    private Long tutorIntentado;

}
