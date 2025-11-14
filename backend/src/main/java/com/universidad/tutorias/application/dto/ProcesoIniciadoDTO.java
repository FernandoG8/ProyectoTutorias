package com.universidad.tutorias.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta al iniciar un proceso de asignación")
public class ProcesoIniciadoDTO {

    @Schema(description = "ID del proceso de asignación iniciado", example = "1")
    private Long procesoId;

    @Schema(description = "Estado actual del proceso", example = "INICIADO")
    private String estado;

    @Schema(description = "ID del semestre", example = "1")
    private Long semestreId;

    @Schema(description = "Código del semestre", example = "2025-2026-F1")
    private String semestreCodigo;

    @Schema(description = "Mensaje informativo", example = "Proceso iniciado correctamente")
    private String mensaje;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp del inicio del proceso", example = "2025-11-14T10:30:00")
    private LocalDateTime timestamp;
}
