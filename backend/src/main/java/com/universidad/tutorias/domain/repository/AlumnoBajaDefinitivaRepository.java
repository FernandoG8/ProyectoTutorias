package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.domain.entity.AlumnoBajaDefinitiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlumnoBajaDefinitivaRepository extends JpaRepository<AlumnoBajaDefinitiva, Long> {
    boolean existsByAlumnoId(Long alumnoId);
}
