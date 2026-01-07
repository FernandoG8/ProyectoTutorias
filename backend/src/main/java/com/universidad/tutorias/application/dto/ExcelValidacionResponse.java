package com.universidad.tutorias.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para la respuesta del endpoint POST /api/asignacion/validar-excel
 * 
 * Dos escenarios:
 * 
 * 1. CON ERRORES (status = "ERROR"):
 *    {
 *      "status": "ERROR",
 *      "message": "Se encontraron 5 errores de validación",
 *      "totalFilas": 150,
 *      "totalValidas": 145,
 *      "totalErrores": 5,
 *      "errors": [ ... ],
 *      "data": null
 *    }
 *    → NO proceder a endpoint /ejecutar
 *    → Mostrar errores a usuario para corrección
 *
 * 2. SIN ERRORES (status = "OK"):
 *    {
 *      "status": "OK",
 *      "message": "Validación completada exitosamente",
 *      "totalFilas": 150,
 *      "totalValidas": 150,
 *      "totalErrores": 0,
 *      "errors": [],
 *      "data": [ AlumnoValidadoDTO[], ... ]  ← ORDENADO por semestre (mayor → menor)
 *    }
 *    → Proceder a endpoint /ejecutar
 *    → Pasar "data" como parte de EjecutarAsignacionRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelValidacionResponse {

    /**
     * Estado de la validación.
     * "OK" = sin errores, data presente, listo para asignación
     * "ERROR" = hay errores, data null, requiere corrección
     * "WARNING" = sin errores críticos pero hay advertencias
     */
    @JsonProperty("status")
    private String status;

    /**
     * Mensaje descriptivo para el usuario.
     * Ejemplos:
     * - "Validación completada exitosamente"
     * - "Se encontraron 5 errores de validación"
     * - "Archivo validado pero con advertencias"
     */
    @JsonProperty("message")
    private String message;

    /**
     * Timestamp de cuándo se ejecutó la validación.
     */
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    /**
     * Total de filas procesadas desde el Excel (excluyendo header).
     */
    @JsonProperty("totalFilas")
    private Integer totalFilas;

    /**
     * Total de filas que pasaron validación.
     */
    @JsonProperty("totalValidas")
    private Integer totalValidas;

    /**
     * Total de filas con errores.
     */
    @JsonProperty("totalErrores")
    private Integer totalErrores;

    /**
     * Porcentaje de filas válidas.
     * Útil para indicador de progreso en UI.
     */
    @JsonProperty("porcentajeExito")
    private Double porcentajeExito;

    /**
     * Lista detallada de errores encontrados (si los hay).
     * 
     * IMPORTANTE: La validación NO se detiene en el primer error.
     * Se reportan TODOS los errores encontrados.
     * 
     * Esto permite que el usuario vea todo lo que hay que corregir
     * sin tener que corregir de uno en uno.
     */
    @JsonProperty("errors")
    private List<ExcelErrorDTO> errors;

    /**
     * Lista de alumnos validados, limpios y ordenados para asignación.
     * 
     * SOLO presente si status == "OK" (sin errores).
     * 
     * Contiene:
     * ✓ Datos válidos y limpiados
     * ✓ ORDENADOS por semestre (semestres altos primero, nuevo ingreso al final)
     * ✓ Con semestreId correcto (NOT string)
     * ✓ Listos para pasar al endpoint /ejecutar
     * 
     * Si status == "ERROR", este campo es null.
     */
    @JsonProperty("data")
    private List<AlumnoValidadoDTO> data;

    /**
     * Información de resumen del procesamiento (para debugging).
     */
    @JsonProperty("resumo")
    private String resumen;

}
