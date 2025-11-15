# MAPEO ARQUITECTURA COMPLETO - SISTEMA DE GESTIÓN DE TUTORÍAS

**Fecha:** 2025-11-14  
**Versión:** Backend Spring Boot 3.3 + Frontend React 19 + Vite  
**Base de Datos:** MySQL 8.x

---

## TABLA DE CONTENIDOS

1. [Estructura General del Backend](#estructura-general-del-backend)
2. [Domain Layer - Entidades y Repositorios](#domain-layer--entidades-y-repositorios)
3. [Application Layer - Servicios y DTOs](#application-layer--servicios-y-dtos)
4. [Infrastructure Layer - Controllers y Configuración](#infrastructure-layer--controllers-y-configuración)
5. [Proceso de Asignación Completo (5 Fases)](#proceso-de-asignación-completo-5-fases)
6. [Base de Datos y Estructura de Tablas](#base-de-datos-y-estructura-de-tablas)
7. [Seguridad y Autenticación](#seguridad-y-autenticación)
8. [Dependencias Críticas](#dependencias-críticas)

---

## ESTRUCTURA GENERAL DEL BACKEND

### Estructura de Directorios

```
backend/
├── src/main/java/com/universidad/tutorias/
│   ├── domain/                          # Capa de Dominio
│   │   ├── entity/                      # Entidades JPA
│   │   ├── enums/                       # Enumeraciones
│   │   ├── exception/                   # Excepciones del dominio
│   │   └── repository/                  # Interfaces de repositorios
│   ├── application/                     # Capa de Aplicación
│   │   ├── dto/                         # Objetos de Transferencia de Datos
│   │   ├── enums/                       # Enums específicos de aplicación
│   │   └── service/                     # Interfaces y implementaciones de servicios
│   └── infrastructure/                  # Capa de Infraestructura
│       ├── config/                      # Configuraciones de Spring
│       ├── controller/                  # Controladores REST
│       ├── exception/                   # Manejadores de excepciones
│       └── security/                    # Componentes de seguridad (JWT, CORS)
├── src/main/resources/
│   ├── application.properties           # Configuración principal
│   └── sql/                             # Scripts SQL opcionales
└── pom.xml                              # Dependencias Maven

```

### Packages Principales

**Domain Layer:**
- `com.universidad.tutorias.domain.entity` - 12 Entidades JPA
- `com.universidad.tutorias.domain.enums` - 9 Enumeraciones
- `com.universidad.tutorias.domain.repository` - 14 Repositorios
- `com.universidad.tutorias.domain.exception` - Excepciones custom

**Application Layer:**
- `com.universidad.tutorias.application.service` - 18 Servicios
- `com.universidad.tutorias.application.service.impl` - Implementaciones
- `com.universidad.tutorias.application.dto` - 40+ DTOs
- `com.universidad.tutorias.application.enums` - Enums de aplicación

**Infrastructure Layer:**
- `com.universidad.tutorias.infrastructure.controller` - 10 Controladores
- `com.universidad.tutorias.infrastructure.config` - Configuración Spring
- `com.universidad.tutorias.infrastructure.security` - JWT + CORS
- `com.universidad.tutorias.infrastructure.exception` - Global Exception Handler

---

## DOMAIN LAYER - ENTIDADES Y REPOSITORIOS

### ENTIDADES JPA

#### 1. **Alumno** (Estudiante)
**Archivo:** `/domain/entity/Alumno.java`

```java
// Campos principales:
- id: Long (PK)
- matricula: String (UNIQUE, 20 chars)
- nombre: String (200 chars)
- carrera: String (100 chars)
- semestre: Integer
- estado: EstadoAlumno (ACTIVO | INACTIVO)
- tutorActual: Tutor (FK, LAZY)
- contadorCambiosTutor: Integer (default: 0)
- fechaRegistro: LocalDateTime (auto-set)

// Relaciones:
- ManyToOne: tutorActual (Tutor)

// Métodos de negocio:
- puedeReasignarse(): boolean (max 2 cambios)
- incrementarCambiosTutor(): void
```

**Índices:**
- `idx_matricula` - búsqueda rápida por matrícula
- `idx_estado` - filtrar activos/inactivos
- `idx_carrera` - agrupar por carrera

---

#### 2. **Tutor** (Asesor/Mentor)
**Archivo:** `/domain/entity/Tutor.java`

```java
// Campos principales:
- id: Long (PK)
- nombre: String (200 chars)
- carrera: String (100 chars)
- capacidadMax: Integer (capacidad máxima de alumnos)
- cargaActual: Integer (alumnos asignados actualmente) **CRÍTICO**
- areaAtencion: String (100 chars)
- letraEdificio: String (1 char)
- activo: Boolean (true | false)
- fechaRegistro: LocalDateTime
- alumnosAsignados: List<Alumno> (OneToMany, LAZY)

// Métodos de negocio:
- tieneCapacidadDisponible(): boolean
- getCapacidadDisponible(): int
- incrementarCarga(): void
- decrementarCarga(): void
- sincronizarCarga(cargaReal: int): void **IMPORTANTE**
```

**Índices:**
- `idx_carrera_tutor` - filtrar por carrera
- `idx_activo` - obtener tutores activos

**CRÍTICO:** `cargaActual` DEBE sincronizarse explícitamente con asignaciones activas

---

#### 3. **Asignacion** (Relación Alumno-Tutor-Semestre)
**Archivo:** `/domain/entity/Asignacion.java`

```java
// Campos principales:
- id: Long (PK)
- alumno: Alumno (FK, ManyToOne, LAZY)
- tutor: Tutor (FK, ManyToOne, LAZY)
- semestre: Semestre (FK, ManyToOne, LAZY)
- tipoAsignacion: TipoAsignacion (INICIAL | REASIGNACION | REINGRESO)
- fechaAsignacion: LocalDateTime (auto-set)
- semestreAcademico: String (deprecated, legacy)

// Constraints:
- UNIQUE(id_alumno, id_tutor, id_semestre) - evita duplicados
```

**Índices:**
- `idx_alumno_asig` - obtener asignaciones del alumno
- `idx_tutor_asig` - obtener asignaciones del tutor
- `idx_semestre_asig` - obtener asignaciones por semestre
- `uk_asignacion_alumno_tutor_semestre` - prevenir duplicados

---

#### 4. **Semestre** (Período Académico)
**Archivo:** `/domain/entity/Semestre.java`

```java
// Campos principales:
- id: Long (PK)
- codigo: String (UNIQUE, formato: "YYYY-YYYY-FN")
  Ejemplo: "2025-2026-F1" (Fall 1), "2025-2026-F2" (Fall 2)
- nombre: String (100 chars, descripción legible)
- fechaInicio: LocalDate
- fechaFin: LocalDate
- activo: Boolean (puede estar activo para nuevas asignaciones)
- fechaCreacion: LocalDateTime
- asignaciones: List<Asignacion> (OneToMany, CASCADE)
- procesos: List<ProcesoAsignacion> (OneToMany, LAZY)

// Validaciones:
- fechaFin > fechaInicio
- Patrón código: ^\\d{4}-\\d{4}-F[12]$

// Métodos de negocio:
- estaActivo(): boolean
- estaVigente(): boolean (dentro del rango de fechas actual)
- getTotalAsignaciones(): Integer
```

**Índices:**
- `idx_semestre_activo` - obtener semestres activos
- `idx_semestre_codigo` - búsqueda por código
- `idx_semestre_fechas` - búsquedas de rango

---

#### 5. **ProcesoAsignacion** (Ejecución del Bulk Assignment)
**Archivo:** `/domain/entity/ProcesoAsignacion.java`

```java
// Campos principales:
- id: Long (PK)
- semestre: Semestre (FK, ManyToOne, LAZY)
- estado: EstadoProceso (INICIADO | COMPARANDO | LIBERANDO_CUPOS | ASIGNANDO | COMPLETADO | FALLIDO)
- totalAlumnosProcesados: Integer
- totalAlumnosAsignados: Integer
- totalErrores: Integer
- totalWarnings: Integer
- archivoOrigen: String (nombre del Excel subido)
- fechaInicio: LocalDateTime (auto-set)
- fechaFin: LocalDateTime (se actualiza al terminar)
- usuarioEjecutor: String (quién ejecutó el proceso)
- detallesJson: String (JSON con metadatos de la ejecución)
- alertas: List<AlertaProceso> (OneToMany, CASCADE)
- errores: List<ErrorValidacion> (OneToMany, CASCADE)

// Métodos de negocio:
- incrementarAlumnosProcesados(): void
- incrementarAlumnosAsignados(): void
- incrementarErrores(): void
- incrementarWarnings(): void
- getTiempoTranscurridoMs(): long
- getPorcentajeProgreso(totalEsperado: int): int
```

**Índices:**
- `idx_estado_proceso` - filtrar por estado
- `idx_fecha_inicio` - búsquedas temporales
- `idx_proceso_semestre` - procesos por semestre

---

#### 6. **AlertaProceso** (Alertas durante asignación)
**Archivo:** `/domain/entity/AlertaProceso.java`

```java
// Campos principales:
- id: Long (PK)
- proceso: ProcesoAsignacion (FK, ManyToOne, LAZY)
- tipo: TipoAlerta (ERROR_FORMATO | ASIGNACION_CRUZADA | CAPACIDAD_EXCEDIDA | SIN_TUTOR_DISPONIBLE | REASIGNACION_FORZADA)
- severidad: SeveridadAlerta (BAJA | MEDIA | ALTA | CRITICA)
- descripcion: String (TEXT)
- alumno: Alumno (FK, ManyToOne, LAZY, optional)
- tutor: Tutor (FK, ManyToOne, LAZY, optional)
- resuelta: Boolean
- fechaCreacion: LocalDateTime
- fechaResolucion: LocalDateTime (null hasta que se resuelva)
```

**Índices:**
- `idx_proceso_alerta` - alertas por proceso
- `idx_severidad` - filtrar por severidad
- `idx_resuelta` - mostrar alertas sin resolver

---

#### 7. **ErrorValidacion** (Errores de validación)
**Archivo:** `/domain/entity/ErrorValidacion.java`

```java
// Campos principales:
- id: Long (PK)
- proceso: ProcesoAsignacion (FK, ManyToOne, LAZY)
- filaExcel: Integer (número de fila en archivo)
- matricula: String (20 chars)
- tipoError: TipoError (MATRICULA_INVALIDA | CAMPO_VACIO | CARRERA_INVALIDA | etc.)
- descripcion: String (TEXT, mensaje detallado)
- datoErroneo: String (500 chars, el valor problemático)
- fechaRegistro: LocalDateTime

// Tipos de error incluidos:
- MATRICULA_INVALIDA: formato incorrecto
- CAMPO_VACIO: campo requerido nulo
- CARRERA_INVALIDA: carrera no existe
- SEMESTRE_INVALIDO: semestre inválido
- MATRICULA_DUPLICADA: repetida en Excel
- ASIGNACION_DUPLICADA: ya existe en BD
- CAPACIDAD_EXCEDIDA: tutor lleno
- SIN_TUTOR_DISPONIBLE: no hay tutor para carrera
- ERROR_SISTEMA: error inesperado
```

**Índices:**
- `idx_proceso_error` - errores por proceso
- `idx_tipo_error` - filtrar por tipo

---

#### 8. **AlumnoInactivo** (Registro de inactivación)
**Archivo:** `/domain/entity/AlumnoInactivo.java`

```java
// Campos principales:
- id: Long (PK)
- alumno: Alumno (FK, ManyToOne, LAZY)
- motivoInactividad: MotivoInactividad (SIN_DEFINIR | BAJA_TEMPORAL | BAJA_DEFINITIVA | MOVILIDAD | EGRESADO)
- tutorPreservado: Tutor (FK, ManyToOne, LAZY) - tutor anterior si aplica
- fechaInactividad: LocalDateTime (auto-set)
- cupoLiberado: Boolean (true si se liberó cupo del tutor)
- fechaResolucion: LocalDateTime (cuando se resuelve la inactividad)

// Métodos de negocio:
- debePreservarTutor(): boolean
  Retorna true si: MOVILIDAD o BAJA_TEMPORAL
```

**Índices:**
- `idx_alumno_inact` - registros de inactividad por alumno
- `idx_motivo` - filtrar por motivo

---

#### 9. **Usuario** (Cuenta de usuario del sistema)
**Archivo:** `/domain/entity/Usuario.java`

```java
// Campos principales:
- id: Long (PK)
- username: String (50 chars, UNIQUE)
- password: String (120 chars, encrypted BCrypt)
- rol: RolUsuario (COORDINADOR_TUTORIAS | ADMIN)
- activo: Boolean

// Implements: UserDetails (Spring Security)
// Métodos de negocio heredados de UserDetails:
- getAuthorities(): Collection (basado en rol)
- isAccountNonExpired(), isAccountNonLocked(), isEnabled(): boolean
```

**Índices:**
- `idx_usuario_username` - búsqueda por username
- `idx_usuario_activo` - filtrar usuarios activos

---

#### 10. **RefreshToken** (Tokens para renovación de sesión)
**Archivo:** `/domain/entity/RefreshToken.java`

```java
// Campos principales:
- id: Long (PK)
- usuario: Usuario (FK, ManyToOne)
- token: String (UNIQUE, largo)
- expiresAt: Instant (cuándo expira el refresh token)
- revokedAt: Instant (cuándo se revocó, null si activo)
```

---

#### 11. **MatrizAfinidadCarrera** (Compatibilidad de carreras para asignación)
**Archivo:** `/domain/entity/MatrizAfinidadCarrera.java`

```java
// Campos principales:
- id: Long (PK)
- carreraOrigen: String (100 chars, carrera del alumno)
- carreraCompatible: String (100 chars, carrera del tutor)
- prioridad: Integer (1=máxima, mayor número=menor prioridad)

// Uso: Si alumno de ISC no encuentra tutor de ISC, busca tutores de carrera compatible
// CONSTRAINT: UNIQUE(carreraOrigen, carreraCompatible)
```

**Índices:**
- `idx_carrera_origen` - obtener compatibles de una carrera

---

#### 12. **LogAuditoria** (Registro de auditoría de cambios)
**Archivo:** `/domain/entity/LogAuditoria.java`

```java
// Campos principales:
- id: Long (PK)
- proceso: Long (FK, ID del ProcesoAsignacion)
- tipoAccion: TipoAccion (enum de acciones)
- tipoEntidad: String (ALUMNO | TUTOR | ASIGNACION)
- idEntidad: Long (ID de la entidad afectada)
- descripcion: String
- datosAntes: String (JSON)
- datosDespues: String (JSON)
- usuario: String (quién hizo la acción)
- fechaRegistro: LocalDateTime
```

---

### ENUMERACIONES (domain/enums)

#### EstadoAlumno
```java
ACTIVO("Activo")
INACTIVO("Inactivo")
```

#### EstadoProceso
```java
INICIADO("Iniciado")
COMPARANDO("Comparando Alumnos")
LIBERANDO_CUPOS("Liberando Cupos")
ASIGNANDO("Asignando Tutores")
COMPLETADO("Completado")
FALLIDO("Fallido")

// Método: esEstadoFinal() -> (COMPLETADO || FALLIDO)
```

#### TipoAsignacion
```java
INICIAL("Asignación Inicial")
REASIGNACION("Reasignación")
REINGRESO("Reingreso")
```

#### MotivoInactividad
```java
SIN_DEFINIR("Sin Definir")
BAJA_TEMPORAL("Baja Temporal")
BAJA_DEFINITIVA("Baja Definitiva")
MOVILIDAD("Movilidad")
EGRESADO("Egresado")

// Método: debePreservarTutor() -> (MOVILIDAD || BAJA_TEMPORAL)
```

#### TipoError
```java
MATRICULA_INVALIDA, CAMPO_VACIO, CARRERA_INVALIDA, SEMESTRE_INVALIDO,
MATRICULA_DUPLICADA, ASIGNACION_DUPLICADA, CAPACIDAD_EXCEDIDA,
SIN_TUTOR_DISPONIBLE, ERROR_SISTEMA
```

#### TipoAlerta
```java
ERROR_FORMATO("Error de Formato")
ASIGNACION_CRUZADA("Asignación Cruzada")
CAPACIDAD_EXCEDIDA("Capacidad Excedida")
SIN_TUTOR_DISPONIBLE("Sin Tutor Disponible")
REASIGNACION_FORZADA("Reasignación Forzada")
```

#### SeveridadAlerta
```java
BAJA, MEDIA, ALTA, CRITICA
```

#### RolUsuario
```java
COORDINADOR_TUTORIAS, ADMIN
```

#### TipoAccion
```java
MARCADO_INACTIVOS, LIBERACION_CUPOS, ASIGNACION, REASIGNACION, etc.
```

---

### REPOSITORIOS (domain/repository)

#### 1. **AlumnoRepository**
**Archivo:** `/domain/repository/AlumnoRepository.java`

```java
Métodos custom:
- findByEstadoWithTutor(EstadoAlumno): List<Alumno>
- findByMatricula(String): Optional<Alumno>
- findByEstadoAndCarrera(EstadoAlumno, String): List<Alumno>
- existsByMatricula(String): boolean
- contarAlumnosActivosPorTutor(Long): int
- findByIdForUpdate(Long): Optional<Alumno> // PESSIMISTIC_WRITE lock
- findByMatriculaIn(List<String>): List<Alumno>
- findDistinctCarreras(): List<String>
```

---

#### 2. **TutorRepository**
**Archivo:** `/domain/repository/TutorRepository.java`

```java
Métodos custom:
- findDisponibles(): List<Tutor> // activo && cargaActual < capacidadMax
- findDisponiblesByCarrera(String): List<Tutor>
- findByIdForUpdate(Long): Optional<Tutor> // PESSIMISTIC_WRITE lock
- findByIdWithAlumnos(Long): Optional<Tutor> // JOIN FETCH
- findAllActivos(): List<Tutor>
- findAllIds(): List<Long> // solo IDs
```

---

#### 3. **AsignacionRepository**
**Archivo:** `/domain/repository/AsignacionRepository.java`

```java
Métodos custom:
- findByAlumnoId(Long): List<Asignacion>
- findByTutorAndSemestre(Long, Long): List<Asignacion>
- findByTutorAndSemestreString(Long, String): List<Asignacion>
- findByTutorIdWithDetalles(Long): List<Asignacion> // JOIN FETCH
- findByCarreraAndSemestre(String, Long): List<Asignacion>
- findByCarreraAndSemestreString(String, String): List<Asignacion>
- findByCarrera(String): List<Asignacion>
- findBySemestreId(Long): List<Asignacion>
- findBySemestreString(String): List<Asignacion>
- findCarrerasDisponiblesBySemestreId(Long): List<String>
- findCarrerasDisponibles(String): List<String>
- countByTutorId(Long): int
- countBySemestreId(Long): Long
- countActivosBySemestreId(Long): Long
- findBySemestreIdAndTutorId(Long, Long): List<Asignacion>
- existsByAlumnoAndTutorAndSemestreId(Long, Long, Long): boolean
- existsByAlumnoAndTutorAndSemestre(Long, Long, String): boolean
```

---

#### 4. **AlumnoSearchRepository**
**Archivo:** `/domain/repository/AlumnoSearchRepository.java`

**Tipo:** Custom Repository (EntityManager directo, NO extends JpaRepository)
**Propósito:** Búsqueda relevancia-based con scoring SQL nativo

```java
Métodos:
- searchWithRanking(
    normalizedQuery: String,
    likeQuery: String,
    regexQuery: String,
    estado: EstadoAlumno,
    carrera: String,
    semestre: Integer,
    pageable: Pageable,
    sortOption: SearchSortOption
  ): Page<Object[]>

- findByMatricula(
    normalizedQuery: String,
    likeQuery: String,
    regexQuery: String
  ): Optional<Object[]>

// Scoring algorithm:
CASE
  WHEN LOWER(a.matricula) = :normalizedQuery THEN 1000        // Exacto en matrícula
  WHEN LOWER(a.matricula) LIKE CONCAT(:likeQuery, '%') THEN 800 // Comienza con
  WHEN INSTR(LOWER(a.matricula), :normalizedQuery) > 0 THEN 600 // Contiene
  WHEN LOWER(a.nombre) = :normalizedQuery THEN 500            // Exacto en nombre
  WHEN LOWER(a.nombre) LIKE CONCAT(:likeQuery, '%') THEN 420   // Nombre comienza
  WHEN LOWER(a.nombre) REGEXP CONCAT('(^| )', :regexQuery) THEN 400 // Palabra completa
  WHEN INSTR(LOWER(a.nombre), :normalizedQuery) > 0 THEN 200  // Nombre contiene
  ELSE 0
END

// Sort options:
- RELEVANCE (default): ORDER BY score DESC, matricula ASC, nombre ASC
- MATRICULA: ORDER BY matricula ASC, score DESC
- NOMBRE: ORDER BY nombre ASC, score DESC
```

---

#### 5. **TutorSearchRepository**
**Archivo:** `/domain/repository/TutorSearchRepository.java`

**Tipo:** Custom Repository (EntityManager directo)
**Propósito:** Búsqueda de tutores con scoring

```java
Métodos:
- searchWithRanking(
    normalizedQuery: String,
    likeQuery: String,
    regexQuery: String,
    carrera: String,
    pageable: Pageable,
    sortOption: SearchSortOption
  ): Page<Object[]>

// Similar scoring a AlumnoSearchRepository
```

---

#### 6. **SemestreRepository**
**Archivo:** `/domain/repository/SemestreRepository.java`

```java
Métodos custom:
- findByCodigoAndActivo(String, Boolean): Optional<Semestre>
- findByActivoTrue(): List<Semestre>
- findByCodigo(String): Optional<Semestre>
- existsByCodigo(String): boolean
```

---

#### 7. **ProcesoAsignacionRepository**
**Archivo:** `/domain/repository/ProcesoAsignacionRepository.java`

```java
Métodos custom:
- findBySemestreId(Long): List<ProcesoAsignacion>
- findByEstado(EstadoProceso): List<ProcesoAsignacion>
- findLatestByEstado(EstadoProceso): Optional<ProcesoAsignacion>
```

---

#### 8. **Otros Repositorios**

```java
// AlertaProcesoRepository
- findByProcesoId(Long): List<AlertaProceso>
- findByProcesoIdAndResuelta(Long, Boolean): List<AlertaProceso>
- findByProcesoIdAndSeveridad(Long, SeveridadAlerta): List<AlertaProceso>

// ErrorValidacionRepository
- findByProcesoId(Long): List<ErrorValidacion>
- countByProcesoId(Long): Long

// AlumnoInactivoRepository
- findByAlumnoId(Long): Optional<AlumnoInactivo>
- findByAlumnoIdAndCupoLiberado(Long, Boolean): List<AlumnoInactivo>

// MatrizAfinidadCarreraRepository
- findByCarreraOrigen(String): List<MatrizAfinidadCarrera>
- findByCarreraOrigenAndCarreraCompatible(String, String): Optional<MatrizAfinidadCarrera>

// UsuarioRepository
- findByUsername(String): Optional<Usuario>

// RefreshTokenRepository
- findByToken(String): Optional<RefreshToken>

// LogAuditoriaRepository
- findByProcesoId(Long): List<LogAuditoria>

// PermitirApijpRepository
```

---

## APPLICATION LAYER - SERVICIOS Y DTOs

### SERVICIOS (application/service)

#### 1. **ProcesoOrchestrator** (Orquestador principal)
**Archivo:** `/application/service/ProcesoOrchestrator.java` (interface)  
**Implementación:** `/application/service/impl/ProcesoOrchestratorImpl.java`

```java
public interface ProcesoOrchestrator {
    @Async("asignacionExecutor")
    CompletableFuture<Long> ejecutarProcesoCompleto(
        MultipartFile archivo,
        Long semestreId,
        String usuario
    );
}

// Coordina las 5 fases del proceso:
// 1. Lectura y validación de Excel
// 2. Comparación con BD e identificación de inactivos
// 3. Liberación de cupos
// 4. Procesamiento de reingresos
// 5. Asignación de nuevos alumnos
```

---

#### 2. **ExcelReaderService**
**Propósito:** Leer y parsear archivos Excel

```java
public interface ExcelReaderService {
    List<AlumnoExcelDTO> leerArchivo(MultipartFile archivo)
        throws ExcelFormatoException;
}

// Lee columnas: Matricula, Nombre, Carrera, Semestre
// Retorna List<AlumnoExcelDTO> con datos del Excel
```

---

#### 3. **AlumnoValidadorService**
**Propósito:** Validar datos de alumnos

```java
public interface AlumnoValidadorService {
    ResultadoValidacion validarAlumnos(List<AlumnoExcelDTO> alumnos, Long procesoId);
}

// Validaciones:
// - Campos no vacíos
// - Formato de matrícula: ^[A-Z0-9]{5,20}$
// - Carrera en lista válida
// - Semestre válido (1-8)
// - Matrícula no duplicada en archivo
// Guarda ErrorValidacion en BD para cada error
```

---

#### 4. **ComparadorAlumnosService**
**Propósito:** Comparar alumnos del Excel con DB para identificar inactivos

```java
public interface ComparadorAlumnosService {
    List<Alumno> identificarInactivos(
        List<AlumnoExcelDTO> alumnosExcel,
        Long semestreId
    );
}

// Algoritmo:
// Para cada alumno ACTIVO en BD del semestre anterior:
//   Si NO está en Excel -> marcar para inactivar
// Retorna lista de Alumno que deben inactivarse
```

---

#### 5. **InactivacionService**
**Propósito:** Marcar alumnos como inactivos y liberar cupos

```java
public interface InactivacionService {
    void marcarInactivos(List<Alumno> alumnos, Long procesoId, Long semestreId);
    void liberarCupos(Long procesoId);
}

// marcarInactivos:
// 1. Cambia estado a INACTIVO en tabla alumnos
// 2. Crea registro en alumnos_inactivos
// 3. Registra auditoría
//
// liberarCupos:
// 1. Para cada alumno inactivo del proceso
// 2. Si tutorPreservado IS NULL (no fue inactividad temporal)
// 3. Decrementa cargaActual del tutor
// 4. Actualiza tutores
```

---

#### 6. **ReingresoService**
**Propósito:** Procesar alumnos que reaparecen (estaban inactivos)

```java
public interface ReingresoService {
    List<Alumno> procesarReingresos(
        List<AlumnoExcelDTO> alumnosValidos,
        Long procesoId
    );
}

// Algoritmo:
// Para cada alumno válido:
//   Si existe en alumnos_inactivos:
//     1. Marcar como ACTIVO nuevamente
//     2. Crear Asignacion de tipo REINGRESO
//     3. Retornar en lista de reingresos
```

---

#### 7. **AsignacionService**
**Propósito:** Asignar alumnos a tutores

```java
public interface AsignacionService {
    ResultadoAsignacion asignarAlumnos(
        List<AlumnoExcelDTO> alumnosValidos,
        Long procesoId,
        Long semestreId
    );
}

// Algoritmo principal:
// 1. Sincronizar carga de todos tutores
// 2. Agrupar alumnos por carrera (para eficiencia)
// 3. Para cada alumno:
//    a. Buscar tutores disponibles de su carrera (cargaActual < capacidadMax)
//    b. Si no hay, buscar en carreras compatibles (MatrizAfinidadCarrera)
//    c. Si hay múltiples candidatos, elegir el de menor carga (round-robin)
//    d. Crear Asignacion, incrementar cargaActual, crear AlertaProceso si aplica
//    e. Si falla (sin tutor, duplicado, capacidad), registrar ErrorAsignacion
// 4. Procesar en batches de 100 para eficiencia
// 5. Retornar ResultadoAsignacion (total procesados, asignados, errores)
```

---

#### 8. **TutorSincronizacionService**
**Propósito:** Sincronizar cargaActual con asignaciones reales en BD

```java
public interface TutorSincronizacionService {
    void recalcularCargaTutor(Long tutorId);
    void recalcularCargaTodosLosTutores();
}

// Algoritmo:
// 1. Contar asignaciones ACTIVAS del tutor en tabla asignaciones
// 2. Comparar con cargaActual en tabla tutores
// 3. Si diferente, actualizar cargaActual a la carga real
// 4. Usar PESSIMISTIC_WRITE lock para evitar race conditions
// CRÍTICO: Se llama al inicio de cada asignación en lote
```

---

#### 9. **TutorReasignacionService**
**Propósito:** Cambiar tutor de un alumno

```java
public interface TutorReasignacionService {
    CambioTutorResponseDTO cambiarTutor(
        Long alumnoId,
        Long nuevoTutorId
    );
}

// Validaciones:
// 1. Alumno existe y es ACTIVO
// 2. Nuevo tutor existe y tiene capacidad disponible
// 3. Alumno puede reasignarse (contador < 2)
// 4. No existe ya una asignación con ese tutor en semestre actual
//
// Operaciones:
// 1. Si tiene tutor actual, decrementar su cargaActual
// 2. Actualizar alumno.tutorActual = nuevoTutor
// 3. Incrementar cargaActual del nuevo tutor
// 4. Crear nueva Asignacion de tipo REASIGNACION
// 5. Incrementar contador_cambios_tutor del alumno
// 6. Registrar auditoría
```

---

#### 10. **AuthService**
**Propósito:** Autenticación y manejo de tokens JWT

```java
public interface AuthService {
    AuthTokensResult login(LoginRequest request);
    AuthTokensResult refresh(String refreshTokenValue);
    void logout(String refreshTokenValue);
    RegisterResponse register(RegisterRequest request);
    UserInfoResponse buildUserInfo(Usuario usuario);
}

// login:
// 1. Validar credenciales con BCryptPasswordEncoder
// 2. Crear JWT access token (expira en 3600s)
// 3. Crear refresh token en BD (expira en 604800s = 7 días)
// 4. Retornar tokens para guardar en cookies HttpOnly
//
// refresh:
// 1. Validar refresh token en BD (no revocado)
// 2. Generar nuevo access token
// 3. Retornar nuevo access token
//
// logout:
// 1. Marcar refresh token como revocado (revokedAt = now)
```

---

#### 11. **SemestreService**
**Propósito:** Operaciones CRUD de semestres

```java
public interface SemestreService {
    Semestre crearSemestre(CrearSemestreDTO dto);
    Semestre obtenerPorId(Long id);
    Semestre obtenerPorCodigo(String codigo);
    Semestre obtenerOCrearSemestrePorCodigoLegacy(String codigo);
    List<Semestre> obtenerActivos();
    List<Semestre> obtenerTodos();
    Semestre actualizar(Long id, ActualizarSemestreDTO dto);
    void eliminar(Long id);
    EstadisticasSemestreDTO obtenerEstadisticas(Long id);
}
```

---

#### 12-17. **Otros Servicios**

```java
// AlumnoCrudService
- crear(AlumnoCreateDTO): AlumnoResponseDTO
- obtenerPorId(Long): AlumnoResponseDTO
- obtenerTodos(): List<AlumnoResponseDTO>
- actualizar(Long, AlumnoUpdateDTO): AlumnoResponseDTO
- eliminar(Long): void
- buscarPorMatricula(String): AlumnoResponseDTO

// AlumnoSearchService
- buscar(String query, EstadoAlumno, String carrera, Integer semestre, Pageable, SearchSortOption): Page<AlumnoSearchResultDTO>

// TutorCrudService
- crear(TutorCreateDTO): TutorResponseDTO
- obtenerPorId(Long): TutorResponseDTO
- actualizar(Long, TutorUpdateDTO): TutorResponseDTO
- eliminar(Long): void
- obtenerActivos(): List<TutorResponseDTO>

// TutorSearchService
- buscar(String query, String carrera, Pageable, SearchSortOption): Page<TutorSearchResultDTO>

// ReporteService
- generarReportePorCarrera(Long semestreId, String format): ...
- generarReportePorTutor(Long semestreId, String format): ...

// AuditoriaService
- registrarLog(procesoId, tipoAccion, tipoEntidad, idEntidad, descripcion, datosAntes, datosDespues, usuario): void

// InactivacionService (ya descrito)
```

---

### DTOs (application/dto)

**Total: 40+ DTOs** organizados en packages:

#### DTOs de Alumno
```
- AlumnoCreateDTO (request: crear alumno)
- AlumnoResponseDTO (response: detalles alumno)
- AlumnoUpdateDTO (request: actualizar alumno)
- AlumnoPatchDTO (request: actualización parcial)
- AlumnoSimpleDTO (response ligero)
- AlumnoSearchResultDTO (búsqueda: matrícula, nombre, carrera, tutor)
- AlumnoDetalleDTO (detalle completo con historial)
- AlumnoExcelDTO (internal: parsing de Excel)
- AlumnoInactivoResponseDTO (response: información de inactividad)
```

#### DTOs de Tutor
```
- TutorCreateDTO (request: crear tutor)
- TutorResponseDTO (response: detalles tutor)
- TutorUpdateDTO (request: actualizar tutor)
- TutorSimpleDTO (response ligero)
- TutorSearchResultDTO (búsqueda: nombre, carrera, carga)
- TutorConAlumnosDTO (response: tutor + lista de alumnos)
```

#### DTOs de Asignación
```
- IniciarProcesoRequest (request: iniciar bulk assignment Excel)
  - archivo: MultipartFile
  - semestreAcademico: String (legacy, ej: "2025-F1")
  - usuario: String
- IniciarProcesoResponse (response: confirmación inicio)
  - procesoId: Long
  - estado: EstadoProceso
  - mensaje: String
- EstadoProcesoResponse (response: estado del proceso)
  - procesoId: Long
  - estado: EstadoProceso
  - progreso: ProgresoDTO
  - fechaInicio, fechaFin: LocalDateTime
  - tiempoTranscurridoMs: long
- ProgresoDTO (porcentaje, contador de procesados/asignados/errores)
- ErrorAsignacionDTO (detalles de error en asignación)
- CambioTutorRequestDTO (request: cambiar tutor alumno)
  - alumnoId: Long
  - nuevoTutorId: Long
- CambioTutorResponseDTO (response: resultado cambio)
```

#### DTOs de Semestre
```
- CrearSemestreDTO (request)
  - codigo: String (ej: "2025-2026-F1")
  - nombre: String
  - fechaInicio: LocalDate
  - fechaFin: LocalDate
- SemestreDTO (response general)
- ActualizarSemestreDTO (request: PATCH)
- EstadisticasSemestreDTO (estadísticas del semestre)
```

#### DTOs de Autenticación
```
- LoginRequest (username, password)
- LoginResponse (mensaje de éxito)
- RegisterRequest (username, password, rol)
- RegisterResponse (mensaje + usuario creado)
- RefreshTokenRequest (refreshToken)
- RefreshTokenResponse (mensaje)
- UserInfoResponse (username, rol, activo)
- AuthTokensResult (accessToken, refreshToken, expiresIn)
```

#### DTOs de Reporte
```
- ReporteCarreraDTO
- ReporteAlumnoDTO
- CarreraResumenDTO
```

#### DTOs Internos
```
- ResultadoValidacion (alumnosValidos, errores, tieneErroresCriticos)
- ResultadoAsignacion (totalProcesados, totalAsignados, totalErrores)
- AlertaDTO (tipo, severidad, descripción)
```

---

## INFRASTRUCTURE LAYER - CONTROLLERS Y CONFIGURACIÓN

### CONTROLADORES REST

#### 1. **AuthController** (`/auth`)
```java
POST   /auth/login                    // LoginRequest -> LoginResponse
POST   /auth/refresh                  // RefreshTokenRequest -> RefreshTokenResponse
POST   /auth/logout                   // RefreshTokenRequest -> null
GET    /auth/me                       // -> UserInfoResponse
POST   /auth/register                 // RegisterRequest -> RegisterResponse (COORDINADOR only)
```

---

#### 2. **AsignacionController** (`/api/asignaciones`)
```java
POST   /api/asignaciones/iniciar                    // IniciarProcesoRequest -> IniciarProcesoResponse
GET    /api/asignaciones/proceso/{procesoId}       // -> EstadoProcesoResponse
GET    /api/asignaciones/proceso/{procesoId}/alertas // ?severidad=, ?resuelta= -> Map alertas
GET    /api/asignaciones/proceso/{procesoId}/errores // -> List<ErrorAsignacionDTO>
POST   /api/asignaciones/cambio-tutor              // CambioTutorRequestDTO -> CambioTutorResponseDTO (COORDINADOR only)
GET    /api/asignaciones/{semestreId}/todas        // -> List<AsignacionDTO>
```

---

#### 3. **AlumnoController** (`/api/alumnos`)
```java
GET    /api/alumnos                   // page, size -> Page<AlumnoResponseDTO>
GET    /api/alumnos/{id}              // -> AlumnoResponseDTO
POST   /api/alumnos                   // AlumnoCreateDTO -> AlumnoResponseDTO
PUT    /api/alumnos/{id}              // AlumnoUpdateDTO -> AlumnoResponseDTO
PATCH  /api/alumnos/{id}              // AlumnoPatchDTO -> AlumnoResponseDTO
DELETE /api/alumnos/{id}              // -> null
GET    /api/alumnos/matricula/{matricula} // -> AlumnoResponseDTO
```

---

#### 4. **AlumnoSearchController** (`/api/alumnos/buscar`)
```java
GET    /api/alumnos/buscar            // ?q=, ?estado=, ?carrera=, ?semestre=, ?sort=, page, size
                                        // -> Page<AlumnoSearchResultDTO>
```

---

#### 5. **TutorController** (`/api/tutores`)
```java
GET    /api/tutores                   // page, size -> Page<TutorResponseDTO>
GET    /api/tutores/{id}              // -> TutorResponseDTO
POST   /api/tutores                   // TutorCreateDTO -> TutorResponseDTO
PUT    /api/tutores/{id}              // TutorUpdateDTO -> TutorResponseDTO
DELETE /api/tutores/{id}              // -> null
GET    /api/tutores/disponibles       // -> List<TutorResponseDTO>
GET    /api/tutores/{id}/alumnos      // -> TutorConAlumnosDTO
```

---

#### 6. **TutorSearchController** (`/api/tutores/buscar`)
```java
GET    /api/tutores/buscar            // ?q=, ?carrera=, ?sort=, page, size
                                        // -> Page<TutorSearchResultDTO>
```

---

#### 7. **SemestreController** (`/api/semestres`)
```java
GET    /api/semestres                 // -> List<SemestreDTO>
GET    /api/semestres/{id}            // -> SemestreDTO
POST   /api/semestres                 // CrearSemestreDTO -> SemestreDTO
PUT    /api/semestres/{id}            // ActualizarSemestreDTO -> SemestreDTO
DELETE /api/semestres/{id}            // -> null
GET    /api/semestres/{id}/estadisticas // -> EstadisticasSemestreDTO
```

---

#### 8. **AlumnoInactivoController** (`/api/alumnos-inactivos`)
```java
GET    /api/alumnos-inactivos         // -> List<AlumnoInactivoResponseDTO>
GET    /api/alumnos-inactivos/{id}    // -> AlumnoInactivoResponseDTO
POST   /api/alumnos-inactivos         // AlumnoInactivoDTO -> AlumnoInactivoResponseDTO
```

---

#### 9. **DashboardController** (`/api/dashboard`)
```java
GET    /api/dashboard                 // -> DashboardDTO (métricas generales)
GET    /api/dashboard/semestre/{id}   // -> EstadisticasSemestreDTO
```

---

#### 10. **ReporteController** (`/api/reportes`)
```java
GET    /api/reportes/carrera/{semestreId}  // ?format=pdf|excel -> File
GET    /api/reportes/tutor/{semestreId}    // ?format=pdf|excel -> File
GET    /api/reportes/proceso/{procesoId}   // -> ProcesoReporteDTO
```

---

### CONFIGURACIÓN DE SPRING

#### 1. **SecurityConfig** (`/infrastructure/config/SecurityConfig.java`)

```java
// Bean: SecurityFilterChain
// Configuración:
// - CORS primero (crítico)
// - CSRF disabled (usamos cookies HttpOnly + SameSite)
// - Session Policy: STATELESS (no sesiones server)
// - Exception handling: autenticación + autorización
// - Authorization rules:
//   * OPTIONS /* permitAll (CORS preflight)
//   * POST /auth/login permitAll
//   * POST /auth/refresh permitAll
//   * POST /auth/register requiere COORDINADOR_TUTORIAS
//   * POST /auth/logout authenticated
//   * GET /auth/me authenticated
//   * POST /api/asignaciones/cambio-tutor requiere COORDINADOR_TUTORIAS
//   * /* /api/** requiere authenticated
// - AuthenticationProvider: DaoAuthenticationProvider (BCrypt)
// - Agregar CookieAuthenticationFilter

@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
    return config.getAuthenticationManager();
}
```

---

#### 2. **CorsConfig** (`/infrastructure/config/CorsConfig.java`)

```java
// Bean: CorsConfigurationSource
// Configuración:
// - Origins permitidos: http://localhost:5173, http://localhost:3000
// - Métodos: GET, POST, PUT, DELETE, PATCH, OPTIONS
// - Headers: * (todos)
// - Expose headers: Set-Cookie, Authorization, Content-Type
// - Credentials: true (permitir cookies)
// - MaxAge: 3600s (cachear preflight)
```

---

#### 3. **AsyncConfiguration** (`/infrastructure/config/AsyncConfiguration.java`)

```java
// Bean: Executor named "asignacionExecutor"
// Pool:
//   - Core size: 2
//   - Max size: 5
//   - Queue capacity: 100
//   - Thread name prefix: "async-asignacion-"
// Propósito: ejecutar ProcesoOrchestrator de forma async
```

---

#### 4. **JacksonConfiguration** (`/infrastructure/config/JacksonConfiguration.java`)

```java
// Bean: ObjectMapper
// Configuración:
// - SerializationFeature.WRITE_DATES_AS_TIMESTAMPS = false
// - Timezone: UTC
// - DefaultPropertyInclusion: NON_NULL (no serializar nulos)
```

---

#### 5. **SecurityDataInitializer** (`/infrastructure/config/SecurityDataInitializer.java`)

```java
// ApplicationListener: ApplicationReadyEvent
// Funciona al iniciar la app:
// 1. Verificar si existe usuario COORDINADOR_TUTORIAS
// 2. Si no existe, crear con credenciales default (de properties):
//    - username: coord_tutorias
//    - password: 123456 (hashed con BCrypt)
//    - rol: COORDINADOR_TUTORIAS
// NOTA: CAMBIAR credenciales en producción
```

---

### SEGURIDAD Y AUTENTICACIÓN

#### JWT Service (`/infrastructure/security/JwtService.java`)

```java
// Métodos:
public String generateAccessToken(UserDetails userDetails) {
    // Genera JWT con:
    // - Subject: username
    // - IssuedAt: ahora
    // - Expiration: ahora + properties.expiration (3600s)
    // - Issuer: "ProyectoTutorias"
    // - Claim "roles": lista de autoridades
    // - Signed: HS256 con secret base64
    return JWT;
}

public boolean isTokenValid(String token, UserDetails userDetails) {
    // Verifica:
    // 1. Username en token == userDetails.username
    // 2. Token no expirado
}

public String extractUsername(String token) {
    // Extrae subject del token
}

public long getAccessTokenExpirationSeconds() {
    return 3600;
}

public long getRefreshTokenExpirationSeconds() {
    return 604800; // 7 días
}
```

**Propiedades JWT:**
```properties
security.jwt.secret=dW5pdmVyc2lkYWRUdXRvcmlhc1NlZ3VyaWRhQWNjZXNvMjAyNA==
security.jwt.expiration=3600
security.jwt.refresh-expiration=604800
security.jwt.issuer=ProyectoTutorias
security.jwt.access-cookie-name=tutorias_access_token
security.jwt.refresh-cookie-name=tutorias_refresh_token
security.jwt.cookie-secure=true              # HTTPS only
security.jwt.cookie-same-site=none           # Cross-origin
security.jwt.cookie-path=/
```

---

#### Cookie Authentication Filter (`/infrastructure/security/CookieAuthenticationFilter.java`)

```java
// Filtro Spring Security
// Funciona:
// 1. Extraer JWT de cookie "tutorias_access_token"
// 2. Validar JWT con JwtService
// 3. Extraer username del JWT
// 4. Cargar UserDetails con CustomUserDetailsService
// 5. Crear Authentication y agregar a SecurityContext
// 6. Pasar al siguiente filtro si válido

// Si no hay token válido o no está autenticado:
//   -> Pasar al siguiente filtro (dejará que falle la autorización si es necesario)
```

---

#### Token Cookie Service (`/infrastructure/security/TokenCookieService.java`)

```java
// Métodos:
public void addAuthCookies(HttpServletResponse response, AuthTokensResult tokens) {
    // 1. Crear cookie "tutorias_access_token"
    //    - value: tokens.accessToken
    //    - maxAge: tokens.expiresIn
    //    - httpOnly: true (NO accesible desde JS)
    //    - secure: true (HTTPS solo)
    //    - sameSite: NONE (cross-origin)
    //    - path: /
    // 2. Crear cookie "tutorias_refresh_token"
    //    - value: tokens.refreshToken
    //    - maxAge: 604800s
    //    - httpOnly: true
    //    - secure: true
    //    - sameSite: NONE
    //    - path: /
    // 3. Agregar cookies a response
}

public void clearAuthCookies(HttpServletResponse response) {
    // Crear cookies con maxAge=0 para borrar
}
```

---

#### Custom User Details Service (`/infrastructure/security/CustomUserDetailsService.java`)

```java
// Implementa UserDetailsService
public UserDetails loadUserByUsername(String username) {
    // 1. Buscar Usuario por username en BD
    // 2. Si no existe -> throw UsernameNotFoundException
    // 3. Retornar Usuario (que implementa UserDetails)
    // 4. Spring Security valida con PasswordEncoder
}
```

---

### EXCEPTION HANDLING

#### Global Exception Handler (`/infrastructure/exception/GlobalExceptionHandler.java`)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    -> 404 NOT_FOUND + "ENTIDAD_NO_ENCONTRADA"

    @ExceptionHandler(ExcelFormatoException.class)
    -> 400 BAD_REQUEST + "FORMATO_INVALIDO"

    @ExceptionHandler(ProcesoAsignacionException.class)
    -> 500 INTERNAL_SERVER_ERROR + "ERROR_PROCESO_ASIGNACION"

    @ExceptionHandler(MethodArgumentNotValidException.class)
    -> 400 BAD_REQUEST + "VALIDACION_FALLIDA" + binding errors

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    -> 401 UNAUTHORIZED + "CREDENCIALES_INVALIDAS"

    @ExceptionHandler(AccessDeniedException.class)
    -> 403 FORBIDDEN + "ACCESO_DENEGADO"

    @ExceptionHandler(AuthenticationException.class)
    -> 401 UNAUTHORIZED + "AUTENTICACION_FALLIDA"

    @ExceptionHandler(SemestreNotFoundException.class)
    -> 404 NOT_FOUND + "SEMESTRE_NO_ENCONTRADO"

    // ... más handlers
}
```

---

#### Response Format

**Éxito:**
```json
{
  "status": "success",
  "code": "OPERACION_EXITOSA",
  "message": "Operación realizada correctamente",
  "data": { /* objeto retornado */ },
  "timestamp": "2025-11-14T10:30:45.123Z"
}
```

**Error:**
```json
{
  "status": "error",
  "code": "CODIGO_ERROR",
  "message": "Descripción del error",
  "errors": ["error1", "error2"],
  "timestamp": "2025-11-14T10:30:45.123Z"
}
```

---

## PROCESO DE ASIGNACIÓN COMPLETO (5 FASES)

### FLUJO GENERAL

```
1. Usuario carga archivo Excel en POST /api/asignaciones/iniciar
   ↓
2. ProcesoOrchestrator.ejecutarProcesoCompleto() se ejecuta ASYNC
   ↓
3. Transactional: Se ejecutan 5 fases secuenciales
   ↓
4. ProcesoAsignacion se actualiza con estado y estadísticas
   ↓
5. Frontend consulta estado con GET /api/asignaciones/proceso/{id}
```

### FASE 1: LECTURA Y VALIDACIÓN DE EXCEL

**Servicio:** `ExcelReaderService.leerArchivo()` + `AlumnoValidadorService.validarAlumnos()`

**Operaciones:**
1. Leer archivo Excel con Apache POI
2. Parsear columnas: Matricula, Nombre, Carrera, Semestre
3. Para cada fila, validar:
   - Campos no vacíos
   - Matrícula: formato ^[A-Z0-9]{5,20}$
   - Carrera en lista válida (ICA, IE, IMECA, IME, ISC, ITS)
   - Semestre válido (1-8)
   - Matrícula no duplicada en archivo
4. Guardar ErrorValidacion en BD por cada error
5. Retornar `ResultadoValidacion` con alumnos válidos y errores

**Estado:** `INICIADO` → `COMPARANDO`

**Salida:**
- `List<AlumnoExcelDTO> alumnosValidos` - para siguiente fase
- `List<ErrorValidacion>` - guardados en BD

---

### FASE 2: COMPARACIÓN Y MARCADO DE INACTIVOS

**Servicio:** `ComparadorAlumnosService.identificarInactivos()` + `InactivacionService.marcarInactivos()`

**Operaciones:**
1. Obtener lista de alumnos ACTIVOS del semestre anterior (o especificado)
2. Para cada alumno activo:
   - Si NO está en alumnos válidos del Excel → está inactivo
3. Marcar como inactivos:
   - Cambiar `Alumno.estado = INACTIVO`
   - Crear registro `AlumnoInactivo` con `MotivoInactividad = SIN_DEFINIR`
   - Preservar tutor actual si aplica
   - Registrar auditoría

**Estado:** `COMPARANDO`

**Salida:**
- `List<Alumno> alumnosAInactivar` - generalmente 5-15% de los alumnos

---

### FASE 3: LIBERACIÓN DE CUPOS

**Servicio:** `InactivacionService.liberarCupos()`

**Operaciones:**
1. Para cada `AlumnoInactivo` del proceso:
   - Si `tutorPreservado IS NULL` (inactividad NO temporal):
     - Obtener el tutor actual del alumno
     - Decrementar `tutor.cargaActual` en 1
   - Si `tutorPreservado IS NOT NULL` (inactividad temporal):
     - Mantener asignación (conservar capacidad para el reingreso)
2. Guardar tutores actualizados
3. Registrar cambios en auditoría

**Propósito:** Crear espacio para nuevos alumnos

**Estado:** `LIBERANDO_CUPOS`

---

### FASE 4: PROCESAMIENTO DE REINGRESOS

**Servicio:** `ReingresoService.procesarReingresos()`

**Operaciones:**
1. Para cada `AlumnoExcelDTO` válido del Excel:
   - Si existe en tabla `alumnos_inactivos`:
     - Cambiar `Alumno.estado = ACTIVO`
     - Crear `Asignacion` de tipo `REINGRESO`
     - Usar el tutor preservado si existe, o asignar uno nuevo
2. Registrar auditoría de cada reingreso

**Propósito:** Reactivar alumnos que vuelven después de una baja temporal

**Estado:** (sin cambio de estado explícito, pero se procesa)

---

### FASE 5: ASIGNACIÓN DE NUEVOS ALUMNOS

**Servicio:** `AsignacionService.asignarAlumnos()`

**Algoritmo principal:**
```
1. Sincronizar cargaActual de todos tutores (contar asignaciones reales)
2. Agrupar alumnos válidos por carrera
3. Agrupar tutores por carrera
4. Para cada alumno:
   a. Buscar tutores disponibles de su carrera
      - Condición: activo = true AND cargaActual < capacidadMax
      - Ordenar por: cargaActual ASC (menor carga primero)
   
   b. Si NO hay tutores en su carrera:
      - Consultar MatrizAfinidadCarrera para carreras compatibles
      - Buscar tutores en carreras compatibles ordenadas por prioridad
   
   c. Si hay múltiples candidatos:
      - Seleccionar el de menor cargaActual (round-robin equilibrado)
   
   d. Crear Asignacion:
      - alumno = alumno actual
      - tutor = tutor seleccionado
      - semestre = semestre del proceso
      - tipoAsignacion = INICIAL (o REASIGNACION si cambia tutor)
      - fechaAsignacion = now
   
   e. Incrementar tutor.cargaActual en 1
   
   f. Actualizar alumno.tutorActual = tutor
   
   g. Generar alertas si aplica:
      - Si hubo reasignación (cambio de tutor)
      - Si tutor llegó a capacidad máxima
      - Si se usó carrera compatible
   
   h. Guardar en BD (batch de 100)
   
   i. Si error:
      - Registrar ErrorAsignacion
      - Crear AlertaProceso con severidad
      - No detener proceso, continuar con siguiente alumno

5. Actualizar ProcesoAsignacion:
   - estado = COMPLETADO
   - fechaFin = now
   - totalAlumnosProcesados = cantidad procesada
   - totalAlumnosAsignados = cantidad exitosa
   - totalErrores = cantidad errores
   - detallesJson = JSON con estadísticas
```

**Excepciones manejadas:**
- `DuplicadoException` - Alumno ya asignado a este tutor en semestre
- `CapacidadExcedidaException` - Tutor lleno
- `SinTutorDisponibleException` - No hay tutor para carrera/compatible
- `Exception` general - Error de sistema

**Estado:** `ASIGNANDO` → `COMPLETADO` (o `FALLIDO` si error crítico)

---

### ESTADÍSTICAS Y AUDITORÍA

Al finalizar el proceso:

```java
ProcesoAsignacion proceso;
proceso.setEstado(EstadoProceso.COMPLETADO);
proceso.setFechaFin(LocalDateTime.now());

Map<String, Object> detalles = {
    "total_excel": 150,              // Filas en Excel
    "validos": 148,                  // Pasan validación
    "errores_validacion": 2,         // Fallan validación
    "inactivados": 15,               // Marcados inactivos
    "reingresos_pendientes": 3,      // Reactivados
    "procesados": 150,               // Total procesado
    "asignados": 147,                // Asignación exitosa
    "errores_asignacion": 1,         // Fallan asignación
    "tiempo_ms": 45000               // Duración total
};
proceso.setDetallesJson(objectMapper.writeValueAsString(detalles));
procesoRepository.save(proceso);
```

---

## BASE DE DATOS Y ESTRUCTURA DE TABLAS

### TABLA: alumnos

```sql
CREATE TABLE alumnos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    matricula VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    carrera VARCHAR(100) NOT NULL,
    semestre INT NOT NULL,
    estado VARCHAR(20) DEFAULT 'ACTIVO',        -- EstadoAlumno
    id_tutor_actual BIGINT,                     -- FK: tutores(id)
    contador_cambios_tutor INT DEFAULT 0,
    fecha_registro DATETIME NOT NULL,
    
    FOREIGN KEY (id_tutor_actual) REFERENCES tutores(id),
    INDEX idx_matricula (matricula),
    INDEX idx_estado (estado),
    INDEX idx_carrera (carrera)
);
```

---

### TABLA: tutores

```sql
CREATE TABLE tutores (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(200) NOT NULL,
    carrera VARCHAR(100) NOT NULL,
    capacidad_max INT NOT NULL,
    carga_actual INT DEFAULT 0,                 -- **CRÍTICO**
    area_atencion VARCHAR(100),
    letra_edificio VARCHAR(1),
    activo BOOLEAN DEFAULT true,
    fecha_registro DATETIME NOT NULL,
    
    INDEX idx_carrera_tutor (carrera),
    INDEX idx_activo (activo)
);
```

---

### TABLA: asignaciones

```sql
CREATE TABLE asignaciones (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_alumno BIGINT NOT NULL,
    id_tutor BIGINT NOT NULL,
    id_semestre BIGINT NOT NULL,
    tipo_asignacion VARCHAR(20) NOT NULL,      -- TipoAsignacion
    fecha_asignacion DATETIME NOT NULL,
    semestre_academico VARCHAR(20),            -- deprecated
    
    FOREIGN KEY (id_alumno) REFERENCES alumnos(id),
    FOREIGN KEY (id_tutor) REFERENCES tutores(id),
    FOREIGN KEY (id_semestre) REFERENCES semestres(id),
    UNIQUE KEY uk_asignacion_alumno_tutor_semestre (id_alumno, id_tutor, id_semestre),
    INDEX idx_alumno_asig (id_alumno),
    INDEX idx_tutor_asig (id_tutor),
    INDEX idx_semestre_asig (id_semestre)
);
```

---

### TABLA: semestres

```sql
CREATE TABLE semestres (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    codigo VARCHAR(15) UNIQUE NOT NULL,        -- Ej: "2025-2026-F1"
    nombre VARCHAR(100) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    activo BOOLEAN DEFAULT false,
    fecha_creacion DATETIME NOT NULL,
    
    UNIQUE KEY uk_semestre_codigo (codigo),
    INDEX idx_semestre_activo (activo),
    INDEX idx_semestre_codigo (codigo),
    INDEX idx_semestre_fechas (fecha_inicio, fecha_fin)
);
```

---

### TABLA: procesos_asignacion

```sql
CREATE TABLE procesos_asignacion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_semestre BIGINT,
    estado VARCHAR(30) NOT NULL,                -- EstadoProceso
    total_alumnos_procesados INT DEFAULT 0,
    total_alumnos_asignados INT DEFAULT 0,
    total_errores INT DEFAULT 0,
    total_warnings INT DEFAULT 0,
    archivo_origen VARCHAR(500),
    fecha_inicio DATETIME NOT NULL,
    fecha_fin DATETIME,
    usuario_ejecutor VARCHAR(100),
    detalles_json TEXT,                         -- JSON con estadísticas
    
    FOREIGN KEY (id_semestre) REFERENCES semestres(id),
    INDEX idx_estado_proceso (estado),
    INDEX idx_fecha_inicio (fecha_inicio),
    INDEX idx_proceso_semestre (id_semestre)
);
```

---

### TABLA: alertas_proceso

```sql
CREATE TABLE alertas_proceso (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_proceso BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL,                  -- TipoAlerta
    severidad VARCHAR(20) NOT NULL,             -- SeveridadAlerta
    descripcion TEXT NOT NULL,
    id_alumno BIGINT,
    id_tutor BIGINT,
    resuelta BOOLEAN DEFAULT false,
    fecha_creacion DATETIME NOT NULL,
    fecha_resolucion DATETIME,
    
    FOREIGN KEY (id_proceso) REFERENCES procesos_asignacion(id),
    FOREIGN KEY (id_alumno) REFERENCES alumnos(id),
    FOREIGN KEY (id_tutor) REFERENCES tutores(id),
    INDEX idx_proceso_alerta (id_proceso),
    INDEX idx_severidad (severidad),
    INDEX idx_resuelta (resuelta)
);
```

---

### TABLA: errores_validacion

```sql
CREATE TABLE errores_validacion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_proceso BIGINT NOT NULL,
    fila_excel INT,
    matricula VARCHAR(20),
    tipo_error VARCHAR(30) NOT NULL,            -- TipoError
    descripcion TEXT NOT NULL,
    dato_erroneo VARCHAR(500),
    fecha_registro DATETIME NOT NULL,
    
    FOREIGN KEY (id_proceso) REFERENCES procesos_asignacion(id),
    INDEX idx_proceso_error (id_proceso),
    INDEX idx_tipo_error (tipo_error)
);
```

---

### TABLA: alumnos_inactivos

```sql
CREATE TABLE alumnos_inactivos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_alumno BIGINT NOT NULL,
    motivo_inactividad VARCHAR(30) NOT NULL,   -- MotivoInactividad
    tutor_preservado BIGINT,                    -- Tutor anterior si aplica
    fecha_inactividad DATETIME NOT NULL,
    cupo_liberado BOOLEAN DEFAULT true,
    fecha_resolucion DATETIME,
    
    FOREIGN KEY (id_alumno) REFERENCES alumnos(id),
    FOREIGN KEY (tutor_preservado) REFERENCES tutores(id),
    INDEX idx_alumno_inact (id_alumno),
    INDEX idx_motivo (motivo_inactividad)
);
```

---

### TABLA: usuarios

```sql
CREATE TABLE usuarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(120) NOT NULL,             -- BCrypt hash
    rol VARCHAR(50) NOT NULL,                   -- RolUsuario
    activo BOOLEAN DEFAULT true,
    
    UNIQUE KEY idx_usuario_username (username),
    INDEX idx_usuario_activo (activo)
);
```

---

### TABLA: refresh_tokens

```sql
CREATE TABLE refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    token VARCHAR(500) UNIQUE NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME,                        -- null = activo, not null = revocado
    
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id),
    INDEX idx_refresh_token_usuario (id_usuario)
);
```

---

### TABLA: matriz_afinidad_carreras

```sql
CREATE TABLE matriz_afinidad_carreras (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    carrera_origen VARCHAR(100) NOT NULL,
    carrera_compatible VARCHAR(100) NOT NULL,
    prioridad INT NOT NULL,                     -- 1=máxima, mayor=menor
    
    UNIQUE KEY uk_matriz_afinidad (carrera_origen, carrera_compatible),
    INDEX idx_carrera_origen (carrera_origen)
);
```

---

### TABLA: logs_auditoria

```sql
CREATE TABLE logs_auditoria (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_proceso BIGINT,
    tipo_accion VARCHAR(50) NOT NULL,           -- TipoAccion
    tipo_entidad VARCHAR(50) NOT NULL,
    id_entidad BIGINT NOT NULL,
    descripcion TEXT,
    datos_antes TEXT,                           -- JSON
    datos_despues TEXT,                         -- JSON
    usuario VARCHAR(100),
    fecha_registro DATETIME NOT NULL
);
```

---

## SEGURIDAD Y AUTENTICACIÓN

### FLUJO DE AUTENTICACIÓN

```
1. Usuario POST /auth/login con LoginRequest
   - username: "coord_tutorias"
   - password: "123456"
   
