# 🏗️ ARQUITECTURA COMPLETA - MÓDULO DE ASIGNACIONES

**Actualizado:** 2025-11-19
**Compilación:** ✅ SIN ERRORES
**Fases Completadas:** 1, 2, 3 de 6

---

## 📋 Tabla de Contenidos

1. [Resumen Arquitectónico](#resumen-arquitectónico)
2. [Capas del Sistema](#capas-del-sistema)
3. [Flujo de Datos Completo](#flujo-de-datos-completo)
4. [Endpoints REST](#endpoints-rest)
5. [Servicios Implementados](#servicios-implementados)
6. [Entidades y DTOs](#entidades-y-dtos)
7. [Almacenamiento (BD)](#almacenamiento-bd)
8. [Patrones de Diseño](#patrones-de-diseño)
9. [Estado Actual vs Próximas Fases](#estado-actual-vs-próximas-fases)

---

## 🎯 Resumen Arquitectónico

### Principios de Diseño Implementados

1. **Separación de Responsabilidades:**
   - Controller: Maneja HTTP, orchestración
   - Services: Lógica de negocio
   - Repositories: Acceso a datos
   - Entities: Modelos de dominio

2. **Validación Completa:**
   - NO se detiene en primer error
   - Recopila TODOS los errores
   - Permite corrección integral del usuario

3. **Ordenamiento por Semestre:**
   - Semestres altos (8°, 7°, 6°) primero
   - Nuevo ingreso (1°) al final
   - Maximiza éxito de asignación

4. **Auditoría de Cambios:**
   - Tabla separada `TutorCambioAuditoria`
   - NO modifica tipo de asignación
   - Trazabilidad completa de cambios manuales

5. **Escalabilidad:**
   - Sin hardcoding de semestres
   - Funciona para N semestres
   - Basado en `semestreId` (NOT `semestreAcademico` string)

---

## 🏛️ Capas del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│ PRESENTATION LAYER                                          │
│ ─────────────────────────────────────────────────────────────
│ AsignacionController                                        │
│  • POST /validar-excel           (Fase 3 ✅)                │
│  • POST /ejecutar                (Fase 4 pending)           │
│  • POST /iniciar                 (Legacy)                   │
│  • POST /cambio-tutor            (Legacy, mejora Fase 4)    │
│  • GET /proceso/:id              (Legacy)                   │
│  • GET /procesos                 (Legacy)                   │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│ APPLICATION LAYER (Services)                                │
│ ─────────────────────────────────────────────────────────────
│ ExcelValidacionYOrdenaService    (Fase 2 ✅)                │
│  • validarYProcesarExcel()       Orquestador principal     │
│  • limpiarDatos()                Normalización de datos    │
│  • ordenarPorSemestre()          Ordenamiento por prioridad│
│  • convertirADtos()              Conversión de tipos       │
│                                                             │
│ TutorCambioAuditoriaService      (Fase 2 ✅)                │
│  • registrarCambio()             Auditoría de cambios      │
│  • registrarCambioDetallado()    Con notas y tipo          │
│  • obtenerHistorial...()         7 métodos de query        │
│                                                             │
│ ExcelReaderService               (Existente)               │
│  • leerArchivo()                 Lectura de Excel          │
│                                                             │
│ AlumnoValidadorService           (Existente, mejorar)      │
│  • validarAlumnos()              Validación completa       │
│                                                             │
│ AsignacionService                (Fase 4 - refactorizar)   │
│  • asignarAlumnos()              Lógica pura de asignación │
│                                                             │
│ TutorReasignacionService         (Existente)               │
│  • reasignarTutor()              Cambio manual de tutor    │
│                                                             │
│ SemestreService                  (Existente)               │
│  • convertirCodigoAId()          Conversión de código      │
│                                                             │
│ ProcesoOrchestrator              (Legacy)                  │
│  • ejecutarProcesoCompleto()     Orquestación antigua      │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│ DOMAIN LAYER (Entities & Repositories)                      │
│ ─────────────────────────────────────────────────────────────
│ Entities:                                                   │
│  • Asignacion (tabla principal)                            │
│  • TutorCambioAuditoria (tabla auditoría) ← Fase 2 ✅       │
│  • Alumno                                                  │
│  • Tutor                                                   │
│  • Semestre                                                │
│                                                             │
│ Repositories:                                              │
│  • AsignacionRepository                                    │
│  • TutorCambioAuditoriaRepository        ← Fase 2 ✅        │
│  • AlumnoRepository                                        │
│  • TutorRepository                                         │
│  • SemestreRepository                                      │
│  • AlumnoSearchRepository                                  │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│ DATA ACCESS LAYER                                           │
│ ─────────────────────────────────────────────────────────────
│ Spring Data JPA / Hibernate ORM                            │
│                                                             │
│ Database (MySQL 8.0)                                       │
│  • asignaciones (tipo: NUEVO_INGRESO, REINGRESO)          │
│  • tutor_cambio_auditoria (auditoría)                      │
│  • alumnos                                                 │
│  • tutores                                                 │
│  • semestres                                               │
│  • ... (otras tablas del sistema)                          │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔄 Flujo de Datos Completo

### Escenario: Carga masiva de alumnos con Excel

```
1. USUARIO FRONTEND
   │
   └─→ Selecciona archivo Excel + semestre
       │
       └─→ POST /api/asignaciones/validar-excel
           │
           ├─ archivo: (MultipartFile)
           └─ semestreId: 1

2. ASIGNACION CONTROLLER
   │
   └─→ validarExcel()
       │
       ├─ Valida que semestre existe
       ├─ Delega a ExcelValidacionYOrdenaService.validarYProcesarExcel()
       │
       └─→ Responde con ExcelValidacionResponse

3. EXCEL VALIDACION Y ORDENA SERVICE
   │
   ├─ Paso 1: Leer Excel
   │  └─ Delega a ExcelReaderService.leerArchivo()
   │     └─ Retorna: List<AlumnoExcelDTO> (150 alumnos)
   │
   ├─ Paso 2: Validar semestre
   │  └─ Verifica semestreId existe en BD
   │
   ├─ Paso 3: Limpiar datos
   │  └─ limpiarDatos(alumnos)
   │     • trim() en todos los strings
   │     • uppercase en códigos
   │     • normalización de espacios
   │
   ├─ Paso 4: Validar alumnos (COMPLETO, sin detener)
   │  └─ Delega a AlumnoValidadorService.validarAlumnos()
   │     └─ Retorna: ResultadoValidacion (con TODOS los errores)
   │
   ├─ Paso 5: ¿Hay errores?
   │  │
   │  ├─ SI → Retorna ExcelValidacionResponse con errors[]
   │  │       status="ERROR"
   │  │       data=null
   │  │
   │  └─ NO → Procede a paso 6
   │
   └─ Paso 6: Ordenar alumnos
      └─ ordenarPorSemestre(alumnos)
         • Semestre 8 → ordenPriority 8 (primero)
         • Semestre 1 → ordenPriority 1 (último)
         • Sorted DESC
         └─ Retorna: List<AlumnoValidadoDTO> ORDENADO

4. RESPUESTA EXITOSA (status="OK")
   │
   └─→ Frontend recibe:
       {
         status: "OK",
         data: [AlumnoValidadoDTO[], ...],  ← ORDENADO
         errors: [],
         message: "150 alumnos listos"
       }

5. USUARIO FRONTEND
   │
   └─→ Revisa listado (visual check opcional)
       │
       └─→ POST /api/asignaciones/ejecutar
           │
           └─ body: {
               semestreId: 1,
               alumnosValidados: [AlumnoValidadoDTO[], ...]
              }

6. ASIGNACION CONTROLLER
   │
   └─→ ejecutarAsignacion()
       │
       ├─ Valida precondiciones
       │  • Datos vienen de /validar-excel ✓
       │  • Alumnos están ordenados ✓
       │  • Todos del mismo semestre ✓
       │  • Validación completada ✓
       │
       └─ Delega a AsignacionService.asignarAlumnos()
          (Implementación Fase 4)

7. ASIGNACION SERVICE (Fase 4 - Próxima)
   │
   ├─ Para cada alumno EN ORDEN:
   │  │
   │  ├─ Obtener tutor óptimo (mínima carga)
   │  ├─ Crear registro Asignacion
   │  │  • tipo = NUEVO_INGRESO o REINGRESO
   │  │  • tutor = tutor óptimo
   │  ├─ Actualizar carga de tutor
   │  ├─ Registrar en TutorCambioAuditoria (si cambio manual)
   │  └─ Incrementar contador de asignados O errores
   │
   └─ Retorna: EjecucionAsignacionResponse
      {
        status: "OK",
        totalAlumnos: 150,
        alumnosAsignados: 150,
        alumnosConError: 0,
        duracionMs: 2500,
        porcentajeExito: 100.0
      }

8. USUARIO FRONTEND
   │
   └─→ Recibe respuesta exitosa
       • Muestra estadísticas
       • Asignación completada
       • Puede consultar historial de cambios
```

---

## 🌐 Endpoints REST

### Nuevos (Phase 3) ✅

#### `POST /api/asignaciones/validar-excel`
| Propiedad | Valor |
|-----------|-------|
| **Responsabilidad** | Validar, limpiar, ordenar Excel |
| **Input** | MultipartFile + semestreId |
| **Output** | ExcelValidacionResponse |
| **Status OK** | 200 (si status="OK" o status="ERROR") |
| **Status Error** | 500 (si excepción no controlada) |
| **Valida** | SÍ (completo, todos los errores) |
| **Modifica BD** | NO |
| **Transactional** | NO (readOnly implícito) |

#### `POST /api/asignaciones/ejecutar`
| Propiedad | Valor |
|-----------|-------|
| **Responsabilidad** | Ejecutar asignación pura |
| **Input** | EjecutarAsignacionRequest |
| **Output** | EjecucionAsignacionResponse |
| **Status OK** | 200 (placeholder Fase 4) |
| **Status Error** | 500 (si excepción) |
| **Precondiciones** | Datos de /validar-excel (status="OK") |
| **Modifica BD** | SÍ (Fase 4) |
| **Transactional** | SÍ (Fase 4) |

### Legacy (Fase anterior)
- `POST /api/asignaciones/iniciar`
- `GET /api/asignaciones/proceso/{id}`
- `GET /api/asignaciones/proceso/{id}/alertas`
- `GET /api/asignaciones/procesos`
- `POST /api/asignaciones/cambio-tutor` (mejora Fase 4)

---

## 🔧 Servicios Implementados

### ExcelValidacionYOrdenaService (Fase 2 ✅)

```java
public interface ExcelValidacionYOrdenaService {
    ExcelValidacionResponse validarYProcesarExcel(MultipartFile, Long semestreId);
    void limpiarDatos(List<AlumnoExcelDTO> alumnos);
    List<AlumnoValidadoDTO> ordenarPorSemestre(List<AlumnoValidadoDTO> alumnos);
    List<AlumnoValidadoDTO> convertirADtos(List<AlumnoExcelDTO>, Long semestreId);
}
```

**Implementación:** ExcelValidacionYOrdenaServiceImpl (350+ líneas)

**Características Clave:**
- Orquestación de flujo completo
- Logging detallado con SLF4j
- Manejo robusto de excepciones
- Conversión de errores de validación
- Algoritmo de ordenamiento por semestre

### TutorCambioAuditoriaService (Fase 2 ✅)

```java
public interface TutorCambioAuditoriaService {
    TutorCambioAuditoriaDTO registrarCambio(Asignacion, Tutor, String usuario, String motivo);
    TutorCambioAuditoriaDTO registrarCambioDetallado(
        Asignacion, Tutor, String usuario, String motivo, String tipoCambio, String notas);
    List<TutorCambioAuditoriaDTO> obtenerHistorialPorAsignacion(Long);
    List<TutorCambioAuditoriaDTO> obtenerHistorialPorAlumno(Long);
    List<TutorCambioAuditoriaDTO> obtenerAsignacionesPorTutor(Long);
    List<TutorCambioAuditoriaDTO> obtenerRemocionesPorTutor(Long);
    List<TutorCambioAuditoriaDTO> obtenerCambiosPorFecha(LocalDateTime, LocalDateTime);
    List<TutorCambioAuditoriaDTO> obtenerCambiosPorUsuario(String);
    List<TutorCambioAuditoriaDTO> obtenerCambiosPorTipo(String);
}
```

**Implementación:** TutorCambioAuditoriaServiceImpl (200+ líneas)

**Características Clave:**
- Registra cambios de tutor en auditoría
- NO modifica tipo_asignacion
- 9 métodos de query para análisis
- Conversión automática a DTO
- Información completa de trazabilidad

---

## 📊 Entidades y DTOs

### Entidades (Domain Layer)

#### **Asignacion** (Existente)
```java
@Entity
public class Asignacion {
    @Id Long id;
    @ManyToOne Alumno alumno;
    @ManyToOne Tutor tutor;
    @ManyToOne Semestre semestre;
    TipoAsignacion tipo;  // NUEVO_INGRESO, REINGRESO (Phase 1 ✅)
    LocalDateTime fechaAsignacion;
    // ... otros campos
}
```

#### **TutorCambioAuditoria** (Phase 2 ✅)
```java
@Entity
public class TutorCambioAuditoria {
    @Id Long id;
    @ManyToOne Asignacion asignacion;
    @ManyToOne(nullable=true) Tutor tutorAnterior;
    @ManyToOne Tutor tutorNuevo;
    String usuarioResponsable;
    LocalDateTime fechaHoraCambio;
    String motivo;
    String tipoCambio;  // MANUAL, SISTEMA, etc
    String notas;
    // ... índices para performance
}
```

### DTOs (Application Layer)

#### Para Validación

**AlumnoExcelDTO** (input de Excel)
- matricula, nombre, carrera, semestre

**AlumnoValidadoDTO** (output del validador)
- alumnoId, matricula, nombre, carrera
- semestreId, semestreCodigo, semestreNumerico
- ordenPriority, validado

**ExcelErrorDTO** (error individual)
- filaExcel, campo, valor, descripcion
- tipoError, severidad

**ExcelValidacionResponse** (respuesta de /validar-excel)
- status (OK, ERROR, WARNING)
- message, timestamp
- totalFilas, totalValidas, totalErrores, porcentajeExito
- errors[], data[] (AlumnoValidadoDTO[])
- resumen

#### Para Ejecución

**EjecutarAsignacionRequest** (request de /ejecutar)
- semestreId
- alumnosValidados[]

**EjecucionAsignacionResponse** (response de /ejecutar)
- status (OK, PARTIAL, ERROR, PENDING)
- message, timestamp
- totalAlumnos, alumnosAsignados, alumnosConError
- duracionMs, porcentajeExito
- detalles, erroresDetalle[]

**AsignacionErrorDTO** (error de asignación)
- alumnoId, alumnoMatricula, alumnoNombre
- error, raizCausa
- tutorIntentado

#### Para Auditoría

**TutorCambioAuditoriaDTO** (auditoría de cambio)
- auditoriaId, asignacionId
- alumno (AlumnoSimpleDTO)
- tutorAnterior, tutorNuevo (TutorSimpleDTO)
- usuarioResponsable, fechaHoraCambio
- motivo, tipoCambio, notas

---

## 💾 Almacenamiento (BD)

### Tabla: `asignaciones` (Modificada)

```sql
CREATE TABLE asignaciones (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_alumno BIGINT NOT NULL,
    id_tutor BIGINT NOT NULL,
    id_semestre BIGINT NOT NULL,
    tipo_asignacion VARCHAR(50),  -- NUEVO_INGRESO, REINGRESO (Phase 1 ✅)
    fecha_asignacion TIMESTAMP,
    FOREIGN KEY (id_alumno) REFERENCES alumnos(id) ON DELETE CASCADE,
    FOREIGN KEY (id_tutor) REFERENCES tutores(id) ON DELETE RESTRICT,
    FOREIGN KEY (id_semestre) REFERENCES semestres(id) ON DELETE RESTRICT,
    INDEX idx_alumno_asignacion (id_alumno),
    INDEX idx_tutor_asignacion (id_tutor),
    INDEX idx_semestre_asignacion (id_semestre)
);
```

**Cambios Phase 1:**
- ✅ Enumeración TipoAsignacion: NUEVO_INGRESO, REINGRESO (2 valores)
- ❌ Eliminados: INICIAL, REASIGNACION

### Tabla: `tutor_cambio_auditoria` (Phase 2 ✅)

```sql
CREATE TABLE tutor_cambio_auditoria (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_asignacion BIGINT NOT NULL,
    id_tutor_anterior BIGINT,  -- nullable
    id_tutor_nuevo BIGINT NOT NULL,
    usuario_responsable VARCHAR(100),
    fecha_hora_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    motivo VARCHAR(500),
    tipo_cambio VARCHAR(50),  -- MANUAL, SISTEMA, etc
    notas LONGTEXT,
    FOREIGN KEY (id_asignacion) REFERENCES asignaciones(id) ON DELETE CASCADE,
    FOREIGN KEY (id_tutor_anterior) REFERENCES tutores(id) ON DELETE RESTRICT,
    FOREIGN KEY (id_tutor_nuevo) REFERENCES tutores(id) ON DELETE RESTRICT,
    INDEX idx_asignacion_cambio (id_asignacion),
    INDEX idx_fecha_cambio (fecha_hora_cambio),
    INDEX idx_usuario_cambio (usuario_responsable),
    INDEX idx_tutor_nuevo (id_tutor_nuevo),
    INDEX idx_tutor_anterior (id_tutor_anterior)
);
```

**Migración:** V4__crear_tabla_tutor_cambio_auditoria.sql

---

## 🎨 Patrones de Diseño

### 1. **Service Layer Pattern**
- Separación clara entre Controller y Business Logic
- Services no conocen detalles de HTTP
- Fácil de testear

### 2. **DTO Pattern**
- Transformación de entidades a DTOs
- Aislamiento de cambios de esquema
- Contrato claro para API

### 3. **Repository Pattern**
- Acceso a datos agnóstico de implementación
- Spring Data JPA para queries automáticas
- Custom queries vía @Query

### 4. **Orchestrator Pattern**
- ExcelValidacionYOrdenaService: orquesta validación completa
- ExcelReaderService + AlumnoValidadorService: servicios delegados

### 5. **Audit Trail Pattern**
- TutorCambioAuditoria entity: registro inmutable de cambios
- Tabla separada: no afecta lógica principal
- Trazabilidad completa

### 6. **Validation Completeness Pattern**
- NO se detiene en primer error
- Recopila TODOS los errores
- Usuario ve todo lo que hay que corregir de una vez

### 7. **Priority-Based Ordering Pattern**
- Cálculo de prioridad: semestreNumerico (1-12)
- Ordenamiento DESC (mayor primero)
- Escalable para N semestres

---

## 📊 Estado Actual vs Próximas Fases

### Phase 1: FUNDAMENTOS ✅ (COMPLETADA)

**Objetivos Logrados:**
- ✅ Enumeración TipoAsignacion refactorizada
- ✅ Entidad TutorCambioAuditoria creada
- ✅ Repositorio y migraciones BD
- ✅ DTOs para validación
- ✅ Compilación sin errores

**Archivos Creados:** 9
**Archivos Modificados:** 4
**Líneas de Código:** ~450

---

### Phase 2: SERVICIOS DE VALIDACIÓN ✅ (COMPLETADA)

**Objetivos Logrados:**
- ✅ ExcelValidacionYOrdenaService implementado
- ✅ TutorCambioAuditoriaService implementado
- ✅ Validación completa (todos los errores)
- ✅ Ordenamiento por semestre
- ✅ Compilación sin errores

**Archivos Creados:** 4
**Archivos Modificados:** 0
**Líneas de Código:** ~700

---

### Phase 3: ENDPOINTS REST ✅ (COMPLETADA)

**Objetivos Logrados:**
- ✅ Endpoint POST /validar-excel implementado
- ✅ Endpoint POST /ejecutar (placeholder)
- ✅ DTOs de respuesta (EjecucionAsignacionResponse, AsignacionErrorDTO)
- ✅ Manejo robusto de excepciones
- ✅ Compilación sin errores

**Archivos Creados:** 2 (DTOs)
**Archivos Modificados:** 1 (Controller)
**Líneas de Código:** ~380

---

### Phase 4: LÓGICA DE ASIGNACIÓN PURA ⏳ (PRÓXIMA)

**Objetivos Pendientes:**
- [ ] Refactorizar AsignacionService
- [ ] Implementar método asignarAlumnos()
- [ ] Crear registros Asignacion correctamente
- [ ] Actualizar cargas de tutores
- [ ] Registrar en auditoría
- [ ] Implementar endpoint /ejecutar
- [ ] Mejorar endpoint /cambio-tutor

**Archivos a Crear:** 1 (refactor AsignacionService)
**Archivos a Modificar:** 1 (TutorReasignacionService - mejora)
**Líneas de Código Estimadas:** ~500

---

### Phase 5: LEGACY CLEANUP ⏳ (FUTURA)

**Objetivos:**
- [ ] Eliminar semestreAcademico de lógica
- [ ] Usar solo semestreId
- [ ] Actualizar todas las queries
- [ ] Validar compatibilidad

---

### Phase 6: TESTING & DOCS ⏳ (FUTURA)

**Objetivos:**
- [ ] Unit tests para servicios
- [ ] Integration tests para endpoints
- [ ] Load tests con N semestres
- [ ] Documentación final
- [ ] User guides

---

## 📈 Métricas Generales

### Código

| Métrica | Fase 1 | Fase 2 | Fase 3 | Total |
|---------|--------|--------|--------|-------|
| Archivos Creados | 9 | 4 | 2 | **15** |
| Archivos Modificados | 4 | 0 | 1 | **5** |
| Líneas Nuevas | ~450 | ~700 | ~380 | ~1,530 |
| Entidades Nuevas | 1 | 0 | 0 | **1** |
| DTOs Nuevos | 5 | 0 | 2 | **7** |
| Servicios Nuevos | 0 | 2 | 0 | **2** |
| Endpoints Nuevos | 0 | 0 | 2 | **2** |
| Compilación | ✅ | ✅ | ✅ | **✅** |

### Cobertura Arquitectónica

- Presentation Layer: ✅ Completa (3 endpoints nuevos)
- Application Layer: ✅ Completa (2 servicios nuevos)
- Domain Layer: ✅ Completa (1 entidad nueva, 7 DTOs)
- Data Access: ✅ Completa (1 tabla nueva, migraciones)

---

## 🔮 Próximos Pasos Inmediatos

1. **Phase 4 - Implementación de /ejecutar:**
   - Crear método `asignarAlumnos()` en AsignacionService
   - Implementar lógica de asignación (best-effort)
   - Actualizar cargas de tutores
   - Registrar en auditoría

2. **Testing:**
   - Tests unitarios para ExcelValidacionYOrdenaService
   - Tests de integración para endpoints

3. **Deployment:**
   - Generar migration para BD
   - Validar cambios de enum en producción
   - Rollback plan si es necesario

---

## 📚 Documentación Generada

- ✅ FASE1_REFACTOR_COMPLETADA.md
- ✅ PLAN_REFACTOR_MODULO_ASIGNACIONES.md
- ✅ FASE3_ENDPOINTS_COMPLETADA.md
- ✅ ARQUITECTURA_COMPLETA.md (este documento)

---

**Última actualización:** 2025-11-19
**Estado:** Phases 1-3 completadas, Phase 4 pendiente

