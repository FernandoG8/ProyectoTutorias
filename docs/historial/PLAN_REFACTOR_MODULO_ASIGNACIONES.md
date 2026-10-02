# Plan de Refactor - Módulo de Asignación de Tutores

**Fecha:** 2025-11-19
**Objetivo:** Separación clara de responsabilidades, escalabilidad y claridad en el flujo de asignaciones
**Riesgo:** BAJO - cambios principalmente de orquestación, sin alterar lógica crítica existente

---

## 1️⃣ PROBLEMAS ACTUALES IDENTIFICADOS

### 1.1 Mezcla de Responsabilidades

**Situación Actual:**
```
ProcesoOrchestratorImpl orquesta 5 fases que se mezclan:
├─ Lectura de Excel (formato, parseo)
├─ Validación de datos (errores, duplicados)
├─ Limpieza y preparación de datos
├─ Comparación con BD
├─ Liberación de cupos
├─ Procesamiento de reingresos
└─ Asignación de tutores

El resultado: difícil de testear, reutilizar y extender.
Endpoint único: POST /api/asignaciones/iniciar
└─ Mezcla: validación + asignación en una sola llamada
```

**Impacto:**
- ❌ No se puede validar Excel sin ejecutar asignación
- ❌ No se puede reusar validación en otro contexto
- ❌ Difícil de debuggear: si falla, no sé dónde
- ❌ Testing requiere mocks complejos de 5 servicios

### 1.2 Inconsistencias Legacy

Asignacion.java tiene dos campos de semestre (semestreAcademico string + semestre entity)
Queries usan indistintamente ambos campos
Riesgo de desincronización de datos

### 1.3 Tipos de Asignación Ambiguos

TipoAsignacion actual: INICIAL, REASIGNACION, REINGRESO
Problema: No hay distinción clara entre cambios manuales y automáticos
Cambios manuales de tutor se registran como REASIGNACION (confunde la tabla principal)

### 1.4 Sin Auditoría de Cambios Manuales

POST /api/asignaciones/cambio-tutor existe pero:
- Solo actualiza tutor, no hay trazabilidad
- No registra quién, cuándo, por qué
- No hay tabla de auditoría específica

---

## 2️⃣ ARQUITECTURA PROPUESTA

### 2.1 Separación en Dos Endpoints

**ENDPOINT 1:** POST /api/asignacion/validar-excel
- INPUT: MultipartFile (Excel)
- RESPONSABILIDAD: Validar, limpiar, ordenar
- OUTPUT: { status, errors[], data[] }

**ENDPOINT 2:** POST /api/asignacion/ejecutar
- INPUT: semestreId + alumnosValidados[]
- RESPONSABILIDAD: Asignar tutores (lógica pura)
- OUTPUT: { status, totalAsignados, errors[] }

### 2.2 Flujo Simplificado

ANTES: ExcelFile → iniciarProceso() → 5 fases entrelazadas → Resultado
DESPUÉS:
  Step 1: ExcelFile → validar-excel → { status, errors, data }
  Step 2: { semestreId, data } → ejecutar → Resultado

### 2.3 Responsabilidades Claras

| Componente | ANTES | DESPUÉS |
|-----------|-------|---------|
| Controller | Orquesta 5 fases | Orquesta 2 endpoints |
| ExcelReader | Lee archivo | Lee archivo |
| Validador | Valida poco | Valida completamente |
| ExcelProcessor (NUEVO) | NO EXISTE | Limpia y ordena |
| AsignacionService | Asigna + orquesta | SOLO asigna |
| ProcesoOrchestrator | Orquesta todo | Simplificado |
| Auditoría | Sin registro de cambios | TutorCambioAuditoria |

---

## 3️⃣ CAMBIOS EN ENTIDADES

### 3.1 TipoAsignacion - Simplificar a 2 Tipos

