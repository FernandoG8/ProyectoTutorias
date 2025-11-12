package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.domain.entity.Tutor;
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
public interface TutorRepository extends JpaRepository<Tutor, Long>, JpaSpecificationExecutor<Tutor> {

    @Query("SELECT t FROM Tutor t WHERE t.activo = true AND t.cargaActual < t.capacidadMax ORDER BY t.cargaActual ASC")
    List<Tutor> findDisponibles();

    @Query("SELECT t FROM Tutor t WHERE t.activo = true AND t.carrera = :carrera AND t.cargaActual < t.capacidadMax ORDER BY t.cargaActual ASC")
    List<Tutor> findDisponiblesByCarrera(@Param("carrera") String carrera);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Tutor t WHERE t.id = :id")
    Optional<Tutor> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT t FROM Tutor t LEFT JOIN FETCH t.alumnosAsignados WHERE t.id = :id")
    Optional<Tutor> findByIdWithAlumnos(@Param("id") Long id);

    @Query("SELECT t FROM Tutor t WHERE t.activo = true ORDER BY t.carrera, t.nombre")
    List<Tutor> findAllActivos();

    @Query("SELECT t.id FROM Tutor t")
    List<Long> findAllIds();
}