package com.universidad.tutorias.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simplificado para representar un tutor en referencias.
 * Contiene solo la información esencial.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorSimpleDTO {

    /**
     * Constructor personalizado para compatibilidad con mapeos existentes.
     * Se usa cuando solo se tienen los 3 datos básicos del tutor.
     */
    public TutorSimpleDTO(Long id, String nombre, String carrera) {
        this.id = id;
        this.nombre = nombre;
        this.carrera = carrera;
    }

    /**
     * ID único del tutor.
     */
    @JsonProperty("id")
    private Long id;

    /**
     * Nombre del tutor.
     */
    @JsonProperty("nombre")
    private String nombre;

    /**
     * Carrera/departamento del tutor.
     */
    @JsonProperty("carrera")
    private String carrera;

    /**
     * Carga actual del tutor (para referencia).
     */
    @JsonProperty("cargaActual")
    private Integer cargaActual;

    /**
     * Capacidad máxima del tutor.
     */
    @JsonProperty("capacidadMax")
    private Integer capacidadMax;

    /**
     * Capacidad disponible (para referencia).
     */
    @JsonProperty("capacidadDisponible")
    private Integer capacidadDisponible;

}
