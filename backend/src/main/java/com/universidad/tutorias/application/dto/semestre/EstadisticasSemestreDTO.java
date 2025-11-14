package com.universidad.tutorias.application.dto.semestre;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasSemestreDTO {

    private Long semestreId;
    private String codigo;
    private String nombre;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;

    private Boolean activo;
    private Boolean estaVigente;

    // Estadísticas generales
    private Long totalAsignaciones;
    private Long alumnosActivos;
    private Long alumnosInactivos;

    // Estadísticas de tutores
    private Integer totalTutoresActivos;
    private Integer tutoresConAsignaciones;
    private Integer tutoresConCapacidadLlena;
    private Integer tutoresDisponibles;
    private Double promedioAlumnosPorTutor;

    // Distribución por carrera
    private Map<String, Integer> distribucionPorCarrera;

    // Alertas
    private Integer totalAlertas;
    private Integer alertasCriticas;
}
