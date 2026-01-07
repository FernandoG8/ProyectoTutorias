package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.domain.entity.TutorCambioAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para acceder a registros de cambios de tutor en auditoría.
 * 
 * Permite consultar histórico de cambios manuales de tutores,
 * sin afectar la tabla principal de asignaciones.
 */
@Repository
public interface TutorCambioAuditoriaRepository extends JpaRepository<TutorCambioAuditoria, Long> {

    /**
     * Obtiene TODOS los cambios de tutor para una asignación específica.
     * 
     * @param asignacionId ID de la asignación
     * @return Lista de cambios en orden cronológico (más antiguo primero)
     */
    @Query("SELECT tca FROM TutorCambioAuditoria tca " +
           "WHERE tca.asignacion.id = :asignacionId " +
           "ORDER BY tca.fechaHoraCambio ASC")
    List<TutorCambioAuditoria> findByAsignacionId(@Param("asignacionId") Long asignacionId);

    /**
     * Obtiene TODOS los cambios de tutor para un alumno específico.
     * 
     * Útil para ver el historial completo de cambios de un alumno
     * sin importar cuántas asignaciones tenga.
     * 
     * @param alumnoId ID del alumno
     * @return Lista de cambios en orden cronológico (más antiguo primero)
     */
    @Query("SELECT tca FROM TutorCambioAuditoria tca " +
           "WHERE tca.asignacion.alumno.id = :alumnoId " +
           "ORDER BY tca.fechaHoraCambio DESC")
    List<TutorCambioAuditoria> findByAlumnoId(@Param("alumnoId") Long alumnoId);

    /**
     * Obtiene TODOS los cambios registrados por un tutor.
     * 
     * Útil para ver a quién se le ha asignado un tutor después del cambio.
     * 
     * @param tutorId ID del tutor nuevo
     * @return Lista de cambios donde este tutor fue asignado
     */
    @Query("SELECT tca FROM TutorCambioAuditoria tca " +
           "WHERE tca.tutorNuevo.id = :tutorId " +
           "ORDER BY tca.fechaHoraCambio DESC")
    List<TutorCambioAuditoria> findByTutorNuevoId(@Param("tutorId") Long tutorId);

    /**
     * Obtiene TODOS los cambios de un tutor anterior.
     * 
     * Útil para ver por qué se le removieron alumnos.
     * 
     * @param tutorId ID del tutor anterior
     * @return Lista de cambios donde este tutor fue removido
     */
    @Query("SELECT tca FROM TutorCambioAuditoria tca " +
           "WHERE tca.tutorAnterior.id = :tutorId " +
           "ORDER BY tca.fechaHoraCambio DESC")
    List<TutorCambioAuditoria> findByTutorAnteriorId(@Param("tutorId") Long tutorId);

    /**
     * Obtiene cambios dentro de un rango de fechas.
     * 
     * Útil para análisis de cambios en períodos específicos.
     * 
     * @param inicio Fecha/hora inicio (inclusive)
     * @param fin Fecha/hora fin (inclusive)
     * @return Lista de cambios en ese período
     */
    @Query("SELECT tca FROM TutorCambioAuditoria tca " +
           "WHERE tca.fechaHoraCambio BETWEEN :inicio AND :fin " +
           "ORDER BY tca.fechaHoraCambio DESC")
    List<TutorCambioAuditoria> findByFechaRango(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    /**
     * Obtiene cambios registrados por un usuario específico.
     * 
     * Útil para auditar qué cambios hizo cada admin/usuario.
     * 
     * @param usuario Nombre del usuario/responsable
     * @return Lista de cambios realizados por este usuario
     */
    @Query("SELECT tca FROM TutorCambioAuditoria tca " +
           "WHERE tca.usuarioResponsable = :usuario " +
           "ORDER BY tca.fechaHoraCambio DESC")
    List<TutorCambioAuditoria> findByUsuarioResponsable(@Param("usuario") String usuario);

    /**
     * Obtiene cambios de un tipo específico.
     * 
     * @param tipoCambio Tipo (MANUAL, SISTEMA, AUTORIZADO, etc)
     * @return Lista de cambios de ese tipo
     */
    @Query("SELECT tca FROM TutorCambioAuditoria tca " +
           "WHERE tca.tipoCambio = :tipoCambio " +
           "ORDER BY tca.fechaHoraCambio DESC")
    List<TutorCambioAuditoria> findByTipoCambio(@Param("tipoCambio") String tipoCambio);

}
