package com.universidad.tutorias.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para alumno validado, limpio y listo para asignación.
 * 
 * Este DTO es la salida del endpoint /validar-excel (si no hay errores)
 * y la entrada del endpoint /ejecutar.
 * 
 * Contiene:
 * ✓ Datos básicos del alumno (de Excel)
 * ✓ Referencia correcta a semestreId (NOT string)
 * ✓ Orden de prioridad para asignación (mayores semestres primero)
 * ✓ Flag de validación completada
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlumnoValidadoDTO {

    /**
     * ID único del alumno (si está en BD).
     * Puede ser NULL si es nuevo ingreso aún no en BD.
     */
    private Long alumnoId;

    /**
     * Matrícula del alumno (PK desde Excel).
     * Requerido, ya validado.
     */
    private String matricula;

    /**
     * Nombre del alumno.
     * Requerido, ya validado y limpiado (trim, etc).
     */
    private String nombre;

    /**
     * Código de carrera (ICA, IE, IMECA, etc).
     * Requerido, ya validado contra lista permitida.
     */
    private String carrera;

    /**
     * ID del semestre (NOT string, FK directo).
     * Requerido, ya validado que semestre existe.
     */
    @JsonProperty("semestreId")
    private Long semestreId;

    /**
     * Código del semestre para display (YYYY-YYYY-FN).
     * Solo lectura, derivado de semestreId.
     */
    @JsonProperty("semestreCodigo")
    private String semestreCodigo;

    /**
     * Número de semestre (1-12) para ordenamiento.
     * Se usa para: ordenar alumnos por semestre (mayor → menor)
     */
    @JsonProperty("semestreNumerico")
    private Integer semestreNumerico;

    /**
     * Prioridad de orden para asignación.
     * Mayor valor = se asigna primero (semestres avanzados)
     * Menor valor = se asigna después (nuevo ingreso)
     * 
     * Esto asegura que liberamos cupos en semestres avanzados
     * ANTES de asignar nuevo ingreso con los cupos liberados.
     */
    @JsonProperty("ordenPriority")
    private Integer ordenPriority;

    /**
     * Flag indicando que este alumno pasó validación completa.
     * Siempre TRUE en esta salida (si aparece en data[], es porque pasó validación).
     */
    @JsonProperty("validado")
    private Boolean validado;

    /**
     * Tipo de ingreso previsto.
     * NUEVO_INGRESO o REINGRESO
     * Se calcula durante asignación basado en historial del alumno.
     */
    @JsonProperty("tipoAsignacionPrevisto")
    private String tipoAsignacionPrevisto;

}
