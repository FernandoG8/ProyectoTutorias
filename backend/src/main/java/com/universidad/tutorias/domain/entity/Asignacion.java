package com.universidad.tutorias.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.universidad.tutorias.domain.enums.TipoAsignacion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "asignaciones", indexes = {
        @Index(name = "idx_alumno_asig", columnList = "id_alumno"),
        @Index(name = "idx_tutor_asig", columnList = "id_tutor"),
        @Index(name = "idx_semestre_asig", columnList = "id_semestre")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_asignacion_alumno_tutor_semestre", columnNames = {"id_alumno", "id_tutor", "id_semestre"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alumno", nullable = false)
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tutor", nullable = false)
    private Tutor tutor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_semestre", nullable = false,
                foreignKey = @ForeignKey(name = "fk_asignacion_semestre"))
    @JsonIgnoreProperties({"asignaciones", "procesos"})
    private Semestre semestre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_asignacion", nullable = false, length = 20)
    private TipoAsignacion tipoAsignacion;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;

    @Column(name = "semestre_academico", length = 20)
    @Deprecated(forRemoval = false, since = "2.0")
    private String semestreAcademico;

    @PrePersist
    protected void onCreate() {
        if (fechaAsignacion == null) {
            fechaAsignacion = LocalDateTime.now();
        }
    }

    @JsonProperty("semestreAcademico")
    public String getSemestreCodigoLegacy() {
        return semestre != null ? semestre.getCodigo() : null;
    }
}