package com.universidad.tutorias.domain.entity;

import com.universidad.tutorias.domain.enums.SeveridadAlerta;
import com.universidad.tutorias.domain.enums.TipoAlerta;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "alertas_proceso", indexes = {
        @Index(name = "idx_proceso_alerta", columnList = "id_proceso"),
        @Index(name = "idx_severidad", columnList = "severidad"),
        @Index(name = "idx_resuelta", columnList = "resuelta")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaProceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proceso", nullable = false)
    private ProcesoAsignacion proceso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoAlerta tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeveridadAlerta severidad;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alumno")
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tutor")
    private Tutor tutor;

    @Column(nullable = false)
    private Boolean resuelta;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (resuelta == null) {
            resuelta = false;
        }
    }
}