package com.universidad.tutorias.application.service;

import com.universidad.tutorias.domain.entity.Asignacion;
import com.universidad.tutorias.domain.entity.Semestre;

import java.util.List;
import java.util.Map;

/**
 * Servicio para detectar y limpiar asignaciones corruptas en el sistema.
 *
 * Define las operaciones disponibles para:
 * - Identificar asignaciones problemáticas
 * - Reportar la corrupción
 * - Ejecutar limpieza controlada
 */
public interface CleanupAsignacionService {

    /**
     * Detecta asignaciones corruptas para un semestre específico.
     *
     * @param semestreId ID del semestre a revisar
     * @return Mapa con tipos de corrupción y cantidad de registros afectados
     */
    Map<String, Object> detectarAsignacionesCorruptas(Long semestreId);

    /**
     * Detecta asignaciones corruptas en TODO el sistema.
     *
     * @return Mapa con tipos de corrupción y cantidad de registros afectados
     */
    Map<String, Object> detectarAsignacionesCorruptasGlobal();

    /**
     * Identifica múltiples asignaciones del mismo alumno en el mismo semestre.
     *
     * @param semestreId ID del semestre
     * @return Lista de asignaciones problemáticas
     */
    List<Asignacion> encontrarDuplicadasPorSemestre(Long semestreId);

    /**
     * Identifica asignaciones sin tutor válido.
     *
     * @return Lista de asignaciones sin tutor
     */
    List<Asignacion> encontrarAsignacionesSinTutor();

    /**
     * Identifica asignaciones sin alumno válido.
     *
     * @return Lista de asignaciones sin alumno
     */
    List<Asignacion> encontrarAsignacionesSinAlumno();

    /**
     * Identifica asignaciones sin semestre válido.
     *
     * @return Lista de asignaciones sin semestre
     */
    List<Asignacion> encontrarAsignacionesSinSemestre();

    /**
     * Limpia asignaciones corruptas para un semestre.
     *
     * Elimina:
     * - Asignaciones duplicadas (mantiene la más reciente)
     * - Asignaciones sin referencias válidas
     * - Asignaciones desincronizadas
     *
     * @param semestreId ID del semestre a limpiar
     * @return Mapa con estadísticas de limpieza
     */
    Map<String, Object> limpiarAsignacionesPorSemestre(Long semestreId);

    /**
     * Limpia asignaciones corruptas en TODO el sistema.
     *
     * @return Mapa con estadísticas globales de limpieza
     */
    Map<String, Object> limpiarAsignacionesGlobal();

    /**
     * Recalcula y sincroniza la carga de tutores después de limpieza.
     *
     * @param semestreId ID del semestre (null = todos)
     * @return Mapa con tutores actualizados
     */
    Map<String, Object> recalcularCargaDespuesLimpieza(Long semestreId);
}
