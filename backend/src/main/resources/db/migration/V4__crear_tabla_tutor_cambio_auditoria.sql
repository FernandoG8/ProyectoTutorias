-- ==================================================================================
-- V4: Crear tabla tutor_cambio_auditoria para auditar cambios de tutores
-- ==================================================================================
-- 
-- Propósito: Registrar histórico de cambios de tutor en asignaciones.
-- Los cambios se registran AQUÍ, NO en la tabla principal de asignaciones.
-- 
-- Esto permite:
-- ✓ Mantener la tabla de asignaciones limpia (solo NUEVO_INGRESO y REINGRESO)
-- ✓ Trazabilidad completa de quién cambió a quién y cuándo
-- ✓ Análisis histórico de cambios manuales vs automáticos
-- ✓ Validación del cumplimiento de políticas de reasignación
--
-- Nota: Un cambio aquí NO modifica el tipo_asignacion en la tabla principal.
--       Solo actualiza el ID del tutor asignado.
--
-- ==================================================================================

CREATE TABLE IF NOT EXISTS tutor_cambio_auditoria (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    
    -- Referencia a la asignación que fue modificada
    id_asignacion BIGINT NOT NULL,
    
    -- Tutor anterior al cambio (puede ser NULL)
    id_tutor_anterior BIGINT,
    
    -- Tutor nuevo asignado en este cambio
    id_tutor_nuevo BIGINT NOT NULL,
    
    -- Usuario responsable del cambio
    usuario_responsable VARCHAR(100),
    
    -- Timestamp exacto del cambio
    fecha_hora_cambio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Motivo del cambio manual
    motivo VARCHAR(500),
    
    -- Tipo de cambio: MANUAL, SISTEMA, AUTORIZADO, etc.
    tipo_cambio VARCHAR(50),
    
    -- Notas adicionales
    notas LONGTEXT,
    
    -- Constraints
    CONSTRAINT fk_tca_asignacion FOREIGN KEY (id_asignacion) 
        REFERENCES asignaciones(id) ON DELETE CASCADE,
    CONSTRAINT fk_tca_tutor_anterior FOREIGN KEY (id_tutor_anterior) 
        REFERENCES tutores(id) ON DELETE SET NULL,
    CONSTRAINT fk_tca_tutor_nuevo FOREIGN KEY (id_tutor_nuevo) 
        REFERENCES tutores(id) ON DELETE RESTRICT,
    
    -- Índices para queries comunes
    INDEX idx_asignacion_cambio (id_asignacion),
    INDEX idx_fecha_cambio (fecha_hora_cambio),
    INDEX idx_usuario_cambio (usuario_responsable),
    INDEX idx_tutor_nuevo (id_tutor_nuevo),
    INDEX idx_tutor_anterior (id_tutor_anterior)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================================================================================
-- Logs y comentarios
-- ==================================================================================

INSERT INTO logs_migracion (version, descripcion, fecha_creacion) 
VALUES ('V4', 'Crear tabla tutor_cambio_auditoria para auditar cambios de tutor', NOW())
ON DUPLICATE KEY UPDATE fecha_actualizacion = NOW();

