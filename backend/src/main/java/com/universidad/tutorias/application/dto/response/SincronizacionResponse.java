package com.universidad.tutorias.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO para sincronización de tutores
 * Proporciona detalles de los cambios realizados
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SincronizacionResponse {

    private int tutoresProcesados;
    private int tutoresCorregidos;
    private List<CambioTutorSincronizacion> cambiosDetallados;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CambioTutorSincronizacion {
        private Long tutorId;
        private String tutorNombre;
        private int cargaAnterior;
        private int cargaNueva;
        private int diferencia;
    }
}