ANTES: INICIAL, REASIGNACION, REINGRESO (ambiguo)
DESPUÉS:
  - NUEVO_INGRESO: Primera asignación al sistema
  - REINGRESO: Retorna tras inactividad
  
Cambios manuales → NO modifican tipo, van a TutorCambioAuditoria

### 3.2 NUEVA ENTIDAD: TutorCambioAuditoria

```
id
id_asignacion (FK)
id_tutor_anterior (FK)
id_tutor_nuevo (FK)
usuario_responsable
fecha_hora_cambio
motivo
tipo_cambio
notas
```

Propósito: Registrar histórico de cambios manuales sin alterar tipo_asignacion

### 3.3 Asignacion.java - Eliminar semestreAcademico

ANTES: Dos campos (semestreAcademico STRING + semestre ENTITY)
DESPUÉS: Solo semestre ENTITY como fuente única de verdad

---

## 4️⃣ NUEVAS CLASES A CREAR

### DTOs
- AlumnoValidadoDTO
- ExcelValidacionResponse
- TutorCambioAuditoriaDTO
- EjecutarAsignacionRequest

### Servicios
- ExcelValidacionYOrdenaService
- TutorCambioAuditoriaService
- TutorCambioAuditoriaRepository

### Entidades
- TutorCambioAuditoria

---

## 5️⃣ CAMBIOS EN SERVICIOS EXISTENTES

### AsignacionService - SIMPLIFICADO

ANTES:
  ResultadoAsignacion asignarAlumnos(List<AlumnoExcelDTO>, Long procesoId, Long semestreId)

DESPUÉS:
  ResultadoAsignacion asignarAlumnos(List<AlumnoValidadoDTO>, Long semestreId)
  
  - Asumir datos ya validados
  - Solo lógica de negocio pura
  - RESPETA el orden de semestres ya definido

### ExcelReaderService - SIN CAMBIOS

### AlumnoValidadorService - MEJORADO

- Reportar TODOS los errores (no detener en primero)
- Validar que semestre existe
- Devolver lista completa de errores + alumnos válidos

### ProcesoOrchestrator - SIMPLIFICADO O ELIMINADO

Si necesitas tracking, reducir a solo eso
Si no, eliminar y llamar servicios directamente

---

## 6️⃣ NUEVOS ENDPOINTS

```java
@PostMapping("/validar-excel")
public ResponseEntity<ExcelValidacionResponse> validarExcel(
    @RequestParam("file") MultipartFile file,
    @RequestParam("semestreId") Long semestreId)

@PostMapping("/ejecutar")
public ResponseEntity<ResultadoAsignacion> ejecutarAsignacion(
    @Valid @RequestBody EjecutarAsignacionRequest request)

@PostMapping("/cambio-tutor")  // MEJORADO
public ResponseEntity<TutorCambioResponse> cambioTutor(
    @Valid @RequestBody CambioTutorRequest request,
    @AuthenticationPrincipal UserDetails usuario)
    // Registra en TutorCambioAuditoria, NO modifica tipo_asignacion
```

---

## 7️⃣ VALIDACIÓN Y ORDENAMIENTO POR SEMESTRE

### Lógica de Ordenamiento

Ordena alumnos por SEMESTRE (mayor → menor)

Razón: Liberar cupos en semestres avanzados PRIMERO
       Al asignar nuevo ingreso, hay más cupo disponible

Implementar:
  alumnos.stream()
    .sorted(Comparator.comparingInt(a -> -a.getSemestreNumerico()))
    .collect(Collectors.toList())

### Validación de Semestre

- Obtener semestre una sola vez
- Validar que existe
- Validar que todos los alumnos son para ESTE semestre
- Reportar cualquier mismatch como error

---

## 8️⃣ ELIMINACIÓN DE INCONSISTENCIAS LEGACY

### Campos a Eliminar de Asignacion.java

❌ @Column(name = "semestre_academico") String semestreAcademico;
✅ @ManyToOne Semestre semestre;  // ÚNICA FUENTE DE VERDAD

