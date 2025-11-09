package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.domain.entity.AlumnoInactivo;
import com.universidad.tutorias.domain.enums.MotivoInactividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlumnoInactivoRepository extends JpaRepository<AlumnoInactivo, Long> {

    @Query("""
            SELECT ai FROM AlumnoInactivo ai
            JOIN FETCH ai.alumno a
            LEFT JOIN FETCH ai.tutorPreservado tp
            WHERE ai.motivoInactividad = 'SIN_DEFINIR'
              AND (:carrera IS NULL OR a.carrera = :carrera)
              AND (:semestre IS NULL OR a.semestre = :semestre)
              AND (:motivo IS NULL OR ai.motivoInactividad = :motivo)
            ORDER BY ai.fechaInactividad DESC
            """)
    List<AlumnoInactivo> findPendientesDeResolucion(@Param("carrera") String carrera,
                                                    @Param("semestre") Integer semestre,
                                                    @Param("motivo") MotivoInactividad motivo);

    @Query("""
            SELECT ai FROM AlumnoInactivo ai
            JOIN FETCH ai.alumno a
            LEFT JOIN FETCH ai.tutorPreservado tp
            WHERE (:motivo IS NULL OR ai.motivoInactividad = :motivo)
              AND (:carrera IS NULL OR a.carrera = :carrera)
              AND (:semestre IS NULL OR a.semestre = :semestre)
            ORDER BY ai.fechaInactividad DESC
            """)
    List<AlumnoInactivo> findAllWithFilters(@Param("carrera") String carrera,
                                            @Param("semestre") Integer semestre,
                                            @Param("motivo") MotivoInactividad motivo);

    @Query("SELECT ai FROM AlumnoInactivo ai WHERE ai.alumno.id = :alumnoId")
    Optional<AlumnoInactivo> findByAlumnoId(@Param("alumnoId") Long alumnoId);

    @Query("SELECT ai FROM AlumnoInactivo ai JOIN FETCH ai.tutorPreservado WHERE ai.cupoLiberado = true")
    List<AlumnoInactivo> findConCupoLiberado();

    @Query("SELECT ai FROM AlumnoInactivo ai WHERE ai.motivoInactividad IN :motivos")
    List<AlumnoInactivo> findByMotivoIn(@Param("motivos") List<MotivoInactividad> motivos);
}