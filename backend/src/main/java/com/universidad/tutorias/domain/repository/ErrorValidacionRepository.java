package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.domain.entity.ErrorValidacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorValidacionRepository extends JpaRepository<ErrorValidacion, Long> {

    @Query("SELECT e FROM ErrorValidacion e WHERE e.proceso.id = :procesoId ORDER BY e.filaExcel")
    List<ErrorValidacion> findByProcesoId(@Param("procesoId") Long procesoId);

    @Query("SELECT COUNT(e) FROM ErrorValidacion e WHERE e.proceso.id = :procesoId")
    int countByProcesoId(@Param("procesoId") Long procesoId);
}