2. AuthService.login():
   - Cargar Usuario de BD
   - Validar password con BCryptPasswordEncoder.matches()
   - Generar access token JWT (3600s)
   - Crear refresh token en BD (604800s)
   - Retornar AuthTokensResult
   
3. TokenCookieService.addAuthCookies():
   - Crear cookie "tutorias_access_token" (httpOnly, secure, sameSite=none)
   - Crear cookie "tutorias_refresh_token" (httpOnly, secure, sameSite=none)
   - Browser almacena cookies automáticamente
   
4. Para requests posteriores:
   - Browser envía automáticamente cookies en header "Cookie"
   - CookieAuthenticationFilter extrae JWT
   - JwtService valida JWT
   - Spring carga UserDetails
   - Autoriza request si roles son válidos
   
5. Cuando JWT expira:
   - Frontend POST /auth/refresh con refresh token en cookie
   - AuthService.refresh():
     - Validar refresh token en BD (no revocado, no expirado)
     - Generar nuevo access token
     - Retornar nuevo access token
   - TokenCookieService.addAuthCookies() actualiza cookie
   
6. Para logout:
   - Frontend POST /auth/logout
   - AuthService.logout():
     - Marcar refresh token como revocado (revokedAt = now)
   - TokenCookieService.clearAuthCookies():
     - Crear cookies con maxAge=0 (borrarlas)
