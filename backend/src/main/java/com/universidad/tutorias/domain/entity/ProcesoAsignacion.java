package com.universidad.tutorias.domain.entity;

import com.universidad.tutorias.domain.enums.EstadoProceso;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "procesos_asignacion", indexes = {
        @Index(name = "idx_estado_proceso", columnList = "estado"),
        @Index(name = "idx_fecha_inicio", columnList = "fecha_inicio")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcesoAsignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoProceso estado;

    @Column(name = "total_alumnos_procesados", nullable = false)
    private Integer totalAlumnosProcesados = 0;

    @Column(name = "total_alumnos_asignados", nullable = false)
    private Integer totalAlumnosAsignados = 0;

    @Column(name = "total_errores", nullable = false)
    private Integer totalErrores = 0;

    @Column(name = "total_warnings", nullable = false)
    private Integer totalWarnings = 0;

    @Column(name = "archivo_origen", length = 500)
    private String archivoOrigen;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "usuario_ejecutor", length = 100)
    private String usuarioEjecutor;

    @Column(name = "detalles_json", columnDefinition = "TEXT")
    private String detallesJson;

    @OneToMany(mappedBy = "proceso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlertaProceso> alertas = new ArrayList<>();

    @OneToMany(mappedBy = "proceso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ErrorValidacion> errores = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (fechaInicio == null) {
            fechaInicio = LocalDateTime.now();
        }
    }

    public void incrementarAlumnosProcesados() {
        this.totalAlumnosProcesados++;
    }

    public void incrementarAlumnosAsignados() {
        this.totalAlumnosAsignados++;
    }

    public void incrementarErrores() {
        this.totalErrores++;
    }

    public void incrementarWarnings() {
        this.totalWarnings++;
    }

    public long getTiempoTranscurridoMs() {
        LocalDateTime fin = fechaFin != null ? fechaFin : LocalDateTime.now();
        return Duration.between(fechaInicio, fin).toMillis();
    }

    public int getPorcentajeProgreso(int totalEsperado) {
        if (totalEsperado == 0) return 0;
        return (int) ((totalAlumnosProcesados * 100.0) / totalEsperado);
    }
}