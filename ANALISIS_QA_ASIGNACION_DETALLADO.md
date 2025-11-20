# 🏗️ Análisis Arquitectónico + QA Senior del Servicio de Asignación
## Sistema de Tutoría - Perspectiva de Robustez y Consistencia de Datos

---

## 📋 TABLA DE CONTENIDOS

1. Resumen del Flujo Actual
2. Invariantes de Negocio Identificados
3. Riesgos y Posibles Bugs (Clasificados por Criticidad)
4. Impacto de las Reasignaciones Manuales
5. Plan de Acción (Blindaje)
6. Estrategia de Pruebas
7. Checklist Final

---

## 1️⃣ RESUMEN DEL FLUJO ACTUAL

### 1.1 Flujo de Alto Nivel: asignarAlumnos()

El servicio recibe una **lista de alumnos desde Excel** y procede así:

```
┌─────────────────────────────────────────────┐
│ ENTRADA: Lista de AlumnoExcelDTO             │
│          - Matrícula, Nombre, Carrera        │
│          - Datos de asignación                │
└────────────────────┬────────────────────────┘
                     │
         ┌───────────▼───────────┐
         │  OBTENER Y VALIDAR    │
         │  - Semestre           │
         │  - Proceso de Asign.  │
         └───────────┬───────────┘
                     │
         ┌───────────▼─────────────────────┐
         │  SINCRONIZAR CARGAS             │
         │  tutorSincronizacionService     │
         │  - Count real por tutor         │
         │  - Excluye egresados/baja      │
         └───────────┬─────────────────────┘
                     │
         ┌───────────▼─────────────────────┐
         │  ORDENAR ALUMNOS               │
         │  Por carrera → matrícula        │
         └───────────┬─────────────────────┘
                     │
         ┌───────────▼────────────────────┐
         │  CREAR MAPA EN MEMORIA         │
         │  tutoresPorCarrera             │
         │  (Map<String, List<Tutor>>)    │
         └───────────┬────────────────────┘
                     │
      ┌──────────────▼──────────────┐
      │  FOR EACH ALUMNO            │
      │  ├─ asignarAlumnoTransaccional()
      │  │  (PROPAGATION.REQUIRES_NEW)
      │  │  - Transacción aislada
      │  │
      │  ├─ Cada 100: flush/clear
      │  │
      │  └─ Catch excepciones
      │     ├─ DuplicadoException
      │     ├─ CapacidadExcedidaException
      │     ├─ SinTutorDisponibleException
      │     └─ Exception (unknown)
      │
      └──────────────┬──────────────┘
                     │
         ┌───────────▼──────────────┐
         │  GUARDAR ALERTAS        │
         │  alertaRepository.saveAll()
         └───────────┬──────────────┘
                     │
         ┌───────────▼──────────────┐
         │  RETORNAR RESULTADO      │
         │ - Procesados             │
         │ - Asignados              │
         │ - Errores                │
         │ - Alertas                │
         └──────────────────────────┘
```

### 1.2 Flujo Transaccional: asignarAlumnoTransaccional()

```
┌─────────────────────────────────────────────────────────┐
│ ENTRADA TRANSACCIONAL (REQUIRES_NEW)                    │
│ alumnoDTO, tutoresPorCarrera (mapa), proceso, semestre  │
└────────────────────┬────────────────────────────────────┘
                     │
         ┌───────────▼──────────────────┐
         │  FASE 1: SELECCIONAR TUTOR   │
         ├─────────────────────────────┤
         │ 1. Normalizar carrera       │
         │ 2. seleccionarTutorDisponible()
         │    ├─ PASO 1: Misma carrera │
         │    │   (menor carga)        │
         │    ├─ PASO 2: Carrera compat│
         │    │   (matriz afinidad)    │
         │    ├─ PASO 3: Menor carga   │
         │    │   (fallback global)    │
         │    └─ PASO 4: null         │
         │        → SinTutorDisponible │
         │ 3. Recalcular carga tutor   │
         │ 4. Bloquear tutor           │
         │    (findByIdForUpdate)      │
         │ 5. Validar capacidad        │
         └────────┬───────────────────┘
                  │
         ┌────────▼─────────────────────┐
         │  FASE 2: BUSCAR/CREAR ALUMNO │
         ├─────────────────────────────┤
         │ 1. findByMatricula()        │
         │    ├─ NUEVO → crear instancia
         │    └─ EXISTE → validar dup. │
         │ 2. Si existe, check:        │
         │    ¿Ya tiene asignacion     │
         │    en ESTE semestre?        │
         │    ├─ SÍ → DuplicadoException
         │    └─ NO → continuar        │
         └────────┬───────────────────┘
                  │
    ┌─────────────▼──────────────────────┐
    │  FASE 3: MANTENER O REASIGNAR      │
    ├────────────────────────────────────┤
    │ IF NUEVO:                          │
    │  ├─ mantuvoPrevio = false          │
    │  └─ tipoAsignacion = INICIAL       │
    │                                    │
    │ ELSE (alumno existe):              │
    │  ├─ tutorAnterior = alumno.getTutorActual()
    │  │                                 │
    │  ├─ IF tutorAnterior != null       │
    │  │    AND activo                   │
    │  │    AND tiene capacidad:         │
    │  │  ├─ ✅ MANTENER               │
    │  │  ├─ mantuvoPrevio = true       │
    │  │  ├─ tipoAsignacion = REINGRESO │
    │  │  └─ usar tutorAnterior         │
    │  │                                 │
    │  └─ ELSE:                          │
    │     ├─ ❌ REASIGNAR               │
    │     ├─ mantuvoPrevio = false      │
    │     ├─ tipoAsignacion = REASIGNACION
    │     ├─ usar tutor seleccionado   │
    │     ├─ generar ALERTA             │
    │     │  (REASIGNACION_FORZADA)    │
    │     └─ registrar motivo:          │
    │        "sin tutor previo"         │
    │        "tutor inactivo"           │
    │        "tutor sin capacidad"      │
    │                                    │
    └────────┬──────────────────────────┘
             │
         ┌───▼─────────────────────────┐
         │  FASE 4: ACTUALIZAR ALUMNO  │
         ├──────────────────────────────┤
         │ - setMatricula()            │
         │ - setNombre()               │
         │ - setCarrera()              │
         │ - setSemestre()             │
         │ - setEstado(ACTIVO)         │
         │ - setTutorActual(tutor)     │
         │ - alumnoRepository.save()   │
         └────────┬────────────────────┘
                  │
         ┌────────▼────────────────────┐
         │  FASE 5: CREAR ASIGNACION   │
         ├─────────────────────────────┤
         │ new Asignacion()            │
         │ - setAlumno(alumno)         │
         │ - setTutor(tutor)           │
         │ - setSemestre(semestre)  ◄─┼─ RELACIÓN NUEVA
         │ - setTipoAsignacion(tipo)   │
         │ - setFechaAsignacion(now)   │
         │ - setSemestreAcademico(...) │ ◄─ LEGACY
         │ - save()                    │
         └────────┬────────────────────┘
                  │
      ┌───────────▼────────────────────┐
      │  FASE 6: ACTUALIZAR CARGA      │
      ├────────────────────────────────┤
      │ IF mantuvoPrevio = false:      │
      │  ├─ tutor.incrementarCarga() │
      │  │  (cargaActual++)           │
      │  ├─ tutorRepository.save()    │
      │  └─ log: "Carga incrementada" │
      │                               │
      │ ELSE:                          │
      │  └─ log: "Carga NO increment" │
      │                               │
      └────────┬───────────────────────┘
               │
      ┌────────▼───────────────────────┐
      │  FASE 7: REGISTRAR AUDITORIA   │
      ├─────────────────────────────────┤
      │ auditoriaService.registrarLog(  │
      │   tipo: ASIGNACION              │
      │   entidad: ALUMNO               │
      │   descripcion: "Alumno X asig.  │
      │                a tutor Y..."    │
      │   detalles: JSON con IDs        │
      │ )                               │
      └────────┬───────────────────────┘
               │
      ┌────────▼──────────────────────┐
      │  SALIDA: Resultado Individual  │
      │  - exitosa=true                │
      │  - asignacion                  │
      │  - alertas (si hay)            │
      └──────────────────────────────────┘
```

### 1.3 Interacción con TutorSincronizacionService

**Entrada:** `recalcularCargaTodosLosTutores()`

```
FOR EACH tutor:
  recalcularCargaTutor(tutorId)
    ├─ Bloquear tutor (PESSIMISTIC_WRITE)
    ├─ SELECT COUNT(asignaciones activas)
    │  WHERE tutor.id = :id
    │  AND alumno.estado = ACTIVO
    │  AND NOT EXISTS (AlumnoEgresado)
    │  AND NOT EXISTS (AlumnoBajaDefinitiva)
    │  ↑ IMPORTANTE: Excluye casos resueltos
    ├─ tutor.sincronizarCarga(conteoReal)
    │  (cargaActual = conteoReal)
    └─ tutorRepository.save()
```

**Crítico:** La sincronización **EXCLUYE** egresados y baja definitiva, por lo que:
- `cargaActual` refleja SOLO alumnos activos vigentes
- No contamina con datos históricos

