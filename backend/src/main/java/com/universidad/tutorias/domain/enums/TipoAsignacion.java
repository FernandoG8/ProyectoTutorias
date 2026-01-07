package com.universidad.tutorias.domain.enums;

import lombok.Getter;

/**
 * Tipos de Asignación permitidos en la tabla de asignaciones.
 * 
 * IMPORTANTE: Esta tabla SOLO debe manejar dos tipos:
 * - NUEVO_INGRESO: Primer assignment del alumno al sistema
 * - REINGRESO: Alumno que retorna tras inactividad
 * 
 * Los cambios de tutor manuales NO deben modificar este tipo.
 * En su lugar, se registran en la tabla de auditoría: TutorCambioAuditoria
 * 
 * Esta separación clara permite:
 * ✓ Distinguir entre asignaciones iniciales y retornos
 * ✓ Mantener historial limpio de cambios automáticos
 * ✓ Registrar cambios manuales en tabla separada (trazabilidad)
 * ✓ Facilitar análisis y reporting de tipos de asignación
 * 
 * @see com.universidad.tutorias.domain.entity.TutorCambioAuditoria
 */
@Getter
public enum TipoAsignacion {
    NUEVO_INGRESO("Nuevo Ingreso", 
        "Primer assignment del alumno al sistema de tutorías"),
    REINGRESO("Reingreso", 
        "Alumno que retorna tras período de inactividad");

    private final String nombre;
    private final String descripcion;

    TipoAsignacion(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

}