### Queries a Actualizar

❌ WHERE semestre_academico = '2025-2026-F1'
✅ WHERE id_semestre = 5

### Mappers a Actualizar

Remover cualquier referencia a semestreAcademico en lógica
Mantener solo para compatibilidad legacy en lectura

---

## 9️⃣ ESCALABILIDAD PARA N SEMESTRES

### Patrón Genérico (Sin Hardcoding)

Clave: Usar SOLO semestreId en toda la lógica

❌ ANTI-PATRÓN:
  if (semestreId == 1) { ... }
  else if (semestreId == 2) { ... }

✅ PATRÓN CORRECTO:
  Usar semestreId como parámetro dinámico en TODAS las queries
  Loop genérico sobre alumnos
  Asignación funciona para ANY semestre

### Determinador de Tipo (Genérico)

```
if (alumno tiene asignación previa) {
    return REINGRESO
} else {
    return NUEVO_INGRESO
}
```

Sin hardcoding por semestre, funciona para todos

---

## 🔟 TABLA DE COMPARATIVA (ANTES vs DESPUÉS)

| Aspecto | ANTES | DESPUÉS |
|--------|-------|---------|
| Endpoints | 1 (iniciar) | 2 (validar-excel, ejecutar) |
| Validación Excel | Dentro de orquestador | Endpoint separado |
| Ordenamiento | No explícito | Explícito por semestre |
| Responsabilidades | Mezcladas | Separadas y claras |
| Tipos de asignación | 3 ambiguo | 2 claros |
| Cambios de tutor | Se crean asignaciones | Se registran en auditoría |
| Auditoría de cambios | No existe | TutorCambioAuditoria |
| Legacy semestreAcademico | Usado en lógica | Eliminado de lógica |
| Testabilidad | Difícil (5 servicios) | Fácil (2 servicios) |
| Escalabilidad N semestres | Hardcoding | Genérico, dinámico |
| Reutilización | Difícil | Fácil |

---

## 1️⃣1️⃣ FASES DE IMPLEMENTACIÓN

**Fase 1:** Fundamentos (1-2 días)
  - Crear TipoAsignacion enum simplificado
  - Crear TutorCambioAuditoria entity + migration
  - Crear DTOs nuevos

**Fase 2:** Servicios de Validación (2-3 días)
  - Crear ExcelValidacionYOrdenaService
  - Mejorar AlumnoValidadorService
  - Crear TutorCambioAuditoriaService

**Fase 3:** Refactor de Asignación (2-3 días)
  - Simplificar AsignacionService
  - Implementar cambio de tutor con auditoría

**Fase 4:** Endpoints y Controller (1-2 días)
  - Crear /api/asignacion/validar-excel
  - Crear /api/asignacion/ejecutar
  - Mejorar /api/asignacion/cambio-tutor

**Fase 5:** Limpieza Legacy (1 día)
  - Eliminar semestreAcademico de lógica
  - Actualizar queries

**Fase 6:** Testing (2-3 días)
  - Unit tests
  - Integration tests
  - Load tests con múltiples semestres

**Fase 7:** Documentación (1 día)
  - Comentarios en código
  - Guía de uso

**Tiempo Total:** 10-15 días

---

## 1️⃣2️⃣ CRITERIOS DE ÉXITO

✅ Dos endpoints claramente separados y funcionales
✅ Validación reporta TODOS los errores
✅ Datos ordenados por semestre en respuesta válida
✅ Cambios de tutor registrados en auditoría
✅ Tipos reducidos a NUEVO_INGRESO y REINGRESO
✅ Escalable para N semestres sin hardcoding
✅ semestreAcademico eliminado de lógica
✅ Tests pasando
✅ Código documentado
✅ Compilación sin errores

---

**Próximo Paso:** Implementar Fase 1 con creación de entidades y DTOs.
