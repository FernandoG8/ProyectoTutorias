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

    @Query("SELECT a FROM Asignacion a WHERE a.tutor.id = :tutorId AND a.semestreAcademico = :semestre")
    List<Asignacion> findByTutorAndSemestre(@Param("tutorId") Long tutorId,
                                            @Param("semestre") String semestre);

    @Query("SELECT a FROM Asignacion a JOIN FETCH a.alumno JOIN FETCH a.tutor " +
            "WHERE a.tutor.id = :tutorId " +
            "ORDER BY a.alumno.matricula")
    List<Asignacion> findByTutorIdWithDetalles(@Param("tutorId") Long tutorId);

    @Query("SELECT a FROM Asignacion a JOIN FETCH a.alumno JOIN FETCH a.tutor " +
            "WHERE UPPER(a.alumno.carrera) = :carrera AND a.semestreAcademico = :semestre " +
            "ORDER BY a.tutor.nombre, a.alumno.matricula")
    List<Asignacion> findByCarreraAndSemestre(@Param("carrera") String carrera,
                                              @Param("semestre") String semestre);

    @Query("SELECT a FROM Asignacion a JOIN FETCH a.alumno JOIN FETCH a.tutor " +
            "WHERE UPPER(a.alumno.carrera) = :carrera " +
            "ORDER BY a.semestreAcademico DESC, a.tutor.nombre, a.alumno.matricula")
    List<Asignacion> findByCarrera(@Param("carrera") String carrera);

    @Query("SELECT a FROM Asignacion a JOIN FETCH a.alumno JOIN FETCH a.tutor " +
            "WHERE a.semestreAcademico = :semestre " +
            "ORDER BY a.alumno.carrera, a.tutor.nombre, a.alumno.matricula")
    List<Asignacion> findBySemestre(@Param("semestre") String semestre);

    @Query("SELECT DISTINCT UPPER(a.alumno.carrera) FROM Asignacion a " +
            "WHERE (:semestre IS NULL OR a.semestreAcademico = :semestre)")
    List<String> findCarrerasDisponibles(@Param("semestre") String semestre);
}