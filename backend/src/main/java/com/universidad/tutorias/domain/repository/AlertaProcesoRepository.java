package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.domain.entity.AlertaProceso;
import com.universidad.tutorias.domain.enums.SeveridadAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaProcesoRepository extends JpaRepository<AlertaProceso, Long> {

    @Query("SELECT a FROM AlertaProceso a WHERE a.proceso.id = :procesoId ORDER BY a.severidad, a.fechaCreacion DESC")
    List<AlertaProceso> findByProcesoId(@Param("procesoId") Long procesoId);

    @Query("SELECT a FROM AlertaProceso a WHERE a.proceso.id = :procesoId AND a.severidad = :severidad")
    List<AlertaProceso> findByProcesoIdAndSeveridad(@Param("procesoId") Long procesoId,
                                                    @Param("severidad") SeveridadAlerta severidad);

    @Query("SELECT a FROM AlertaProceso a WHERE a.resuelta = false ORDER BY a.severidad, a.fechaCreacion DESC")
    List<AlertaProceso> findNoResueltas();

    @Query("SELECT COUNT(a) FROM AlertaProceso a WHERE a.proceso.id = :procesoId AND a.severidad = :severidad")
    int countByProcesoIdAndSeveridad(@Param("procesoId") Long procesoId,
                                     @Param("severidad") SeveridadAlerta severidad);
}