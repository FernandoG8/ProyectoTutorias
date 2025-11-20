package com.universidad.tutorias.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar un error encontrado durante validación de Excel.
 * 
 * Se devuelve como parte de ExcelValidacionResponse cuando hay problemas.
 * Cada error es una fila/columna específica con descripción clara.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelErrorDTO {

    /**
     * Número de fila en el Excel donde ocurrió el error.
     * Fila 1 = headers, fila 2 = primer dato, etc.
     */
    @JsonProperty("filaExcel")
    private Integer filaExcel;

    /**
     * Campo/columna donde ocurrió el error.
     * Ejemplos: "matricula", "nombre", "carrera", "semestre"
     */
    @JsonProperty("campo")
    private String campo;

    /**
     * Valor que causó el error (para debugging).
     * Ejemplos: "INVALID123", "", "99", "2025-2026-F3"
     */
    @JsonProperty("valor")
    private String valor;

    /**
     * Descripción clara del error en español.
     * Visible para el usuario final.
     * Ejemplos:
     * - "Matrícula inválida: debe tener 5-20 caracteres alfanuméricos"
     * - "Campo requerido vacío"
     * - "Carrera no reconocida: debe ser uno de [ICA, IE, IMECA, IME, ISC, ITS]"
     * - "Semestre inválido: debe ser número 1-12"
     * - "Semestre no coincide con semestres de la lista"
     * - "Matrícula duplicada en el Excel"
     */
    @JsonProperty("descripcion")
    private String descripcion;

    /**
     * Tipo de error para clasificación y reportes.
     * Valores posibles (del enum TipoError):
     * - MATRICULA_INVALIDA
     * - CAMPO_VACIO
     * - CARRERA_INVALIDA
     * - SEMESTRE_INVALIDO
     * - MATRICULA_DUPLICADA
     * - ASIGNACION_DUPLICADA
     * - CAPACIDAD_EXCEDIDA
     * - SIN_TUTOR_DISPONIBLE
     * - ERROR_SISTEMA
     */
    @JsonProperty("tipoError")
    private String tipoError;

    /**
     * Severidad del error.
     * ERROR: impide continuar
     * WARNING: se reporta pero no impide
     */
    @JsonProperty("severidad")
    private String severidad = "ERROR";

}
