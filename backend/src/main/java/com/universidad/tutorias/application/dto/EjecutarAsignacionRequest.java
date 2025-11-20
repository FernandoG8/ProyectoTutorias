package com.universidad.tutorias.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para el body del endpoint POST /api/asignacion/ejecutar
 * 
 * Input esperado:
 * {
 *   "semestreId": 5,
 *   "alumnosValidados": [ ... ]  ← Del endpoint /validar-excel
 * }
 * 
 * Precondiciones:
 * ✓ Los alumnos ya están validados (del endpoint anterior)
 * ✓ Los alumnos ya están limpios y ordenados
 * ✓ El semestreId ya fue validado en /validar-excel
 * ✓ La lista NO está vacía
 * 
 * Este endpoint asume que los datos son válidos y solo ejecuta
 * la lógica de asignación (sin validación adicional).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EjecutarAsignacionRequest {

    /**
     * ID del semestre para el cual se asignan alumnos.
     * 
     * Requerido y ya validado.
     * Debe corresponder con los semestreId de cada alumno en la lista.
     */
    @NotNull(message = "semestreId es requerido")
    @JsonProperty("semestreId")
    private Long semestreId;

    /**
     * Lista de alumnos ya validados, limpios y listos para asignación.
     * 
     * Esta es la salida del endpoint /validar-excel (campo "data").
     * 
     * Precondiciones:
     * ✓ Todos los alumnos pasaron validación
     * ✓ Los datos ya fueron limpiados (trim, uppercase, etc)
     * ✓ Ya están ORDENADOS por semestre (mayores primero)
     * ✓ Los semestreId ya fueron validados
     * 
     * La asignación respeta este orden: procesa mayores semestres primero
     * para liberar cupos antes de asignar nuevo ingreso.
     */
    @NotEmpty(message = "alumnosValidados no puede estar vacío")
    @Valid
    @JsonProperty("alumnosValidados")
    private List<AlumnoValidadoDTO> alumnosValidados;

    /**
     * Flag opcional: si true, solo simular asignación sin guardar.
     * 
     * Útil para:
     * - Validación previa sin hacer cambios reales
     * - Testing de capacidades
     * - Previsualizacion de resultados
     * 
     * Default: false (ejecutar de verdad)
     */
    @JsonProperty("simular")
    private Boolean simular = false;

    /**
     * Flag opcional: si true, reporte detallado con cada asignación.
     * 
     * Default: false (solo resumen)
     */
    @JsonProperty("reporteDetallado")
    private Boolean reporteDetallado = false;

}
