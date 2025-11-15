package com.universidad.tutorias.application.service;

import com.universidad.tutorias.domain.enums.TipoAccion;

public interface AuditoriaService {
    /**
     * Registra un log de auditoría
     */
    void registrarLog(Long procesoId,
                      TipoAccion tipoAccion,
                      String entidad,
                      Long idEntidad,
                      String descripcion,
                      String datosAntes,
                      String datosDespues,
                      String usuario);
}