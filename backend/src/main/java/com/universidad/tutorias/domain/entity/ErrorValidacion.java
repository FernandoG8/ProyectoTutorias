package com.universidad.tutorias.domain.entity;

import com.universidad.tutorias.domain.enums.TipoError;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "errores_validacion", indexes = {
        @Index(name = "idx_proceso_error", columnList = "id_proceso"),
        @Index(name = "idx_tipo_error", columnList = "tipo_error")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorValidacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proceso", nullable = true)
    private ProcesoAsignacion proceso;

    @Column(name = "fila_excel")
    private Integer filaExcel;

    @Column(length = 20)
    private String matricula;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_error", nullable = false, length = 30)
    private TipoError tipoError;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "dato_erroneo", length = 500)
    private String datoErroneo;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }
}