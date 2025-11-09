package com.universidad.tutorias.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "matriz_afinidad_carreras",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"carrera_origen", "carrera_compatible"}
        ),
        indexes = {
                @Index(name = "idx_carrera_origen", columnList = "carrera_origen")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatrizAfinidadCarrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "carrera_origen", nullable = false, length = 100)
    private String carreraOrigen;

    @Column(name = "carrera_compatible", nullable = false, length = 100)
    private String carreraCompatible;

    @Column(nullable = false)
    private Integer prioridad;
}
