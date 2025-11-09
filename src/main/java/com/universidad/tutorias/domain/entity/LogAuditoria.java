package com.universidad.tutorias.domain.entity;

import com.universidad.tutorias.domain.enums.TipoAccion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "logs_auditoria", indexes = {
        @Index(name = "idx_proceso_log", columnList = "id_proceso"),
        @Index(name = "idx_tipo_accion", columnList = "tipo_accion"),
        @Index(name = "idx_fecha_log", columnList = "fecha_registro")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proceso")
    private ProcesoAsignacion proceso;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_accion", nullable = false, length = 30)
    private TipoAccion tipoAccion;

    @Column(length = 50)
    private String entidad;

    @Column(name = "id_entidad")
    private Long idEntidad;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "datos_antes", columnDefinition = "TEXT")
    private String datosAntes;

    @Column(name = "datos_despues", columnDefinition = "TEXT")
    private String datosDespues;

    @Column(length = 100)
    private String usuario;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }
}