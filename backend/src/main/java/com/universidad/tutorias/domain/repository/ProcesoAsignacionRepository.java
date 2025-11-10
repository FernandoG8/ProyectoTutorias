package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.domain.entity.ProcesoAsignacion;
import com.universidad.tutorias.domain.enums.EstadoProceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcesoAsignacionRepository extends JpaRepository<ProcesoAsignacion, Long> {

    @Query("SELECT p FROM ProcesoAsignacion p ORDER BY p.fechaInicio DESC")
    List<ProcesoAsignacion> findAllOrderByFechaDesc();

    @Query("SELECT p FROM ProcesoAsignacion p WHERE p.estado = :estado")
    List<ProcesoAsignacion> findByEstado(@Param("estado") EstadoProceso estado);

    @Query("SELECT p FROM ProcesoAsignacion p WHERE p.usuarioEjecutor = :usuario ORDER BY p.fechaInicio DESC")
    List<ProcesoAsignacion> findByUsuario(@Param("usuario") String usuario);
}