---

## 2️⃣ INVARIANTES DE NEGOCIO IDENTIFICADOS

### 2.1 Invariante 1: Asignación Única por Alumno-Semestre

**Regla:**
```
Un alumno NO puede tener más de una asignación ACTIVA en el MISMO semestre
```

**Implementación actual:**
```sql
UNIQUE uk_asignacion_alumno_tutor_semestre (id_alumno, id_tutor, id_semestre)
```

**Validación en código (AsignacionServiceImpl:215-225):**
```java
Optional<Asignacion> asignacionEnSemestre =
    asignacionRepository.findByAlumnoAndSemestreId(alumno.getId(), semestre.getId());

if (asignacionEnSemestre.isPresent()) {
    throw new DuplicadoException(...);
}
```

**Estado:** ✅ BIEN - Validación duplicada (constraint + código)

---

### 2.2 Invariante 2: Coherencia tutorActual ↔ Asignacion

**Regla:**
```
alumno.tutorActual DEBE corresponder a la asignación más reciente
en el semestre activo actual
```

**Escenario problemático:**
```
alumno.tutorActual = Tutor A
Asignacion{alumno, tutorB, semestre2025-F1}
                    ↑ MISMATCH
```

**Implementación actual:**
- Se actualiza `tutorActual` al mismo tiempo que se crea la asignación
- PERO si hay múltiples semestres, ¿cuál es el "actual"?

**Riesgo:** Alto
- No hay concepto de "semestre actual" en el alumno
- `tutorActual` siempre apunta al último semestre asignado
- Si hay reasignación en semestre N, `tutorActual` puede no reflejar semestre anterior

---

### 2.3 Invariante 3: Capacidad de Tutor

**Regla:**
```
tutor.cargaActual DEBE ser siempre <= tutor.capacidadMax
```

**Fórmula correcta:**
```
cargaActual = COUNT(asignaciones donde alumno.estado=ACTIVO
                    AND alumno NOT IN (egresados, baja definitiva))
```

**Implementación actual:**
```java
// En Tutor.java
public boolean tieneCapacidadDisponible() {
    return activo && cargaActual < capacidadMax;
}

public void incrementarCarga() {
    this.cargaActual++;
}

// Sincronización
public void sincronizarCarga(int cargaReal) {
    this.cargaActual = Math.max(0, cargaReal);
}
```

**Estado:** ✅ BIEN - Si la sincronización es correcta

**PERO:** Riesgo en procesos concurrentes

---

### 2.4 Invariante 4: Contador de Reasignaciones

**Regla:**
```
alumno.contadorCambiosTutor <= 2
(Un alumno puede ser reasignado máximo 2 veces en su vida)
```

**Implementación actual:**
```java
public boolean puedeReasignarse() {
    return contadorCambiosTutor < 2;
}

public void incrementarCambiosTutor() {
    this.contadorCambiosTutor++;
}
```

**Validación:**
- En `TutorReasignacionServiceImpl`: SI valida
- En `AsignacionServiceImpl`: NO valida (no existe reasignación manual)

**Riesgo:** CRÍTICO
- Sin lock optimista, race condition puede incrementar más de 2 veces
- Múltiples threads leen 0 → todos hacen ++

---

### 2.5 Invariante 5: Tipo de Asignación Consistente

**Regla:**
```
TipoAsignacion DEBE reflejar correctamente si fue:
- INICIAL: Primera asignación del alumno
- REASIGNACION: Cambio de tutor (automático o manual)
- REINGRESO: Alumno mantiene tutor anterior
```

**Implementación actual:**
```java
// En asignarAlumnoTransaccional()
if (esAlumnoNuevo) {
    tipoAsignacion = TipoAsignacion.INICIAL;
} else if (mantuvoPrevio) {
    tipoAsignacion = TipoAsignacion.REINGRESO;
} else {
    tipoAsignacion = TipoAsignacion.REASIGNACION;
}
```

**Estado:** ✅ BIEN - Lógica clara

**PERO:** Reasignación manual también usa REASIGNACION
- No se diferencia ORIGEN de reasignación (manual vs automática)

---

### 2.6 Invariante 6: Registros Históricos NO Se Modifican

**Regla:**
```
Una vez que un alumno está INACTIVO (egresado, baja definitiva),
sus asignaciones NO deben cambiar
```

**Implementación actual:**
- No hay validación explícita
- Pero `CleanupAsignacionService` EXCLUYE estos registros

**Riesgo:** MEDIO
- Nada impide que una operación de reasignación afecte alumno inactivo

---

## 3️⃣ RIESGOS Y POSIBLES BUGS (Clasificados por Criticidad)

### 🔴 CRITICO (Impacto Inmediato, Debe Arreglarse Ya)

#### RIESGO 1: Race Condition en Contador de Cambios de Tutor

**Categoría:** Concurrencia / Integridad de Datos

**Código problemático:**
```java
// Alumno.java
public void incrementarCambiosTutor() {
    this.contadorCambiosTutor++;  // ← SIN SINCRONIZACIÓN
}

// TutorReasignacionServiceImpl
if (!alumno.puedeReasignarse()) {  // ← Check
    throw new IllegalStateException(...);
}
// ... intervalo de tiempo ...
alumno.incrementarCambiosTutor();  // ← Act
alumnoRepository.save(alumno);
```

**Escenario de fallo:**

```
Thread A (Reasignación 1)          Thread B (Reasignación 2)
─────────────────────────────────────────────────────────
alumno.contadorCambiosTutor = 0
                                  alumno.contadorCambiosTutor = 0
puedeReasignarse() → true (0 < 2)
                                  puedeReasignarse() → true (0 < 2)
... procesamiento ...
                                  ... procesamiento ...
incrementarCambiosTutor() → 1
                                  incrementarCambiosTutor() → 1 (DEBERÍA SER 2!)
save()
                                  save()

RESULTADO: contador = 1 cuando debería ser 2
CONSECUENCIA: Un 3er cambio se permitiría, violando el invariante
```

**Severidad:** CRÍTICA
- Viola invariante de negocio
- Afecta lógica de negocio importante
- Difícil de detectar en pruebas

**Solución inmediata:**
```java
@Entity
public class Alumno {
    @Version  // ← Optimistic locking
    private Integer version;

    @Column(name = "contador_cambios_tutor", nullable = false)
    private Integer contadorCambiosTutor = 0;
}

// O usar pessimistic lock:
findByIdForUpdate(alumnoId)  // PESSIMISTIC_WRITE
```

---

#### RIESGO 2: Check-Then-Act en Duplicado de Asignación

**Categoría:** Concurrencia / Integridad de Datos

**Código problemático:**
```java
// asignarAlumnoTransaccional (línea 215-225)
Optional<Asignacion> asignacionEnSemestre =
    asignacionRepository.findByAlumnoAndSemestreId(alumno.getId(), semestre.getId());

if (asignacionEnSemestre.isPresent()) {  // ← CHECK (no-atomic)
    throw new DuplicadoException(...);
}

// ... intervalo de tiempo ...

asignacionRepository.save(asignacion);  // ← ACT
// Pero otro thread pudo crear la asignación en el intervalo!
```

**Escenario de fallo:**

```
Thread A                          Thread B
────────────────────────────────────────────
findByAlumnoAndSemestreId(X, S)
→ no existe ✓
                                  findByAlumnoAndSemestreId(X, S)
                                  → no existe ✓
... procesamiento ...
                                  ... procesamiento ...
save(asignacion A) ✓
                                  save(asignacion B)
                                  ↓
                                  DataIntegrityViolationException
                                  (constraint unique violado)
```

**Severidad:** CRÍTICA
- Violación de invariante crítico
- Excepción no capturada correctamente
- Inconsistencia de datos

**Solución:**
```java
// Opción 1: Usar aislamiento SERIALIZABLE
@Transactional(isolation = Isolation.SERIALIZABLE)

// Opción 2: Usar optimistic locking
@Entity
public class Asignacion {
    @Version
    private Integer version;
}

// Opción 3: Usar constraint y manejar excepción
try {
    asignacionRepository.save(asignacion);
} catch (DataIntegrityViolationException e) {
    if (e.getMessage().contains("uk_asignacion_alumno_tutor_semestre")) {
        throw new DuplicadoException(...);
    }
    throw e;
}
```

---

#### RIESGO 3: Desincronización de Mapa en Memoria vs BD en Procesos Concurrentes

**Categoría:** Concurrencia / Integridad de Datos

**Código problemático:**
```java
// asignarAlumnos (línea 87)
Map<String, List<Tutor>> tutoresPorCarrera = agruparTutoresPorCarrera();

// Línea 97-122: Procesamiento de 1000 alumnos
for (int index = 0; index < alumnosOrdenados.size(); index++) {
    AlumnoExcelDTO alumnoDTO = alumnosOrdenados.get(index);

    // El mapa se usa para seleccionar tutor
    // Pero OTRO PROCESO puede estar modificando cargas en paralelo

    // Línea 429-441: Se actualiza mapa localmente
    actualizarTutorEnMapa(tutoresPorCarrera, resultado.getAsignacion().getTutor());
    // ← Mapa solo en memoria de ESTE proceso
}
```

