package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.domain.entity.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {

    @Query("SELECT a FROM Asignacion a WHERE a.alumno.id = :alumnoId ORDER BY a.fechaAsignacion DESC")
    List<Asignacion> findByAlumnoId(@Param("alumnoId") Long alumnoId);

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

    @Query("SELECT COUNT(a) FROM Asignacion a WHERE a.tutor.id = :tutorId")
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
}