package com.universidad.tutorias.application.dto.mantenimiento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlumnoInactivoPendienteDTO {
    private Long alumnoInactivoId;
    private Long alumnoId;
    private String matricula;
    private String nombre;
    private String carrera;
    private Long tutorPreservadoId;
    private String tutorNombre;
    private int diasInactivo;
    private boolean tutorActivo;
    private int tutorCuposDisponibles;
}