**Escenario de fallo:**

```
Proceso A (asignarAlumnos)              Proceso B (reasignación manual)
──────────────────────────────────────────────────────────────────────
agruparTutoresPorCarrera()
  Tutor X: carga = 20/30 ✓

Procesando alumno 1-50
  Tutor X: seleccionado 10 veces
  Mapa local: carga = 20 + 10 = 30
                                        reasignarTutor(alumno50, Tutor X)
                                          Tutor X: cargaActual = 20 + 10 = 30/30 ✓
                                          save()

Procesando alumno 51
  Mapa local: Tutor X aun = 30 (desactualizado!)
  seleccionarTutorDisponible() → Tutor X
  tieneCapacidadDisponible() → true (mapa dice 30)

  Bloquear tutor y recalcular:
    SELECT COUNT(*) = 30 ✓
    OK para asignar

  Guardar alumno 51
    tutor.incrementarCarga() → 31 ✗
    VIOLA capacidad_max = 30!
```

**Severidad:** CRÍTICA
- Sobrepasa capacidad del tutor
- Afecta invariante de capacidad

**Solución:**
```java
// NO confiar en mapa en memoria para carga
// SIEMPRE leer del tutor bloquead in BD

// Opción 1: Eliminar mapa de carga, leer siempre del tutor
SeleccionTutor seleccionarTutorDisponible(...) {
    // Usar query COUNT() en lugar de carga en memoria
    int cargaReal = asignacionRepository.countByTutorIdExcludingResolved(tutor.id);
    if (cargaReal >= tutor.capacidadMax) {
        // no disponible
    }
}

// Opción 2: Usar refresh en cada iteración
if (procesados % REFRESH_SIZE == 0) {
    tutoresPorCarrera = agruparTutoresPorCarrera(); // ← Refrescar mapa
}

// Opción 3: PESSIMISTIC_WRITE en selección de tutor
Tutor tutor = tutorRepository.findByIdForUpdate(tutorId)
    .orElseThrow(...);
// Ahora mapa irrelevante, siempre leemos del tutor actualizado
```

---

#### RIESGO 4: Carga Actual Inconsistente Después de Limpieza

**Categoría:** Integridad de Datos / Auditoría

**Problema:**
```
CleanupAsignacionService elimina asignaciones duplicadas:
├─ Elimina 15 asignaciones de Tutor X
└─ PERO NO actualiza tutor.cargaActual

RESULTADO: tutor.cargaActual = 30 (antiguo)
           COUNT(asignaciones reales) = 15 (nuevo)
           MISMATCH de 15!
```

**Solución:**
```java
// En CleanupAsignacionServiceImpl
Map<String, Object> resultado = limpiarAsignacionesPorSemestre(semestreId);

// Luego SIEMPRE:
Map<String, Object> recalculo = recalcularCargaDespuesLimpieza(semestreId);
// Este sincroniza todas las cargas
```

---

### 🟠 ALTO (Impacto Significativo, Debe Arreglarse Pronto)

#### RIESGO 5: Inconsistencia Entre Campos Legacy y Nuevos de Semestre

**Categoría:** Migración / Integridad de Datos

**Código problemático:**
```java
// AsignacionServiceImpl (línea 309-310)
asignacion.setSemestre(semestre);                    // Nueva relación
asignacion.setSemestreAcademico(semestre.getCodigo()); // Legacy

// TutorReasignacionServiceImpl (línea 91-92)
Asignacion asignacion = asignacionRepository
    .findByAlumnoAndSemestreAcademico(alumno.getId(), semestreNormalizado)
    .orElse(null);  // ← Busca por STRING

// Luego crea con:
asignacion.setSemestre(semestre);  // ← Con ENTIDAD
```

**Problema:**
- Búsqueda por `semestreAcademico` (string)
- Creación con `semestre` (entidad)
- Si código no se sincroniza → inconsistencia

**Escenario:**
```
Reasignación manual crea asignacion:
  semestreAcademico = "2025-2026-F1"
  semestre.id = 5
  semestre.codigo = "2025-2026-F1" ✓

Pero qué si alguien modifica:
  semestre.codigo = "2025-2026F1"  (sin guión)

Ahora:
  findByAlumnoAndSemestreAcademico() buscará por "2025-2026-F1" (antigua)
  findByAlumnoAndSemestreId() buscará por id=5 (ambas ok)

PROBLEMA: Dos queries retornan registros diferentes!
```

**Severidad:** ALTA
- Causa inconsistencia silenciosa
- Auditoría se distorsiona

**Solución:**
```java
// Opción 1: Deprecar completamente semestreAcademico
// En migración SQL:
ALTER TABLE asignaciones DROP COLUMN semestre_academico;

// Opción 2: Mantener sincronizado en BD
// Trigger o constraint:
CHECK (semestre_academico = (SELECT codigo FROM semestres WHERE id = id_semestre))

// Opción 3: En código, NUNCA usar semestreAcademico
// Usar siempre semestre.id o semestre.codigo directamente
```

---

#### RIESGO 6: Alertas Perdidas en Caso de Excepción

**Categoría:** Auditoría / Trazabilidad

**Código problemático:**
```java
// asignarAlumnoTransaccional (línea 257-268)
AlertaProceso alerta = crearAlerta(
    proceso,
    TipoAlerta.REASIGNACION_FORZADA,
    SeveridadAlerta.WARNING,
    "Alumno X reasignado de Y a Z (motivo: tutor sin capacidad)",
    alumnoDTO,
    tutor
);
// ← Alerta creada pero no guardada aún

// Luego, durante save de asignacion, puede fallar
asignacionRepository.save(asignacion);  // ← Puede fallar aquí
// Si falla, la alerta en memoria se pierde
```

**Impacto:**
- Reasignación forzada ocurrió pero no se registró
- Auditoría incompleta
- Coordinador no se entera del cambio

**Severidad:** ALTA
- Afecta trazabilidad crítica

**Solución:**
```java
// Guardar alertas ANTES de que puedan fallar
AlertaProceso alerta = crearAlerta(...);
alertaRepository.save(alerta);  // ← Guardar inmediatamente

// O colectar alertas y guardar en un listner transaccional
@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
public void guardarAlertasPendientes() {
    alertaRepository.saveAll(alertasPendientes);
}
```

---

#### RIESGO 7: Sin Validación de Que Semestre Esté Vigente

**Categoría:** Lógica de Negocio

**Código problemático:**
```java
// asignarAlumnos (línea 74-75)
Semestre semestre = semestreService.obtenerPorId(semestreId);
log.info("Semestre encontrado: {} - {}", semestre.getCodigo(), semestre.getNombre());

// NO se valida:
// - ¿Está activo?
// - ¿Está en fechas vigentes?
// - ¿No es un semestre pasado?
```

**Escenario:**
```
Admin intenta cargar alumnos para semestre PASADO: 2023-2024-F1
Sistema PERMITE porque no hay validación
Resultado: Datos históricos se modifican, auditoría se distorsiona
```

**Severidad:** ALTA
- Viola separación entre presente e histórico
- Datos históricos deben ser immutables

**Solución:**
```java
// En SemestreService
public void validarSemestreParaAsignacion(Semestre semestre) {
    if (!semestre.estaActivo()) {
        throw new IllegalStateException("Semestre no está activo");
    }
    if (!semestre.estaVigente()) {  // Validar fechas
        throw new IllegalStateException("Semestre no está en fechas vigentes");
    }
}
```

---

#### RIESGO 8: Contador de Cambios NO se Valida en AsignacionServiceImpl

**Categoría:** Lógica de Negocio

**Problema:**
```
En TutorReasignacionServiceImpl:
  ✓ valida puedeReasignarse() (contadorCambiosTutor < 2)
  ✓ incrementa contadorCambiosTutor

En AsignacionServiceImpl:
  ✗ NO valida puedeReasignarse()
  ✗ NO incrementa contadorCambiosTutor

PROBLEMA: Un alumno con contador=2 puede ser reasignado por proceso automático
```

**Severidad:** ALTA
- Viola invariante de negocio
- Lógica inconsistente entre flujos manuales y automáticos

**Solución:**
```java
// En asignarAlumnoTransaccional()
if (!esAlumnoNuevo && tipoAsignacion == TipoAsignacion.REASIGNACION) {
    if (!alumno.puedeReasignarse()) {
        throw new IllegalStateException(
            "El alumno ha alcanzado el límite de cambios de tutor");
    }
    // Si creamos la reasignacion AUTOMATICAMENTE, incrementar
    // (aunque el flujo automático actual no parece hacerlo)
}
```

---

### 🟡 MEDIO (Impacto Moderado, Debe Mejorarse)

#### RIESGO 9: Null Checks Redundantes

**Categoría:** Calidad de Código

```java
// asignarAlumnoTransaccional (línea 185-188)
if (seleccionTutor == null || seleccionTutor.getTutor() == null) {
    throw new SinTutorDisponibleException(...);
}
```

**Problema:**
- SeleccionTutor nunca debería tener tutor null si existe el objeto
- Check redundante e innecesario

