package com.universidad.tutorias.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO para alumnos inactivos pendientes de resolución
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendienteResolucionResponse {

    private Long alumnoInactivoId;
    private Long alumnoId;
    private String matricula;
    private String nombre;
    private String carrera;

    private Long tutorId;
    private String tutorNombre;
    private Boolean tutorActivo;
    private int tutorCargaActual;
    private int tutorCapacidadMax;
    private int tutorCuposDisponibles;
}