```

---

### TOKENS Y COOKIES

#### Access Token (JWT)

```
Header: {
  "alg": "HS256",
  "typ": "JWT"
}

Payload: {
  "sub": "coord_tutorias",      // username
  "iat": 1234567890,            // issued at
  "exp": 1234571490,            // expires at (+3600s)
  "iss": "ProyectoTutorias",
  "roles": ["COORDINADOR_TUTORIAS"]
}

Signature: HMAC-SHA256(encoded_header.encoded_payload, secret)
```

**Cookie:**
```
Name: tutorias_access_token
Value: <JWT>
Max-Age: 3600
HttpOnly: true         <- No accessible from JS
Secure: true           <- HTTPS only
SameSite: none         <- Permite cross-origin
Path: /
```

---

#### Refresh Token

```
Almacenado en BD (tabla refresh_tokens):
- token: string largo y aleatorio
- id_usuario: quién pertenece
- expires_at: 7 días desde creación
- revoked_at: null (mientras esté activo)
```

**Cookie:**
```
Name: tutorias_refresh_token
Value: <refresh_token>
Max-Age: 604800
HttpOnly: true
Secure: true
SameSite: none
Path: /
```

---

### CORS CONFIGURATION

```properties
app.cors.allowed-origins=http://localhost:5173,http://localhost:3000

