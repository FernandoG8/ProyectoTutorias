# Corrección de Bugs en Liberación de Cupos - Documentación

## Resumen de Cambios

Se han corregido 3 bugs críticos en el sistema de asignación de tutores que impedían la liberación correcta de cupos cuando alumnos se marcaban como inactivos.

---

## Bugs Corregidos

### Bug #1: Query SQL defectuosa en `AlumnoInactivoRepository`
**Archivo**: `backend/src/main/java/com/universidad/tutorias/domain/repository/AlumnoInactivoRepository.java`

**Problema**:
```java
// ❌ ANTES: Usa JOIN FETCH que EXCLUYE registros con tutorPreservado NULL
@Query("SELECT ai FROM AlumnoInactivo ai JOIN FETCH ai.tutorPreservado WHERE ai.cupoLiberado = true")
List<AlumnoInactivo> findConCupoLiberado();
```

**Solución**:
```java
// ✅ DESPUÉS: Usa LEFT JOIN FETCH que INCLUYE nulls
@Query("SELECT ai FROM AlumnoInactivo ai LEFT JOIN FETCH ai.tutorPreservado WHERE ai.cupoLiberado = true")
List<AlumnoInactivo> findConCupoLiberado();
```

**Impacto**:
- Si un alumno se marcaba como inactivo pero no tenía tutor (o el tutor se eliminó), su cupo **NUNCA** se liberaba
- Ahora se incluyen correctamente en la liberación de cupos

---

### Bug #2: Sincronización incorrecta de carga del tutor
**Archivo**: `backend/src/main/java/com/universidad/tutorias/domain/repository/AsignacionRepository.java`

**Problema**:
```java
// ❌ ANTES: Cuenta TODAS las asignaciones, incluyendo las de alumnos INACTIVOS
@Query("SELECT COUNT(a) FROM Asignacion a WHERE a.tutor.id = :tutorId")
int countByTutorId(@Param("tutorId") Long tutorId);
```

**Ejemplo del Problema**:
```
Tutor "Dr. García" tiene:
  - A0001 (ACTIVO)     → Asignacion
  - A0002 (ACTIVO)     → Asignacion
  - A0003 (INACTIVO)   → Asignacion ← Sigue contando!
  - A0004 (INACTIVO)   → Asignacion ← Sigue contando!

countByTutorId = 4 (INCORRECTO)
Debería ser = 2 (solo ACTIVOS)
```

**Solución**:
```java
// ✅ DESPUÉS: Solo cuenta asignaciones de alumnos ACTIVOS
@Query("SELECT COUNT(a) FROM Asignacion a WHERE a.tutor.id = :tutorId AND a.alumno.estado = 'ACTIVO'")
int countByTutorId(@Param("tutorId") Long tutorId);
```

**Impacto**:
- Sincronización de carga correcta
- Tutores no aparecen como "sobrecargados" cuando realmente tienen cupos disponibles
- Asignación de nuevos alumnos más precisa

---

### Bug #3: Motivo de inactividad siempre era SIN_DEFINIR
**Archivo**: `backend/src/main/java/com/universidad/tutorias/application/service/impl/InactivacionServiceImpl.java`

**Problema**:
```java
// ❌ ANTES: Todos los inactivos se marcaban con SIN_DEFINIR
AlumnoInactivo inactivo = AlumnoInactivo.builder()
    .alumno(alumno)
    .motivoInactividad(MotivoInactividad.SIN_DEFINIR)  // ← SIEMPRE
    .tutorPreservado(alumno.getTutorActual())
    .cupoLiberado(true)
    .build();
```

**Impacto**:
- No se podía distinguir entre:
  - `BAJA_TEMPORAL`: El alumno podría volver, conservar el tutor
  - `BAJA_DEFINITIVA`: Alumno se va permanentemente, liberar cupo
  - `EGRESADO`: Ya no necesita tutor
  - `MOVILIDAD`: Cambio de carrera/universidad

---

## Nuevas Funcionalidades

### Nuevo Endpoint: Resolver Motivo de Inactividad

**Ruta**: `PATCH /api/alumnos-inactivos/{alumnoInactivoId}/resolver-motivo`

**Parámetros**:
- `alumnoInactivoId` (path): ID del registro de alumno inactivo
- `motivo` (query): El motivo de inactividad a asignar

**Motivos disponibles**:
- `BAJA_TEMPORAL`: El alumno puede regresar, preservar tutor
- `BAJA_DEFINITIVA`: El alumno no regresará, liberar cupo
- `EGRESADO`: El alumno egresó, liberar cupo
- `MOVILIDAD`: Cambio de carrera/universidad, preservar tutor

**Ejemplo de Solicitud**:
```bash
curl -X PATCH "http://localhost:8080/api/alumnos-inactivos/1/resolver-motivo?motivo=BAJA_TEMPORAL"
```

---

## Respuesta del Endpoint

