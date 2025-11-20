package com.universidad.tutorias.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para la respuesta del endpoint POST /api/asignaciones/ejecutar
 *
 * Representa el resultado de ejecutar la asignación de alumnos a tutores.
 *
 * FLUJO ESPERADO:
 * 1. Cliente obtiene respuesta de POST /validar-excel (status="OK", data=[])
 * 2. Cliente extrae "data" y construye EjecutarAsignacionRequest
 * 3. Cliente envía POST /ejecutar con request
 * 4. Servidor retorna EjecucionAsignacionResponse con resultado
 *
 * RESPUESTA EXITOSA:
 * {
 *   "status": "OK",
 *   "message": "Asignación completada exitosamente",
 *   "timestamp": "2025-11-19T14:30:00",
 *   "totalAlumnos": 150,
 *   "alumnosAsignados": 150,
 *   "alumnosConError": 0,
 *   "duracionMs": 2500,
 *   "detalles": "150 alumnos asignados exitosamente en 2500ms",
 *   "erroresDetalle": []
 * }
 *
 * RESPUESTA CON ERRORES PARCIALES:
 * {
 *   "status": "PARTIAL",
 *   "message": "Asignación completada parcialmente",
 *   "timestamp": "2025-11-19T14:30:00",
 *   "totalAlumnos": 150,
 *   "alumnosAsignados": 145,
 *   "alumnosConError": 5,
 *   "duracionMs": 2500,
 *   "detalles": "145 de 150 alumnos asignados. 5 errores durante asignación",
 *   "erroresDetalle": [ ... ]
 * }
 *
 * RESPUESTA CON ERROR COMPLETO:
 * {
 *   "status": "ERROR",
 *   "message": "Asignación fallida",
 *   "timestamp": "2025-11-19T14:30:00",
 *   "totalAlumnos": 150,
 *   "alumnosAsignados": 0,
 *   "alumnosConError": 150,
 *   "duracionMs": 500,
 *   "detalles": "Error al procesar asignación: [descripción del error]",
 *   "erroresDetalle": [ { "alumnoId": null, "error": "..." } ]
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EjecucionAsignacionResponse {

    /**
     * Estado de la ejecución.
     * "OK" = todos los alumnos asignados exitosamente
     * "PARTIAL" = algunos alumnos asignados, otros con error
     * "ERROR" = no se pudo asignar (fallo general)
     * "PENDING" = aún no implementado
     */
    @JsonProperty("status")
    private String status;

    /**
     * Mensaje descriptivo para el usuario.
     * Ejemplos:
     * - "Asignación completada exitosamente"
     * - "Asignación completada parcialmente"
     * - "Error durante asignación"
     */
    @JsonProperty("message")
    private String message;

    /**
     * Timestamp de cuándo se ejecutó la asignación.
     */
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    /**
     * Total de alumnos que se intentó asignar.
     */
    @JsonProperty("totalAlumnos")
    private Integer totalAlumnos;

    /**
     * Total de alumnos asignados exitosamente.
     */
    @JsonProperty("alumnosAsignados")
    private Integer alumnosAsignados;

    /**
     * Total de alumnos que fallaron en asignación.
     */
    @JsonProperty("alumnosConError")
    private Integer alumnosConError;

    /**
     * Duración de la ejecución en milisegundos.
     * Útil para monitoreo de performance.
     */
    @JsonProperty("duracionMs")
    private Long duracionMs;

    /**
     * Información textual de resumen.
     * Ejemplos:
     * - "150 alumnos asignados exitosamente en 2500ms"
     * - "145 de 150 alumnos asignados. 5 errores durante asignación"
     * - "Error general: No se pudo conectar a base de datos"
     */
    @JsonProperty("detalles")
    private String detalles;

    /**
     * Porcentaje de alumnos asignados exitosamente.
     * Cálculo: (alumnosAsignados / totalAlumnos) * 100
     */
    @JsonProperty("porcentajeExito")
    private Double porcentajeExito;

    /**
     * Lista detallada de errores encontrados durante asignación.
     *
     * Solo presente si alumnosConError > 0.
     *
     * Cada elemento describe:
     * - ID del alumno que falló
     * - Descripción del error
     * - Tutor que se intentó asignar (si aplica)
     * - Causa raíz (e.g., "sin capacidad disponible", "tutor inactivo")
     */
    @JsonProperty("erroresDetalle")
    private List<AsignacionErrorDTO> erroresDetalle;

}