# Configuración:
- Allowed origins: localhost:5173 (React dev), localhost:3000 (fallback)
- Allowed methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
- Allowed headers: * (todos)
- Expose headers: Set-Cookie, Authorization, Content-Type
- Allow credentials: true
- Max age: 3600s
```

---

### SEGURIDAD - VALIDACIONES

1. **Entrada:**
   - Bean Validation en DTOs (@NotBlank, @NotNull, @Pattern)
   - AlumnoValidadorService valida datos del Excel
   - SearchQuerySanitizer limpia parámetros de búsqueda

2. **Autorización:**
   - JWT en cookies HttpOnly (CSRF-safe)
   - Roles verificados en SecurityConfig
   - @PreAuthorize("hasRole('COORDINADOR_TUTORIAS')") en controladores

3. **Base de datos:**
   - Foreign keys mantienen integridad referencial
   - Unique constraints previenen duplicados
   - Pessimistic locks en actualizaciones críticas (TutorRepository.findByIdForUpdate)

4. **Logging:**
   - AuditoriaService registra todos los cambios
   - LogAuditoria guarda datosAntes y datosDespues para auditoría

---

## DEPENDENCIAS CRÍTICAS

### pom.xml (Maven)

**Spring Boot Parent:** 3.3.1

```xml
<!-- Spring Framework -->
<spring-boot-starter-web>           <!-- REST, MVC, embedded Tomcat -->
<spring-boot-starter-security>      <!-- Autenticación, autorización -->
<spring-boot-starter-data-jpa>      <!-- Hibernatespan, JPA -->
<spring-boot-starter-validation>    <!-- Bean Validation -->

