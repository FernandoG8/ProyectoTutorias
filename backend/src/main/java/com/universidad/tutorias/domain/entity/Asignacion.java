package com.universidad.tutorias.domain.entity;

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
        @Index(name = "idx_semestre_asig", columnList = "semestre_academico")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_asignacion_unica", columnNames = {"id_alumno", "id_tutor", "semestre_academico"})
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

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_asignacion", nullable = false, length = 20)
    private TipoAsignacion tipoAsignacion;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;

    @Column(name = "semestre_academico", length = 20)
    private String semestreAcademico;

    @PrePersist
    protected void onCreate() {
        if (fechaAsignacion == null) {
            fechaAsignacion = LocalDateTime.now();
        }
    }
}