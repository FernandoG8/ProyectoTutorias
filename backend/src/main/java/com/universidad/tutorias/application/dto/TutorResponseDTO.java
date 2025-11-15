package com.universidad.tutorias.application.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TutorResponseDTO {
    Long id;
    String nombre;
    String carrera;
    Integer capacidadMax;
    Integer cargaActual;
    Integer capacidadDisponible;
    String areaAtencion;
    String letraEdificio;
    Boolean activo;
}
