package com.universidad.tutorias;

import com.universidad.tutorias.domain.entity.*;
import com.universidad.tutorias.domain.enums.EstadoAlumno;
import com.universidad.tutorias.domain.enums.RolUsuario;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TestDataBuilder para crear datos de prueba consistentes y reutilizables.
 * Este builder proporciona métodos fluidos para crear entidades de test con valores por defecto.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestDataBuilder {

    /**
     * Crea y retorna un nuevo SemestreBuilder
     */
    public static SemestreBuilder semestre() {
        return new SemestreBuilder();
    }

    /**
     * Crea y retorna un nuevo UsuarioBuilder
     */
    public static UsuarioBuilder usuario() {
        return new UsuarioBuilder();
    }

    /**
     * Crea y retorna un nuevo AlumnoBuilder
     */
    public static AlumnoBuilder alumno() {
        return new AlumnoBuilder();
    }

    /**
     * Crea y retorna un nuevo TutorBuilder
     */
    public static TutorBuilder tutor() {
        return new TutorBuilder();
    }

    /**
     * Crea y retorna un nuevo RefreshTokenBuilder
     */
    public static RefreshTokenBuilder refreshToken() {
        return new RefreshTokenBuilder();
    }

    // ==================== BUILDERS ====================

    /**
     * Builder para Semestre
     */
    public static class SemestreBuilder {
        private Long id = 1L;
        private String codigo = "2025-2026-F1";
        private String nombre = "Semestre Agosto 2025 - Enero 2026";
        private LocalDate fechaInicio = LocalDate.of(2025, 8, 1);
        private LocalDate fechaFin = LocalDate.of(2026, 1, 31);
        private Boolean activo = false;

        public SemestreBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SemestreBuilder codigo(String codigo) {
            this.codigo = codigo;
            return this;
        }

        public SemestreBuilder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public SemestreBuilder fechaInicio(LocalDate fechaInicio) {
            this.fechaInicio = fechaInicio;
            return this;
        }

        public SemestreBuilder fechaFin(LocalDate fechaFin) {
            this.fechaFin = fechaFin;
            return this;
        }

        public SemestreBuilder activo(Boolean activo) {
            this.activo = activo;
            return this;
        }

        public Semestre build() {
            return new Semestre(id, codigo, nombre, fechaInicio, fechaFin, activo, LocalDateTime.now(), new ArrayList<>(), new ArrayList<>());
        }
    }

    /**
     * Builder para Usuario
     */
    public static class UsuarioBuilder {
        private Long id = 1L;
        private String username = "usuario@test.com";
        private String password = "$2a$10$encrypted_password";
        private RolUsuario rol = RolUsuario.ROLE_COORDINADOR_TUTORIAS;
        private Boolean activo = true;

        public UsuarioBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UsuarioBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UsuarioBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UsuarioBuilder rol(RolUsuario rol) {
            this.rol = rol;
            return this;
        }

        public UsuarioBuilder activo(Boolean activo) {
            this.activo = activo;
            return this;
        }

        public Usuario build() {
            return new Usuario(id, username, password, rol, activo);
        }
    }

    /**
     * Builder para Alumno
     */
    public static class AlumnoBuilder {
        private Long id = 1L;
        private String matricula = "A20230001";
        private String nombre = "Juan Pérez García";
        private String carrera = "Ingeniería en Sistemas";
        private Integer semestre = 3;
        private EstadoAlumno estado = EstadoAlumno.ACTIVO;
        private Tutor tutorActual = null;

        public AlumnoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public AlumnoBuilder matricula(String matricula) {
            this.matricula = matricula;
            return this;
        }

        public AlumnoBuilder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public AlumnoBuilder carrera(String carrera) {
            this.carrera = carrera;
            return this;
        }

        public AlumnoBuilder semestre(Integer semestre) {
            this.semestre = semestre;
            return this;
        }

        public AlumnoBuilder estado(EstadoAlumno estado) {
            this.estado = estado;
            return this;
        }

        public AlumnoBuilder tutorActual(Tutor tutorActual) {
            this.tutorActual = tutorActual;
            return this;
        }

        public Alumno build() {
            Alumno alumno = new Alumno(id, matricula, nombre, carrera, semestre, estado, tutorActual, 0, LocalDateTime.now());
            return alumno;
        }
    }

    /**
     * Builder para Tutor
     */
    public static class TutorBuilder {
        private Long id = 1L;
        private String nombre = "Dr. Carlos López";
        private String carrera = "Ingeniería en Sistemas";
        private Integer capacidadMax = 5;
        private Integer cargaActual = 0;
        private String areaAtencion = null;
        private String letraEdificio = null;
        private Boolean activo = true;

        public TutorBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public TutorBuilder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public TutorBuilder carrera(String carrera) {
            this.carrera = carrera;
            return this;
        }

        public TutorBuilder capacidadMax(Integer capacidadMax) {
            this.capacidadMax = capacidadMax;
            return this;
        }

        public TutorBuilder cargaActual(Integer cargaActual) {
            this.cargaActual = cargaActual;
            return this;
        }

        public TutorBuilder areaAtencion(String areaAtencion) {
            this.areaAtencion = areaAtencion;
            return this;
        }

        public TutorBuilder letraEdificio(String letraEdificio) {
            this.letraEdificio = letraEdificio;
            return this;
        }

        public TutorBuilder activo(Boolean activo) {
            this.activo = activo;
            return this;
        }

        public Tutor build() {
            Tutor tutor = new Tutor(id, nombre, carrera, capacidadMax, cargaActual, areaAtencion, letraEdificio, activo, LocalDateTime.now(), new ArrayList<>());
            return tutor;
        }
    }

    /**
     * Builder para RefreshToken
     */
    public static class RefreshTokenBuilder {
        private Long id = 1L;
        private String token = "refresh_token_test_" + System.currentTimeMillis();
        private Usuario usuario = null;
        private Instant expiryDate = Instant.now().plusSeconds(7 * 24 * 60 * 60); // 7 días
        private Boolean revoked = false;

        public RefreshTokenBuilder() {
            this.usuario = TestDataBuilder.usuario().build();
        }

        public RefreshTokenBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public RefreshTokenBuilder token(String token) {
            this.token = token;
            return this;
        }

        public RefreshTokenBuilder usuario(Usuario usuario) {
            this.usuario = usuario;
            return this;
        }

        public RefreshTokenBuilder expiryDate(Instant expiryDate) {
            this.expiryDate = expiryDate;
            return this;
        }

        public RefreshTokenBuilder revoked(Boolean revoked) {
            this.revoked = revoked;
            return this;
        }

        public RefreshToken build() {
            return new RefreshToken(id, token, usuario, expiryDate, revoked);
        }
    }
}
