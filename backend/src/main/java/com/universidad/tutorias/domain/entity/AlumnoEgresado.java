package com.universidad.tutorias.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "alumnos_egresados", uniqueConstraints = {
        @UniqueConstraint(name = "uk_alumno_egresado", columnNames = "id_alumno")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlumnoEgresado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alumno", nullable = false, foreignKey = @ForeignKey(name = "fk_egresado_alumno"))
    private Alumno alumno;

    @Column(nullable = false, length = 20)
    private String matricula;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String carrera;

    @Column(nullable = false)
    private Integer semestre;

    @Column(name = "fecha_egreso", nullable = false, updatable = false)
    private LocalDateTime fechaEgreso;

    @PrePersist
    protected void onCreate() {
        if (fechaEgreso == null) {
            fechaEgreso = LocalDateTime.now();
        }
    }
}
