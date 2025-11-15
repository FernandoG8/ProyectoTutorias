// ============================================
// AUDITORIA SERVICE IMPLEMENTATION
// ============================================

package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.service.AuditoriaService;
import com.universidad.tutorias.domain.entity.LogAuditoria;
import com.universidad.tutorias.domain.entity.ProcesoAsignacion;
import com.universidad.tutorias.domain.enums.TipoAccion;
import com.universidad.tutorias.domain.repository.LogAuditoriaRepository;
import com.universidad.tutorias.domain.repository.ProcesoAsignacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService {

    private final LogAuditoriaRepository logRepository;
    private final ProcesoAsignacionRepository procesoRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarLog(Long procesoId,
                             TipoAccion tipoAccion,
                             String entidad,
                             Long idEntidad,
                             String descripcion,
                             String datosAntes,
                             String datosDespues,
                             String usuario) {

        ProcesoAsignacion proceso = null;
        if (procesoId != null) {
            proceso = procesoRepository.findById(procesoId).orElse(null);
        }

        LogAuditoria log = LogAuditoria.builder()
                .proceso(proceso)
                .tipoAccion(tipoAccion)
                .entidad(entidad)
                .idEntidad(idEntidad)
                .descripcion(descripcion)
                .datosAntes(datosAntes)
                .datosDespues(datosDespues)
                .usuario(usuario)
                .build();

        try {
            logRepository.save(log);
        } catch (Exception e) {
            log.error("Error al registrar log de auditoría: {}", e.getMessage());
            // No lanzar excepción para no afectar el proceso principal
        }
    }
}