package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.domain.entity.MatrizAfinidadCarrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatrizAfinidadCarreraRepository extends JpaRepository<MatrizAfinidadCarrera, Long> {

    @Query("SELECT m FROM MatrizAfinidadCarrera m WHERE m.carreraOrigen = :carrera ORDER BY m.prioridad ASC")
    List<MatrizAfinidadCarrera> findByCarreraOrigenOrderByPrioridad(@Param("carrera") String carrera);

    @Query("SELECT m.carreraCompatible FROM MatrizAfinidadCarrera m " +
            "WHERE m.carreraOrigen = :carrera AND m.carreraCompatible <> :carrera " +
            "ORDER BY m.prioridad ASC")
    List<String> findCarrerasCompatibles(@Param("carrera") String carrera);
}