**Impacto:** BAJO (solo limpieza de código)

---

#### RIESGO 10: Batch Processing Sin Manejo de Errores en Flush

**Categoría:** Robustez

```java
// asignarAlumnos (línea 124-128)
if (procesados % BATCH_SIZE == 0) {
    entityManager.flush();  // ← Puede fallar silenciosamente
    entityManager.clear();
    actualizarProceso(proceso, procesados, exitosos, errores.size(), alertas);
}
```

**Problema:**
- Si flush falla, error no se captura
- Transacciones pueden quedar en estado inconsistente

**Solución:**
```java
if (procesados % BATCH_SIZE == 0) {
    try {
        entityManager.flush();
        entityManager.clear();
    } catch (Exception e) {
        log.error("Error en flush batch: {}", e.getMessage(), e);
        // Registrar error y continuar con precaución
        // O fallar completamente
    }
}
```

---

#### RIESGO 11: Mapa en Memoria Sin Sincronización (ConcurrentModificationException)

**Categoría:** Concurrencia

```java
// asignarAlumnos (línea 87)
Map<String, List<Tutor>> tutoresPorCarrera = agruparTutoresPorCarrera();
// ← HashMap normal (NO thread-safe)

// Línea 429-441: Se modifica dentro del loop
private void actualizarTutorEnMapa(Map<String, List<Tutor>> mapa, Tutor tutor) {
    List<Tutor> tutoresCarrera = mapa.computeIfAbsent(...);
    // Modificación potencial en thread A
}
```

**Problema:**
- Si dos threads modifican el mapa → ConcurrentModificationException

**Solución:**
```java
Map<String, List<Tutor>> tutoresPorCarrera =
    Collections.synchronizedMap(new HashMap<>());

// O usar ConcurrentHashMap
Map<String, List<Tutor>> tutoresPorCarrera =
    new ConcurrentHashMap<>();
```

---

## 4️⃣ IMPACTO DE REASIGNACIONES MANUALES Y RECOMENDACIONES

### 4.1 Flujo de Reasignación Manual (TutorReasignacionServiceImpl)

**Código de interés (línea 75-114):**

```java
@Transactional
public CambioTutorResponseDTO reasignarTutor(CambioTutorRequestDTO request) {
    // 1. Bloquear alumno
    Alumno alumno = alumnoRepository.findByIdForUpdate(request.getAlumnoId())
            .orElseThrow(...);

    // 2. Sincronizar y bloquear tutores
    Tutor tutorOrigen = tutorSincronizacionService
        .sincronizarYBloquearTutor(request.getTutorOrigenId());
    Tutor tutorDestino = tutorSincronizacionService
        .sincronizarYBloquearTutor(request.getTutorDestinoId());

    // 3. Validaciones
    if (!alumno.getTutorActual().getId().equals(tutorOrigen.getId())) {
        throw new IllegalArgumentException("Alumno no está asignado a origen");
    }

    if (!tutorDestino.tieneCapacidadDisponible()) {
        throw new IllegalArgumentException("Tutor destino sin capacidad");
    }

    if (!alumno.puedeReasignarse()) {
        throw new IllegalStateException("Límite de cambios alcanzado");
    }

    // 4. Actualizar cargas
    tutorOrigen.decrementarCarga();
    tutorDestino.incrementarCarga();

    // 5. Actualizar alumno
    alumno.setTutorActual(tutorDestino);
    alumno.incrementarCambiosTutor();  // ← INCREMENTA CONTADOR

    alumnoRepository.save(alumno);
    tutorRepository.save(tutorOrigen);
    tutorRepository.save(tutorDestino);

    // 6. Actualizar/crear asignacion
    Asignacion asignacion = asignacionRepository
        .findByAlumnoAndSemestreAcademico(alumno.getId(), request.getSemestreAcademico())
        .orElse(null);

    if (asignacion != null) {
        asignacion.setTutor(tutorDestino);
        asignacion.setTipoAsignacion(TipoAsignacion.REASIGNACION);
        asignacion.setFechaAsignacion(LocalDateTime.now());
    } else {
        asignacion = new Asignacion();
        asignacion.setAlumno(alumno);
        asignacion.setTutor(tutorDestino);
        asignacion.setSemestre(semestre);  // ← OBTIENE POR CODIGO
        asignacion.setTipoAsignacion(TipoAsignacion.REASIGNACION);
        asignacion.setSemestreAcademico(request.getSemestreAcademico());
        asignacion.setFechaAsignacion(LocalDateTime.now());
    }

    asignacionRepository.save(asignacion);

    // 7. Auditoría
    registrarAuditoria(...);

    return response;
}
```

### 4.2 Conflictos Potenciales Entre Manual y Automático

#### CONFLICTO A: Reasignación Manual DURANTE Asignación Automática

**Escenario:**

```
Tiempo T0: Inicia proceso automático de carga de 500 alumnos
           Semestre 2025-2026-F1

Tiempo T1: Procesando alumno #250 (alumno X)
           Mapa: Tutor A tiene carga 20/30
           Seleccionado: Tutor A

Tiempo T2: Coordinador ejecuta reasignación manual
           alumno X: Tutor A → Tutor B

           - alumno.tutorActual = B
           - alumno.contadorCambiosTutor = 1
           - tutorA.cargaActual = 19
           - tutorB.cargaActual = 21
           - Crea/actualiza Asignacion{alumno X, tutor B}
           - save()

Tiempo T3: Proceso automático continúa
           asignarAlumnoTransaccional() para alumno X:

           - Busca alumno X: EXISTE
           - Busca asignacion(alumno X, semestre S): EXISTE
           - throw DuplicadoException() ← CORRECTO, detecta conflicto
```

**Resultado:** ✅ BIEN - La validación de duplicado previene el conflicto

**PERO:** ¿Es esto lo deseado?

```
La pregunta es: ¿Si se reasignó MANUALMENTE hace 2 segundos,
debería el proceso automático intentar reasignarlo de nuevo?

Respuesta: NO, debería saltarlo como DuplicadoException
(lo que HACE actualmente)
```

---

#### CONFLICTO B: Contador de Cambios Incremen tado Ambas Formas

**Escenario:**

```
Alumno X: contadorCambiosTutor = 1

Reasignación manual:
  alumno.incrementarCambiosTutor()  // contador = 2
  puedeReasignarse() → false (2 < 2 es false)

Ahora el límite se alcanzó.

Proceso automático:
  Alumno X existe
  Si necesitara reasignar: no sabe que contador=2 (buena práctica: debería validar)
  RIESGO: Si AsignacionService NO valida, podría permitir 3era reasignación
```

**Status:** El código actual de AsignacionService NO valida, así que RIESGO REAL

---

#### CONFLICTO C: Sem estre Legacy vs Relación Nueva

**Escenario:**

```
Reasignación manual busca asignacion por:
  findByAlumnoAndSemestreAcademico("2025-2026-F1")

Luego crea asignacion con:
  asignacion.setSemestre(semestreEntity)
  asignacion.setSemestreAcademico("2025-2026-F1")

Pero qué si:
  - Hay dos semestres con código similar: "2025-2026-F1" y "20252026F1"
  - La búsqueda retorna uno pero se guarda con otro

INCONSISTENCIA: Búsquedas posteriores pueden devolver registros incorrectos
```

---

### 4.3 Recomendaciones para Reasignaciones Manuales

#### Recomendación 1: Agregar Campo de Origen en Asignacion

**Código:**
```java
@Entity
public class Asignacion {
    // ... campos existentes ...

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_asignacion")
    private OrigenAsignacion origenAsignacion;  // MANUAL o AUTOMATICO
}

@Getter
public enum OrigenAsignacion {
    AUTOMATICO("Asignación automática"),
    MANUAL("Reasignación manual");

    private final String descripcion;

    OrigenAsignacion(String descripcion) {
        this.descripcion = descripcion;
    }
}
```

**Uso:**
```java
// En AsignacionServiceImpl
asignacion.setOrigenAsignacion(OrigenAsignacion.AUTOMATICO);

// En TutorReasignacionServiceImpl
asignacion.setOrigenAsignacion(OrigenAsignacion.MANUAL);
```

**Beneficio:**
- Auditoría clara del origen
- Reporting diferenciado
- Trazabilidad completa

---

#### Recomendación 2: Usar Optimistic Locking en Alumno

**Código:**
```java
@Entity
public class Alumno {
    // ... campos existentes ...

    @Version  // ← Optimistic locking
    private Integer version;

    @Column(name = "contador_cambios_tutor", nullable = false)
    private Integer contadorCambiosTutor = 0;
}
```

**Beneficio:**
- Previene race conditions en contador
- Si hay conflicto, excepción clara
- No requiere locks pesimistas

**Uso:**
```java
try {
    alumnoRepository.save(alumno);  // Version check automático
} catch (OptimisticLockingFailureException e) {
    log.warn("Conflicto de versión en alumno: otro proceso modificó");
    // Retry o fallar explícitamente
}
```

---

#### Recomendación 3: Validar Contador en AsignacionServiceImpl

