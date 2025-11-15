// ============================================
// ENTIDADES
// ============================================

package com.universidad.tutorias.domain.entity;

import com.universidad.tutorias.domain.enums.EstadoAlumno;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "alumnos", indexes = {
        @Index(name = "idx_matricula", columnList = "matricula"),
        @Index(name = "idx_estado", columnList = "estado"),
        @Index(name = "idx_carrera", columnList = "carrera")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String matricula;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String carrera;

    @Column(nullable = false)
    private Integer semestre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAlumno estado = EstadoAlumno.ACTIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tutor_actual")
    private Tutor tutorActual;

    @Column(name = "contador_cambios_tutor", nullable = false)
    private Integer contadorCambiosTutor = 0;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }

    public boolean puedeReasignarse() {
        return contadorCambiosTutor < 2;
    }

    public void incrementarCambiosTutor() {
        this.contadorCambiosTutor++;
    }
}