### Escenario 1: Éxito con capacidad disponible
```json
{
  "status": "success",
  "data": {
    "exitoso": true,
    "mensaje": "Motivo actualizado a BAJA_TEMPORAL - Tutor preservado tiene capacidad",
    "sugerencia": "✓ El tutor Dr. García tiene 3 cupo(s) disponible(s). El alumno puede reintegrarse cuando sea necesario.",
    "alumnoInactivoId": 1,
    "alumnoMatricula": "A0001",
    "alumnoNombre": "Juan Pérez",
    "motivoInactividad": "BAJA_TEMPORAL",
    "tutorPreservadoId": 5,
    "tutorNombre": "Dr. García",
    "tutorCargaActual": 12,
    "tutorCapacidadMax": 15,
    "tutorCuposDisponibles": 3,
    "tutorActivo": true,
    "tutorTieneCapacidad": true
  },
  "message": "Motivo actualizado a BAJA_TEMPORAL - Tutor preservado tiene capacidad",
  "timestamp": "2025-11-14T19:30:00"
}
```

### Escenario 2: Tutor sin capacidad (requiere incremento)
```json
{
  "status": "success",
  "data": {
    "exitoso": true,
    "mensaje": "Motivo actualizado a BAJA_TEMPORAL (requiere preservar tutor)",
    "sugerencia": "⚠️ AVISO: El tutor Dr. García está a CAPACIDAD MÁXIMA (15/15). Para mantener al alumno con este tutor, debe incrementar la capacidad a 16 o más.",
    "alumnoInactivoId": 2,
    "alumnoMatricula": "A0002",
    "alumnoNombre": "María López",
    "motivoInactividad": "BAJA_TEMPORAL",
    "tutorPreservadoId": 5,
    "tutorNombre": "Dr. García",
    "tutorCargaActual": 15,
    "tutorCapacidadMax": 15,
    "tutorCuposDisponibles": 0,
    "tutorActivo": true,
    "tutorTieneCapacidad": false
  },
  "message": "Motivo actualizado a BAJA_TEMPORAL (requiere preservar tutor)",
  "timestamp": "2025-11-14T19:31:00"
}
```

### Escenario 3: Motivo que libera cupo
```json
{
  "status": "success",
  "data": {
    "exitoso": true,
    "mensaje": "Motivo actualizado a BAJA_DEFINITIVA - Cupo será liberado en próximo proceso",
    "sugerencia": "El cupo será liberado del tutor Dr. García en la próxima ejecución del proceso de liberación.",
    "alumnoInactivoId": 3,
    "alumnoMatricula": "A0003",
    "alumnoNombre": "Carlos Rodríguez",
    "motivoInactividad": "BAJA_DEFINITIVA",
    "tutorPreservadoId": 5,
    "tutorNombre": "Dr. García",
    "tutorCargaActual": 15,
    "tutorCapacidadMax": 15,
    "tutorCuposDisponibles": 0,
    "tutorActivo": true,
    "tutorTieneCapacidad": false
  },
  "message": "Motivo actualizado a BAJA_DEFINITIVA - Cupo será liberado en próximo proceso",
  "timestamp": "2025-11-14T19:32:00"
}
```

### Escenario 4: Error - Sin tutor preservado
```json
{
  "status": "success",
  "data": {
    "exitoso": false,
    "mensaje": "No hay tutor preservado para este alumno inactivo",
    "sugerencia": "El alumno no tenía tutor asignado cuando se marcó como inactivo",
    "alumnoInactivoId": 4,
    "alumnoMatricula": "A0004",
    "alumnoNombre": "Ana Martínez"
  },
  "message": "No hay tutor preservado para este alumno inactivo",
  "timestamp": "2025-11-14T19:33:00"
}
```

---

## Flujo de Uso Completo

### Paso 1: Cargar lista de alumnos
```bash
# El sistema identifica automáticamente alumnos inactivos (no aparecen en Excel)
POST /api/procesos/iniciar
{
  "semestreId": 1
}
```

### Paso 2: Alumnos se marcan como inactivos con motivo SIN_DEFINIR
```sql
SELECT * FROM alumno_inactivo WHERE motivo_inactividad = 'SIN_DEFINIR';
-- Resultado: N alumnos sin motivo definido
```

### Paso 3: Usuario resuelve el motivo de cada alumno inactivo
```bash
# Para un alumno que se fue temporalmente
PATCH /api/alumnos-inactivos/123/resolver-motivo?motivo=BAJA_TEMPORAL

# Para un alumno que se egresó
PATCH /api/alumnos-inactivos/124/resolver-motivo?motivo=EGRESADO

# Para un alumno que cambió de carrera
PATCH /api/alumnos-inactivos/125/resolver-motivo?motivo=MOVILIDAD
```

