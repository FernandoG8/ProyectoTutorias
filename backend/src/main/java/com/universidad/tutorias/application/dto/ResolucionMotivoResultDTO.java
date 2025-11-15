package com.universidad.tutorias.application.dto;

import com.universidad.tutorias.domain.enums.MotivoInactividad;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con el resultado de cambiar el motivo de inactividad de un alumno
 * Incluye información sobre la capacidad disponible del tutor preservado
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolucionMotivoResultDTO {

    /**
     * Si la operación fue exitosa
     */
    private boolean exitoso;

    /**
     * Mensaje descriptivo del resultado
     */
    private String mensaje;

    /**
     * Sugerencia (si es necesario incrementar capacidad, etc.)
     */
    private String sugerencia;

    /**
     * ID del alumno inactivo
     */
    private Long alumnoInactivoId;

    /**
     * Matrícula del alumno
     */
    private String alumnoMatricula;

    /**
     * Nombre del alumno
     */
    private String alumnoNombre;

    /**
     * Nuevo motivo asignado
     */
    private MotivoInactividad motivoInactividad;

    /**
     * ID del tutor preservado
     */
    private Long tutorPreservadoId;

    /**
     * Nombre del tutor preservado
     */
    private String tutorNombre;

    /**
     * Carga actual del tutor
     */
    private Integer tutorCargaActual;

    /**
     * Capacidad máxima del tutor
     */
    private Integer tutorCapacidadMax;

    /**
     * Cupos disponibles en el tutor
     */
    private Integer tutorCuposDisponibles;

    /**
     * Si el tutor está activo
     */
    private Boolean tutorActivo;

    /**
     * Si el tutor tiene capacidad para mantener el alumno preservado
     */
    private Boolean tutorTieneCapacidad;
}
