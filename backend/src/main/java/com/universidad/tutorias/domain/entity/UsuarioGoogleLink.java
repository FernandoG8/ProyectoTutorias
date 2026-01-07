package com.universidad.tutorias.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios_google_link", indexes = {
        @Index(name = "idx_google_sub_unique", columnList = "google_sub", unique = true),
        @Index(name = "idx_google_usuario_unique", columnList = "usuario_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioGoogleLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "google_sub", nullable = false, length = 64, unique = true)
    private String googleSub;

    @Column(name = "google_email", length = 255)
    private String googleEmail;

    @Column(name = "google_refresh_token_enc", length = 512)
    private String googleRefreshTokenEnc;

    @Column(name = "google_refresh_token_iv", length = 64)
    private String googleRefreshTokenIv;

    @Column(name = "token_updated_at")
    private LocalDateTime tokenUpdatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
