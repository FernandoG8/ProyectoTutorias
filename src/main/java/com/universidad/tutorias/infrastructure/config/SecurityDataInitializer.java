package com.universidad.tutorias.infrastructure.config;

import com.universidad.tutorias.domain.entity.Usuario;
import com.universidad.tutorias.domain.enums.RolUsuario;
import com.universidad.tutorias.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityDataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.default-coordinator.username:coord_tutorias}")
    private String defaultUsername;

    @Value("${app.security.default-coordinator.password:123456}")
    private String defaultPassword;

    @Override
    public void run(String... args) {
        usuarioRepository.findByUsername(defaultUsername).ifPresentOrElse(usuario -> {
            if (!usuario.isEnabled()) {
                usuario.setActivo(true);
                usuarioRepository.save(usuario);
                log.info("Usuario coordinador {} reactivado automáticamente", defaultUsername);
            }
        }, () -> {
            Usuario coordinador = Usuario.builder()
                    .username(defaultUsername)
                    .password(passwordEncoder.encode(defaultPassword))
                    .rol(RolUsuario.ROLE_COORDINADOR_TUTORIAS)
                    .activo(true)
                    .build();
            usuarioRepository.save(coordinador);
            log.info("Usuario coordinador por defecto {} creado", defaultUsername);
        });
    }
}
