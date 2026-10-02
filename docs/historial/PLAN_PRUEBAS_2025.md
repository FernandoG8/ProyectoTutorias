# PLAN DE PRUEBAS – Sistema de Gestión de Tutorías (ProyectoTutoriasBackend)

**Versión del Plan:** 1.0
**Fecha de Elaboración:** 17 de noviembre de 2025
**Estado:** Activo

---

## 📋 Tabla de Contenidos

1. [Introducción](#1-introducción)
2. [Alcance y Enfoque de Pruebas](#2-alcance-y-enfoque-de-pruebas)
3. [Estrategia de Pruebas](#3-estrategia-de-pruebas)
4. [Gestión de Riesgos](#4-gestión-de-riesgos)
5. [Recursos de Prueba](#5-recursos-de-prueba)
6. [Criterios de Éxito y Aceptación](#6-criterios-de-éxito-y-aceptación)
7. [Cronograma de Pruebas por Versión](#7-cronograma-de-pruebas-por-versión)
8. [Herramientas de Prueba Recomendadas](#8-herramientas-de-prueba-recomendadas)
9. [Casos de Prueba Detallados](#9-casos-de-prueba-detallados)
10. [Conclusiones y Próximos Pasos](#10-conclusiones-y-próximos-pasos)

---

## 1. Introducción

### 1.1 Contexto del Sistema

**ProyectoTutoriasBackend** es un **Sistema de Gestión Integral de Tutorías Académicas** diseñado para administrar la asignación y supervisión de tutores a estudiantes en una institución educativa superior (Universidad Autónoma de Campeche).

**Arquitectura Actual:**
- **Backend:** Spring Boot 3.3 + Java 21 + MySQL/PostgreSQL
- **Frontend:** React 19 + Vite + TypeScript
- **Base de Datos:** 12 entidades JPA con relaciones complejas
- **API REST:** 40+ endpoints con autenticación JWT

### 1.2 Objetivo General del Plan de Pruebas

Establecer una **estrategia de pruebas sistemática y progresiva** que garantice:

1. **Calidad del código** mediante pruebas unitarias e integración
2. **Confiabilidad de procesos críticos** (asignación masiva de tutores, sincronización de carga)
3. **Seguridad de datos** (autenticación, autorización, integridad)
4. **Rendimiento bajo carga** (manejo de 1000+ estudiantes)
5. **Cobertura progresiva** del 12% actual al 80%+ en módulos críticos

### 1.3 Alcance General

**Incluido:**
- ✅ Pruebas unitarias (servicios, entidades, DTOs)
- ✅ Pruebas de integración (servicios + repositorios)
- ✅ Pruebas de API REST (controladores, autenticación)
- ✅ Pruebas de regresión
- ✅ Pruebas de rendimiento (asignación masiva)
- ✅ Pruebas de seguridad (JWT, CORS, autenticación)

**Excluido:**
- ❌ Pruebas del frontend (React) - Separado en plan de pruebas frontend
- ❌ Pruebas de carga extrema (>10,000 estudiantes)
- ❌ Pruebas de penetración avanzadas
- ❌ Pruebas de recuperación ante desastres (DR)

---

## 2. Alcance y Enfoque de Pruebas

### 2.1 Tipos de Pruebas a Realizar

#### 2.1.1 Pruebas Unitarias (Unit Tests)

**Objetivo:** Verificar el comportamiento aislado de métodos y funciones

**Alcance:**
- Servicios: 18 implementaciones
- Entidades JPA: 12 clases
- DTOs: 40+ clases
- Enumeraciones: 9 tipos
- Repositorios custom: 2 (AlumnoSearchRepository, TutorSearchRepository)

**Ejemplo de prueba unitaria:**
```java
@Test
@DisplayName("Debe validar correctamente una matrícula de alumno")
void testValidarMatriculaAlumno() {
    // Arrange
    Alumno alumno = TestDataBuilder.alumnoBuilder()
        .matricula("A001234")
        .estado(EstadoAlumno.ACTIVO)
        .build();

    // Act
    boolean esValido = alumnoService.validarMatricula(alumno.getMatricula());

    // Assert
    assertThat(esValido).isTrue();
}
```

#### 2.1.2 Pruebas de Integración (Integration Tests)

**Objetivo:** Verificar la interacción entre componentes (Servicios + Repositorios + BD)

**Alcance:**
- Servicios que usan repositorios
- Transacciones y persistencia
- Relaciones entre entidades
- Validaciones a nivel de BD

**Ejemplo:**
```java
@SpringBootTest
@Transactional
class SemestreServiceIntegrationTest {

    @Test
    @DisplayName("Debe crear un semestre y recuperarlo de la BD")
    void testCrearYRecuperarSemestre(@Autowired SemestreRepository repo) {
        // Arrange
        Semestre semestre = TestDataBuilder.semestreBuilder()
            .codigo("2025-2026-F1")
            .nombre("Primavera 2025")
            .build();

        // Act
        Semestre guardado = repo.save(semestre);
        Semestre recuperado = repo.findByCodigo("2025-2026-F1").orElse(null);

        // Assert
        assertThat(recuperado).isNotNull()
            .extracting(Semestre::getCodigo)
            .isEqualTo("2025-2026-F1");
    }
}
```

#### 2.1.3 Pruebas de API REST (Controller Tests)

**Objetivo:** Verificar los endpoints HTTP de forma funcional

**Alcance:**
- 10 controladores REST
- 40+ endpoints
- Validación de request/response
- Códigos HTTP (200, 400, 401, 404, 409, 500)
- Autenticación JWT

**Ejemplo:**
```java
@SpringBootTest
@AutoConfigureMockMvc
class AsignacionControllerTest {

    @Test
    @DisplayName("POST /api/asignaciones/iniciar debe aceptar archivo Excel válido")
    @WithMockUser(roles = "COORDINADOR")
    void testIniciarAsignacionConArchivoValido() throws Exception {
        // Arrange
        MockMultipartFile archivo = new MockMultipartFile(
            "archivo", "alumnos.xlsx", "application/vnd.ms-excel",
            cargarArchivoExcelValido()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/asignaciones/iniciar")
            .file(archivo)
            .param("semestreAcademico", "2025-2026-F1")
            .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.estado").value("INICIADO"));
    }
}
```

#### 2.1.4 Pruebas de Regresión

**Objetivo:** Garantizar que cambios no rompan funcionalidad existente

**Alcance:**
- Validación de cambios en APIs existentes
- Compatibilidad hacia atrás
- Enumeraciones y tipos

#### 2.1.5 Pruebas de Rendimiento y Carga

**Objetivo:** Validar comportamiento bajo carga típica

**Alcance:**
- Asignación masiva de 250-500 estudiantes
- Búsqueda con ranking de 1000+ registros
- Generación de reportes PDF/Excel
- Sincronización de tutores

**Herramientas:** JMeter, Gatling, o pruebas custom

#### 2.1.6 Pruebas de Seguridad

**Objetivo:** Verificar controles de seguridad

**Alcance:**
- Autenticación JWT (generación, validación, expiración)
- Autorización por roles (COORDINADOR, TUTOR, ADMIN)
- CORS (origen, credenciales)
- Inyección SQL (validación de queries)
- CSRF en endpoints POST/PUT/DELETE

### 2.2 Funcionalidades Críticas a Probar

#### 2.2.1 Módulo de Asignación (CRÍTICO - 0% cobertura actual)

**Funcionalidad:** Asignación inteligente de tutores a estudiantes mediante Excel

**Procesos a Probar:**
1. **Fase 1: Lectura de Excel**
   - Parseo correcto de archivo
   - Validación de campos obligatorios
   - Detección de errores de formato
   - Gestión de caracteres especiales

2. **Fase 2: Validación de Datos**
   - Validar matriculas existentes
   - Validar estado de alumnos
   - Validar semestre activo
   - Detectar duplicados

3. **Fase 3: Cálculo de Afinidad**
   - Ranking por carrera
   - Ranking por semestre
   - Cálculo de compatibilidad
   - Matriz de afinidad

4. **Fase 4: Asignación Inteligente**
   - Respetar capacidad de tutores
   - Distribuir carga equitativamente
   - Manejar casos sin asignación posible
   - Logging de decisiones

5. **Fase 5: Persistencia**
   - Crear registros de Asignacion
   - Actualizar cargaActual de tutores
   - Registrar alertas
   - Manejo de excepciones con rollback

**Casos de Prueba:**
- ✅ Asignación exitosa de 100 estudiantes
- ✅ Asignación con tutores saturados
- ✅ Asignación con alumnos inactivos
- ✅ Archivo Excel corrupto
- ✅ Excepción durante persistencia (rollback)
- ✅ Cálculo de afinidad con matriz vacía

#### 2.2.2 Módulo de Autenticación (CRÍTICO - 77% cobertura actual)

**Funcionalidad:** Login, JWT, refresh tokens, logout

**Procesos a Probar:**
1. **Login**
   - Credenciales válidas
   - Credenciales inválidas
   - Usuario deshabilitado
   - Password hasheado con BCrypt
   - Generación de JWT (3600s)
   - Refresh token en cookie HttpOnly

2. **Refresh Token**
   - Validar token en cookie
   - Generar nuevo JWT
   - Rechazar tokens revocados
   - Expiración correcta

3. **Logout**
   - Marcar refresh token como revocado
   - Limpiar cookies
   - Invalidar sesión

**Casos de Prueba:**
- ✅ Login con usuario válido
- ✅ Login con password incorrecto
- ✅ Login con usuario inexistente
- ✅ Refresh con token válido
- ✅ Refresh con token expirado
- ✅ Refresh con token revocado
- ✅ Logout marca token como revocado

#### 2.2.3 Módulo de Búsqueda (IMPORTANTE - Ranking SQL)

**Funcionalidad:** Búsqueda de alumnos/tutores con algoritmo de ranking nativo

**Procesos a Probar:**
1. **Búsqueda por Matrícula**
   - Exacto: 1000 puntos
   - Comienza con: 800 puntos
   - Contiene: 200 puntos

2. **Búsqueda por Nombre**
   - Exacto: 500 puntos
   - Comienza con: 420 puntos
   - Palabra completa: 400 puntos
   - Contiene: 200 puntos

3. **Filtros**
   - Por estado (ACTIVO, INACTIVO)
   - Por carrera
   - Por semestre
   - Combinación de filtros

4. **Ordenamiento**
   - RELEVANCE (score DESC)
   - MATRICULA (ascendente)
   - NOMBRE (ascendente)

**Casos de Prueba:**
- ✅ Búsqueda exacta de matrícula
- ✅ Búsqueda parcial de nombre
- ✅ Búsqueda con filtros múltiples
- ✅ Búsqueda sin resultados
- ✅ Paginación (page=0, size=50)
- ✅ Orden por relevancia

#### 2.2.4 Módulo de Semestres (IMPORTANTE - 90% cobertura actual)

**Funcionalidad:** CRUD de semestres, validación de período, cambio de semestre activo

**Procesos a Probar:**
1. **Crear Semestre**
   - Validar patrón código (YYYY-YYYY-FN)
   - Validar fechas (fin > inicio)
   - Código único
   - Constraints a nivel BD

2. **Cambiar Semestre Activo**
   - Solo un semestre activo por vez
   - Transición correcta
   - Histórico de cambios

3. **Estadísticas de Semestre**
   - Total alumnos en semestre
   - Total tutores asignados
   - Total asignaciones

**Casos de Prueba:**
- ✅ Crear semestre con patrón válido
- ✅ Rechazar código inválido
- ✅ Rechazar fechas inversas
- ✅ Rechazar código duplicado
- ✅ Cambiar semestre activo
- ✅ Validar único semestre activo

#### 2.2.5 Módulo de Reportes (IMPORTANTE - 0% cobertura actual)

**Funcionalidad:** Generación de PDF y Excel con datos de asignaciones

**Procesos a Probar:**
1. **Reporte PDF**
   - Generación correcta
   - Datos completos
   - Formato válido
   - Descarga correcta

2. **Reporte Excel**
   - Generación correcta
   - Columnas y datos
   - Validación de celdas
   - Descarga correcta

3. **Reporte por Carrera**
   - Filtro correcto
   - Datos consolidados
   - Totales y subtotales

**Casos de Prueba:**
- ✅ Generar PDF con datos válidos
- ✅ Generar Excel con datos válidos
- ✅ Reporte por carrera específica
- ✅ Manejo de reportes vacíos

#### 2.2.6 Módulo de Sincronización de Tutores (CRÍTICO - 0% cobertura)

**Funcionalidad:** Sincronizar cargaActual de tutores con asignaciones reales

**Procesos a Probar:**
1. **Detectar Inconsistencias**
   - Comparar cargaActual vs. COUNT(*) asignaciones
   - Identificar tutores sobrecargados
   - Verificar alumnos sin tutor

2. **Corregir Inconsistencias**
   - Actualizar cargaActual
   - Crear alertas
   - Logging de cambios

**Casos de Prueba:**
- ✅ Sincronización con datos consistentes
- ✅ Sincronización con tutores sobrecargados
- ✅ Sincronización con alumnos sin tutor
- ✅ Generación correcta de alertas

### 2.3 Fuera de Alcance (Explícitamente)

| Elemento | Razón |
|----------|-------|
| Pruebas del Frontend (React) | Separado en plan frontend, usa Jest + React Testing Library |
| Pruebas de penetración avanzadas | Requiere especialista en seguridad |
| Pruebas de carga extrema (>10K usuarios) | Fuera de escala actual |
| Pruebas de recuperación ante desastres | Corresponde a DevOps/Infraestructura |
| Pruebas de bases de datos (backup/restore) | DBA responsibilities |

---

## 3. Estrategia de Pruebas

### 3.1 Niveles de Prueba

```
┌─────────────────────────────────────────────────────┐
│             PRUEBAS DE SISTEMA (E2E)               │  5%
│  (Seleccionadas: flujos críticos completos)        │
├─────────────────────────────────────────────────────┤
│           PRUEBAS DE INTEGRACIÓN                    │ 20%
│  (Servicios + Repositorios + BD H2)                │
├─────────────────────────────────────────────────────┤
│           PRUEBAS DE API REST                       │ 30%
│  (Controladores + Validación HTTP)                 │
├─────────────────────────────────────────────────────┤
│            PRUEBAS UNITARIAS                        │ 45%
│  (Servicios, Entidades, DTOs, Repos)              │
└─────────────────────────────────────────────────────┘

Distribución Recomendada: 45% Unitarias, 30% API, 20% Integración, 5% Sistema
```

### 3.2 Estrategia de Datos de Prueba

#### 3.2.1 TestDataBuilder Pattern (YA IMPLEMENTADO)

El proyecto usa **TestDataBuilder** (297 LOC) para crear fixtures reutilizables:

```java
// Reutilizable en múltiples tests
Alumno alumno = TestDataBuilder.alumnoBuilder()
    .matricula("A001234")
    .nombre("Juan Pérez")
    .estado(EstadoAlumno.ACTIVO)
    .semestre(7)
    .build();

// Con valores por defecto inteligentes
Semestre semestre = TestDataBuilder.semestreBuilder().build();
// Genera: codigo="2025-2026-F1", estado=ACTIVO, etc.
```

#### 3.2.2 Datos de Prueba por Nivel

| Nivel | Datos | Origen | Cantidad |
|-------|-------|--------|----------|
| **Unitario** | Mocks con Mockito | TestDataBuilder | Min |
| **Integración** | BD H2 en-memoria | TestDataBuilder + @Sql | 10-100 registros |
| **API REST** | BD H2 + Spring Security Mock | @WebMvcTest + JWT fake | 50-200 registros |
| **Sistema (E2E)** | BD real/staging | Scripts SQL + fixtures | 500+ registros |

#### 3.2.3 Scripts SQL de Datos de Prueba

**Ubicación:** `backend/src/test/resources/`

Crear archivos SQL para cada módulo:
```sql
-- test-data-alumnos.sql
INSERT INTO alumno (id, matricula, nombre, estado, semestre)
VALUES
  (1, 'A001000', 'Juan Pérez', 'ACTIVO', 7),
  (2, 'A001001', 'María García', 'ACTIVO', 7),
  (3, 'A001002', 'Carlos López', 'INACTIVO', 0);

-- test-data-tutores.sql
INSERT INTO tutor (id, nombre, carrera, capacidad_maxima, carga_actual)
VALUES
  (1, 'Dr. García', 'Ingeniería', 5, 3),
  (2, 'Dra. López', 'Administración', 4, 4);
```

### 3.3 Gestión de Resultados y Defectos

#### 3.3.1 Clasificación de Bugs

| Severidad | Ejemplos | Impacto | Acción |
|-----------|----------|--------|--------|
| **CRÍTICO** | Asignación fallida, pérdida de datos, error en auth | Bloquea features | Arreglar inmediatamente |
| **ALTO** | Búsqueda sin resultados, reportes incompletos | Afecta usabilidad | Arreglar en sprint actual |
| **MEDIO** | Validaciones menores, rendimiento lento | Degrada experiencia | Arreglar en siguiente sprint |
| **BAJO** | Textos, estilos, optimizaciones | Cosmético | Agendar para futura |

#### 3.3.2 Flujo de Defectos

```
Test Falla (FAIL)
    ↓
Crear Issue en GitHub/Jira
  - Descripción clara
  - Stacktrace
  - Pasos de reproducción
  - Entorno (BD, versión, OS)
    ↓
Asignar a Desarrollador
    ↓
Desarrollador crea fix
    ↓
Ejecutar test de regresión
    ↓
Verificar que PASA
    ↓
Deploy a siguiente versión
```

### 3.4 Ciclo de Testing

```
SEMANA 1 (Preparación)
├─ Diseñar casos de prueba
├─ Configurar entorno
└─ Crear datos de prueba

SEMANA 2-3 (Ejecución)
├─ Ejecutar tests unitarios
├─ Ejecutar tests integración
├─ Ejecutar tests API
└─ Reportar defectos

SEMANA 4 (Cierre)
├─ Desarrollador arregla bugs
├─ Regresión de tests
├─ Análisis de cobertura
└─ Reporte final
```

---

## 4. Gestión de Riesgos

### 4.1 Riesgos Identificados

#### 4.1.1 Riesgos Técnicos

| # | Riesgo | Descripción | Probabilidad | Impacto | Mitigación |
|---|--------|-------------|--------------|---------|-----------|
| **R1** | Asignación masiva incompleta | Excel muy grande causa timeout o memoria agotada | MEDIA | CRÍTICO | Pruebas de carga con 500+ registros, optimizar queries |
| **R2** | Inconsistencia de datos | cargaActual de tutores desincronizado con asignaciones | ALTA | CRÍTICO | Sincronización automática, tests de integridad |
| **R3** | Inyección SQL en búsqueda | Query ranking vulnerable a injection | BAJA | CRÍTICO | Usar parametrización nativa SQL, input validation |
| **R4** | JWT expiración no manejada | Token expirado no genera nuevo correctamente | MEDIA | ALTO | Tests de refresh token, manejo de 401 en frontend |
| **R5** | Pérdida de tokens refresh | Cookie HttpOnly no se envía/recibe | BAJA | ALTO | Tests con MockMvc, validar CORS |
| **R6** | Cambio de tutor sin validación | Asignar tutor a alumno que ya tiene uno | MEDIA | ALTO | Validación en endpoint, tests de precondiciones |
| **R7** | Reportes generados incorrectamente | PDF/Excel con datos faltantes o corruptos | MEDIA | MEDIO | Tests de generación, validación de estructura |
| **R8** | Búsqueda sin paginación | Query sin límite devuelve miles de registros | BAJA | MEDIO | Validar límites en test, usar LIMIT SQL |

#### 4.1.2 Riesgos de Testing

| # | Riesgo | Descripción | Probabilidad | Impacto | Mitigación |
|---|--------|-------------|--------------|---------|-----------|
| **R9** | Cobertura insuficiente | Solo 12% cubierto, módulos críticos sin tests | ALTA | CRÍTICO | Plan gradual a 80%, métricas en CI/CD |
| **R10** | Tests frágiles | Tests que fallan por cambios menores | MEDIA | MEDIO | Usar builders pattern, evitar hardcoding |
| **R11** | BD de test con datos reales | Usar BD prod en tests por error | BAJA | CRÍTICO | Configuración clara, tests con H2, CI/CD checks |
| **R12** | Ambiente de test inconsistente | Diferentes versiones de JDK, MySQL entre miembros | MEDIA | MEDIO | Docker Compose para tests, documentar requisitos |

#### 4.1.3 Riesgos de Negocio

| # | Riesgo | Descripción | Probabilidad | Impacto | Mitigación |
|---|--------|-------------|--------------|---------|-----------|
| **R13** | Retraso en entrega | Testing toma más de lo estimado | MEDIA | ALTO | Ejecución paralela, herramientas de CI/CD |
| **R14** | Defectos en producción | Bugs críticos en usuarios finales | MEDIA | CRÍTICO | Testing exhaustivo, tests de regresión antes de deploy |
| **R15** | Falta de documentación | Tests no documentados, difícil mantener | ALTA | MEDIO | Documentar casos, comentarios en código, README |

### 4.2 Estrategias de Mitigación

#### 4.2.1 Para Riesgos Técnicos

**R1 - Asignación Masiva Incompleta:**
```
- Prueba con archivo de 500 alumnos
- Validar memoria usada (heap)
- Timeout configurado a 30 segundos (ajustable)
- Logging de progreso cada 50 registros
```

**R2 - Inconsistencia de Datos:**
```
- Test de sincronización automática post-asignación
- Trigger en BD para auditar cambios de cargaActual
- Task scheduling diaria para resincronizar
```

**R3 - Inyección SQL:**
```
- Usar NamedParameterJdbcTemplate en lugar de string concat
- Validar input con regex: ^[a-zA-Z0-9ñáéíóú%_]*$
- Test de injection: ' OR '1'='1
```

**R6 - Cambio de Tutor sin Validación:**
```
- Test: Validar que alumno está activo
- Test: Validar que tutor origen tiene alumno asignado
- Test: Validar que tutor destino tiene capacidad
- Máximo 2 cambios por alumno
```

#### 4.2.2 Para Riesgos de Testing

**R9 - Cobertura Insuficiente:**
```
META: 12% → 80% (Fase 2-4)

Fase 1 (v1.0.0):  20% (5 servicios críticos)
Fase 2 (v1.1.0):  50% (10 servicios + APIs)
Fase 3 (v1.2.0):  75% (14 servicios + Controllers)
Fase 4 (v2.0.0):  85%+ (Todos los módulos)

Métrica en CI/CD: Fallar si cobertura < 75% en módulos críticos
```

**R10 - Tests Frágiles:**
```
- Usar TestDataBuilder para fixtures reutilizables
- No hardcodear IDs (usar auto-increment)
- Usar AssertJ para assertions legibles
- No depender de orden de pruebas
```

**R12 - Ambiente Inconsistente:**
```
docker-compose -f backend/docker-compose-test.yml up
# Ejecuta: MySQL 8, H2 en-memory, Redis (opcional)
# Todos los tests usan perfil 'test'
```

#### 4.2.3 Plan de Contingencia

| Escenario | Acción |
|-----------|--------|
| **Test suite tarda >30 min** | Dividir en tests rápidos (<10s) y lentos (>10s), ejecutar separado en CI/CD |
| **BD de test se corrompe** | Script de cleanup automático antes de cada suite, resetear con @Sql |
| **Fallan múltiples tests sin cambio** | Revisar entorno (JDK, MySQL), resetear caché Maven, git clean |
| **Defecto crítico encontrado tarde** | Incluir smoke tests en pre-commit hooks con Husky |

---

## 5. Recursos de Prueba

### 5.1 Entornos de Prueba

#### 5.1.1 Entorno Local de Desarrollo

```
┌─────────────────────────────────────────┐
│    MacOS/Windows/Linux (WSL2)          │
│  ┌──────────────────────────────────┐  │
│  │  IDE: IntelliJ / VS Code        │  │
│  ├──────────────────────────────────┤  │
│  │  JDK 21 (OpenJDK o Eclipse)     │  │
│  ├──────────────────────────────────┤  │
│  │  Maven 3.9+ / Gradle            │  │
│  ├──────────────────────────────────┤  │
│  │  H2 Database (in-memory)        │  │
│  ├──────────────────────────────────┤  │
│  │  Git + GitHub                   │  │
│  └──────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

#### 5.1.2 Entorno CI/CD (GitHub Actions)

```yaml
# .github/workflows/test.yml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      mysql:
        image: mysql:8
        env:
          MYSQL_ROOT_PASSWORD: root
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3

    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'

      - name: Run Tests
        run: mvn clean test jacoco:report

      - name: Upload Coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./backend/target/site/jacoco/jacoco.xml
```

#### 5.1.3 Entorno de Staging (Pre-Producción)

```
┌────────────────────────────────────┐
│     Docker Compose (Staging)       │
│  ┌────────────────────────────────┤
│  │  Spring Boot 3.3 (prod profile) │
│  │  MySQL 8 (persistente)         │
│  │  Redis (cache, opcional)       │
│  │  Nginx reverse proxy           │
│  └────────────────────────────────┘
```

### 5.2 Hardware y Software Requerido

#### 5.2.1 Mínimo

| Recurso | Mínimo | Recomendado |
|---------|--------|-------------|
| CPU | 4 cores | 8 cores |
| RAM | 8 GB | 16 GB |
| Disco | 10 GB | 50 GB |
| JDK | 21 LTS | 21.0.1+ |
| Maven | 3.8+ | 3.9+ |
| MySQL | 8.0 | 8.0.35+ |
| Docker | 20.10+ | 24.0+ |
| Git | 2.30+ | 2.40+ |

#### 5.2.2 Software de Testing

| Tool | Versión | Propósito | Licencia |
|------|---------|----------|---------|
| JUnit 5 | 5.9+ | Framework unitario | Apache 2.0 |
| Mockito | 4.8+ | Mocking | MIT |
| Spring Test | 6.0+ | Testing Spring Boot | Apache 2.0 |
| H2 Database | 2.1+ | BD en-memoria | MPL 2.0 |
| JaCoCo | 0.8.10 | Cobertura | EPL 1.0 |
| AssertJ | 3.24+ | Assertions fluidas | Apache 2.0 |
| Testcontainers | 1.19+ | BD containerizada | MIT |
| REST Assured | 5.3+ | Tests API | Apache 2.0 |

### 5.3 Configuración de Perfiles Spring

#### 5.3.1 Perfil de Testing (`test`)

**Archivo:** `backend/src/test/resources/application-test.properties`

```properties
# Base de Datos H2 (in-memory)
spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop

# Sin logs verbosos
logging.level.org.hibernate=WARN
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# JWT Test Secret
jwt.secret=test-secret-key-super-segura-para-tests

# Timeout tests
spring.test.mockmvc.print=true
```

#### 5.3.2 Perfil de Desarrollo (`dev`)

Usa MySQL local en `localhost:3306`

#### 5.3.3 Perfil de Producción (`prod`)

Variables de entorno para RDS/Cloud DB

### 5.4 Docker Compose para Testing

**Archivo:** `backend/docker-compose-test.yml`

```yaml
version: '3.8'

services:
  mysql-test:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: test_tutorias
      MYSQL_ROOT_PASSWORD: test_root
      MYSQL_PASSWORD: test_user
    ports:
      - "3307:3306"  # Puerto diferente para no conflictuar
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  # H2 Console (opcional, para debugging)
  h2-console:
    image: oscarfonts/h2:latest
    ports:
      - "8082:8082"
```

---

## 6. Criterios de Éxito y Aceptación

### 6.1 Métricas de Cobertura

#### 6.1.1 Cobertura Global por Versión

| Versión | Meta | Módulos Críticos | Plazo |
|---------|------|------------------|-------|
| **v1.0.0** | 20% | Auth (100%), Semestre (95%) | 4 semanas |
| **v1.1.0** | 50% | +Búsqueda (90%), +Alumnos (85%) | 4 semanas |
| **v1.2.0** | 75% | +Asignaciones (80%), +Reportes (75%) | 6 semanas |
| **v2.0.0** | 85%+ | Todos los módulos | 8 semanas |

#### 6.1.2 Cobertura por Módulo (Mínimo Aceptable)

| Módulo | Meta v1.0 | Meta v1.2 | Meta v2.0 |
|--------|-----------|-----------|-----------|
| **Auth Service** | 100% | 100% | 100% |
| **Semestre Service** | 95% | 100% | 100% |
| **Asignación Service** | 0% → 50% | 80% | 95% |
| **Búsqueda (SQL Ranking)** | 0% → 60% | 90% | 95% |
| **Reportes Service** | 0% → 40% | 70% | 90% |
| **Controladores** | 0% → 30% | 70% | 85% |
| **Sincronización Tutores** | 0% → 50% | 75% | 90% |

**Comando para verificar cobertura:**
```bash
cd backend
mvn clean test jacoco:report
# Resultado: backend/target/site/jacoco/index.html
```

### 6.2 Criterios de Pase/Fallo

#### 6.2.1 Suite de Tests

| Criterio | Pasa | Falla |
|----------|------|-------|
| **Tests Ejecutados** | 100% pasan | >0 fallos |
| **Cobertura Línea** | ≥ Meta % | < Meta % |
| **Cobertura Rama** | ≥ Meta % | < Meta % |
| **Cobertura Método** | ≥ Meta % | < Meta % |
| **Tiempo Ejecución** | < 10 min | > 15 min |
| **Warnings/Errores Compiler** | 0 | > 0 |

#### 6.2.2 Criterios de Prueba por Funcionalidad

**Asignación Masiva:**
- ✅ Procesa 500 alumnos en <30 segundos
- ✅ Detecta y reporta 100% de errores en Excel
- ✅ Crea correctamente todas las asignaciones
- ✅ Actualiza cargaActual de tutores correctamente
- ✅ Rollback completo en caso de error

**Autenticación:**
- ✅ Login válido genera JWT + Refresh Token
- ✅ Refresh Token válido genera nuevo JWT
- ✅ Logout marca token como revocado
- ✅ Endpoint protegido rechaza sin JWT

**Búsqueda:**
- ✅ Ranking devuelve resultados ordenados por relevancia
- ✅ Filtros funcionan correctamente combinados
- ✅ Paginación entrega exactamente 50 registros
- ✅ Sin resultados devuelve 200 OK con array vacío

### 6.3 Condiciones para Liberación

#### 6.3.1 Pre-Requisitos para Deploy

```
☐ Cobertura global ≥ Meta %
☐ Módulos críticos ≥ Meta %
☐ 100% de tests pasen
☐ Sin defectos CRÍTICOS abiertos
☐ Máximo 2 defectos ALTO abiertos
☐ Aprobación de Code Review
☐ Tests de regresión verdes
☐ Performance benchmarks OK
```

#### 6.3.2 Definición de "Listo para Producción"

1. **Código:** Cobertura ≥ 80%, tests verdes
2. **Seguridad:** Penetration test OK, OWASP Top 10 validado
3. **Rendimiento:** <500ms para 95% de requests
4. **Documentación:** APIs documentadas, casos de uso ejemplificados
5. **Rollback:** Plan y script de rollback validados

---

## 7. Cronograma de Pruebas por Versión

### 7.1 Versión 1.0.0 (Baseline - 4 semanas)

**Objetivo:** Establecer cobertura base del 20%, focalizarse en módulos de autenticación y semestres.

| Semana | Actividad | Artefactos | Responsable | Horas |
|--------|-----------|-----------|-------------|-------|
| **Semana 1** | **Preparación y Setup** | | | |
| | 1.1 Diseñar casos de prueba (Auth, Semestre) | Test Design Doc (50 casos) | QA Lead | 8 |
| | 1.2 Configurar entorno (JDK 21, Maven, H2) | docker-compose-test.yml | DevOps | 4 |
| | 1.3 Crear TestDataBuilders | code + docs | Dev | 6 |
| | 1.4 Configurar CI/CD (GitHub Actions) | .github/workflows/test.yml | DevOps | 6 |
| | **Subtotal S1** | | | **24h** |
| **Semana 2** | **Ejecución: Tests Unitarios** | | | |
| | 2.1 Tests AuthService (18 casos) | AuthServiceImplTest.java | Dev | 8 |
| | 2.2 Tests SemestreService (24 casos) | SemestreServiceImplTest.java | Dev | 8 |
| | 2.3 Tests DTOs Auth (8 casos) | DTOTest.java | Dev | 4 |
| | 2.4 Ejecución inicial (todas las suites) | Results Report | QA | 3 |
| | **Subtotal S2** | | | **23h** |
| **Semana 3** | **Ejecución: Tests Integración + API** | | | |
| | 3.1 Tests integración: Auth + JWT | AuthIntegrationTest.java | Dev | 6 |
| | 3.2 Tests integración: Semestre + BD | SemestreIntegrationTest.java | Dev | 6 |
| | 3.3 Tests API: AuthController (6 casos) | AuthControllerTest.java | Dev | 5 |
| | 3.4 Tests API: SemestreController (8 casos) | SemestreControllerTest.java | Dev | 5 |
| | 3.5 Análisis inicial de cobertura | JaCoCo Report v1.0 | QA | 2 |
| | **Subtotal S3** | | | **24h** |
| **Semana 4** | **Cierre y Refinamiento** | | | |
| | 4.1 Bug fixes y regresión | Fixed Issues List | Dev | 8 |
| | 4.2 Documentación de casos | Test Case Doc | QA | 4 |
| | 4.3 Informe final v1.0.0 | QA Report v1.0.0 | QA Lead | 4 |
| | 4.4 Release y tagging | Release Notes | DevOps | 2 |
| | **Subtotal S4** | | | **18h** |
| | **TOTAL v1.0.0** | | | **89h** |

**Métricas Esperadas v1.0.0:**
- ✅ Cobertura global: 20%
- ✅ Auth Service: 100%
- ✅ Semestre Service: 95%
- ✅ Tests ejecutados: 56
- ✅ Tests aprobados: 56/56 (100%)

---

### 7.2 Versión 1.1.0 (Búsqueda + Alumnos - 4 semanas)

**Objetivo:** Extender cobertura a 50%, agregar tests de búsqueda con ranking SQL y CRUD de alumnos.

| Semana | Actividad | Artefactos | Responsable | Horas |
|--------|-----------|-----------|-------------|-------|
| **Semana 1** | **Diseño de Casos** | | | |
| | 1.1 Casos búsqueda con ranking (20 casos) | Test Design Doc | QA Lead | 8 |
| | 1.2 Casos CRUD Alumnos (16 casos) | Test Design Doc | QA Lead | 6 |
| | 1.3 Actualizar entorno test (si aplica) | docker-compose.yml | DevOps | 2 |
| | **Subtotal S1** | | | **16h** |
| **Semana 2-3** | **Implementación de Tests** | | | |
| | 2.1 Tests unitarios AlumnoSearchService | AlumnoSearchServiceTest.java | Dev | 10 |
| | 2.2 Tests integración búsqueda SQL | AlumnoSearchIntegrationTest.java | Dev | 8 |
| | 2.3 Tests API AlumnoSearchController | AlumnoSearchControllerTest.java | Dev | 8 |
| | 2.4 Tests CRUD AlumnoCrudService | AlumnoCrudServiceTest.java | Dev | 8 |
| | 2.5 Tests API AlumnoController | AlumnoControllerTest.java | Dev | 8 |
| | 2.6 Análisis cobertura incremental | JaCoCo Report v1.1 | QA | 2 |
| | **Subtotal S2-3** | | | **44h** |
| **Semana 4** | **Regresión y Cierre** | | | |
| | 4.1 Regresión v1.0.0 (re-ejecutar) | Regression Results | QA | 4 |
| | 4.2 Bug fixes encontrados | Fixed Issues List | Dev | 6 |
| | 4.3 Documentación y reporte v1.1.0 | QA Report v1.1.0 | QA Lead | 4 |
| | **Subtotal S4** | | | **14h** |
| | **TOTAL v1.1.0** | | | **74h** |

**Métricas Esperadas v1.1.0:**
- ✅ Cobertura global: 50%
- ✅ Búsqueda SQL Ranking: 90%
- ✅ Alumnos CRUD: 85%
- ✅ Tests ejecutados: 112 (+56)
- ✅ Regresión v1.0.0: 100% pass

---

### 7.3 Versión 1.2.0 (Asignación + Reportes - 6 semanas)

**Objetivo:** Llegar a 75% cobertura, cubrir módulos críticos de asignación y reportes.

| Semana | Actividad | Artefactos | Responsable | Horas |
|--------|-----------|-----------|-------------|-------|
| **Semana 1** | **Diseño Detallado** | | | |
| | 1.1 Casos asignación (40 casos - 5 fases) | Test Design Doc | QA Lead | 12 |
| | 1.2 Casos reportes PDF/Excel (20 casos) | Test Design Doc | QA Lead | 8 |
| | **Subtotal S1** | | | **20h** |
| **Semana 2-3** | **Tests Asignación (CRÍTICO)** | | | |
| | 2.1 Tests Fase 1: Lectura Excel | ExcelReaderTest.java | Dev | 8 |
| | 2.2 Tests Fase 2: Validación | AlumnoValidadorTest.java | Dev | 8 |
| | 2.3 Tests Fase 3: Afinidad | CalculoAfinidadTest.java | Dev | 10 |
| | 2.4 Tests Fase 4: Asignación inteligente | AsignacionInteligente Test.java | Dev | 10 |
| | 2.5 Tests Fase 5: Persistencia + Rollback | PersistenciaTest.java | Dev | 8 |
| | 2.6 Tests integración completa asignación | AsignacionIntegrationTest.java | Dev | 10 |
| | 2.7 Tests API AsignacionController | AsignacionControllerTest.java | Dev | 8 |
| | **Subtotal S2-3** | | | **62h** |
| **Semana 4** | **Tests Reportes + Sincronización** | | | |
| | 4.1 Tests ReporteService (PDF/Excel) | ReporteServiceTest.java | Dev | 10 |
| | 4.2 Tests sincronización tutores | SincronizacionTest.java | Dev | 8 |
| | 4.3 Tests API ReporteController | ReporteControllerTest.java | Dev | 6 |
| | 4.4 Análisis cobertura v1.2.0 | JaCoCo Report v1.2 | QA | 3 |
| | **Subtotal S4** | | | **27h** |
| **Semana 5-6** | **Regresión y Optimización** | | | |
| | 5.1 Regresión completa (v1.0 + v1.1) | Regression Matrix | QA | 8 |
| | 5.2 Performance tests (asignación 500 alumnos) | Perf Report | QA | 6 |
| | 5.3 Bug fixes críticos | Fixed Issues | Dev | 10 |
| | 5.4 Documentación final v1.2.0 | Test Case Compendium | QA Lead | 6 |
| | **Subtotal S5-6** | | | **30h** |
| | **TOTAL v1.2.0** | | | **139h** |

**Métricas Esperadas v1.2.0:**
- ✅ Cobertura global: 75%
- ✅ Asignación: 80%
- ✅ Reportes: 75%
- ✅ Tests ejecutados: 252 (+140)
- ✅ Performance OK (500 alumnos en <30s)

---

### 7.4 Versión 2.0.0 (Cobertura Completa - 8 semanas)

**Objetivo:** Alcanzar 85%+ de cobertura global, cubrir todos los módulos restantes, optimizaciones.

| Semana | Actividad | Artefactos | Responsable | Horas |
|--------|-----------|-----------|-------------|-------|
| **Semana 1-2** | **Diseño y Casos Pendientes** | | | |
| | 1.1 Casos: Mantenimiento, Salud, Edge cases | Test Design Doc v2.0 | QA Lead | 16 |
| | 1.2 Casos: Inactivación, Reingreso | Test Design Doc | QA Lead | 12 |
| | **Subtotal S1-2** | | | **28h** |
| **Semana 3-4** | **Implementación Módulos Restantes** | | | |
| | 3.1 Tests AlumnoInactivoService | AlumnoInactivoTest.java | Dev | 8 |
| | 3.2 Tests ReingresoService | ReingresoTest.java | Dev | 8 |
| | 3.3 Tests MantenimientoService | MantenimientoTest.java | Dev | 8 |
| | 3.4 Tests DashboardController | DashboardControllerTest.java | Dev | 6 |
| | 3.5 Tests Exception Handlers | GlobalExceptionHandlerTest.java | Dev | 6 |
| | 3.6 Edge cases y escenarios complejos | EdgeCaseTests.java | Dev | 10 |
| | **Subtotal S3-4** | | | **46h** |
| **Semana 5-6** | **Tests de Rendimiento y Seguridad** | | | |
| | 5.1 Performance tests (1000 alumnos) | Perf Report 2.0 | QA | 8 |
| | 5.2 Stress tests (concurrencia) | Stress Test Report | QA | 8 |
| | 5.3 Security tests (inyección SQL, XSS) | Security Report | QA Sec | 10 |
| | 5.4 Load test en staging | JMeter Results | DevOps | 8 |
| | **Subtotal S5-6** | | | **34h** |
| **Semana 7-8** | **Cierre, Documentación, Optimización** | | | |
| | 7.1 Regresión completa (v1.0 + v1.1 + v1.2) | Full Regression Report | QA | 12 |
| | 7.2 Análisis y optimización cobertura | Coverage Analysis | QA | 6 |
| | 7.3 Documentación exhaustiva | Test Strategy Doc v2.0 | QA Lead | 12 |
| | 7.4 Training: Testing best practices | Training Slides | QA Lead | 4 |
| | 7.5 Release v2.0.0 | Release Notes | DevOps | 3 |
| | **Subtotal S7-8** | | | **37h** |
| | **TOTAL v2.0.0** | | | **145h** |

**Métricas Esperadas v2.0.0:**
- ✅ Cobertura global: 85%+
- ✅ Todos los módulos: ≥75%
- ✅ Controladores: 85%+
- ✅ Tests ejecutados: 400+ (+150)
- ✅ Performance y seguridad validados

---

### 7.5 Resumen de Cronograma

| Versión | Duración | Horas | Cobertura | Tests | Hito |
|---------|----------|-------|-----------|-------|------|
| v1.0.0 | 4 sem | 89h | 20% | 56 | Auth + Semestre OK |
| v1.1.0 | 4 sem | 74h | 50% | 112 | Búsqueda OK |
| v1.2.0 | 6 sem | 139h | 75% | 252 | Asignación OK |
| v2.0.0 | 8 sem | 145h | 85%+ | 400+ | Sistema completo |
| **TOTAL** | **22 semanas** | **447h** | **85%+** | **400+** | **Listo Prod** |

---

## 8. Herramientas de Prueba Recomendadas

### 8.1 Herramientas Seleccionadas

#### 8.1.1 Framework de Testing: JUnit 5 + Mockito + AssertJ

**Por qué:**
- ✅ Estándar de facto en Spring Boot
- ✅ Soportado nativamente en Spring 6.x
- ✅ Ya configurado en pom.xml (Spring Boot Test Starter)
- ✅ Sintaxis moderna (@Test, @DisplayName, parameterized tests)
- ✅ Plugins en IDEs (IntelliJ, VS Code)
- ✅ Integración con CI/CD (Maven, GitHub Actions)

**Alternativas Descartadas:**
- ❌ TestNG - Menos usado en Spring Boot
- ❌ Spock - Requiere Groovy (complejidad)

#### 8.1.2 Cobertura: JaCoCo 0.8.10

**Por qué:**
- ✅ Ya integrado en pom.xml (Maven plugin)
- ✅ Genera reportes HTML automáticamente
- ✅ Soporta coverage gates en CI/CD
- ✅ Análisis por línea, rama, método
- ✅ Compatible con codecov.io

**Instalación (ya hecha):**
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

#### 8.1.3 BD de Pruebas: H2 2.x

**Por qué:**
- ✅ BD en-memoria (tests rápidos)
- ✅ SQL compatible con MySQL
- ✅ Auto-configurado en Spring Boot Test
- ✅ No requiere infraestructura externa
- ✅ Soporte H2 Console para debugging

**Configuración (en application-test.properties):**
```properties
spring.datasource.url=jdbc:h2:mem:test;MODE=MYSQL;DB_CLOSE_ON_EXIT=FALSE
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
```

#### 8.1.4 Mocking: Mockito 4.8+

**Por qué:**
- ✅ Standard en Spring Boot Testing
- ✅ Sintaxis intuitiva (when/thenReturn)
- ✅ Captura de argumentos con ArgumentCaptor
- ✅ Verificación flexible (verify, atLeast, times)

**Ejemplo:**
```java
@Mock
private AlumnoRepository alumnoRepository;

@Test
void testGetAlumnoById() {
    // Arrange
    Alumno expectedAlumno = TestDataBuilder.alumnoBuilder().id(1).build();
    when(alumnoRepository.findById(1L))
        .thenReturn(Optional.of(expectedAlumno));

    // Act
    Alumno result = alumnoService.getAlumnoById(1);

    // Assert
    assertThat(result).isEqualTo(expectedAlumno);
    verify(alumnoRepository, times(1)).findById(1L);
}
```

#### 8.1.5 Tests de API REST: RestAssured 5.3+

**Por qué:**
- ✅ Sintaxis fluida para tests API
- ✅ Soporte para JSON Schema validation
- ✅ Manejo de autenticación (Bearer tokens, OAuth)
- ✅ Assertions HTTP (status, headers, body)
- ✅ Compatible con MockMvc y real HTTP

**Alternativa incluida: MockMvc (Spring Test)**
```java
@AutoConfigureMockMvc
class AsignacionControllerTest {
    @Test
    void testIniciarAsignacion() throws Exception {
        mockMvc.perform(post("/api/asignaciones/iniciar")
            .contentType(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.estado").value("INICIADO"));
    }
}
```

#### 8.1.6 Performance Tests: JMeter (Opcional) o Custom

**Para v1.2.0+:**

**Script simple en Java:**
```java
@Test
void testAsignacionMasivaCon500Alumnos() {
    // Arrange: 500 alumnos en H2
    List<Alumno> alumnos = generarAlumnos(500);
    alumnoRepository.saveAll(alumnos);

    // Act: Medir tiempo
    long inicio = System.currentTimeMillis();
    procesarAsignacion(alumnos);
    long duracion = System.currentTimeMillis() - inicio;

    // Assert: Debe ser <30 segundos
    assertThat(duracion).isLessThan(30_000);
}
```

### 8.2 CI/CD: GitHub Actions

**Archivo:** `.github/workflows/test.yml`

```yaml
name: Tests

on:
  push:
    branches: [ main, develop, ramapruebas ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        java-version: [ 21 ]

    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_DATABASE: test_tutorias
          MYSQL_ROOT_PASSWORD: root
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s

    steps:
      - uses: actions/checkout@v4

      - name: Set up Java
        uses: actions/setup-java@v4
        with:
          java-version: ${{ matrix.java-version }}
          distribution: 'temurin'
          cache: maven

      - name: Run Tests
        run: cd backend && mvn clean test jacoco:report

      - name: Upload Coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./backend/target/site/jacoco/jacoco.xml
          flags: unittests
          name: codecov-umbrella

      - name: Check Coverage (Must be ≥ 75% for critical modules)
        run: |
          cd backend
          # Parse JaCoCo report (bash script o Maven plugin)
          COVERAGE=$(grep -oP '(?<=<counter type="INSTRUCTION".*covered=")[^"]*' \
            target/site/jacoco/index.html | head -1)
          if (( $(echo "$COVERAGE < 75" | bc -l) )); then
            echo "Coverage $COVERAGE% is below 75% threshold!"
            exit 1
          fi

      - name: Build Passing Report
        if: success()
        run: echo "✅ All tests passed! Coverage OK."

      - name: Notify Slack (on failure)
        if: failure()
        uses: slackapi/slack-github-action@v1
        with:
          webhook-url: ${{ secrets.SLACK_WEBHOOK }}
          payload: |
            {
              "text": "❌ Tests failed on ${{ github.ref }}"
            }
```

### 8.3 Herramientas Opcionales Recomendadas

| Herramienta | Propósito | Versión | Impacto |
|-------------|----------|---------|--------|
| Testcontainers | BD containerizada (MySQL real) | 1.19+ | ALTO (v1.2.0+) |
| Cucumber | BDD tests (Gherkin) | 7.14+ | MEDIO (opcional) |
| SonarQube | Análisis de código + cobertura | 10.0+ | ALTO (v2.0+) |
| JMeter | Performance tests | 5.5+ | MEDIO (v1.2.0+) |
| Postman/Insomnia | Tests manuales API | Latest | BAJO (desarrollo) |

---

## 9. Casos de Prueba Detallados

### 9.1 Casos de Prueba: Autenticación (Auth Service)

**Módulo:** `com.universidad.tutorias.application.service.impl.AuthServiceImpl`

#### Caso 1: Login exitoso con credenciales válidas

| Atributo | Valor |
|----------|-------|
| ID | AUTH-001 |
| Módulo | Autenticación |
| Prioridad | CRÍTICA |
| Tipo | Unitario + Integración |

**Precondiciones:**
- Usuario "coordinador" existe en BD
- Password: "password123" hasheado con BCrypt

**Pasos:**
```
1. Llamar authService.login("coordinador", "password123")
2. Validar respuesta
```

**Resultado Esperado:**
```json
{
  "accessToken": "eyJhbGc...",      // JWT válido (3600s)
  "refreshToken": "abc123def...",
  "usuario": {
    "id": 1,
    "username": "coordinador",
    "rol": "COORDINADOR"
  },
  "expiresIn": 3600
}
```

**Assertions:**
```java
assertThat(response.getAccessToken()).isNotBlank()
    .matches("eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+");
assertThat(response.getExpiresIn()).isEqualTo(3600);
assertThat(jwtValidator.validarToken(response.getAccessToken())).isTrue();
```

---

#### Caso 2: Login fallido con password incorrecto

| Atributo | Valor |
|----------|-------|
| ID | AUTH-002 |
| Módulo | Autenticación |
| Prioridad | CRÍTICA |
| Tipo | Unitario |

**Precondiciones:**
- Usuario "coordinador" existe
- Password incorrecto: "wrongpassword"

**Pasos:**
```
1. Llamar authService.login("coordinador", "wrongpassword")
2. Capturar excepción
```

**Resultado Esperado:**
```java
Exception: AuthenticationException
Message: "Credenciales inválidas"
Status: 401 Unauthorized
```

**Assertions:**
```java
assertThatThrownBy(() -> authService.login("coordinador", "wrongpassword"))
    .isInstanceOf(AuthenticationException.class)
    .hasMessageContaining("Credenciales inválidas");
```

---

### 9.2 Casos de Prueba: Asignación Masiva (CRÍTICO)

**Módulo:** `com.universidad.tutorias.application.service.impl.ProcesoOrchestrator`

#### Caso 3: Asignación masiva de 100 alumnos exitosa

| Atributo | Valor |
|----------|-------|
| ID | ASG-001 |
| Módulo | Asignación |
| Prioridad | CRÍTICA |
| Tipo | Integración + Rendimiento |
| Duración Máxima | 30 segundos |

**Precondiciones:**
- 100 alumnos activos sin asignación en semestre 2025-2026-F1
- 5 tutores disponibles con capacidad de 20 alumnos c/u
- Archivo Excel válido con 100 registros

**Pasos:**
```
1. Preparar archivo Excel con 100 alumnos
2. Llamar orchestrator.iniciarProceso(file, "2025-2026-F1")
3. Esperar a que completa (async)
4. Verificar resultados
```

**Validaciones por Fase:**
```
✅ Fase 1 (Lectura): 100 registros parseados
✅ Fase 2 (Validación): 100 registros válidos, 0 errores
✅ Fase 3 (Afinidad): Matriz calculada
✅ Fase 4 (Asignación): 100 alumnos asignados a tutores
✅ Fase 5 (Persistencia): Registros creados en BD
```

**Assertions:**
```java
ProcesoAsignacion proceso = procesarAsignacion(archivo);

assertThat(proceso.getEstado()).isEqualTo(EstadoProceso.COMPLETADO);
assertThat(proceso.getTotalAlumnosProcesados()).isEqualTo(100);
assertThat(proceso.getTotalAsignados()).isEqualTo(100);
assertThat(proceso.getTotalErrores()).isEqualTo(0);

// Verificar BD
List<Asignacion> asignaciones = asignacionRepository.findAll();
assertThat(asignaciones).hasSize(100);

// Verificar cargaActual de tutores se actualizó
Tutor tutor = tutorRepository.findById(1L).get();
assertThat(tutor.getCargaActual()).isEqualTo(20);
```

**Tiempo de Ejecución:**
```
Esperado: 15-25 segundos para 100 alumnos
Máximo:   30 segundos (fallo si excede)
```

---

#### Caso 4: Asignación falla con archivo Excel corrupto

| Atributo | Valor |
|----------|-------|
| ID | ASG-002 |
| Módulo | Asignación |
| Prioridad | CRÍTICA |
| Tipo | Integración |

**Precondiciones:**
- Archivo Excel sin columna "MATRICULA" obligatoria
- 50 alumnos en BD

**Pasos:**
```
1. Preparar archivo Excel incompleto
2. Llamar orchestrator.iniciarProceso(file, "2025-2026-F1")
3. Esperar cierre (error)
4. Verificar rollback
```

**Resultado Esperado:**
```json
{
  "estado": "ERROR",
  "totalErrores": 50,
  "errores": [
    {
      "fila": 2,
      "tipo": "COLUMNA_FALTANTE",
      "descripcion": "Columna MATRICULA no encontrada"
    }
  ]
}
```

**Assertions:**
```java
ProcesoAsignacion proceso = procesarAsignacion(archivoCorrupto);

assertThat(proceso.getEstado()).isEqualTo(EstadoProceso.ERROR);
assertThat(proceso.getTotalErrores()).isEqualTo(50);

// Verificar que NO se crean asignaciones (rollback)
List<Asignacion> asignaciones = asignacionRepository.findAll();
assertThat(asignaciones).isEmpty();  // Vacío = no se guardó nada
```

---

### 9.3 Casos de Prueba: Búsqueda con Ranking

**Módulo:** `com.universidad.tutorias.application.service.impl.AlumnoSearchServiceImpl`

#### Caso 5: Búsqueda exacta de matrícula (1000 puntos)

| Atributo | Valor |
|----------|-------|
| ID | SEARCH-001 |
| Módulo | Búsqueda |
| Prioridad | IMPORTANTE |
| Tipo | Integración (query SQL nativa) |

**Precondiciones:**
- BD con 100 alumnos
- Alumno con matrícula "A001234"

**Pasos:**
```
1. Llamar: searchService.buscar("A001234", null, null, null)
2. Validar resultados
```

**Resultado Esperado:**
```json
{
  "content": [
    {
      "id": 1,
      "matricula": "A001234",
      "nombre": "Juan Pérez",
      "score": 1000,
      "ranking": 1
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

**Assertions:**
```java
Page<AlumnoSearchDTO> result = searchService.buscar(
    "A001234",    // query
    null,         // estado filter
    null,         // carrera filter
    null,         // semestre filter
    null,         // sort
    PageRequest.of(0, 50)
);

assertThat(result.getContent())
    .hasSize(1)
    .extracting("matricula")
    .contains("A001234");

assertThat(result.getContent().get(0).getScore())
    .isEqualTo(1000);  // Máxima puntuación
```

---

#### Caso 6: Búsqueda parcial con múltiples resultados ordenados por relevancia

| Atributo | Valor |
|----------|-------|
| ID | SEARCH-002 |
| Módulo | Búsqueda |
| Prioridad | IMPORTANTE |
| Tipo | Integración |

**Precondiciones:**
- BD con alumnos:
  - "Juan Pérez" (matrícula A001000)
  - "Juanito López" (matrícula A002000)
  - "María Junco" (matrícula A003000)
  - "Pedro Jiménez" (matrícula A004000)

**Pasos:**
```
1. Buscar: "Juan"
2. Validar orden por relevancia
```

**Resultado Esperado (ordenado por score DESC):**
```
1. A001000 (Juan Pérez) - score 500 (exacto en nombre)
2. A002000 (Juanito López) - score 420 (comienza con Juan)
3. A003000 (María Junco) - score 200 (contiene Juan)
```

**Assertions:**
```java
Page<AlumnoSearchDTO> result = searchService.buscar(
    "Juan",
    null, null, null,
    SearchSortOption.RELEVANCE,
    PageRequest.of(0, 50)
);

assertThat(result.getContent())
    .hasSize(3)
    .extracting("matricula")
    .containsExactly("A001000", "A002000", "A003000");

// Validar scores en orden descendente
assertThat(result.getContent().get(0).getScore())
    .isGreaterThan(result.getContent().get(1).getScore());
```

---

## 10. Conclusiones y Próximos Pasos

### 10.1 Resumen Ejecutivo del Plan

Este **Plan de Pruebas 2025** establece una estrategia sistemática para elevar la cobertura del proyecto **ProyectoTutoriasBackend** del **12% actual al 85%+** en un período de **22 semanas**, distribuido en 4 versiones incrementales.

**Logros Clave:**
- ✅ Framework de testing completo (JUnit 5, Mockito, JaCoCo)
- ✅ Cobertura de módulos críticos (Auth 100%, Semestre 95%)
- ✅ Casos de prueba detallados para cada funcionalidad
- ✅ CI/CD automático con GitHub Actions
- ✅ Métricas claras de éxito y aceptación
- ✅ Gestión estructurada de riesgos

### 10.2 Recomendaciones Inmediatas (v1.0.0)

1. **Setup Inicial (Semana 1):**
   - [ ] Instalar JDK 21 en todas las máquinas
   - [ ] Configurar Maven con perfil 'test'
   - [ ] Ejecutar: `mvn clean test jacoco:report`
   - [ ] Revisar reporte en `target/site/jacoco/index.html`

2. **Crear Base de Testing (Semana 1-2):**
   - [ ] Expandir TestDataBuilder con más builders
   - [ ] Crear scripts SQL para datos de prueba
   - [ ] Documentar patrones de testing en CONTRIBUTING.md

3. **Kick-off de v1.0.0 (Semana 2):**
   - [ ] QA Lead diseña 56 casos de prueba
   - [ ] Desarrolladores implementan AuthServiceImplTest
   - [ ] Desarrolladores implementan SemestreServiceImplTest

### 10.3 Hitos por Versión

| Versión | Hito | Métrica | Fecha Estimada |
|---------|------|--------|--------|
| v1.0.0 | Auth + Semestres listo | 20% cobertura | 4 semanas |
| v1.1.0 | Búsqueda + Alumnos OK | 50% cobertura | 8 semanas |
| v1.2.0 | Asignación + Reportes OK | 75% cobertura | 14 semanas |
| v2.0.0 | Sistema completo | 85%+ cobertura | 22 semanas |

### 10.4 Responsabilidades del Equipo

#### QA Lead
- Diseño de casos de prueba
- Gestión de métricas de cobertura
- Reporte de defectos y análisis
- Training del equipo

#### Desarrolladores
- Implementación de tests unitarios
- Tests de integración
- Corrección de bugs encontrados
- Code review de tests

#### DevOps
- Configuración de CI/CD
- Gestión de entornos de test
- Docker Compose para tests
- Reportes de cobertura automatizados

### 10.5 Documentación a Mantener

| Documento | Ubicación | Frecuencia de Actualización |
|-----------|-----------|---------------------------|
| **Test Case Document** | `/docs/testing/CASOS_PRUEBA.md` | Cada versión |
| **JaCoCo Coverage Report** | `backend/target/site/jacoco/` | Semanal (post-tests) |
| **CI/CD Pipeline Logs** | GitHub Actions | Automático |
| **Defect Tracking** | GitHub Issues | Diario |
| **Test Metrics Dashboard** | SonarQube (opcional v2.0) | Semanal |

### 10.6 Indicadores de Éxito

✅ **Fase Completada cuando:**
- Cobertura global ≥ Meta %
- Módulos críticos ≥ Meta %
- 100% de tests pasen (0 fallos)
- Defectos críticos = 0
- Documentación actualizada

### 10.7 Contacto y Escalaciones

| Rol | Responsable | Contacto |
|-----|-------------|----------|
| QA Lead | [Tu nombre] | [Email] |
| Tech Lead | [Nombre Dev] | [Email] |
| Product Owner | [Coordinador] | [Email] |
| DevOps | [Nombre] | [Email] |

---

## 📝 Aprobaciones

| Rol | Nombre | Firma | Fecha |
|-----|--------|-------|-------|
| Tech Lead | Pendiente | ⬜ | - |
| Product Owner | Pendiente | ⬜ | - |

---

**Documento Versión:** 1.0
**Última Actualización:** 17 de noviembre de 2025
**Próxima Revisión:** 15 de diciembre de 2025 (post v1.0.0)

---

**Anexos:**
- [A] TestDataBuilder Pattern (código)
- [B] SQL Scripts de Datos de Prueba
- [C] JaCoCo Configuration
- [D] GitHub Actions Workflow
- [E] Casos de Prueba Extendidos (50+ casos)

