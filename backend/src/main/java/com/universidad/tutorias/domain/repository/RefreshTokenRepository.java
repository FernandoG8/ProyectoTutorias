// ============================================
// REPOSITORIO - REFRESH TOKENS
// ============================================

package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.domain.entity.RefreshToken;
import com.universidad.tutorias.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    @Modifying
    @Transactional
    @Query("update RefreshToken rt set rt.revoked = true where rt.usuario = :usuario and rt.revoked = false")
    void revokeAllByUsuario(Usuario usuario);
}
