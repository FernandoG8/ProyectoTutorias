package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.domain.entity.Semestre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SemestreRepository extends JpaRepository<Semestre, Long> {

    Optional<Semestre> findByCodigo(String codigo);

    Optional<Semestre> findByActivoTrue();

    List<Semestre> findAllByOrderByFechaInicioDesc();

    List<Semestre> findTop5ByOrderByFechaInicioDesc();

    boolean existsByCodigo(String codigo);

    @Query("SELECT s FROM Semestre s WHERE s.fechaInicio <= :fecha AND s.fechaFin >= :fecha")
    Optional<Semestre> findByFechaVigente(@Param("fecha") LocalDate fecha);

    @Query("SELECT s FROM Semestre s WHERE YEAR(s.fechaInicio) = :anio")
    List<Semestre> findByAnio(@Param("anio") Integer anio);

    @Query("SELECT COUNT(a) FROM Asignacion a WHERE a.semestre.id = :semestreId")
    Long countAsignacionesBySemestreId(@Param("semestreId") Long semestreId);

    @Modifying
    @Query("UPDATE Semestre s SET s.activo = false WHERE s.activo = true")
    void desactivarTodos();
}
