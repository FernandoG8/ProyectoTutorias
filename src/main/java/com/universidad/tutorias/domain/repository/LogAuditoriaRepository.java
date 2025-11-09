package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.domain.entity.LogAuditoria;
import com.universidad.tutorias.domain.enums.TipoAccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {

    @Query("SELECT l FROM LogAuditoria l WHERE l.proceso.id = :procesoId ORDER BY l.fechaRegistro DESC")
    List<LogAuditoria> findByProcesoIdOrderByFechaDesc(@Param("procesoId") Long procesoId);

    @Query("SELECT l FROM LogAuditoria l WHERE l.entidad = :entidad AND l.idEntidad = :idEntidad ORDER BY l.fechaRegistro DESC")
    List<LogAuditoria> findByEntidadAndId(@Param("entidad") String entidad,
                                          @Param("idEntidad") Long idEntidad);

    @Query("SELECT l FROM LogAuditoria l WHERE l.tipoAccion = :tipo ORDER BY l.fechaRegistro DESC")
    List<LogAuditoria> findByTipoAccion(@Param("tipo") TipoAccion tipo);
}