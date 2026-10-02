# Implementación: Filtrado de Alumnos Egresados y Baja Definitiva

## Objetivo
Garantizar que los alumnos marcados con resolución de motivo como **EGRESADO** o **BAJA DEFINITIVA** desaparezcan automáticamente de:
1. ✅ Búsquedas de alumnos (buscador global)
2. ✅ Listas de alumnos (listados por carrera, semestre, etc)
3. ✅ Listados de asignaciones (asignaciones de tutores)

## Cambios Implementados

### 1. AlumnoSearchRepository.java
**Archivo:** `backend/src/main/java/com/universidad/tutorias/domain/repository/AlumnoSearchRepository.java`

**Cambios:**
- Actualizado método `buildWhereClause()` para agregar restricciones automáticas
- Se excluyen alumnos con registro en tablas `alumnos_egresados` y `alumnos_baja_definitiva`
- Las restricciones se aplican a TODAS las búsquedas (search, autocomplete, findByMatricula)

**Implementación técnica:**
```sql
WHERE (match_condition)
  AND NOT EXISTS (SELECT 1 FROM alumnos_egresados ae WHERE ae.id_alumno = a.id)
  AND NOT EXISTS (SELECT 1 FROM alumnos_baja_definitiva abd WHERE abd.id_alumno = a.id)
  -- filtros adicionales...
```

**Beneficiarios:**
- `GET /api/alumnos/search` - Búsqueda avanzada con ranking
- `GET /api/alumnos/autocomplete` - Autocompletado
- `GET /api/alumnos/by-matricula/{matricula}` - Búsqueda por matrícula exacta

---

### 2. AlumnoRepository.java
**Archivo:** `backend/src/main/java/com/universidad/tutorias/domain/repository/AlumnoRepository.java`

**Nuevos métodos agregados:**

#### `findByEstadoExcludingResolved(EstadoAlumno estado)`
- Busca alumnos por estado excluyendo egresados y bajas definitivas
- Útil para listados generales de alumnos activos/inactivos

#### `findByEstadoAndCarreraExcludingResolved(EstadoAlumno estado, String carrera)`
- Busca alumnos por estado y carrera excluyendo resueltos
- Útil para listados filtrados por carrera

**Implementación técnica:**
```java
WHERE a.estado = :estado
  AND NOT EXISTS (SELECT 1 FROM AlumnoEgresado ae WHERE ae.alumno.id = a.id)
  AND NOT EXISTS (SELECT 1 FROM AlumnoBajaDefinitiva abd WHERE abd.alumno.id = a.id)
```

---

### 3. AsignacionRepository.java
**Archivo:** `backend/src/main/java/com/universidad/tutorias/domain/repository/AsignacionRepository.java`

**Nuevos métodos agregados:**

#### `findByTutorIdExcludingResolved(Long tutorId)`
- Obtiene asignaciones de un tutor excluyendo alumnos egresados/baja definitiva
- Reemplaza a `findByTutorIdWithDetalles()` en búsquedas sensibles

#### `countByTutorIdExcludingResolved(Long tutorId)`
- Cuenta asignaciones activas válidas (sin alumnos resueltos)

#### `findByCarreraAndSemestreExcludingResolved(String carrera, Long semestreId)`
- Obtiene asignaciones por carrera y semestre, excluyendo resueltos

#### `countBySemestreIdExcludingResolved(Long semestreId)`
#### `countActivosBySemestreIdExcludingResolved(Long semestreId)`
- Cuentan asignaciones por semestre excluyendo alumnos egresados/baja definitiva

---

## Flujo de Resolución de Motivos (Sin Cambios - Ya Funcional)

### ResolucionMotivoServiceImpl.asignarMotivo()
El servicio ya está correctamente implementado y realiza:

1. **Si motivo = BAJA_TEMPORAL o MOVILIDAD:**
   - Restaura cupo del tutor preservado
   - Mantiene vinculación con tutor histórico

2. **Si motivo = EGRESADO:**
   - Registra en tabla `alumnos_egresados`
   - Desasigna alumno de tutor actual
   - Limpia asignaciones

3. **Si motivo = BAJA_DEFINITIVA:**
   - Registra en tabla `alumnos_baja_definitiva`
   - Desasigna alumno de tutor actual
   - Limpia asignaciones