### Paso 4: Sistema maneja automáticamente según el motivo
```
Si BAJA_TEMPORAL o MOVILIDAD:
  → tutorPreservado se MANTIENE
  → Si tutor tiene capacidad: alumno puede reintegrarse inmediatamente
  → Si tutor sin capacidad: Sugerir incrementar capacidad del tutor

Si BAJA_DEFINITIVA o EGRESADO:
  → tutorPreservado se LIBERA
  → Cupo se libera en próxima ejecución de liberarCupos()
  → En próximo ciclo, cupo disponible para nuevos alumnos
```

### Paso 5: Próximo ciclo de asignación
```
Nuevo Excel con nuevos alumnos:
  - Alumnos que habían sido inactivos con BAJA_TEMPORAL:
    * Se marcan como ACTIVOS automáticamente
    * Se reasignan a su tutor preservado (si tiene capacidad)

  - Alumnos que habían sido inactivos con BAJA_DEFINITIVA:
    * Sus cupos ya fueron liberados
    * Nuevos alumnos pueden ser asignados a esos tutores
```

---

## Comparación: Antes vs Después

### Problema Antes
```
SEMANA 1:
- Alumno A0001 se marca INACTIVO
- Dr. García sigue mostrando: 15/15 (sin cupo)
- Nuevo alumno A0500 no puede ser asignado a Dr. García
- El cupo de A0001 NUNCA se libera
- BLOQUEO: Sistema no puede asignar nuevos alumnos

SEMANA 2:
- A0001 reaparece en Excel
- Sistema piensa: ¿Cómo puede estar activo si ya era inactivo?
- CONFUSIÓN: Inconsistencias en datos
```

### Solución Después
```
SEMANA 1:
- Alumno A0001 se marca INACTIVO
- Usuario hace: PATCH /api/alumnos-inactivos/123/resolver-motivo?motivo=BAJA_DEFINITIVA
- Sistema detecta: "Cupo será liberado"
- Dr. García: 15/15 → 14/15 (1 cupo liberado)
- Nuevo alumno A0500 se asigna a Dr. García
- A0500: 14/15 → 15/15 CORRECTO

SEMANA 2:
- A0001 reaparece en Excel
- Sistema verifica: ¿Tutor preservado tiene capacidad?
- Si NO: "Tutor sin capacidad, requiere incrementar"
- Si SÍ: "Alumno puede reintegrarse con su tutor"
- CONSISTENCIA: Sistema trabaja correctamente
```

---

## Impacto en el Sistema

| Aspecto | Antes | Después |
|--------|-------|---------|
| Cupos liberados | NO se liberaban | Se liberan correctamente |
| Sincronización de carga | Incorrecta | Precisa |
| Reingresos | Podrían fallar | Funcionan correctamente |
| Capacidad de tutores | Aparente "sobrecarga" | Refleja realidad |
| Motivo de inactividad | Siempre SIN_DEFINIR | Usuario especifica |
| Manejo de capacidad | Sin validación | Con detección inteligente |

---

## Próximos Pasos Recomendados

1. **Sincronizar estado actual**: Ejecutar `TutorSincronizacionService.recalcularCargaTodosLosTutores()` para corregir cargas actuales

2. **Resolver alumnos inactivos pendientes**: Usar el nuevo endpoint para asignar motivos a todos los `SIN_DEFINIR`

3. **Verificar integridad**: Ejecutar proceso de liberación de cupos nuevamente

4. **Monitorear**: Observar que nuevos alumnos se asignen correctamente

---

## Cambios de Archivos

### Archivos Modificados:
1. `backend/src/main/java/com/universidad/tutorias/domain/repository/AlumnoInactivoRepository.java` - Query de findConCupoLiberado()
2. `backend/src/main/java/com/universidad/tutorias/domain/repository/AsignacionRepository.java` - countByTutorId()
3. `backend/src/main/java/com/universidad/tutorias/application/service/InactivacionService.java` - Agregado nuevo método
4. `backend/src/main/java/com/universidad/tutorias/application/service/impl/InactivacionServiceImpl.java` - Implementación
5. `backend/src/main/java/com/universidad/tutorias/infrastructure/controller/AlumnoInactivoController.java` - Nuevo endpoint

### Archivos Creados:
1. `backend/src/main/java/com/universidad/tutorias/application/dto/ResolucionMotivoResultDTO.java` - DTO de respuesta

---

## Testing

Para probar los cambios:

```bash
# 1. Compilar
mvn clean compile

# 2. Ejecutar tests (si existen)
mvn test

# 3. Ejecutar la aplicación
mvn spring-boot:run

# 4. Probar endpoints
curl -X GET "http://localhost:8080/api/alumnos-inactivos/pendientes"
curl -X PATCH "http://localhost:8080/api/alumnos-inactivos/1/resolver-motivo?motivo=BAJA_TEMPORAL"
```

---

## Notas Importantes

- Los cambios son **retrocompatibles** con el código existente
- El sistema sigue usando `SIN_DEFINIR` como defecto inicial
- El nuevo endpoint es **transaccional** y garantiza consistencia
- La auditoría registra todos los cambios de motivo

