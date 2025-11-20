package com.universidad.tutorias.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Auditoría de cambios de tutor en asignaciones.
 * 
 * Esta tabla registra TODOS los cambios manuales de tutor para un alumno.
 * Los cambios se registran AQUÍ, NO en la tabla principal de asignaciones.
 * 
 * Esto permite:
 * ✓ Mantener la tabla de asignaciones limpia (solo NUEVO_INGRESO y REINGRESO)
 * ✓ Trazabilidad completa de quién cambió a quién y cuándo
 * ✓ Análisis histórico de cambios manuales vs automáticos
 * ✓ Validación del cumplimiento de políticas de reasignación
 * 
 * IMPORTANTE: Un cambio aquí NO modifica el tipo de asignación en la tabla principal.
 * Solo actualiza el ID del tutor asignado.
 * 
 * Flujo:
 * 1. Alumno tiene asignación inicial con Tutor A (NUEVO_INGRESO)
 * 2. Se solicita cambio manual a Tutor B
 * 3. Se crea registro en TutorCambioAuditoria: { tutor_anterior: A, tutor_nuevo: B }
 * 4. Se actualiza Asignacion.tutor a B (tipo sigue siendo NUEVO_INGRESO)
 * 5. Se registra en logs_auditoria para completa trazabilidad
 * 
 * @see com.universidad.tutorias.domain.entity.Asignacion
 * @see com.universidad.tutorias.domain.entity.TutorCambioAuditoriaRepository
 */
@Entity
@Table(name = "tutor_cambio_auditoria", indexes = {
    @Index(name = "idx_asignacion_cambio", columnList = "id_asignacion"),
    @Index(name = "idx_fecha_cambio", columnList = "fecha_hora_cambio"),
    @Index(name = "idx_usuario_cambio", columnList = "usuario_responsable")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorCambioAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Referencia a la asignación que fue modificada.
     * Esta asignación mantiene su tipo_asignacion intacto (NUEVO_INGRESO o REINGRESO).
     */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_asignacion", nullable = false)
    private Asignacion asignacion;

    /**
     * Tutor anterior al cambio.
     * Puede ser NULL si era el primer tutor (aunque normalmente no ocurre).
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tutor_anterior")
    private Tutor tutorAnterior;

    /**
     * Tutor nuevo asignado en este cambio.
     * Será el valor actualizado en Asignacion.tutor.
     */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tutor_nuevo", nullable = false)
    private Tutor tutorNuevo;

    /**
     * Usuario responsable del cambio.
     * Puede ser un usuario admin, sistema, etc.
     */
    @Column(name = "usuario_responsable", length = 100)
    private String usuarioResponsable;

    /**
     * Timestamp exacto del cambio.
     * Se establece automáticamente en la creación.
     */
    @Column(name = "fecha_hora_cambio", nullable = false, updatable = false)
    private LocalDateTime fechaHoraCambio;

    /**
     * Motivo del cambio manual.
     * Ejemplos: "Solicitud del alumno", "Tutor no disponible", "Balance de carga", etc.
     */
    @Column(name = "motivo", length = 500)
    private String motivo;

    /**
     * Tipo de cambio: MANUAL, SISTEMA, AUTORIZADO, etc.
     */
    @Column(name = "tipo_cambio", length = 50)
    private String tipoCambio;

    /**
     * Notas adicionales sobre el cambio.
     */
    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @PrePersist
    protected void onCreate() {
        if (this.fechaHoraCambio == null) {
            this.fechaHoraCambio = LocalDateTime.now();
        }
    }

}