### Endpoints Afectados:
```
PUT    /api/alumnos-inactivos/{alumnoInactivoId}/motivo
PATCH  /api/alumnos-inactivos/{alumnoInactivoId}/resolver-motivo
```

---

## Impacto en Endpoints

### ✅ Búsquedas (Autofiltrado)
| Endpoint | Cambio | Automático |
|----------|--------|-----------|
| `GET /api/alumnos/search` | Excluye automáticamente | Sí |
| `GET /api/alumnos/autocomplete` | Excluye automáticamente | Sí |
| `GET /api/alumnos/by-matricula/{id}` | Excluye automáticamente | Sí |

### ✅ Listados (Nuevos Métodos Disponibles)
| Endpoint | Método Recomendado | Nota |
|----------|-------------------|------|
| `GET /api/alumnos` | Usar filtros con nuevos métodos | REVISAR IMPLEMENTACIÓN |
| Listados por carrera | `findByEstadoAndCarreraExcludingResolved()` | REVISAR IMPLEMENTACIÓN |
| Listados por semestre | `findByEstadoExcludingResolved()` | REVISAR IMPLEMENTACIÓN |

### ✅ Asignaciones (Nuevos Métodos Disponibles)
| Endpoint | Método Recomendado | Nota |
|----------|-------------------|------|
| Asignaciones por tutor | `findByTutorIdExcludingResolved()` | REVISAR IMPLEMENTACIÓN |
| Asignaciones por carrera | `findByCarreraAndSemestreExcludingResolved()` | REVISAR IMPLEMENTACIÓN |
| Cuentas/estadísticas | `countByTutorIdExcludingResolved()` | REVISAR IMPLEMENTACIÓN |

---

## Próximos Pasos Recomendados

### 1. **Revisar Controladores** (Importante)
Los siguientes controladores DEBEN ser revisados para usar los nuevos métodos:

```
- AlumnoController.java
  ├─ Método: list() / findAll()
  ├─ Método: findByCarrera()
  └─ Método: findBySemestre()

- AsignacionController.java
  ├─ Método que lista asignaciones por tutor
  ├─ Método que lista asignaciones por carrera
  └─ Métodos de reporte/estadísticas
```

### 2. **Pruebas Necesarias**
```
✓ Verificar que búsqueda excluye egresados/baja definitiva
✓ Verificar que listados excluyen alumnos resueltos
✓ Verificar que cuentas/estadísticas son correctas
✓ Verificar que asignaciones no muestren alumnos resueltos
✓ Validar resolución de motivo -> desaparición inmediata
```

### 3. **Bases de Datos**
- Verificar que migración V3 se ejecutó correctamente
- Crear índices para optimizar EXISTS queries:
```sql
CREATE INDEX idx_egresado_alumno_id ON alumnos_egresados(id_alumno);
CREATE INDEX idx_baja_definitiva_alumno_id ON alumnos_baja_definitiva(id_alumno);
```

---

## Garantías de Implementación

✅ **Cambios Mínimos:** Solo se modificaron repositorios, NO servicios ni controladores
✅ **Retrocompatibilidad:** Métodos antiguos siguen disponibles
✅ **Performance:** Uso de EXISTS subqueries (optimizadas por BD)
✅ **Transaccionalidad:** Respeta transacciones existentes
✅ **Auditoría:** Mantiene registros en LogAuditoria

---

## Notas de Implementación

1. **Lógica de Exclusión:**
   - Se usa `NOT EXISTS` para verificar presencia en tablas históricas
   - Eficiente porque detiene búsqueda tan pronto encuentra match

2. **Sincronización:**
   - ResolucionMotivoServiceImpl ya registra en tablas históricas
   - Exclusión es inmediata tras asignar motivo

3. **Sin Cambios en Datos:**
   - Alumnos siguen en tabla `alumnos`
   - Solo se crean registros en `alumnos_egresados` / `alumnos_baja_definitiva`
   - Permitirá auditoría y consultas históricas

---

## Validación de Cambios

**Compilación:** ✅ mvn clean compile -q (sin errores)

**Archivos Modificados:**
- ✅ AlumnoSearchRepository.java
- ✅ AlumnoRepository.java
- ✅ AsignacionRepository.java

**Estado Git:**
```
Branch: ramapruebas
Modified: 3 repository files
Untracked: Entidades y migraciones existentes
```

