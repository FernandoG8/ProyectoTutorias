// ============================================
// REPOSITORIO - USUARIOS
// ============================================

package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);
}