<!-- Base de datos -->
<mysql-connector-j>                 <!-- MySQL driver -->
<spring-boot-starter-h2>            <!-- H2 para tests -->

<!-- Utilidades -->
<lombok>1.18.34</lombok>            <!-- Anotaciones @Data, @RequiredArgsConstructor, etc. -->
<mapstruct>1.5.5.Final</mapstruct>  <!-- DTO mapping -->
<jackson-databind>                  <!-- JSON serialization -->

<!-- Seguridad -->
<jjwt-api>                          <!-- JWT tokens -->
<jjwt-impl>
<jjwt-jackson>

<!-- Excel -->
<poi>5.2.5</poi>                    <!-- Apache POI para leer Excel -->
<poi-ooxml>5.2.5</poi-ooxml>       <!-- Soporte XLSX -->

<!-- PDF -->
<openpdf>1.3.40</openpdf>          <!-- Generación de PDFs -->

<!-- Test -->
<spring-boot-starter-test>
<junit-jupiter>
```

---

### Versiones Clave

| Dependencia | Versión | Uso |
|------------|---------|-----|
| Java | 21 | Lenguaje |
| Spring Boot | 3.3.1 | Framework |
| Hibernate | 6.x | ORM |
| MySQL | 8.x (runtime) | BD producción |
| H2 | 2.x | BD tests |
| POI | 5.2.5 | Lectura Excel |
| JWT (jjwt) | 0.12.x | Tokens JWT |
| Lombok | 1.18.34 | Generación código |
| MapStruct | 1.5.5 | Mapping DTOs |

---

### Transactionalidad

```java
// Configuración global:
spring.jpa.properties.hibernate.jdbc.batch_size=100
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true

