# Propuesta: Reasignación Automática de Alumnos cuando un Tutor se Desactiva

**Fecha**: 2025-11-20
**Estado**: Propuesta Técnica
**Autor**: Equipo de Desarrollo
**Proyecto**: Sistema de Tutorías Universitarias

---

## Tabla de Contenidos
1. [Descripción del Problema](#1-descripción-del-problema)
2. [Solución Propuesta](#2-solución-propuesta)
3. [Consideraciones de Implementación](#3-consideraciones-de-implementación)
4. [Estimación de Esfuerzo](#4-estimación-de-esfuerzo)
5. [Riesgos y Mitigaciones](#5-riesgos-y-mitigaciones)
6. [Roadmap de Implementación](#6-roadmap-de-implementación)

---

## 1. Descripción del Problema

### 1.1 Situación Actual

Actualmente, cuando un tutor se marca como inactivo (`activo = false`) en el sistema:

- Los alumnos asignados a ese tutor quedan **sin tutor vigente**
- Las asignaciones existentes permanecen apuntando al tutor inactivo
- El sistema no tiene forma de identificar estos alumnos "huérfanos"
- Los reportes y consultas pueden incluir datos inconsistentes (alumno + tutor inactivo)
- No hay registro de la transición ni de quién quedó sin tutor

### 1.2 Impacto en el Negocio

| Aspecto | Impacto |
|--------|--------|
| **Alumnos** | Pierden seguimiento académico y asesoría |
| **Tutores** | Nuevos tutores no saben quién quedó sin asignar |
| **Administración** | Requiere identificar y reasignar manualmente |
| **Auditoría** | Falta trazabilidad de cambios |
| **Reportes** | Datos inconsistentes o incompletos |

### 1.3 Casos de Uso

1. **Tutor en licencia**: Se desactiva temporalmente durante permiso
2. **Tutor jubilado**: Se desactiva al terminar contrato
3. **Reorganización**: Se desactivan varios tutores para consolidar
4. **Reestructuración de carreras**: Cambio de estructura del programa

---

## 2. Solución Propuesta

### 2.1 Descripción General

Implementar un **proceso automático de reasignación** que se ejecute cuando un tutor se desactiva:

```
┌─────────────────────────────────────────────────────────────┐
│ DESACTIVACION DE TUTOR (UI o API)                           │
│  Cambio: activo = false                                     │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ TutorCrudService.actualizarTutor()                          │
│  - Valida que tutor existe y está activo                   │
│  - Marca activo = false                                     │
│  - Dispara evento @EventListener                            │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ EVENT: TutorDesactivadoEvent                                │
│  Parámetros: tutorId, carrera, timestamp                   │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ @EventListener: TutorDesactivacionHandler                   │
│  Ejecuta proceso async de reasignación                      │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ ReasignacionTutorInactivoService.reasignarAlumnos()         │
│  @Async - Ejecución en background                          │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
            ┌────────┴────────┐
            │                 │
            ▼                 ▼
    ┌──────────────┐   ┌──────────────────┐
    │ OK: Todos    │   │ PARCIAL: Algunos │
    │ reasignados  │   │ sin asignar      │
    └──────────────┘   └──────────────────┘
            │                 │
            ▼                 ▼
    ┌──────────────────────────────────────┐
    │ Auditoría y Notificaciones           │
    │  - Registrar en tutor_cambio_auditoria
    │  - Email a admin y tutores           │
    │  - Tabla pendientes_reasignacion     │
    └──────────────────────────────────────┘
```

### 2.2 Algoritmo de Reasignación

```
ENTRADA: tutorId (tutor a desactivar)
SALIDA: ResultadoReasignacion { exitosos[], pendientes[], errores[] }

INICIO:
  1. Obtener tutor por ID
     SI tutor.activo == false:
        RETORNAR error "Tutor ya está inactivo"

  2. Obtener lista de alumnos activos asignados al tutor
     alumnos = SELECT a FROM Alumno a
               WHERE a.tutorActual.id = :tutorId
               AND a.estado = ACTIVO

  3. SI alumnos está vacío:
        RETORNAR "0 alumnos afectados"

  4. PARA CADA alumno en alumnos:
     a. Obtener carrera del alumno
        carrera = alumno.carrera

     b. Buscar tutores candidatos
        tutores = SELECT t FROM Tutor t
                  WHERE t.carrera = carrera
                  AND t.id != tutorId
                  AND t.activo = true
                  AND t.tieneCapacidadDisponible()
                  ORDER BY t.cargaActual ASC
                  LIMIT 1

     c. SI tutores está vacío:
           GUARDAR en pendientes_reasignacion:
           {
             alumnoId,
             carrera,
             motivoPendencia: "NO_TUTORES_DISPONIBLES",
             fechaCreacion: NOW(),
             tutorOriginalId: tutorId
           }
           SIGUIENTE alumno

     d. Obtener primer tutor (menor carga)
        tutorDestino = tutores[0]

     e. Verificar capacidad actual (validación final)
        SI tutorDestino.cargaActual >= tutorDestino.capacidadMax:
           GUARDAR en pendientes_reasignacion
           SIGUIENTE alumno

     f. TRANSACCION ATOMICA:
        i.   Bloquear alumno (pessimistic write)
        ii.  Bloquear tutores (origen y destino)
        iii. alumno.tutorActual = tutorDestino
        iv.  tutorOrigen.decrementarCarga()
        v.   tutorDestino.incrementarCarga()
        vi.  Crear/actualizar Asignacion
        vii. Registrar en tutor_cambio_auditoria:
             {
               asignacion,
               tutorAnterior: tutorOrigen,
               tutorNuevo: tutorDestino,
               usuarioResponsable: "SISTEMA",
               motivo: "REASIGNACION_POR_DESACTIVACION",
               tipoCambio: "AUTOMATICO"
             }
        viii. Guardar en exitosos[]

     g. SI error en transaccion:
           Registrar en errores[] y continuar

  5. RETORNAR ResultadoReasignacion:
     {
       totalAlumnos: alumnos.size(),
       reasignados: exitosos.size(),
       pendientes: pendientes.size(),
       errores: errores.size(),
       detalles: {...}
     }

FIN
```

### 2.3 Trigger del Proceso

**Ubicación**: Método `actualizarTutor()` en `TutorCrudService`/`TutorCrudServiceImpl`

```java
// Pseudocódigo (NO implementación real)
public TutorResponseDTO actualizarTutor(Long id, TutorUpdateDTO request) {
    tutor = obtenerTutor(id)

    boolean estabaActivo = tutor.activo
    tutor.actualizar(request)  // Incluyendo activo = false
    tutorRepository.save(tutor)

    // EVENTO: Despedir al tutor activa reasignación
    if (estabaActivo && !tutor.activo) {
        applicationEventPublisher.publishEvent(
            new TutorDesactivadoEvent(id, tutor.carrera)
        )
    }

    return mapToResponse(tutor)
}
```

**Manejador de evento**:

```java
@Component
@Slf4j
public class TutorDesactivacionHandler {

    @EventListener
    public void alTutorDesactivarse(TutorDesactivadoEvent evento) {
        log.info("Tutor {} desactivado. Iniciando reasignación automática", evento.getTutorId())
        reasignacionService.reasignarAlumnosAsync(evento.getTutorId())
    }
}
```

### 2.4 Nuevas Entidades/Tablas Propuestas

#### 2.4.1 Tabla: `pendientes_reasignacion`

**Propósito**: Registrar alumnos que no pudieron ser reasignados automáticamente

```sql
CREATE TABLE pendientes_reasignacion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- Referencia
    id_alumno BIGINT NOT NULL,
    id_tutor_original BIGINT,  -- Tutor del que fue desasignado

    -- Información del alumno
    matricula VARCHAR(20) NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    carrera VARCHAR(100) NOT NULL,
    semestre INT NOT NULL,

    -- Razón de pendencia
    motivo_pendencia VARCHAR(50) NOT NULL,
      -- Valores: NO_TUTORES_DISPONIBLES, TUTOR_SIN_CAPACIDAD, ERROR_TECNICO
    descripcion VARCHAR(500),

    -- Timestamps
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_resolucion TIMESTAMP NULL,

    -- Estado
    resuelta BOOLEAN DEFAULT FALSE,
    usuario_resolucion VARCHAR(100),

    -- Auditoria
    notas_resolucion TEXT,

    -- Índices
    INDEX idx_alumno (id_alumno),
    INDEX idx_tutor_original (id_tutor_original),
    INDEX idx_carrera (carrera),
    INDEX idx_resuelta (resuelta),
    CONSTRAINT fk_alumno FOREIGN KEY (id_alumno) REFERENCES alumnos(id),
    CONSTRAINT fk_tutor FOREIGN KEY (id_tutor_original) REFERENCES tutores(id)
);
```

#### 2.4.2 Tabla: `snapshot_asignaciones_tutor`

**Propósito**: Guardar snapshot de asignaciones antes de desactivar tutor (para posible reversión)

```sql
CREATE TABLE snapshot_asignaciones_tutor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- Referencia
    id_tutor BIGINT NOT NULL,

    -- Snapshots
    datos_json JSON NOT NULL,
      -- [{alumnoId, asignacionId, nombreAlumno, tutorNuevoId}, ...]

    -- Metadata
    motivo VARCHAR(50) NOT NULL,  -- 'DESACTIVACION_TUTOR'
    fecha_snapshot TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Estado de reversión (OPCIONAL - ver 2.5)
    restaurado BOOLEAN DEFAULT FALSE,
    fecha_restauracion TIMESTAMP NULL,
    usuario_restauracion VARCHAR(100),

    -- Índices
    INDEX idx_tutor (id_tutor),
    INDEX idx_fecha (fecha_snapshot),
    CONSTRAINT fk_tutor FOREIGN KEY (id_tutor) REFERENCES tutores(id)
);
```

#### 2.4.3 Extensión: Tabla `logs_auditoria` (ya existe)

**Cambios propuestos**: Agregar nuevos tipos de acción

```java
public enum TipoAccion {
    // ... existentes
    DESACTIVACION_TUTOR,           // Nuevo
    REASIGNACION_POR_DESACTIVACION, // Nuevo
    RESTAURACION_ASIGNACIONES      // Nuevo (opcional)
}
```

### 2.5 Reversibilidad: Opción B (Recomendada - Automática)

#### Flujo de Reactivación

```
┌─────────────────────────────────────────┐
│ REACTIVACION DE TUTOR                   │
│ Cambio: activo = false → true           │
└────────────────┬────────────────────────┘
                 │
                 ▼
        ┌────────────────────┐
        │ ¿Hay snapshot?     │
        └──┬──────────────┬──┘
           │ SI           │ NO
           ▼              ▼
    ┌─────────────┐  ┌──────────────┐
    │ Mostrar     │  │ Solo reactivar
    │ opciones:   │  │ (Sin reversión)
    │ - Restaurar │  └──────────────┘
    │ - Mantener  │
    └──────┬──────┘
           │
           ├─→ SI "Restaurar":
           │   ├─ Validar capacidad de tutores actuales
           │   ├─ SI capacidad OK → Restaurar asignaciones
           │   └─ SI capacidad NO → Ofrecer reversión parcial
           │
           └─→ SI "Mantener":
               └─ Dejar alumnos con tutores actuales
```

#### Validaciones de Restauración

```
PRECONDICIONES PARA RESTAURAR:

1. Tutor debe estar reactivado (activo = true)
2. Debe existir snapshot válido
3. Para CADA alumno en snapshot:
   a. Alumno debe existir y estar ACTIVO
   b. Alumno no debe tener ya asignado otro tutor
      (si lo tiene, solicitar confirmación)
   c. Tutor destino (el original) debe tener capacidad

RESULTADO:
- ✓ Todos pueden restaurarse → Ejecutar restauración
- ⚠ Algunos pueden restaurarse → Ofrecer reversión PARCIAL
- ✗ Ninguno puede restaurarse → Mostrar error, mantener estado actual
```

### 2.6 Nuevos Endpoints Propuestos

#### 2.6.1 Desactivación con Reasignación

```http
PUT /api/tutores/{id}/desactivar
Content-Type: application/json

{
  "motivo": "Licencia por maternidad",
  "usuarioResponsable": "admin@universidad.edu"
}

Response 200 OK:
{
  "tutor": {
    "id": 5,
    "nombre": "Dra. María García",
    "activo": false
  },
  "reasignacion": {
    "totalAlumnos": 12,
    "reasignados": 11,
    "pendientes": 1,
    "errores": 0,
    "detalles": {
      "exitosos": [
        {
          "alumnoId": 1,
          "nombre": "Juan Pérez",
          "tutorAnterior": "Dra. María García",
          "tutorNuevo": "Dr. Carlos López"
        }
      ],
      "pendientes": [
        {
          "alumnoId": 13,
          "nombre": "Ana Rodríguez",
          "motivo": "NO_TUTORES_DISPONIBLES",
          "carrera": "INGENIERIA"
        }
      ]
    }
  },
  "timestamp": "2025-11-20T14:30:45Z"
}
```

#### 2.6.2 Reactivación con Reversión Opcional

```http
PUT /api/tutores/{id}/reactivar
Content-Type: application/json

{
  "usuarioResponsable": "admin@universidad.edu"
}

Response 200 OK:
{
  "tutor": {
    "id": 5,
    "nombre": "Dra. María García",
    "activo": true
  },
  "snapshot": {
    "existe": true,
    "fechaSnapshot": "2025-11-15T10:00:00Z",
    "alumnosEnSnapshot": 12,
    "opcionesDisponibles": ["RESTAURAR", "MANTENER_ACTUAL"]
  },
  "timestamp": "2025-11-20T15:45:30Z"
}
```

#### 2.6.3 Restauración de Asignaciones

```http
POST /api/tutores/{id}/restaurar-asignaciones
Content-Type: application/json

{
  "snapshotId": 42,
  "tipoRestauracion": "COMPLETA",  -- o "PARCIAL"
  "usuarioResponsable": "admin@universidad.edu"
}

Response 200 OK:
{
  "restauracion": {
    "exitosa": true,
    "alumnosRestaurados": 11,
    "alumnosNoRestaurados": 1,
    "detalles": {
      "restaurados": [
        {
          "alumnoId": 1,
          "nombre": "Juan Pérez",
          "tutorActual": "Dr. Carlos López",
          "tutorAsignado": "Dra. María García"
        }
      ],
      "noRestaurados": [
        {
          "alumnoId": 2,
          "nombre": "Luis Martín",
          "razon": "Tutor actual sin capacidad para desasignación"
        }
      ]
    }
  },
  "timestamp": "2025-11-20T16:10:15Z"
}
```

#### 2.6.4 Gestión de Pendientes

```http
GET /api/tutores/pendientes-reasignacion?carrera=INGENIERIA&resuelta=false

Response 200 OK:
{
  "total": 5,
  "por_motivo": {
    "NO_TUTORES_DISPONIBLES": 3,
    "TUTOR_SIN_CAPACIDAD": 2
  },
  "pendientes": [
    {
      "id": 101,
      "alumno": {
        "id": 1,
        "matricula": "MAT001",
        "nombre": "Juan Pérez"
      },
      "motivo": "NO_TUTORES_DISPONIBLES",
      "carrera": "INGENIERIA",
      "fechaCreacion": "2025-11-15T10:00:00Z"
    }
  ]
}
```

```http
POST /api/tutores/pendientes/{id}/resolver
Content-Type: application/json

{
  "tutorNuevoId": 8,
  "usuarioResponsable": "admin@universidad.edu"
}

Response 200 OK:
{
  "resolucion": {
    "exitosa": true,
    "alumnoId": 1,
    "tutorAsignado": "Dr. Roberto Sánchez",
    "timestamp": "2025-11-20T16:20:00Z"
  }
}
```

### 2.7 Cambios en Servicios Existentes

#### 2.7.1 TutorCrudService (Actualizar)

```java
// Interfaz modificada:
public interface TutorCrudService {
    // ... métodos existentes

    // NUEVO:
    TutorResponseDTO desactivarTutor(Long id, String motivo);

    TutorResponseDTO reactivarTutor(Long id);

    Optional<SnapshotAsignacionesDTO> obtenerUltimoSnapshot(Long tutorId);

    ResultadoRestauracionDTO restaurarAsignaciones(Long tutorId, Long snapshotId);
}
```

#### 2.7.2 Nuevo Servicio: ReasignacionTutorInactivoService

```java
// Interfaz:
public interface ReasignacionTutorInactivoService {

    @Async
    CompletableFuture<ResultadoReasignacion> reasignarAlumnosAsync(Long tutorId);

    ResultadoReasignacion reasignarAlumnos(Long tutorId);

    List<PendienteReasignacionDTO> obtenerPendientes(String carrera, Boolean resuelta);

    void resolverPendiente(Long pendienteId, Long tutorNuevoId, String usuario);
}
```

#### 2.7.3 Nuevo Servicio: SnapshotAsignacionesService

```java
// Interfaz:
public interface SnapshotAsignacionesService {

    void crearSnapshot(Long tutorId, String motivo);

    Optional<SnapshotAsignacionesDTO> obtenerUltimo(Long tutorId);

    ResultadoRestauracionDTO restaurar(Long snapshotId, TipoRestauracion tipo);

    boolean validarRestauracionPosible(Long snapshotId);
}
```

---

## 3. Consideraciones de Implementación

### 3.1 Arquitectura de Eventos

Usar **Spring Event Publishing** para desacoplamiento:

```
TutorCrudService.actualizarTutor()
    ↓
    applicationEventPublisher.publishEvent(TutorDesactivadoEvent)
    ↓
    @EventListener en TutorDesactivacionHandler
    ↓
    reasignacionService.reasignarAlumnosAsync()
    ↓
    Ejecución en thread pool @Async
```

**Ventajas**:
- Desacoplamiento entre servicios
- No bloquea la respuesta HTTP
- Fácil de testear
- Permite agregar más listeners en el futuro

### 3.2 Procesamiento Asincrónico

Usar `@Async` para reasignaciones masivas:

```java
@Service
@Slf4j
public class ReasignacionTutorInactivoServiceImpl {

    @Async("reasignacionExecutor")
    public CompletableFuture<ResultadoReasignacion> reasignarAlumnosAsync(Long tutorId) {
        try {
            log.info("Iniciando reasignación async para tutor {}", tutorId);
            ResultadoReasignacion resultado = reasignarAlumnos(tutorId);
            notificarResultado(resultado);
            return CompletableFuture.completedFuture(resultado);
        } catch (Exception e) {
            log.error("Error en reasignación async", e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
```

**Configuración del Executor**:

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "reasignacionExecutor")
    public Executor reasignacionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("reasignacion-");
        executor.initialize();
        return executor;
    }
}
```

### 3.3 Transaccionalidad

Usar `@Transactional` para garantizar consistencia:

```java
@Transactional
private void reasignarAlumnos(Long tutorId) {
    // Cada reasignación de alumno es atómica
    for (Alumno alumno : alumnos) {
        try {
            reasignarAlumno(alumno, tutorDestino);
        } catch (Exception e) {
            // Rollback solo de este alumno, continuar con los demás
            registrarError(alumno, e);
        }
    }
}
```

### 3.4 Bloqueos Pesimistas

Usar `@Lock(LockModeType.PESSIMISTIC_WRITE)` para evitar condiciones de carrera:

```java
// En repositorio (ya existe patrón similar)
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT a FROM Alumno a WHERE a.id = :id")
Optional<Alumno> findByIdForUpdate(@Param("id") Long id);
```

### 3.5 Notificaciones

Implementar notificaciones a:

- **Tutores afectados**: Email con lista de alumnos reasignados
- **Administrador**: Email con resumen y pendientes
- **Dashboard**: Badge de "pendientes de reasignación"

```java
private void notificarResultado(ResultadoReasignacion resultado) {
    if (resultado.tienePendientes()) {
        notificationService.notificarAdministrador(
            "Reasignación Incompleta",
            "Se encontraron " + resultado.getPendientes().size() + " alumnos sin reasignar"
        );
    }
}
```

### 3.6 Logging y Auditoria

Registros detallados en cada paso:

```java
log.info("Iniciando desactivación de tutor {}", tutorId);
log.info("Encontrados {} alumnos asignados", alumnos.size());
log.info("Reasignación de alumno {} a tutor {}", alumnoId, tutorNuevoId);
log.warn("Alumno {} pendiente: {}", alumnoId, motivo);
log.error("Error en reasignación: {}", e.getMessage(), e);
```

### 3.7 Validaciones Edge Cases

| Caso | Manejo |
|------|--------|
| Tutor sin alumnos | Sin error, operación completada (0 reasignados) |
| Carrera sin tutores activos | Guardar como pendientes |
| Todos los tutores sin capacidad | Guardar todos como pendientes |
| Alumno ya pendiente | Actualizar registro existente |
| Tutor reactivado sin snapshot | Solo reactivación, sin reversión |
| Snapshot corrupto | Error con fallback a estado actual |

---

## 4. Estimación de Esfuerzo

### 4.1 Desglose por Componentes

| Componente | Horas | Notas |
|-----------|-------|-------|
| **Backend - Eventos y Servicios** | 6-8 | Implementar ReasignacionTutorInactivoService, eventos |
| **Backend - Nuevas Entidades** | 2-3 | Crear entidades y migraciones |
| **Backend - Endpoints** | 3-4 | Controlador + validaciones |
| **Backend - Testing** | 4-6 | Unit tests + integration tests |
| **Frontend - UI Desactivación** | 3-4 | Diálogo/confirmación con resumen |
| **Frontend - UI Gestión Pendientes** | 2-3 | Tabla de pendientes + acciones |
| **Documentación** | 1-2 | Actualizar OpenAPI/Swagger |
| **Deployment** | 1-2 | Migraciones, scripts SQL |
| **TOTAL BACKEND** | **18-23 horas** | |
| **TOTAL FRONTEND** | **5-7 horas** | |
| **TOTAL PROYECTO** | **23-30 horas** | |

### 4.2 Fases de Implementación

#### **Fase 1: MVP (1-2 semanas)**
- ✓ Reasignación automática básica
- ✓ Tabla `pendientes_reasignacion`
- ✓ Endpoint desactivar tutor
- ✓ Endpoint gestión de pendientes

#### **Fase 2: Reversibilidad (2-3 semanas)**
- ✓ Tabla `snapshot_asignaciones_tutor`
- ✓ Endpoint reactivar tutor
- ✓ Endpoint restaurar asignaciones
- ✓ Validaciones de restauración

#### **Fase 3: Polish (1 semana)**
- ✓ Notificaciones email
- ✓ Dashboard/Reportes
- ✓ Mejoras UX/UI
- ✓ Testing exhaustivo

---

## 5. Riesgos y Mitigaciones

### 5.1 Tabla de Riesgos

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|-------------|--------|-----------|
| **Carrera sin tutores disponibles** | Alta | Alto | Tabla pendientes + alerta admin + reasignación manual futura |
| **Reasignación masiva lenta (100+ alumnos)** | Media | Medio | Procesamiento @Async + progress tracking + UI feedback |
| **Conflictos de capacidad al restaurar** | Media | Medio | Validación previa + reversión parcial + manual override |
| **Pérdida de datos en snapshot corrupto** | Baja | Alto | Validación JSON en insert + backups de BD |
| **Transacciones largas con deadlock** | Baja | Medio | Usar pessimistic locking + timeout + retry logic |
| **Notificación email no se envía** | Media | Bajo | Logger fallback + cola de reintentos |
| **Usuario no entiende estado pendiente** | Media | Bajo | UI clara + tooltip explicativo + documentación |
| **Reversión rompe ciclo académico** | Baja | Alto | Validación de fechas (solo reversión en semestre actual) |

### 5.2 Plan de Rollback

Si hay problemas críticos después del deployment:

1. **Desactivar evento automático**: Comentar `@EventListener`
2. **Mantener datos históricos**: Snapshot intacto para auditoría
3. **Reasignaciones manuales**: Endpoint de reasignación manual sigue disponible
4. **Revertir cambios**: Scripts SQL para rollback de migraciones

---

## 6. Roadmap de Implementación

### 6.1 Timeline Propuesta

```
SEMANA 1-2: MVP
├─ Día 1-2: Setup + Entidades + Migraciones
├─ Día 3-4: ReasignacionTutorInactivoService
├─ Día 5-7: Eventos + Endpoints desactivación
├─ Día 8-10: Testing + Fixes
└─ Día 11-12: Deploy a staging + UAT

SEMANA 3: Reversibilidad
├─ Día 1-3: SnapshotAsignacionesService
├─ Día 4-6: Endpoints reactivación/restauración
├─ Día 7-8: Validaciones + Testing
└─ Día 9-10: Deploy a staging + UAT

SEMANA 4: Polish
├─ Día 1-2: Notificaciones email
├─ Día 3-4: Dashboard/Reportes
├─ Día 5-7: Testing exhaustivo
├─ Día 8-9: Documentación + Capacitación
└─ Día 10: Deploy a producción

TOTAL: ~4 semanas (MVP + Reversibilidad + Polish)
```

### 6.2 Criterios de Aceptación

#### **MVP (Fase 1)**
- [ ] Al desactivar tutor, todos los alumnos asignados son reasignados a otro tutor activo
- [ ] Los alumnos que no pueden ser reasignados aparecen en tabla de pendientes
- [ ] Se registra auditoría de cada reasignación
- [ ] Endpoint desactivar tutor retorna resumen de resultados
- [ ] Admin puede ver y resolver pendientes manualmente

#### **Reversibilidad (Fase 2)**
- [ ] Antes de desactivar, se crea snapshot de asignaciones
- [ ] Al reactivar tutor, se ofrece opción de restaurar asignaciones
- [ ] Restauración solo ocurre si hay capacidad en tutor original
- [ ] Se registra auditoría de restauraciones
- [ ] UI muestra estado de reversibilidad

#### **Polish (Fase 3)**
- [ ] Admin recibe email con resumen de desactivación
- [ ] Dashboard muestra KPIs: total pendientes, resueltos, tasa de éxito
- [ ] Documentación actualizada (OpenAPI, README)
- [ ] 100% cobertura de test (unit + integration)
- [ ] Performance < 5 segundos para 100+ alumnos

---

## 7. Referencias y Contexto del Proyecto

### 7.1 Entidades Existentes Relacionadas

```
Tutor (domain/entity/Tutor.java)
  ├─ id, nombre, carrera, capacidadMax, cargaActual, activo
  └─ métodos: tieneCapacidadDisponible(), incrementarCarga(), decrementarCarga()

Alumno (domain/entity/Alumno.java)
  ├─ id, matricula, nombre, carrera, semestre, estado
  ├─ tutorActual (FK a Tutor)
  └─ contadorCambiosTutor

Asignacion (domain/entity/Asignacion.java)
  ├─ id, alumno (FK), tutor (FK), semestre, tipoAsignacion
  └─ fechaAsignacion

TutorCambioAuditoria (domain/entity/TutorCambioAuditoria.java)
  ├─ id, asignacion, tutorAnterior, tutorNuevo
  ├─ usuarioResponsable, fechaHoraCambio, motivo, tipoCambio
  └─ notas

LogsAuditoria (domain/entity/LogsAuditoria.java)
  ├─ id, tipoAccion, tipoEntidad, idEntidad
  ├─ descripcion, datosAntes, datosDespues, usuarioResponsable
  └─ timestamp
```

### 7.2 Servicios Existentes Relacionados

```
TutorCrudService / TutorCrudServiceImpl
  └─ listarTutores(), obtenerTutor(), actualizarTutor(), eliminarTutor()

TutorReasignacionService / TutorReasignacionServiceImpl
  └─ reasignarTutor(alumno, tutorOrigen, tutorDestino)

TutorSincronizacionService / TutorSincronizacionServiceImpl
  └─ sincronizarYBloquearTutor(), recalcularCargaTutor()

TutorCambioAuditoriaService / TutorCambioAuditoriaServiceImpl
  └─ registrarCambio(), obtenerHistorial()

AuditoriaService
  └─ registrarLog() -- Usar para registrar desactivación
```

### 7.3 Excepciones Custom Disponibles

```
domain/exception/:
  ├─ ReasignacionTutorException ✓ (ya existe)
  ├─ CapacidadExcedidaException ✓ (ya existe)
  ├─ DomainValidationException ✓ (ya existe)
  └─ ... (otras)

infrastructure/exception/GlobalExceptionHandler ✓ (ya existe)
  └─ Mapeo a HTTP status codes
```

### 7.4 Patrón de Respuestas API

```java
// Usar ApiResponse existente
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success,
    T data,
    String message
) {}

// Errores mapeados en GlobalExceptionHandler
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
    int status,
    String error,
    String message,
    String path,
    Instant timestamp,
    List<FieldErrorDetail> fieldErrors,
    List<?> excelErrors
) {}
```

---

## 8. Apéndice: Ejemplos de Código

*(Este apéndice solo contiene pseudocódigo/estructura, NO código real de implementación)*

### 8.1 Estructura de Eventos

```java
// Event
public class TutorDesactivadoEvent extends ApplicationEvent {
    private Long tutorId;
    private String carrera;
    private String usuario;
    // getters
}

// Handler
@Component
@EventListener
public class TutorDesactivacionHandler {
    public void handle(TutorDesactivadoEvent evento) { ... }
}
```

### 8.2 Estructura de DTOs Respuesta

```java
public record ResultadoReasignacion(
    Long tutorId,
    Integer totalAlumnos,
    Integer reasignados,
    Integer pendientes,
    Integer errores,
    List<ReasignacionExitosaDTO> exitosos,
    List<PendienteReasignacionDTO> pendientesList,
    List<ErrorReasignacionDTO> erroresList,
    LocalDateTime timestamp
) {}

public record PendienteReasignacionDTO(
    Long id,
    Long alumnoId,
    String matricula,
    String nombre,
    String carrera,
    String motivoPendencia,
    LocalDateTime fechaCreacion
) {}
```

### 8.3 Estructura de Servicios

```java
@Service
@Slf4j
@Transactional
public class ReasignacionTutorInactivoServiceImpl
    implements ReasignacionTutorInactivoService {

    private final AlumnoRepository alumnoRepository;
    private final TutorRepository tutorRepository;
    private final PendienteReasignacionRepository pendienteRepo;
    private final SnapshotAsignacionesRepository snapshotRepo;
    // ... más deps

    @Async("reasignacionExecutor")
    public CompletableFuture<ResultadoReasignacion> reasignarAlumnosAsync(Long tutorId) {
        // 1. Validar tutor
        // 2. Obtener alumnos
        // 3. Para cada alumno: buscar tutor, reasignar, registrar
        // 4. Guardar pendientes
        // 5. Publicar evento
        // 6. Retornar resultado
    }

    private void reasignarAlumno(Alumno alumno, Tutor tutorDestino) {
        // Bloquear, validar, actualizar, guardar
    }

    public List<PendienteReasignacionDTO> obtenerPendientes(...) {
        // Query a BD
    }
}
```

---

## 9. Conclusiones y Recomendaciones

### 9.1 Recomendación Final

**Se recomienda proceder con la implementación en 3 fases**:

1. **MVP (Fase 1)**: Reasignación automática + gestión de pendientes
2. **Reversibilidad (Fase 2)**: Snapshot + restauración
3. **Polish (Fase 3)**: Notificaciones, dashboard, documentación

### 9.2 Próximos Pasos

1. **Aprobación**: Validar propuesta con stakeholders
2. **Estimación refinada**: Sesión de planning con equipo
3. **Design review**: Revisar esquemas de BD y APIs
4. **Sprint planning**: Crear tickets y estimar velocidad
5. **Implementación**: Comenzar Phase 1

### 9.3 Preguntas Abiertos a Discutir

- ¿Priorizar MVP o incluir reversibilidad desde el inicio?
- ¿Notificaciones email inmediatas o cola asincrónica?
- ¿Permitir reversión parcial o solo completa?
- ¿Qué rol debe tener el tutor original en la reversión?
- ¿Límite de tiempo para reversión (ej: solo en mismo semestre)?

---

**Documento generado**: 2025-11-20
**Versión**: 1.0
**Estado**: Propuesta Abierta a Feedback
