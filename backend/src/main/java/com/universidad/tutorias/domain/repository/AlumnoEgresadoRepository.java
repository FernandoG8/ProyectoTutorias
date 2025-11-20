package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.domain.entity.AlumnoEgresado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlumnoEgresadoRepository extends JpaRepository<AlumnoEgresado, Long> {
    boolean existsByAlumnoId(Long alumnoId);
}
