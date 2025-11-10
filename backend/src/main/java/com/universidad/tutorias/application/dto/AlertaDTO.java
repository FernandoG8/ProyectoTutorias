package com.universidad.tutorias.application.dto;

import com.universidad.tutorias.domain.enums.SeveridadAlerta;
import com.universidad.tutorias.domain.enums.TipoAlerta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaDTO {
    private Long id;
    private TipoAlerta tipo;
    private SeveridadAlerta severidad;
    private String descripcion;
    private AlumnoSimpleDTO alumno;
    private TutorSimpleDTO tutor;
    private Boolean resuelta;
    private LocalDateTime fechaCreacion;
}