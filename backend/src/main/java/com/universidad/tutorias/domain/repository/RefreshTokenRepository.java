// ============================================
// REPOSITORIO - REFRESH TOKENS
// ============================================

package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.domain.entity.RefreshToken;
import com.universidad.tutorias.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUsuario(Usuario usuario);
}