**Código:**
```java
// En asignarAlumnoTransaccional()
if (!esAlumnoNuevo) {
    // ... código existente ...

    // Agregar validación de contador
    if (tipoAsignacion == TipoAsignacion.REASIGNACION && !alumno.puedeReasignarse()) {
        throw new IllegalStateException(
            String.format("Alumno %s ha alcanzado límite de reasignaciones (%d/%d)",
                alumno.getMatricula(),
                alumno.getContadorCambiosTutor(),
                2));
    }
}
```

**Beneficio:**
- Coherencia entre flujos manuales y automáticos
- Previene violaciones de invariante

---

#### Recomendación 4: Eliminar Completamente Campo Legacy semestreAcademico

**Plan de Migración:**

```
Paso 1: Crear migración SQL
  ALTER TABLE asignaciones
  ADD CONSTRAINT chk_semestre_codigo
  CHECK (semestreAcademico = (SELECT codigo FROM semestres WHERE id = id_semestre));

Paso 2: Validar que se sincroniza
  SELECT * FROM asignaciones
  WHERE semestreAcademico != (SELECT codigo FROM semestres WHERE id = id_semestre);
  // Debería retornar 0 filas

Paso 3: Remover de código (usar siempre semestre.id o semestre.codigo)
  Remover: asignacion.setSemestreAcademico(...)
  Remover: findByAlumnoAndSemestreAcademico()
  Usar siempre: semestre.id (relación)

Paso 4: En futuro, eliminar columna
  ALTER TABLE asignaciones DROP COLUMN semestre_academico;
```

---

#### Recomendación 5: Crear Transacción Explícita para Conflictos

**Código:**

```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public ResultadoAsignacionIndividual asignarAlumnoConAislamiento(...) {
    // Garantiza que no hay race conditions
    // Costo: más bloqueos, menos concurrencia
}

// O combinar con Optimistic locking
@Transactional(isolation = Isolation.READ_COMMITTED)
public ResultadoAsignacionIndividual asignarAlumnoConOptimistic(...) {
    // Trata versiones y lanza OptimisticLockingFailureException
    // Si falla, reintenta automáticamente
}
```

---

## 5️⃣ PLAN DE ACCIÓN (BLINDAJE)

### Fase 1: Correcciones CRÍTICAS (Semana 1-2)

#### 1.1 Agregar Optimistic Locking en Alumno

**Archivo:** `Alumno.java`

```java
@Entity
@Table(name = "alumnos", ...)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ... campos existentes ...

    @Version  // ← NUEVO
    private Integer version;

    @Column(name = "contador_cambios_tutor", nullable = false)
    private Integer contadorCambiosTutor = 0;

    // ... resto ...
}
```

**Migración SQL:**
```sql
ALTER TABLE alumnos ADD COLUMN version INT DEFAULT 0;
UPDATE alumnos SET version = 0;
ALTER TABLE alumnos MODIFY COLUMN version INT NOT NULL;
```

**Testing:**
```java
@Test
public void testOptimisticLockingEnContador() {
    // Simular 2 threads incrementando contador
    // Verificar que uno falla con OptimisticLockingFailureException
}
```

---

#### 1.2 Manejar DataIntegrityViolationException en Duplicado

**Archivo:** `AsignacionServiceImpl.java`

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
protected ResultadoAsignacionIndividual asignarAlumnoTransaccional(...) {
    // ... código anterior ...

    try {
        asignacionRepository.save(asignacion);
    } catch (DataIntegrityViolationException e) {
        // Verificar si es constraint unique
        if (e.getMessage() != null &&
            e.getMessage().contains("uk_asignacion_alumno_tutor_semestre")) {

            // Otro proceso probablemente creó la asignación
            throw new DuplicadoException(
                "El alumno ya fue asignado en el intervalo de tiempo. " +
                "Reintente.", e);
        }
        // Re-lanzar si es otro tipo de error
        throw e;
    }
}
```

**Testing:**
```java
@Test
public void testRaceConditionDuplicado() {
    // Simular 2 threads creando asignación simultáneamente
    // Verificar que uno lanza DuplicadoException (por constraint o by-code)
}
```

---

#### 1.3 Validar Contador de Cambios en AsignacionServiceImpl

**Archivo:** `AsignacionServiceImpl.java`

```java
// En asignarAlumnoTransaccional() (línea 248-279)
if (!esAlumnoNuevo && tipoAsignacion == TipoAsignacion.REASIGNACION) {
    if (!alumno.puedeReasignarse()) {
        throw new IllegalStateException(
            String.format("Alumno %s (%d) ha alcanzado el límite de " +
                "reasignaciones permitidas. Contador actual: %d, Máximo: 2",
                alumno.getMatricula(),
                alumno.getId(),
                alumno.getContadorCambiosTutor()));
    }

    // NOTA: No incrementamos el contador aquí porque es asignación automática
    // El contador solo se incrementa en reasignación MANUAL
    log.debug("Alumno {} reasignado automáticamente, " +
        "contador de cambios NO incrementado", alumno.getMatricula());
}
```

**Testing:**
```java
@Test
public void testBloqueoAlumnoConDosReasignaciones() {
    // Crear alumno con contadorCambiosTutor = 2
    // Intentar asignar automáticamente
    // Verificar que NO lanza excepción si es REINGRESO
    // Verificar que lanza si es REASIGNACION
}
```

---

#### 1.4 Usar Sincronización en Mapa o Refrescar Carga

**Archivo:** `AsignacionServiceImpl.java`

**Opción A: Usar ConcurrentHashMap**
```java
private Map<String, List<Tutor>> agruparTutoresPorCarrera() {
    List<Tutor> todosLosTutores = tutorRepository.findDisponibles();
    return todosLosTutores.stream()
            .collect(Collectors.toCollection(ConcurrentHashMap::new),
                     (map, tutor) -> map.computeIfAbsent(
                         tutor.getCarrera(),
                         k -> Collections.synchronizedList(new ArrayList<>()))
                         .add(tutor));

    // Más limpio:
    return Collections.synchronizedMap(
        todosLosTutores.stream()
            .collect(Collectors.groupingBy(Tutor::getCarrera)));
}
```

**Opción B: Refrescar mapa cada N iteraciones (RECOMENDADO)**
```java
private static final int REFRESH_TUTOR_MAP_SIZE = 50;

@Override
public ResultadoAsignacion asignarAlumnos(...) {
    // ... código previo ...

    for (int index = 0; index < alumnosOrdenados.size(); index++) {
        AlumnoExcelDTO alumnoDTO = alumnosOrdenados.get(index);

        // Refrescar mapa cada 50 alumnos
        if (index > 0 && index % REFRESH_TUTOR_MAP_SIZE == 0) {
            log.debug("Refrescando mapa de tutores en iteración {}", index);
            tutoresPorCarrera = agruparTutoresPorCarrera();
        }

        // ... resto del procesamiento ...
    }
}
```

**Mejor: NO confiar en mapa para carga**
```java
// Modificar seleccionarTutorDisponible() para leer carga real
private SeleccionTutor seleccionarTutorDisponible(...) {
    // ... código previo ...

    // En lugar de confiar en tutor.cargaActual del mapa:
    Optional<Tutor> tutorMismaCarrera = buscarTutorDisponible(
        tutoresPorCarrera.get(carreraAlumno));

    if (tutorMismaCarrera.isPresent()) {
        Tutor tutor = tutorMismaCarrera.get();

        // LEER CARGA REAL DESDE BD
        int cargaReal = asignacionRepository.countByTutorIdExcludingResolved(tutor.getId());
        if (cargaReal >= tutor.getCapacidadMax()) {
            // No disponible, continuar búsqueda
        } else {
            return SeleccionTutor.builder().tutor(tutor).build();
        }
    }

    // ... resto ...
}
```

---

### Fase 2: Mejoras ALTAS (Semana 3-4)

#### 2.1 Eliminar Campo Legacy semestreAcademico

**Paso 1: Validación Previa**
```sql
-- Verificar sincronización
SELECT COUNT(*) FROM asignaciones
WHERE semestreAcademico != (SELECT codigo FROM semestres WHERE id = id_semestre);
-- Debería retornar 0
```

**Paso 2: Agregar Constraint (Migración SQL V4)**
```sql
ALTER TABLE asignaciones
ADD CONSTRAINT chk_semestre_academico_sync
CHECK (semestreAcademico = (SELECT codigo FROM semestres WHERE id = id_semestre));
```

**Paso 3: Remover de código (AsignacionServiceImpl)**
```java
// REMOVER estas líneas:
// asignacion.setSemestreAcademico(semestre.getCodigo());

// REMOVER método deprecated:
// asignacionRepository.findByAlumnoAndSemestreAcademico(...)

// ACTUALIZAR TutorReasignacionServiceImpl:
Asignacion asignacion = asignacionRepository
    .findByAlumnoAndSemestreId(alumno.getId(), semestre.getId())  // ← USAR ID
    .orElse(null);
