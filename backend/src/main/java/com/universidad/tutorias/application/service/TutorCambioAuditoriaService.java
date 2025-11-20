package com.universidad.tutorias.application.service;

import com.universidad.tutorias.application.dto.TutorCambioAuditoriaDTO;
import com.universidad.tutorias.domain.entity.Asignacion;
import com.universidad.tutorias.domain.entity.Tutor;
import com.universidad.tutorias.domain.entity.TutorCambioAuditoria;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestionar auditoría de cambios de tutor.
 * 
 * RESPONSABILIDAD:
 * Registrar TODOS los cambios manuales de tutor en una tabla de auditoría
 * sin modificar el tipo_asignacion en la tabla principal de asignaciones.
 * 
 * FLUJO:
 * 1. Usuario solicita cambio de tutor para alumno
 * 2. Sistema valida que nuevo tutor está disponible
 * 3. Registra cambio en TutorCambioAuditoria
 * 4. Actualiza Asignacion.tutor (tipo_asignacion sigue igual)
 * 5. Retorna DTO con detalles de lo que se cambió
 * 
 * AUDITORÍA COMPLETA:
 * - Tutor anterior (quién la tenía)
 * - Tutor nuevo (quién la tiene ahora)
 * - Usuario responsable del cambio
 * - Fecha y hora exacta
 * - Motivo del cambio
 * - Tipo de cambio (MANUAL, SISTEMA, etc)
 * - Notas adicionales
 */
public interface TutorCambioAuditoriaService {

    /**
     * Registra un cambio de tutor en auditoría.
     * 
     * IMPORTANTE:
     * - NO modifica tipo_asignacion (se mantiene NUEVO_INGRESO o REINGRESO)
     * - Solo registra cambio de tutor
     * - Mantiene histórico completo
     * 
     * @param asignacion Asignación que será modificada
     * @param tutorNuevo Nuevo tutor a asignar
     * @param usuarioResponsable Usuario que hace el cambio
     * @param motivo Motivo del cambio (ej: "Solicitud del alumno")
     * @return DTO con detalles del cambio registrado
     */
    TutorCambioAuditoriaDTO registrarCambio(
            Asignacion asignacion,
            Tutor tutorNuevo,
            String usuarioResponsable,
            String motivo);

    /**
     * Registra un cambio con más detalles.
     * 
     * @param asignacion Asignación a modificar
     * @param tutorNuevo Nuevo tutor
     * @param usuarioResponsable Usuario del cambio
     * @param motivo Motivo
     * @param tipoCambio Tipo: MANUAL, SISTEMA, AUTORIZADO, etc
     * @param notas Notas adicionales
     * @return DTO del cambio
     */
    TutorCambioAuditoriaDTO registrarCambioDetallado(
            Asignacion asignacion,
            Tutor tutorNuevo,
            String usuarioResponsable,
            String motivo,
            String tipoCambio,
            String notas);

    /**
     * Obtiene historial completo de cambios para una asignación.
     * 
     * @param asignacionId ID de la asignación
     * @return Lista de cambios en orden cronológico (más antiguo primero)
     */
    List<TutorCambioAuditoriaDTO> obtenerHistorialPorAsignacion(Long asignacionId);

    /**
     * Obtiene todos los cambios realizados a un alumno.
     * 
     * Útil para ver el histórico completo de cambios de tutor del alumno,
     * sin importar cuántas asignaciones tenga.
     * 
     * @param alumnoId ID del alumno
     * @return Lista de cambios en orden cronológico (más reciente primero)
     */
    List<TutorCambioAuditoriaDTO> obtenerHistorialPorAlumno(Long alumnoId);

    /**
     * Obtiene cambios donde un tutor fue asignado.
     * 
     * Útil para ver a quién se le asignó un tutor después del cambio.
     * 
     * @param tutorId ID del tutor nuevo
     * @return Lista de cambios
     */
    List<TutorCambioAuditoriaDTO> obtenerAsignacionesPorTutor(Long tutorId);

    /**
     * Obtiene cambios donde un tutor fue removido.
     * 
     * Útil para ver por qué se le sacaron alumnos a un tutor.
     * 
     * @param tutorId ID del tutor anterior
     * @return Lista de cambios
     */
    List<TutorCambioAuditoriaDTO> obtenerRemocionesPorTutor(Long tutorId);

    /**
     * Obtiene cambios en un rango de fechas.
     * 
     * @param inicio Fecha/hora inicio (inclusive)
     * @param fin Fecha/hora fin (inclusive)
     * @return Lista de cambios en ese período
     */
    List<TutorCambioAuditoriaDTO> obtenerCambiosPorFecha(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene cambios realizados por un usuario específico.
     * 
     * Útil para auditar qué cambios hizo cada admin/usuario.
     * 
     * @param usuario Nombre del usuario/responsable
     * @return Lista de cambios
     */
    List<TutorCambioAuditoriaDTO> obtenerCambiosPorUsuario(String usuario);

    /**
     * Obtiene cambios de un tipo específico.
     * 
     * @param tipoCambio Tipo: MANUAL, SISTEMA, AUTORIZADO, etc
     * @return Lista de cambios
     */
    List<TutorCambioAuditoriaDTO> obtenerCambiosPorTipo(String tipoCambio);

}
