package com.universidad.tutorias.application.service;

import com.universidad.tutorias.application.dto.EjecutarAsignacionRequest;
import com.universidad.tutorias.application.dto.EjecucionAsignacionResponse;

/**
 * Servicio encargado de orquestar la ejecución pura de asignaciones.
 *
 * RESPONSABILIDADES:
 * - Recibir datos ya validados (del endpoint /validar-excel)
 * - Invocar AsignacionService.asignarAlumnos()
 * - Transformar ResultadoAsignacion a EjecucionAsignacionResponse
 * - Calcular estadísticas y timing
 * - Manejar respuesta final (OK/PARTIAL/ERROR)
 *
 * NO HACE:
 * - Re-validación de Excel
 * - Lectura de archivos
 * - Validación de estructura de datos
 *
 * ENTRADA:
 * - EjecutarAsignacionRequest (ya con datos validados y limpios)
 *
 * SALIDA:
 * - EjecucionAsignacionResponse (con resultado, estadísticas, errores)
 */
public interface EjecucionAsignacionService {

    /**
     * Ejecuta la asignación de alumnos a tutores.
     *
     * Precondiciones:
     * ✓ semestreId es válido (existe en BD)
     * ✓ alumnosValidados no está vacío
     * ✓ Todos los alumnos ya pasaron validación
     * ✓ Los datos ya están limpiados y ordenados
     *
     * Responsabilidades:
     * 1. Crear un ProcesoAsignacion en BD
     * 2. Invocar AsignacionService.asignarAlumnos()
     * 3. Procesar resultado (best-effort, sin parar en errores)
     * 4. Calcular duracionMs, porcentajes, estadísticas
     * 5. Retornar respuesta con status (OK/PARTIAL/ERROR)
     *
     * @param request EjecutarAsignacionRequest con datos validados
     * @return EjecucionAsignacionResponse con resultado de asignación
     */
    EjecucionAsignacionResponse ejecutar(EjecutarAsignacionRequest request);

}