```

**Paso 4: Remover columna (Migración SQL V5, después de 2-3 versiones)**
```sql
ALTER TABLE asignaciones DROP COLUMN semestre_academico;
```

---

#### 2.2 Guardar Alertas Atomicamente

**Archivo:** `AsignacionServiceImpl.java`

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
protected ResultadoAsignacionIndividual asignarAlumnoTransaccional(...) {
    List<AlertaProceso> alertas = new ArrayList<>();

    // ... fase 1-3 ...

    try {
        // Crear asignación
        asignacionRepository.save(asignacion);
    } catch (Exception e) {
        // Guardar alertas ANTES de fallar
        for (AlertaProceso alerta : alertas) {
            try {
                alertaRepository.save(alerta);
            } catch (Exception e2) {
                log.error("No se pudo guardar alerta: {}", e2.getMessage());
            }
        }
        throw e;  // Re-lanzar original
    }

    // Guardar alertas si todo ok
    for (AlertaProceso alerta : alertas) {
        alertaRepository.save(alerta);
    }

    return ResultadoAsignacionIndividual.builder()
            .exitosa(true)
            .asignacion(asignacion)
            .alertas(alertas)
            .build();
}
```

---

#### 2.3 Validar Semestre Vigente

**Archivo:** `SemestreService.java`

```java
@Service
public class SemestreServiceImpl implements SemestreService {

    public void validarSemestreParaAsignacion(Semestre semestre) {
        if (semestre == null) {
            throw new EntityNotFoundException("Semestre no encontrado");
        }

        if (!semestre.estaActivo()) {
            throw new IllegalStateException(
                String.format("Semestre %s no está activo", semestre.getCodigo()));
        }

        // Opcional: validar fechas vigentes
        if (!semestre.estaVigente()) {
            log.warn("Semestre {} está fuera de fechas vigentes", semestre.getCodigo());
            // Podrías lanzar excepción o solo advertir
        }
    }
}
```

**Uso en AsignacionServiceImpl:**
```java
@Override
public ResultadoAsignacion asignarAlumnos(..., Long semestreId) {
    Semestre semestre = semestreService.obtenerPorId(semestreId);
    semestreService.validarSemestreParaAsignacion(semestre);  // ← NUEVO

    // ... resto ...
}
```

---

#### 2.4 Agregar Campo de Origen en Asignacion

**Archivo:** `Asignacion.java`

```java
@Entity
@Table(name = "asignaciones", ...)
public class Asignacion {
    // ... campos existentes ...

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_asignacion", length = 20)
    private OrigenAsignacion origenAsignacion = OrigenAsignacion.AUTOMATICO;

    // ... getter/setter ...
}

@Getter
public enum OrigenAsignacion {
    AUTOMATICO("Asignación Automática"),
    MANUAL("Reasignación Manual");

    private final String descripcion;

    OrigenAsignacion(String descripcion) {
        this.descripcion = descripcion;
    }
}
```

**Migración SQL (V4):**
```sql
ALTER TABLE asignaciones
ADD COLUMN origen_asignacion VARCHAR(20) DEFAULT 'AUTOMATICO' NOT NULL;
```

**Uso:**
```java
// En AsignacionServiceImpl
asignacion.setOrigenAsignacion(OrigenAsignacion.AUTOMATICO);

// En TutorReasignacionServiceImpl
asignacion.setOrigenAsignacion(OrigenAsignacion.MANUAL);
```

---

### Fase 3: Mejoras MEDIAS (Semana 5-6)

#### 3.1 Refactorizar Lógica de Selección de Tutor

**Antes (problemático):**
```java
private SeleccionTutor seleccionarTutorDisponible(...) {
    // Confía en mapa, puede estar desincronizado
}
```

**Después (robusto):**
```java
private SeleccionTutor seleccionarTutorDisponible(
        Map<String, List<Tutor>> tutoresPorCarrera,
        AlumnoExcelDTO alumnoDTO,
        ProcesoAsignacion proceso) {

    String carreraAlumno = normalizarCarrera(alumnoDTO.getCarrera());

    // PASO 1: BUSCAR EN MISMA CARRERA
    Optional<Tutor> tutorMismaCarrera = buscarTutorDisponibleConValidacionBD(
        tutoresPorCarrera.get(carreraAlumno));

    if (tutorMismaCarrera.isPresent()) {
        return SeleccionTutor.builder()
                .tutor(tutorMismaCarrera.get())
                .build();
    }

    // PASO 2: BUSCAR CARRERA COMPATIBLE
    List<String> carrerasCompatibles = matrizAfinidadRepository
        .findCarrerasCompatibles(carreraAlumno);

    for (String carreraCompatible : carrerasCompatibles) {
        Optional<Tutor> tutorCompatible = buscarTutorDisponibleConValidacionBD(
            tutoresPorCarrera.get(carreraCompatible));

        if (tutorCompatible.isPresent()) {
            Tutor tutor = tutorCompatible.get();
            AlertaProceso alerta = crearAlerta(
                proceso, TipoAlerta.ASIGNACION_CRUZADA, SeveridadAlerta.WARNING,
                String.format("Asignación cruzada: %s de %s a tutor de %s",
                    alumnoDTO.getMatricula(), carreraAlumno, carreraCompatible),
                alumnoDTO, tutor);

            return SeleccionTutor.builder()
                    .tutor(tutor)
                    .alertas(Collections.singletonList(alerta))
                    .build();
        }
    }

    // PASO 3: FALLBACK GLOBAL
    Optional<Tutor> tutorMenorCarga = buscarTutorMenorCargaConValidacionBD(
        tutoresPorCarrera);

    if (tutorMenorCarga.isPresent()) {
        Tutor tutor = tutorMenorCarga.get();
        AlertaProceso alerta = crearAlerta(
            proceso, TipoAlerta.ASIGNACION_CRUZADA, SeveridadAlerta.WARNING,
            String.format("Asignación por menor carga: %s a tutor de %s",
                alumnoDTO.getMatricula(), tutor.getCarrera()),
            alumnoDTO, tutor);

        return SeleccionTutor.builder()
                .tutor(tutor)
                .alertas(Collections.singletonList(alerta))
                .build();
    }

    return null;  // Sin tutor disponible
}

// Método auxiliar que valida carga en BD
private Optional<Tutor> buscarTutorDisponibleConValidacionBD(List<Tutor> tutores) {
    if (tutores == null || tutores.isEmpty()) {
        return Optional.empty();
    }

    // Ordenar por carga EN BD (no confiando en memoria)
    return tutores.stream()
            .filter(tutor -> {
                int cargaReal = asignacionRepository
                    .countByTutorIdExcludingResolved(tutor.getId());
                return cargaReal < tutor.getCapacidadMax();
            })
            .min(Comparator.comparingInt(tutor ->
                asignacionRepository.countByTutorIdExcludingResolved(tutor.getId())));
}
```

---

#### 3.2 Tests Automatizados Exhaustivos

**Archivo:** `AsignacionServiceTest.java`

```java
@SpringBootTest
@Transactional
public class AsignacionServiceTest {

    @Autowired
    private AsignacionService asignacionService;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private TutorRepository tutorRepository;

    // ... setup ...

    @Test
    public void testAlumnoNuevoSeAsignaCorrectamente() {
        // Given: Alumno nuevo, tutor disponible
        // When: Asignar
        // Then: Asignación creada, carga incrementada, tipo=INICIAL
    }

    @Test
    public void testAlumnoExistenteSinTutorAnterior() {
        // Given: Alumno existente, sin tutorActual
        // When: Asignar
        // Then: Reasignación, nuevo tutor, alerta
    }

    @Test
    public void testAlumnoExistenteConTutorActivoYCapacidad() {
        // Given: Alumno con tutor activo y con capacidad
        // When: Asignar en mismo semestre (después de inactividad)
        // Then: Mantiene tutor (REINGRESO), carga NO incrementada
    }

    @Test
    public void testAlumnoExistenteConTutorSinCapacidad() {
        // Given: Alumno con tutor activo pero SIN capacidad
        // When: Asignar
        // Then: Reasignación a nuevo tutor, alerta REASIGNACION_FORZADA
    }

    @Test
    public void testAlumnoYaAsignadoEnSemestreLanzaExcepcion() {
        // Given: Alumno ya con asignación en semestre S
        // When: Intentar asignar de nuevo
        // Then: DuplicadoException
    }

    @Test
    public void testTutorSinCapacidadLanzaExcepcion() {
        // Given: Tutor con carga = capacidad_max
        // When: Intentar asignar
        // Then: CapacidadExcedidaException
    }

    @Test
    public void testContadorCambiosTutorLimitado() {
        // Given: Alumno con contador = 2
        // When: Intentar REASIGNACION
        // Then: IllegalStateException (en TutorReasignacionService)
    }

    @Test
    public void testRaceConditionEnDuplicado() {
        // Given: Dos threads intentando asignar mismo alumno simultáneamente
        // When: Ejecutar en paralelo
        // Then: Uno de ellos recibe DuplicadoException
    }

    @Test
    public void testAsignacionCruzadaGeneraAlerta() {
        // Given: Alumno de carrera A, solo hay tutor carrera B
        // When: Asignar
        // Then: Asignación exitosa, alerta ASIGNACION_CRUZADA
    }

    @Test
    public void testLimpiezaActualizaCargasTutores() {
        // Given: Asignaciones duplicadas
        // When: Ejecutar cleanup
        // Then: Duplicados eliminados, cargas recalculadas correctamente
    }

    @Test
    public void testSemestreInactivoRechaza() {
        // Given: Semestre.activo = false
        // When: Intentar asignar
        // Then: IllegalStateException
    }

    @Test
    public void testConsistenciaTutorActualVsAsignacion() {
        // Given: Alumno con múltiples asignaciones en diferentes semestres
        // When: Verificar tutorActual
        // Then: Corresponde a última asignación
    }
}
```

