package com.universidad.tutorias.application.service;

import com.universidad.tutorias.application.dto.AlumnoValidadoDTO;
import com.universidad.tutorias.application.dto.ExcelErrorDTO;
import com.universidad.tutorias.application.dto.ExcelValidacionResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servicio especializado en validación, limpieza y ordenamiento de Excel.
 * 
 * Este servicio orquesta el flujo completo del Endpoint 1:
 * 
 * FLUJO:
 * 1. Leer archivo Excel (delegado a ExcelReaderService)
 * 2. Validar datos completos (delegado a AlumnoValidadorService)
 * 3. Recolectar TODOS los errores (sin detener en primero)
 * 4. Limpiar datos válidos (trim, uppercase, formato)
 * 5. Ordenar alumnos por semestre (mayores primero, nuevo ingreso al final)
 * 6. Convertir a AlumnoValidadoDTO
 * 7. Construir respuesta final
 * 
 * IMPORTANTE - Validación Completa:
 * La validación NO se detiene en el primer error.
 * Se recorren TODAS las filas y se reportan TODOS los errores encontrados.
 * Esto permite que el usuario vea TODO lo que hay que corregir de una vez.
 * 
 * @see ExcelReaderService (leer archivo)
 * @see AlumnoValidadorService (validar datos)
 * @see AlumnoValidadoDTO (DTO de salida)
 * @see ExcelValidacionResponse (respuesta final)
 */
public interface ExcelValidacionYOrdenaService {

    /**
     * Valida, limpia y ordena un archivo Excel de alumnos.
     * 
     * Este es el método principal que ejecuta el Endpoint 1 completo.
     * 
     * PRECONDICIONES:
     * - El archivo existe y es válido (multipart)
     * - El semestreId existe en BD
     * 
     * PROCESO:
     * 1. Leer Excel
     * 2. Validar estructura (headers)
     * 3. Validar datos (para CADA fila)
     * 4. Recolectar TODOS los errores
     * 5. Limpiar y normalizar datos válidos
     * 6. Ordenar por semestre
     * 7. Convertir a DTOs
     * 8. Construir respuesta
     * 
     * RESPUESTA SI HAY ERRORES:
     * {
     *   "status": "ERROR",
     *   "message": "Se encontraron 5 errores de validación",
     *   "totalFilas": 100,
     *   "totalValidas": 95,
     *   "totalErrores": 5,
     *   "porcentajeExito": 95.0,
     *   "errors": [ { filaExcel, campo, valor, descripcion }, ... ],
     *   "data": null
     * }
     * 
     * RESPUESTA SI NO HAY ERRORES:
     * {
     *   "status": "OK",
     *   "message": "Validación completada exitosamente",
     *   "totalFilas": 100,
     *   "totalValidas": 100,
     *   "totalErrores": 0,
     *   "porcentajeExito": 100.0,
     *   "errors": [],
     *   "data": [ AlumnoValidadoDTO[], ... ]  ← ORDENADO por semestre
     * }
     * 
     * @param archivo Excel con datos de alumnos
     * @param semestreId ID del semestre (para validación cruzada)
     * @return ExcelValidacionResponse con status, errores y data
     * @throws IllegalArgumentException si semestreId es inválido
     * @throws ExcelFormatoException si Excel tiene formato inválido
     */
    ExcelValidacionResponse validarYProcesarExcel(
            MultipartFile archivo,
            Long semestreId);

    /**
     * Limpia datos de alumno Excel (normalize, trim, uppercase).
     * 
     * Operaciones:
     * - Trim en strings
     * - Uppercase en campos de código (matricula, carrera)
     * - Validar formato de datos
     * - Normalizar referencias
     * 
     * @param alumnos Lista bruta desde Excel
     * @return Lista limpia y normalizada
     */
    void limpiarDatos(java.util.List<com.universidad.tutorias.application.dto.AlumnoExcelDTO> alumnos);

    /**
     * Ordena alumnos por semestre para asignación correcta.
     * 
     * ORDEN DE PRIORIDAD:
     * Semestres MAYORES primero (8°, 7°, 6°, etc.)
     * Semestres MENORES después (3°, 2°, 1°)
     * Nuevo ingreso (sin semestre o semestre 1) AL FINAL
     * 
     * RAZÓN:
     * Liberar cupos en semestres avanzados PRIMERO.
     * Luego, con cupos liberados, asignar nuevo ingreso.
     * Esto maximiza éxito de asignación.
     * 
     * IMPLEMENTAR:
     * - Extraer número de semestre (1-12)
     * - Asignar ordenPriority: 12-semestreNumerico
     * - Ordenar DESC por ordenPriority
     * 
     * @param alumnos Lista de alumnos validados
     * @return Lista ORDENADA con ordenPriority asignado
     */
    java.util.List<AlumnoValidadoDTO> ordenarPorSemestre(
            java.util.List<AlumnoValidadoDTO> alumnos);

    /**
     * Convierte AlumnoExcelDTO a AlumnoValidadoDTO (con datos limpiados).
     * 
     * Agrega campos faltantes:
     * - semestreId (buscado en BD)
     * - semestreCodigo (derivado de semestreId)
     * - semestreNumerico (1-12)
     * - ordenPriority (para ordenamiento)
     * - validado: true
     * 
     * @param alumnosExcel Lista de Excel
     * @param semestreId ID del semestre
     * @return Lista de AlumnoValidadoDTO
     */
    java.util.List<AlumnoValidadoDTO> convertirADtos(
            java.util.List<com.universidad.tutorias.application.dto.AlumnoExcelDTO> alumnosExcel,
            Long semestreId);

}