// En servicios:
@Transactional                              // Read-write, level REQUIRED
@Transactional(readOnly = true)             // Read-only queries
@Transactional(propagation = REQUIRES_NEW)  // Nueva transacción
@Transactional(propagation = NESTED)        // Sub-transacción

// Locks en operaciones críticas:
@Lock(LockModeType.PESSIMISTIC_WRITE)       // Evitar race conditions
```

---

### Pool de Conexiones (HikariCP)

```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000  # 30s
```

---

### Async/Threading

```java
// Executor para ProcesoOrchestrator:
spring.task.execution.pool.core-size=2        # threads de inicio
spring.task.execution.pool.max-size=5          # máximo threads
spring.task.execution.pool.queue-capacity=100  # cola de tasks
spring.task.execution.thread-name-prefix=async-

// Configurado en AsyncConfiguration:
@Bean("asignacionExecutor")
public Executor asignacionExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(5);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("asignacion-");
    executor.initialize();
    return executor;
}
```

---

## ENDPOINTS RESUMEN

| Método | Endpoint | Autenticación | Rol Requerido | Descripción |
|--------|----------|---------------|---------------|-------------|
| POST | `/auth/login` | No | - | Autenticar usuario |
| POST | `/auth/refresh` | No | - | Renovar access token |
| POST | `/auth/logout` | Sí | - | Cerrar sesión |
| GET | `/auth/me` | Sí | - | Info usuario actual |
| POST | `/auth/register` | Sí | COORDINADOR | Crear usuario |
| POST | `/api/asignaciones/iniciar` | Sí | - | Iniciar proceso bulk |
| GET | `/api/asignaciones/proceso/{id}` | Sí | - | Estado del proceso |
| GET | `/api/asignaciones/proceso/{id}/alertas` | Sí | - | Alertas del proceso |
| POST | `/api/asignaciones/cambio-tutor` | Sí | COORDINADOR | Cambiar tutor alumno |
| GET | `/api/alumnos` | Sí | - | Listar alumnos |
| GET | `/api/alumnos/buscar` | Sí | - | Buscar alumnos (ranking) |
| POST | `/api/alumnos` | Sí | - | Crear alumno |
| GET | `/api/tutores` | Sí | - | Listar tutores |
| GET | `/api/tutores/buscar` | Sí | - | Buscar tutores |
| POST | `/api/tutores` | Sí | - | Crear tutor |
| GET | `/api/semestres` | Sí | - | Listar semestres |
| POST | `/api/semestres` | Sí | - | Crear semestre |
| GET | `/api/reportes/carrera/{id}` | Sí | - | Reporte por carrera |

---

## RESUMEN ARQUIT EDÓNICO

### FORTALEZAS

1. **Separación de Capas:** Domain, Application, Infrastructure claramente separadas
2. **Async Processing:** ProcesoOrchestrator usa @Async para no bloquear servidor
3. **Validación Multinivel:** Excel parsing → validación → negocio
4. **Auditoría Completa:** LogAuditoria registra todos los cambios con datosAntes/Después
5. **Búsqueda Inteligente:** Relevance-based scoring en DB con native SQL
6. **Seguridad:** JWT en cookies HttpOnly, CORS controlado, roles granulares
7. **Sincronización de Datos:** TutorSincronizacionService evita desincronización de cargaActual

### PUNTOS DE CUIDADO

1. **cargaActual CRÍTICO:** NUNCA actualizar directamente, siempre usar servicios
2. **Race Conditions:** Usar findByIdForUpdate() en operaciones concurrentes
3. **Batch Procesamiento:** Limpiar EntityManager cada 100 items
4. **Transaccionalidad:** ProcesoOrchestrator es @Transactional pero async
5. **CORS:** Requiere credenciales=true en frontend + secure cookies

---

**FIN DEL MAPEO COMPLETO**
