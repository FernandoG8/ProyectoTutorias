package com.universidad.tutorias.domain.entity;

import com.universidad.tutorias.domain.enums.MotivoInactividad;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "alumnos_inactivos", indexes = {
        @Index(name = "idx_alumno_inact", columnList = "id_alumno"),
        @Index(name = "idx_motivo", columnList = "motivo_inactividad")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlumnoInactivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alumno", nullable = false)
    private Alumno alumno;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_inactividad", nullable = false, length = 30)
    private MotivoInactividad motivoInactividad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_preservado")
    private Tutor tutorPreservado;

    @Column(name = "fecha_inactividad", nullable = false)
    private LocalDateTime fechaInactividad;

    @Column(name = "cupo_liberado", nullable = false)
    private Boolean cupoLiberado;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @PrePersist
    protected void onCreate() {
        if (fechaInactividad == null) {
            fechaInactividad = LocalDateTime.now();
        }
        if (motivoInactividad == null) {
            motivoInactividad = MotivoInactividad.SIN_DEFINIR;
        }
        if (cupoLiberado == null) {
            cupoLiberado = true;
        }
    }

    public boolean debePreservarTutor() {
        return motivoInactividad.debePreservarTutor();
    }
}