---

### Fase 4: Monitoreo y Logging (Semana 7)

#### 4.1 Mejorar Logging

**Archivo:** `AsignacionServiceImpl.java`

```java
@Override
public ResultadoAsignacion asignarAlumnos(...) {
    log.info("════════════════════════════════════════════════════════════");
    log.info("INICIANDO ASIGNACIÓN DE ALUMNOS");
    log.info("  Total a procesar: {}", alumnosValidos.size());
    log.info("  Semestre: {} (ID: {})", semestre.getCodigo(), semestre.getId());
    log.info("  Proceso: {} (ID: {})", proceso.getId());
    log.info("════════════════════════════════════════════════════════════");

    // ... procesamiento ...

    log.info("ASIGNACIÓN COMPLETADA");
    log.info("  Procesados: {}", procesados);
    log.info("  Asignados: {} ({:.1f}%)", exitosos,
             (double) exitosos / procesados * 100);
    log.info("  Errores: {} ({:.1f}%)", errores.size(),
             (double) errores.size() / procesados * 100);
    log.info("  Alertas: {}", alertas.size());
    log.info("════════════════════════════════════════════════════════════");
}
```

---

#### 4.2 Métricas y Alertas

```java
@Component
public class AsignacionMetricsCollector {

    private final MeterRegistry meterRegistry;

    public void registrarAsignacionExitosa(String tipoAsignacion) {
        meterRegistry.counter("asignacion.exitosa",
            "tipo", tipoAsignacion).increment();
    }

    public void registrarErrorAsignacion(String tipoError) {
        meterRegistry.counter("asignacion.error",
            "tipo", tipoError).increment();
    }

    public void registrarAlerta(String tipoAlerta) {
        meterRegistry.counter("asignacion.alerta",
            "tipo", tipoAlerta).increment();
    }
}
```

---

## 6️⃣ ESTRATEGIA DE PRUEBAS

### 6.1 Pirámide de Pruebas

```
                    /\
                   /  \
                  /    \    MANUAL (pocos)
                 /______\
                /        \
               /          \   E2E / INTEGRACION (algunos)
              /____________\
             /              \
            /                \  UNITARIAS + INTEGRACION (muchos)
           /________________\

Total coverage: 80%+
```

---

### 6.2 Pruebas Unitarias Críticas

#### Test Suite 1: AsignacionServiceTest

```java
@Category(UnitTest.class)
public class AsignacionServiceUnitTest {

    @Mock private AlumnoRepository alumnoRepository;
    @Mock private TutorRepository tutorRepository;
    @Mock private AsignacionRepository asignacionRepository;
    @Mock private TutorSincronizacionService tutorSincronizacionService;
    @Mock private SemestreService semestreService;

    @InjectMocks private AsignacionServiceImpl asignacionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSeleccionTutorMismaCarrera() {
        // Verificar que selecciona tutor de misma carrera primero
    }

    @Test
    public void testSeleccionTutorCarreraCompatible() {
        // Verificar búsqueda matriz afinidad
    }

    @Test
    public void testMantenerTutorAnteriorSiTieneCapacidad() {
        // Verificar lógica de REINGRESO
    }

    @Test
    public void testIncrementarCargaSoloSiMantuvoPrevioFalse() {
        // Verificar que carga se incrementa en INICIAL/REASIGNACION
        // Pero NO en REINGRESO
    }

    @Test
    public void testGenerarAlertaReasignacionForzada() {
        // Verificar que se genera alerta cuando necesario
    }
}
```

---

### 6.3 Pruebas de Integración Críticas

#### Test Suite 2: AsignacionIntegrationTest

```java
@SpringBootTest
@Transactional
public class AsignacionServiceIntegrationTest {

    @Autowired private AsignacionService asignacionService;
    @Autowired private AlumnoRepository alumnoRepository;
    @Autowired private TutorRepository tutorRepository;
    @Autowired private AsignacionRepository asignacionRepository;
    @Autowired private SemestreRepository semestreRepository;
    @Autowired private ProcesoAsignacionRepository procesoRepository;
    @Autowired private TestDataBuilder testDataBuilder;

    @Test
    public void testAsignacion100AlumnosACapacidadLlena() {
        // Crear 100 alumnos, tutores con capacidad exacta
        // Asignar todos
        // Verificar: 100 asignados, cargas correctas
        List<AlumnoExcelDTO> alumnos = testDataBuilder.crearAlumnos(100);
        List<Tutor> tutores = testDataBuilder.crearTutores(10, capacidadMax: 10);

        ResultadoAsignacion resultado = asignacionService.asignarAlumnos(
            alumnos, procesoId, semestreId);

        assertEquals(100, resultado.getTotalAsignados());
        assertEquals(0, resultado.getTotalErrores());

        // Verificar cargas
        tutores.forEach(t -> {
            Tutor tutorActualizado = tutorRepository.findById(t.getId()).get();
            assertEquals(10, tutorActualizado.getCargaActual());
        });
    }

    @Test
    public void testAsignacionConAlumnosYaExistentes() {
        // Crear alumnos existentes, asignarlos semestre anterior
        // Luego asignar al mismo semestre nuevos datos
        // Verificar: DuplicadoException

        Alumno alumnoExistente = testDataBuilder.crearAlumnoCon(
            tutorActual: tutorA,
            semestre: semestre2025F1);

        asignacionRepository.save(new Asignacion()...);

        AlumnoExcelDTO alumnoEnLista = testDataBuilder.crearAlumnoExcelDTO(
            matricula: alumnoExistente.getMatricula());

        ResultadoAsignacion resultado = asignacionService.asignarAlumnos(
            List.of(alumnoEnLista), procesoId, semestre2025F1.getId());

        assertEquals(0, resultado.getTotalAsignados());
        assertEquals(1, resultado.getTotalErrores());
        assertEquals(ALUMNO_DUPLICADO, resultado.getErrores().get(0).getTipoError());
    }

    @Test
    public void testReasignacionManuralDuranteProceso() throws InterruptedException {
        // Simular:
        // - Thread A: Proceso automático cargando 500 alumnos
        // - Thread B: En el medio, reasignación manual de alumno X
        // Verificar: No hay corrupción de datos

        // Setup
        List<Alumno> alumnos = testDataBuilder.crearAlumnos(500);
        List<Tutor> tutores = testDataBuilder.crearTutores(10);

        // Thread A: Inicia proceso
        Thread procesoThread = new Thread(() -> {
            asignacionService.asignarAlumnos(
                convertirADTO(alumnos), procesoId, semestreId);
        });
        procesoThread.start();

        // Esperar a que procese ~100
        Thread.sleep(1000);

        // Thread B: Reasignar alumno 50
        tutorSincronizacionService.reasignarTutor(
            alumno50.getId(), tutorActualId, nuevoTutorId, semestre);

        // Esperar a que proceso termine
        procesoThread.join();

        // Verificar: Sin corrupción
        long totalAsignaciones = asignacionRepository.count();
        // Debería ser 500, no más
    }

    @Test
    public void testContadorCambiosTutorConOptimisticLocking() {
        // Simular race condition en increment de contador
        Alumno alumno = testDataBuilder.crearAlumnoConContador(0);

        Runnable incrementar = () -> {
            try {
                Alumno loaded = alumnoRepository.findByIdForUpdate(alumno.getId()).get();
                loaded.incrementarCambiosTutor();
                alumnoRepository.save(loaded);
            } catch (OptimisticLockingFailureException e) {
                // Esperado
            }
        };

        Thread t1 = new Thread(incrementar);
        Thread t2 = new Thread(incrementar);

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        Alumno actualizado = alumnoRepository.findById(alumno.getId()).get();
        // AMBOS deberían haber incrementado (o uno fallar)
        // La prueba verifica que no es posible que ambos hagan ++
    }

    @Test
    public void testCleanupActualizaCargasTutores() {
        // Crear asignaciones duplicadas manualmente
        // Ejecutar cleanup
        // Verificar: Duplicados eliminados, cargas sincronizadas

        Tutor tutor = testDataBuilder.crearTutor();
        Alumno alumno = testDataBuilder.crearAlumno();

        // Crear 3 asignaciones duplicadas (problema a limpiar)
        for (int i = 0; i < 3; i++) {
            asignacionRepository.save(new Asignacion()
                .setAlumno(alumno)
                .setTutor(tutor)
                .setSemestre(semestre)
                .setTipoAsignacion(TipoAsignacion.REASIGNACION));
        }

        // Antes: 3 asignaciones
        assertEquals(3, asignacionRepository.count());

        // Cleanup
        cleanupService.limpiarAsignacionesPorSemestre(semestre.getId());

        // Después: 1 asignación (la más reciente)
        assertEquals(1, asignacionRepository.count());

        // Cargas sincronizadas
        Tutor tutorActualizado = tutorRepository.findById(tutor.getId()).get();
        assertEquals(1, tutorActualizado.getCargaActual());
    }
}
```

