package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.domain.entity.Usuario;
import com.universidad.tutorias.domain.entity.UsuarioGoogleLink;
import com.universidad.tutorias.domain.repository.UsuarioGoogleLinkRepository;
import com.universidad.tutorias.infrastructure.security.oauth.OAuth2Flow;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth/google")
@RequiredArgsConstructor
public class AuthGoogleController {

    private final UsuarioGoogleLinkRepository usuarioGoogleLinkRepository;

    @GetMapping("/link")
    public void iniciarVinculacion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(OAuth2Flow.FLOW_ATTR, OAuth2Flow.FLOW_LINK);
        session.setAttribute(OAuth2Flow.LINK_USER_ID_ATTR, usuario.getId());

        response.sendRedirect("/oauth2/authorization/google");
    }

    @GetMapping("/connect-drive")
    public void conectarDrive(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(OAuth2Flow.FLOW_ATTR, OAuth2Flow.FLOW_DRIVE_CONNECT);
        session.setAttribute(OAuth2Flow.LINK_USER_ID_ATTR, usuario.getId());

        response.sendRedirect("/oauth2/authorization/google");
    }

    @PostMapping("/disconnect-drive")
    public ResponseEntity<Void> desconectarDrive() {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).build();
        }

        usuarioGoogleLinkRepository.findByUsuarioId(usuario.getId()).ifPresent(link -> {
            link.setGoogleRefreshTokenEnc(null);
            link.setGoogleRefreshTokenIv(null);
            link.setTokenUpdatedAt(null);
            usuarioGoogleLinkRepository.save(link);
        });

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> estadoVinculacion() {
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).build();
        }

        Optional<UsuarioGoogleLink> link = usuarioGoogleLinkRepository.findByUsuarioId(usuario.getId());
        Map<String, Object> body = new HashMap<>();
        body.put("linked", link.isPresent());
        link.map(UsuarioGoogleLink::getGoogleEmail).ifPresent(email -> body.put("googleEmail", email));
        body.put("driveConnected", link.map(l -> l.getGoogleRefreshTokenEnc() != null).orElse(false));

        return ResponseEntity.ok(body);
    }

    private Usuario obtenerUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Usuario usuario) {
            return usuario;
        }
        if (principal instanceof UserDetails userDetails) {
            if (userDetails instanceof Usuario usuarioDetalle) {
                return usuarioDetalle;
            }
        }
        return null;
    }
}
