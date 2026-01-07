package com.universidad.tutorias.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO para liberación segura de cupos
 * Proporciona detalles de validaciones y resultados
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiberacionCuposResponse {

    private boolean exitoso;
    private int cuposLiberados;
    private int tutoresSincronizados;
    private List<String> advertencias;
    private List<String> errores;

    public boolean esCompleto() {
        return exitoso && errores.isEmpty();
    }
}
