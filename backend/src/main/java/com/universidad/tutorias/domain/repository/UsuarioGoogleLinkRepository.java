package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.domain.entity.UsuarioGoogleLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioGoogleLinkRepository extends JpaRepository<UsuarioGoogleLink, Long> {

    Optional<UsuarioGoogleLink> findByGoogleSub(String sub);

    Optional<UsuarioGoogleLink> findByUsuarioId(Long usuarioId);

    boolean existsByGoogleSub(String sub);
}
