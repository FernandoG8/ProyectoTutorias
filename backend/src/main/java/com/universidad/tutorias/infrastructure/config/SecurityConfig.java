package com.universidad.tutorias.infrastructure.config;

import com.universidad.tutorias.infrastructure.security.CookieAuthenticationFilter;
import com.universidad.tutorias.infrastructure.security.CustomUserDetailsService;
import com.universidad.tutorias.infrastructure.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CookieAuthenticationFilter cookieAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CRÍTICO: CORS debe ir primero
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Deshabilitar CSRF (usamos cookies HttpOnly + SameSite)
                .csrf(csrf -> csrf.disable())

                // Sin sesiones - stateless
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Manejo de excepciones
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"status\":\"error\",\"code\":\"NO_AUTORIZADO\"," +
                                            "\"message\":\"Acceso no autorizado\"}"
                            );
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"status\":\"error\",\"code\":\"ACCESO_DENEGADO\"," +
                                            "\"message\":\"No tiene permisos para acceder a este recurso\"}"
                            );
                        })
                )

                // Autorización de peticiones
                .authorizeHttpRequests(auth -> auth
                        // Permitir preflight CORS (OPTIONS) sin autenticación
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Endpoints públicos de autenticación
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()

                        // Registro solo para coordinadores
                        .requestMatchers(HttpMethod.POST, "/auth/register").hasRole("COORDINADOR_TUTORIAS")

                        // Endpoints autenticados
                        .requestMatchers(HttpMethod.POST, "/auth/logout").authenticated()
                        .requestMatchers(HttpMethod.GET, "/auth/me").authenticated()

                        // Operaciones especiales
                        .requestMatchers(HttpMethod.POST, "/api/asignaciones/cambio-tutor")
                        .hasRole("COORDINADOR_TUTORIAS")

                        // Todo lo demás requiere autenticación
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )

                // Proveedor de autenticación
                .authenticationProvider(authenticationProvider())

                // Agregar filtro de cookies JWT
                .addFilterBefore(cookieAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}