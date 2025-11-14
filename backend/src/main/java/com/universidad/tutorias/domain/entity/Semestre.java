package com.universidad.tutorias.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "semestres",
        uniqueConstraints = @UniqueConstraint(name = "uk_semestre_codigo", columnNames = "codigo"),
        indexes = {
                @Index(name = "idx_semestre_activo", columnList = "activo"),
                @Index(name = "idx_semestre_codigo", columnList = "codigo"),
                @Index(name = "idx_semestre_fechas", columnList = "fecha_inicio, fecha_fin")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Semestre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código del semestre es obligatorio")
    @Pattern(regexp = "^\\d{4}-\\d{4}-F[12]$",
             message = "Formato inválido. Use: YYYY-YYYY-FN (ej: 2025-2026-F1)")
    @Column(nullable = false, unique = true, length = 15)
    private String codigo;

    @NotBlank(message = "El nombre del semestre es obligatorio")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = false;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "semestre",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnoreProperties({"semestre", "tutor", "alumno"})
    private List<Asignacion> asignaciones = new ArrayList<>();

    @OneToMany(mappedBy = "semestre",
               fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnoreProperties({"semestre", "alertas", "errores"})
    private List<ProcesoAsignacion> procesos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        validarFechas();
    }

    @PreUpdate
    protected void onUpdate() {
        validarFechas();
    }

    public void validarFechas() {
        if (fechaFin != null && fechaInicio != null &&
            fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException(
                "La fecha de fin debe ser posterior a la fecha de inicio"
            );
        }
    }

    public boolean estaActivo() {
        return Boolean.TRUE.equals(activo);
    }

    public boolean estaVigente() {
        if (fechaInicio == null || fechaFin == null) {
            return false;
        }
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(fechaInicio) && !hoy.isAfter(fechaFin);
    }

    public Integer getTotalAsignaciones() {
        return asignaciones != null ? asignaciones.size() : 0;
    }

    @Override
    public String toString() {
        return "Semestre{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", activo=" + activo +
                ", totalAsignaciones=" + getTotalAsignaciones() +
                '}';
    }
}