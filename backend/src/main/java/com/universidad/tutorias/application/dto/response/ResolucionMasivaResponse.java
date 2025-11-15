package com.universidad.tutorias.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO para resolución masiva de inactivos
 * Proporciona detalles de éxitos y fallos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolucionMasivaResponse {

    private int totalProcesados;
    private int exitosos;
    private int fallidos;
    private List<ResolucionExitosa> detalleExitosos;
    private List<ResolucionFallida> detalleFallidos;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResolucionExitosa {
        private Long alumnoInactivoId;
        private String motivo;
        private boolean exitoso;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResolucionFallida {
        private Long alumnoInactivoId;
        private String razon;
    }
}
