
package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.domain.entity.Alumno;
import com.universidad.tutorias.domain.enums.EstadoAlumno;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlumnoRepository extends JpaRepository<Alumno, Long>, JpaSpecificationExecutor<Alumno> {

    @Query("SELECT a FROM Alumno a LEFT JOIN FETCH a.tutorActual WHERE a.estado = :estado")
    List<Alumno> findByEstadoWithTutor(@Param("estado") EstadoAlumno estado);

    Optional<Alumno> findByMatricula(String matricula);

    @Query("SELECT a FROM Alumno a WHERE a.estado = :estado AND a.carrera = :carrera")
    List<Alumno> findByEstadoAndCarrera(@Param("estado") EstadoAlumno estado,
                                        @Param("carrera") String carrera);

    boolean existsByMatricula(String matricula);

    @Query("SELECT COUNT(a) FROM Alumno a WHERE a.tutorActual.id = :tutorId AND a.estado = 'ACTIVO'")
    int contarAlumnosActivosPorTutor(@Param("tutorId") Long tutorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Alumno a WHERE a.id = :id")
    Optional<Alumno> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT a FROM Alumno a WHERE a.matricula IN :matriculas")
    List<Alumno> findByMatriculaIn(@Param("matriculas") List<String> matriculas);

    boolean existsByCarreraIgnoreCase(String carrera);

    @Query("SELECT DISTINCT UPPER(a.carrera) FROM Alumno a")
    List<String> findDistinctCarreras();
}