---

### 6.4 Pruebas de Concurrencia

#### Test Suite 3: AsignacionConcurrencyTest

```java
@SpringBootTest
public class AsignacionConcurrencyTest {

    @Autowired private AsignacionService asignacionService;
    @Autowired private TutorSincronizacionService tutorSincronizacionService;
    @Autowired private TutorRepository tutorRepository;
    @Autowired private AsignacionRepository asignacionRepository;

    @Test
    public void testAsignacionConcurrenteAlMismoTutor() throws InterruptedException {
        // Simular 5 threads intentando asignar alumnos al mismo tutor
        // Tutor tiene capacidad de 5
        // Verificar: Exactamente 5 asignados, resto rechazados

        Tutor tutor = testDataBuilder.crearTutor(capacidad: 5);
        List<Alumno> alumnos = testDataBuilder.crearAlumnos(10);
        CountDownLatch latch = new CountDownLatch(10);
        AtomicInteger exitosos = new AtomicInteger(0);
        AtomicInteger errores = new AtomicInteger(0);

        for (Alumno alumno : alumnos) {
            new Thread(() -> {
                try {
                    asignacionService.asignarAlumno(alumno, tutor, semestre);
                    exitosos.incrementAndGet();
                } catch (CapacidadExcedidaException e) {
                    errores.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        assertEquals(5, exitosos.get());  // Capacidad llena
        assertEquals(5, errores.get());   // 5 rechazados

        Tutor actualizado = tutorRepository.findById(tutor.getId()).get();
        assertEquals(5, actualizado.getCargaActual());
    }

    @Test
    public void testReasignacionManualesConcurrentes() throws InterruptedException {
        // 3 coordinadores intenta reasignar al mismo alumno simultáneamente
        // Solo UNO debe tener éxito

        Alumno alumno = testDataBuilder.crearAlumno(tutorActual: tutor1);
        Tutor[] tutoresDestino = {tutor2, tutor3, tutor4};

        CountDownLatch latch = new CountDownLatch(3);
        AtomicInteger exitosos = new AtomicInteger(0);
        AtomicInteger errores = new AtomicInteger(0);

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            new Thread(() -> {
                try {
                    tutorSincronizacionService.reasignarTutor(
                        alumno.getId(),
                        tutor1.getId(),
                        tutoresDestino[idx].getId(),
                        semestre);
                    exitosos.incrementAndGet();
                } catch (Exception e) {
                    errores.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        // Al menos uno debe haber ejecutado
        assertTrue(exitosos.get() >= 1);
        // El alumno debe estar asignado a UNO de los tutores
        Alumno actualizado = alumnoRepository.findById(alumno.getId()).get();
        assertTrue(
            actualizado.getTutorActual().getId().equals(tutor1.getId()) ||
            actualizado.getTutorActual().getId().equals(tutor2.getId()) ||
            actualizado.getTutorActual().getId().equals(tutor3.getId()) ||
            actualizado.getTutorActual().getId().equals(tutor4.getId())
        );
    }

    @Test
    public void testOptimisticLockingEnContadorCambios() throws InterruptedException {
        // Simular 3 threads intentando incrementar contador del mismo alumno
        // Verificar que no sobrepasa 3

        Alumno alumno = testDataBuilder.crearAlumno(contadorCambios: 0);
        CountDownLatch latch = new CountDownLatch(3);
        AtomicInteger incrementados = new AtomicInteger(0);

        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                try {
                    Alumno loaded = alumnoRepository.findByIdForUpdate(alumno.getId()).get();
                    loaded.incrementarCambiosTutor();
                    alumnoRepository.save(loaded);
                    incrementados.incrementAndGet();
                } catch (OptimisticLockingFailureException e) {
                    // Uno de los threads falla (esperado)
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        // No todos los 3 deberían incrementar (al menos uno falla)
        assertTrue(incrementados.get() <= 2);

        Alumno actualizado = alumnoRepository.findById(alumno.getId()).get();
        // El contador debería ser <= 3 (y probablemente 1 o 2)
        assertTrue(actualizado.getContadorCambiosTutor() <= 3);
    }
}
```

---

## 7️⃣ CHECKLIST FINAL DE BLINDAJE

### ✅ Paso a Paso de Validación

#### Base de Datos
- [ ] Constraint `uk_asignacion_alumno_tutor_semestre` existe y funciona
- [ ] Constraint `fk_asignacion_semestre` con CASCADE ON DELETE/UPDATE
- [ ] Indices en `(id_alumno, id_semestre)` para búsquedas rápidas
- [ ] Índices en `(id_tutor, estado_alumno)` para conteos de carga
- [ ] @Version en tabla `alumnos` para optimistic locking
- [ ] Columna `origen_asignacion` en `asignaciones` (AUTOMATICO/MANUAL)
- [ ] Validación: COUNT duplicados = 0

#### Código
- [ ] `@Version` en entidad `Alumno`
- [ ] `DataIntegrityViolationException` manejada en `asignarAlumnoTransaccional()`
- [ ] Validación de contador en `AsignacionServiceImpl` para REASIGNACION
- [ ] Mapa de tutores es `ConcurrentHashMap` o se refresca cada N iteraciones
- [ ] Carga de tutor se lee de BD en `seleccionarTutorDisponible()`
- [ ] `semestreAcademico` eliminado de búsquedas, usándose siempre `semestre.id`
- [ ] Alertas se guardan atómicamente (antes de poder fallar)
- [ ] `validarSemestreParaAsignacion()` llamada en `asignarAlumnos()`
- [ ] `OrigenAsignacion` anotado en `Asignacion` en ambos flows (automático y manual)
- [ ] Logging mejorado con secciones delimitadas
- [ ] `findByIdForUpdate()` usado en lugares críticos (alumno, tutor)

#### Testing
- [ ] 40+ casos de prueba unitaria en `AsignacionServiceTest`
- [ ] 10+ casos de prueba integración en `AsignacionIntegrationTest`
- [ ] 5+ pruebas de concurrencia en `AsignacionConcurrencyTest`
- [ ] Coverage >= 80% en ramas críticas
- [ ] Tests de regresión para cada bug reportado

#### Operacional
- [ ] Plan de migración SQL documentado (V4, V5, V6)
- [ ] Rollback scripts preparados para cada migración
- [ ] Documentación de invariantes publicada
- [ ] Runbooks para incidents creados
- [ ] Alertas Prometheus/Grafana configuradas
- [ ] Métricas de asignación siendo recolectadas

#### Documentación
- [ ] Diagrama de flujo de asignación (actualizado)
- [ ] Documento de invariantes de negocio (publicado)
- [ ] Guía de troubleshooting (creada)
- [ ] Comentarios en código explicando lógica compleja

---

### Validación Pre-Producción

```bash
# 1. Ejecutar test suite completa
mvn clean test -P integration-tests

# 2. Verificar coverage
mvn jacoco:report
# Debe ser >= 80% en clases críticas

# 3. Static analysis
mvn sonar:sonar

# 4. Performance testing (carga 10K alumnos)
jmeter -n -t load-test-plan.jmx

# 5. Stress testing (concurrencia 100 threads)
# Custom script que simula reasignaciones manual + automática simultáneamente

# 6. Validar base de datos
SELECT COUNT(*) FROM asignaciones
WHERE (id_alumno, id_tutor, id_semestre) IN (
    SELECT id_alumno, id_tutor, id_semestre
    FROM asignaciones
    GROUP BY id_alumno, id_tutor, id_semestre
    HAVING COUNT(*) > 1
);
# Debe retornar 0

# 7. Verificar carga de tutores vs realidad
SELECT t.id, t.nombre, t.carga_actual,
       COUNT(a.id) as carga_real,
       ABS(t.carga_actual - COUNT(a.id)) as diferencia
FROM tutores t
LEFT JOIN asignaciones a ON t.id = a.id_tutor
WHERE a.alumno_id NOT IN (
    SELECT DISTINCT alumno_id FROM alumno_egresado
    UNION
    SELECT DISTINCT alumno_id FROM alumno_baja_definitiva
)
GROUP BY t.id
HAVING diferencia > 0
ORDER BY diferencia DESC;
# Debe retornar 0 filas
```

---

## CONCLUSIÓN

El servicio de asignación de tutores es **funcional pero requiere blindaje crítico en concurrencia**. Las recomendaciones de este análisis, implementadas en orden de prioridad, convertirán el módulo en un sistema **robusto, auditable y seguro para datos**.

**Tiempo estimado:**
- Fase 1 (CRÍTICA): 10 días
- Fase 2 (ALTA): 10 días
- Fase 3 (MEDIA): 10 días
- Fase 4 (MONITOREO): 5 días
- Testing y validación: 15 días
- **Total: 6-8 semanas**

---

**Documento preparado por:** Arquitecto Backend + QA Senior
**Fecha:** 2024-01-20
**Versión:** 1.0 - ANÁLISIS COMPLETO
