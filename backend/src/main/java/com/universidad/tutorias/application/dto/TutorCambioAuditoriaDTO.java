package com.universidad.tutorias.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para representar un cambio de tutor registrado en auditoría.
 * 
 * Se devuelve como parte de respuestas sobre cambios de tutores.
 * Contiene información completa del cambio para trazabilidad.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorCambioAuditoriaDTO {

    /**
     * ID del registro de auditoría (para referencia posterior si es necesario).
     */
    @JsonProperty("auditoriaId")
    private Long auditoriaId;

    /**
     * ID de la asignación que fue modificada.
     */
    @JsonProperty("asignacionId")
    private Long asignacionId;

    /**
     * Información del alumno (para contexto).
     */
    @JsonProperty("alumno")
    private AlumnoSimpleDTO alumno;

    /**
     * Tutor anterior (quién lo tenía antes).
     * Puede ser null si era el primer asignador.
     */
    @JsonProperty("tutorAnterior")
    private TutorSimpleDTO tutorAnterior;

    /**
     * Tutor nuevo (quién lo tiene ahora).
     */
    @JsonProperty("tutorNuevo")
    private TutorSimpleDTO tutorNuevo;

    /**
     * Usuario responsable del cambio.
     * Puede ser: ADMIN_USERNAME, SISTEMA, etc.
     */
    @JsonProperty("usuarioResponsable")
    private String usuarioResponsable;

    /**
     * Cuándo se realizó el cambio.
     */
    @JsonProperty("fechaHoraCambio")
    private LocalDateTime fechaHoraCambio;

    /**
     * Motivo del cambio (por qué se hizo).
     * Ejemplos: "Solicitud del alumno", "Tutor no disponible", "Balance de carga", etc.
     */
    @JsonProperty("motivo")
    private String motivo;

    /**
     * Tipo de cambio: MANUAL, SISTEMA, AUTORIZADO, etc.
     */
    @JsonProperty("tipoCambio")
    private String tipoCambio;

    /**
     * Notas adicionales del cambio.
     */
    @JsonProperty("notas")
    private String notas;

}
