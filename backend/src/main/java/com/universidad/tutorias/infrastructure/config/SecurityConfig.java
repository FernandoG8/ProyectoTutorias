package com.universidad.tutorias.infrastructure.config;

import com.universidad.tutorias.infrastructure.security.CookieAuthenticationFilter;
import com.universidad.tutorias.infrastructure.security.CustomUserDetailsService;
import com.universidad.tutorias.infrastructure.security.JwtProperties;
import com.universidad.tutorias.infrastructure.security.oauth.GoogleAuthorizationRequestResolver;
import com.universidad.tutorias.infrastructure.security.oauth.OAuth2FailureHandler;
import com.universidad.tutorias.infrastructure.security.oauth.OAuth2Properties;
import com.universidad.tutorias.infrastructure.security.oauth.OAuth2SuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({JwtProperties.class, OAuth2Properties.class})
@RequiredArgsConstructor
public class SecurityConfig {

    private final CookieAuthenticationFilter cookieAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final ClientRegistrationRepository clientRegistrationRepository;

    // ✅ Handlers OAuth
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorsConfigurationSource corsConfigurationSource) throws Exception {
        // Usa la configuración CORS centralizada (CorsConfig)
        http.setSharedObject(CorsConfigurationSource.class, corsConfigurationSource);

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"status\":\"error\",\"code\":\"NO_AUTORIZADO\",\"message\":\"Acceso no autorizado\"}"
                            );
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"status\":\"error\",\"code\":\"ACCESO_DENEGADO\",\"message\":\"No tiene permisos para acceder a este recurso\"}"
                            );
                        })
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ OAuth2 endpoints (DEBEN ser públicos)
                        .requestMatchers("/oauth2/**", "/login/oauth2/**", "/error").permitAll()

                        // Públicos actuales
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()

                        // Registro solo coordinador
                        .requestMatchers(HttpMethod.POST, "/auth/register").hasRole("COORDINADOR_TUTORIAS")

                        // Authenticated
                        .requestMatchers(HttpMethod.POST, "/auth/logout").authenticated()
                        .requestMatchers(HttpMethod.GET, "/auth/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/auth/google/link").authenticated()
                        .requestMatchers(HttpMethod.GET, "/auth/google/connect-drive").authenticated()
                        .requestMatchers(HttpMethod.GET, "/auth/google/status").authenticated()

                        // Operaciones especiales
                        .requestMatchers(HttpMethod.POST, "/api/asignaciones/cambio-tutor")
                        .hasRole("COORDINADOR_TUTORIAS")

                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )

                // ✅ Activa OAuth2 login + handlers
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(authz -> authz
                                .authorizationRequestResolver(new GoogleAuthorizationRequestResolver(
                                        clientRegistrationRepository,
                                        "/oauth2/authorization"
                                ))
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )

                .authenticationProvider(authenticationProvider())
                .addFilterBefore(cookieAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
