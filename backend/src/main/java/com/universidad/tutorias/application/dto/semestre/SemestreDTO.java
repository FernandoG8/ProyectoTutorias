package com.universidad.tutorias.application.dto.semestre;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.universidad.tutorias.domain.entity.Semestre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemestreDTO {

    private Long id;
    private String codigo;
    private String nombre;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;

    private Boolean activo;
    private Integer totalAsignaciones;
    private Boolean estaVigente;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaCreacion;

    public static SemestreDTO fromEntity(Semestre semestre) {
        if (semestre == null) {
            return null;
        }

        return SemestreDTO.builder()
                .id(semestre.getId())
                .codigo(semestre.getCodigo())
                .nombre(semestre.getNombre())
                .fechaInicio(semestre.getFechaInicio())
                .fechaFin(semestre.getFechaFin())
                .activo(semestre.getActivo())
                .totalAsignaciones(semestre.getTotalAsignaciones())
                .estaVigente(semestre.estaVigente())
                .fechaCreacion(semestre.getFechaCreacion())
                .build();
    }
}
