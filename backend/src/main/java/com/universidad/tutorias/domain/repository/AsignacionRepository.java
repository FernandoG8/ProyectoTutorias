package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.domain.entity.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {

    @Query("SELECT a FROM Asignacion a WHERE a.alumno.id = :alumnoId ORDER BY a.fechaAsignacion DESC")
    List<Asignacion> findByAlumnoId(@Param("alumnoId") Long alumnoId);

    @Query("SELECT a FROM Asignacion a WHERE a.alumno.id = :alumnoId AND a.semestreAcademico = :semestre")
    Optional<Asignacion> findByAlumnoAndSemestreAcademico(@Param("alumnoId") Long alumnoId,
                                                          @Param("semestre") String semestre);

    @Query("SELECT a FROM Asignacion a WHERE a.alumno.id = :alumnoId AND a.semestre.id = :semestreId")
    Optional<Asignacion> findByAlumnoAndSemestreId(@Param("alumnoId") Long alumnoId,
                                                    @Param("semestreId") Long semestreId);

    @Query("SELECT a FROM Asignacion a WHERE a.tutor.id = :tutorId AND a.semestre.id = :semestreId")
    List<Asignacion> findByTutorAndSemestre(@Param("tutorId") Long tutorId,
                                            @Param("semestreId") Long semestreId);

    @Query("SELECT a FROM Asignacion a WHERE a.tutor.id = :tutorId AND a.semestreAcademico = :semestre")
    List<Asignacion> findByTutorAndSemestreString(@Param("tutorId") Long tutorId,
                                                   @Param("semestre") String semestre);

    @Query("SELECT a FROM Asignacion a JOIN FETCH a.alumno JOIN FETCH a.tutor " +
            "WHERE a.tutor.id = :tutorId " +
            "ORDER BY a.alumno.matricula")
    List<Asignacion> findByTutorIdWithDetalles(@Param("tutorId") Long tutorId);

    @Query("SELECT a FROM Asignacion a JOIN FETCH a.alumno JOIN FETCH a.tutor " +
            "WHERE UPPER(a.alumno.carrera) = :carrera AND a.semestre.id = :semestreId " +
            "ORDER BY a.tutor.nombre, a.alumno.matricula")
    List<Asignacion> findByCarreraAndSemestre(@Param("carrera") String carrera,
                                              @Param("semestreId") Long semestreId);

    @Query("SELECT a FROM Asignacion a JOIN FETCH a.alumno JOIN FETCH a.tutor " +
            "WHERE UPPER(a.alumno.carrera) = :carrera AND a.semestreAcademico = :semestre " +
            "ORDER BY a.tutor.nombre, a.alumno.matricula")
    List<Asignacion> findByCarreraAndSemestreString(@Param("carrera") String carrera,
                                                     @Param("semestre") String semestre);

    @Query("SELECT a FROM Asignacion a JOIN FETCH a.alumno JOIN FETCH a.tutor " +
            "WHERE UPPER(a.alumno.carrera) = :carrera " +
            "ORDER BY a.semestre.codigo DESC, a.tutor.nombre, a.alumno.matricula")
    List<Asignacion> findByCarrera(@Param("carrera") String carrera);

    @Query("SELECT a FROM Asignacion a JOIN FETCH a.alumno JOIN FETCH a.tutor " +
            "WHERE a.semestre.id = :semestreId " +
            "ORDER BY a.alumno.carrera, a.tutor.nombre, a.alumno.matricula")
    List<Asignacion> findBySemestreId(@Param("semestreId") Long semestreId);

    @Query("SELECT a FROM Asignacion a JOIN FETCH a.alumno JOIN FETCH a.tutor " +
            "WHERE a.semestreAcademico = :semestre " +
            "ORDER BY a.alumno.carrera, a.tutor.nombre, a.alumno.matricula")
    List<Asignacion> findBySemestreString(@Param("semestre") String semestre);

    @Query("SELECT DISTINCT UPPER(a.alumno.carrera) FROM Asignacion a " +
            "WHERE (:semestreId IS NULL OR a.semestre.id = :semestreId)")
    List<String> findCarrerasDisponiblesBySemestreId(@Param("semestreId") Long semestreId);

    @Query("SELECT DISTINCT UPPER(a.alumno.carrera) FROM Asignacion a " +
            "WHERE (:semestre IS NULL OR a.semestreAcademico = :semestre)")
    List<String> findCarrerasDisponibles(@Param("semestre") String semestre);

    /**
     * Devuelve IDs de tutores con asignaciones para un semestre dado.
     * Si semestre es null, devuelve todos los tutores con al menos una asignación histórica.
     */
    @Query("""
            SELECT DISTINCT a.tutor.id FROM Asignacion a
            WHERE (:semestre IS NULL OR a.semestreAcademico = :semestre)
            """)
    List<Long> findTutorIdsBySemestre(@Param("semestre") String semestre);

    @Query("SELECT COUNT(DISTINCT a.alumno.id) FROM Asignacion a WHERE a.tutor.id = :tutorId AND a.alumno.estado = 'ACTIVO'")
    int countByTutorId(@Param("tutorId") Long tutorId);

    @Query("SELECT COUNT(a) FROM Asignacion a WHERE a.semestre.id = :semestreId")
    Long countBySemestreId(@Param("semestreId") Long semestreId);

    @Query("SELECT COUNT(a) FROM Asignacion a WHERE a.semestre.id = :semestreId AND a.alumno.estado = 'ACTIVO'")
    Long countActivosBySemestreId(@Param("semestreId") Long semestreId);

    @Query("SELECT a FROM Asignacion a WHERE a.semestre.id = :semestreId AND a.tutor.id = :tutorId")
    List<Asignacion> findBySemestreIdAndTutorId(@Param("semestreId") Long semestreId,
                                                @Param("tutorId") Long tutorId);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Asignacion a " +
            "WHERE a.alumno.id = :alumnoId AND a.tutor.id = :tutorId AND a.semestre.id = :semestreId")
    boolean existsByAlumnoAndTutorAndSemestreId(@Param("alumnoId") Long alumnoId,
                                                @Param("tutorId") Long tutorId,
                                                @Param("semestreId") Long semestreId);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Asignacion a " +
            "WHERE a.alumno.id = :alumnoId AND a.tutor.id = :tutorId AND a.semestreAcademico = :semestre")
    boolean existsByAlumnoAndTutorAndSemestre(@Param("alumnoId") Long alumnoId,
                                              @Param("tutorId") Long tutorId,
                                              @Param("semestre") String semestre);

    @Deprecated
    @Query("SELECT DISTINCT a.semestre.codigo FROM Asignacion a ORDER BY a.semestre.codigo DESC")
    List<String> findDistinctSemestreAcademico();

    /**
     * Obtiene asignaciones de un tutor excluyendo alumnos egresados o con baja definitiva.
     */
    @Query("""
            SELECT a FROM Asignacion a
            JOIN FETCH a.alumno
            JOIN FETCH a.tutor
            WHERE a.tutor.id = :tutorId
            AND NOT EXISTS (SELECT 1 FROM AlumnoEgresado ae WHERE ae.alumno.id = a.alumno.id)
            AND NOT EXISTS (SELECT 1 FROM AlumnoBajaDefinitiva abd WHERE abd.alumno.id = a.alumno.id)
            ORDER BY a.alumno.matricula
            """)
    List<Asignacion> findByTutorIdExcludingResolved(@Param("tutorId") Long tutorId);

    /**
     * Cuenta asignaciones de un tutor con alumnos activos que no sean egresados ni baja definitiva.
     */
    @Query("""
            SELECT COUNT(DISTINCT a.alumno.id) FROM Asignacion a
            WHERE a.tutor.id = :tutorId
            AND a.alumno.estado = 'ACTIVO'
            AND NOT EXISTS (SELECT 1 FROM AlumnoEgresado ae WHERE ae.alumno.id = a.alumno.id)
            AND NOT EXISTS (SELECT 1 FROM AlumnoBajaDefinitiva abd WHERE abd.alumno.id = a.alumno.id)
            """)
    int countByTutorIdExcludingResolved(@Param("tutorId") Long tutorId);

    /**
     * Obtiene asignaciones por carrera y semestre excluyendo alumnos egresados o baja definitiva.
     */
    @Query("""
            SELECT a FROM Asignacion a
            JOIN FETCH a.alumno
            JOIN FETCH a.tutor
            WHERE UPPER(a.alumno.carrera) = :carrera
            AND a.semestre.id = :semestreId
            AND NOT EXISTS (SELECT 1 FROM AlumnoEgresado ae WHERE ae.alumno.id = a.alumno.id)
            AND NOT EXISTS (SELECT 1 FROM AlumnoBajaDefinitiva abd WHERE abd.alumno.id = a.alumno.id)
            ORDER BY a.tutor.nombre, a.alumno.matricula
            """)
    List<Asignacion> findByCarreraAndSemestreExcludingResolved(@Param("carrera") String carrera,
                                                                @Param("semestreId") Long semestreId);

    /**
     * Cuenta asignaciones por semestre excluyendo alumnos resueltos.
     */
    @Query("""
            SELECT COUNT(a) FROM Asignacion a
            WHERE a.semestre.id = :semestreId
            AND NOT EXISTS (SELECT 1 FROM AlumnoEgresado ae WHERE ae.alumno.id = a.alumno.id)
            AND NOT EXISTS (SELECT 1 FROM AlumnoBajaDefinitiva abd WHERE abd.alumno.id = a.alumno.id)
            """)
    Long countBySemestreIdExcludingResolved(@Param("semestreId") Long semestreId);

    /**
     * Cuenta asignaciones activas por semestre excluyendo alumnos resueltos.
     */
    @Query("""
            SELECT COUNT(a) FROM Asignacion a
            WHERE a.semestre.id = :semestreId
            AND a.alumno.estado = 'ACTIVO'
            AND NOT EXISTS (SELECT 1 FROM AlumnoEgresado ae WHERE ae.alumno.id = a.alumno.id)
            AND NOT EXISTS (SELECT 1 FROM AlumnoBajaDefinitiva abd WHERE abd.alumno.id = a.alumno.id)
            """)
    Long countActivosBySemestreIdExcludingResolved(@Param("semestreId") Long semestreId);
}
