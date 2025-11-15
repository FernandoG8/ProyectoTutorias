package com.universidad.tutorias.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tutores", indexes = {
        @Index(name = "idx_carrera_tutor", columnList = "carrera"),
        @Index(name = "idx_activo", columnList = "activo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String carrera;

    @Column(name = "capacidad_max", nullable = false)
    private Integer capacidadMax;

    @Column(name = "carga_actual", nullable = false)
    private Integer cargaActual = 0;

    @Column(name = "area_atencion", length = 100)
    private String areaAtencion;

    @Column(name = "letra_edificio", length = 1)
    private String letraEdificio;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @OneToMany(mappedBy = "tutorActual", fetch = FetchType.LAZY)
    private List<Alumno> alumnosAsignados = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }

    public boolean tieneCapacidadDisponible() {
        return activo && cargaActual < capacidadMax;
    }

    public int getCapacidadDisponible() {
        return Math.max(0, capacidadMax - cargaActual);
    }

    public void incrementarCarga() {
        this.cargaActual++;
    }

    public void decrementarCarga() {
        if (this.cargaActual > 0) {
            this.cargaActual--;
        }
    }

    public void sincronizarCarga(int cargaReal) {
        this.cargaActual = Math.max(0, cargaReal);
    }
}