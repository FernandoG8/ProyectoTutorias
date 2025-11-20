package com.universidad.tutorias.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "alumnos_baja_definitiva", uniqueConstraints = {
        @UniqueConstraint(name = "uk_alumno_baja_definitiva", columnNames = "id_alumno")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlumnoBajaDefinitiva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alumno", nullable = false, foreignKey = @ForeignKey(name = "fk_baja_alumno"))
    private Alumno alumno;

    @Column(nullable = false, length = 20)
    private String matricula;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String carrera;

    @Column(nullable = false)
    private Integer semestre;

    @Column(name = "fecha_baja", nullable = false, updatable = false)
    private LocalDateTime fechaBaja;

    @PrePersist
    protected void onCreate() {
        if (fechaBaja == null) {
            fechaBaja = LocalDateTime.now();
        }
    }
}
