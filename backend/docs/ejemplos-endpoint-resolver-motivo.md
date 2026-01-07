# Ejemplos de Uso: Endpoint Resolver Motivo de Inactividad

## Descripción General
El endpoint permite cambiar el motivo de inactividad de un alumno inactivo y detecta automáticamente si el tutor preservado tiene capacidad disponible.

---

## Base URL
```
http://localhost:8080/api/alumnos-inactivos/{alumnoInactivoId}/resolver-motivo
```

---

## Ejemplos por Escenario

### 1. Alumno se va temporalmente (BAJA_TEMPORAL)
Usar cuando el alumno se toma un semestre de descanso pero puede regresar.

**Solicitud cURL**:
```bash
curl -X PATCH "http://localhost:8080/api/alumnos-inactivos/1/resolver-motivo?motivo=BAJA_TEMPORAL" \
  -H "Content-Type: application/json"
```

**Respuesta esperada** (si tutor tiene capacidad):
```json
{
  "status": "success",
  "data": {
    "exitoso": true,
    "mensaje": "Motivo actualizado a BAJA_TEMPORAL - Tutor preservado tiene capacidad",
    "sugerencia": "✓ El tutor Dr. García tiene 3 cupo(s) disponible(s). El alumno puede reintegrarse cuando sea necesario.",
    "alumnoInactivoId": 1,
    "alumnoMatricula": "2020001",
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

---

### 2. Alumno se fue definitivamente (BAJA_DEFINITIVA)
Usar cuando el alumno abandonó permanentemente los estudios.

**Solicitud cURL**:
```bash
curl -X PATCH "http://localhost:8080/api/alumnos-inactivos/2/resolver-motivo?motivo=BAJA_DEFINITIVA" \
  -H "Content-Type: application/json"
```

**Respuesta esperada**:
```json
{
  "status": "success",
  "data": {
    "exitoso": true,
    "mensaje": "Motivo actualizado a BAJA_DEFINITIVA - Cupo será liberado en próximo proceso",
    "sugerencia": "El cupo será liberado del tutor Dr. García en la próxima ejecución del proceso de liberación.",
    "alumnoInactivoId": 2,
    "alumnoMatricula": "2020002",
    "alumnoNombre": "María López",
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
  "timestamp": "2025-11-14T19:31:00"
}
```

**Acción posterior**: El cupo será liberado en la próxima ejecución del proceso `liberarCupos()`.

---

### 3. Alumno se egresó (EGRESADO)
Usar cuando el alumno terminó su carrera exitosamente.

**Solicitud cURL**:
```bash
curl -X PATCH "http://localhost:8080/api/alumnos-inactivos/3/resolver-motivo?motivo=EGRESADO" \
  -H "Content-Type: application/json"
```

**Respuesta esperada**:
```json
{
  "status": "success",
  "data": {
    "exitoso": true,
    "mensaje": "Motivo actualizado a EGRESADO - Cupo será liberado en próximo proceso",
    "sugerencia": "El cupo será liberado del tutor Dra. Martínez en la próxima ejecución del proceso de liberación.",
    "alumnoInactivoId": 3,
    "alumnoMatricula": "2020003",
    "alumnoNombre": "Carlos Rodríguez",
    "motivoInactividad": "EGRESADO",
    "tutorPreservadoId": 6,
    "tutorNombre": "Dra. Martínez",
    "tutorCargaActual": 13,
    "tutorCapacidadMax": 15,
    "tutorCuposDisponibles": 2,
    "tutorActivo": true,
    "tutorTieneCapacidad": true
  },
  "message": "Motivo actualizado a EGRESADO - Cupo será liberado en próximo proceso",
  "timestamp": "2025-11-14T19:32:00"
}
```

---

### 4. Alumno cambió de carrera (MOVILIDAD)
Usar cuando el alumno se trasladó a otra carrera o universidad.

**Solicitud cURL**:
```bash
curl -X PATCH "http://localhost:8080/api/alumnos-inactivos/4/resolver-motivo?motivo=MOVILIDAD" \
  -H "Content-Type: application/json"
```

**Respuesta esperada**:
```json
{
  "status": "success",
  "data": {
    "exitoso": true,
    "mensaje": "Motivo actualizado a MOVILIDAD - Tutor preservado tiene capacidad",
    "sugerencia": "✓ El tutor Dr. García tiene 1 cupo(s) disponible(s). El alumno puede reintegrarse cuando sea necesario.",
    "alumnoInactivoId": 4,
    "alumnoMatricula": "2020004",
    "alumnoNombre": "Ana Martínez",
    "motivoInactividad": "MOVILIDAD",
    "tutorPreservadoId": 5,
    "tutorNombre": "Dr. García",
    "tutorCargaActual": 14,
    "tutorCapacidadMax": 15,
    "tutorCuposDisponibles": 1,
    "tutorActivo": true,
    "tutorTieneCapacidad": true
  },
  "message": "Motivo actualizado a MOVILIDAD - Tutor preservado tiene capacidad",
  "timestamp": "2025-11-14T19:33:00"
}
```

---

## Escenarios Especiales

### Escenario A: Tutor sin capacidad (requiere incremento)
Cuando el motivo requiere preservar tutor pero el tutor está a máxima capacidad.

**Solicitud cURL**:
```bash
curl -X PATCH "http://localhost:8080/api/alumnos-inactivos/5/resolver-motivo?motivo=BAJA_TEMPORAL" \
  -H "Content-Type: application/json"
```

**Respuesta**:
```json
{
  "status": "success",
  "data": {
    "exitoso": true,
    "mensaje": "Motivo actualizado a BAJA_TEMPORAL (requiere preservar tutor)",
    "sugerencia": "⚠️ AVISO: El tutor Dr. García está a CAPACIDAD MÁXIMA (15/15). Para mantener al alumno con este tutor, debe incrementar la capacidad a 16 o más.",
    "alumnoInactivoId": 5,
    "alumnoMatricula": "2020005",
    "alumnoNombre": "Pedro González",
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
  "timestamp": "2025-11-14T19:34:00"
}
```

**Acción recomendada**:
1. Aumentar capacidad del tutor Dr. García de 15 a 16
2. Re-ejecutar el endpoint o esperar al próximo ciclo de asignación

---

### Escenario B: Tutor inactivo
Cuando el tutor preservado ya no está activo.

**Solicitud cURL**:
```bash
curl -X PATCH "http://localhost:8080/api/alumnos-inactivos/6/resolver-motivo?motivo=BAJA_TEMPORAL" \
  -H "Content-Type: application/json"
```

**Respuesta**:
```json
{
  "status": "success",
  "data": {
    "exitoso": true,
    "mensaje": "Motivo actualizado a BAJA_TEMPORAL",
    "sugerencia": "⚠️ AVISO: El tutor Lic. Pérez (ID: 7) NO ESTÁ ACTIVO. El alumno podría necesitar reasignación.",
    "alumnoInactivoId": 6,
    "alumnoMatricula": "2020006",
    "alumnoNombre": "Sofía Ramírez",
    "motivoInactividad": "BAJA_TEMPORAL",
    "tutorPreservadoId": 7,
    "tutorNombre": "Lic. Pérez",
    "tutorCargaActual": 10,
    "tutorCapacidadMax": 15,
    "tutorCuposDisponibles": 5,
    "tutorActivo": false,
    "tutorTieneCapacidad": false
  },
  "message": "Motivo actualizado a BAJA_TEMPORAL",
  "timestamp": "2025-11-14T19:35:00"
}
```

**Acción recomendada**:
1. Activar nuevamente el tutor o
2. Cambiar motivo a BAJA_DEFINITIVA para liberar cupo
3. Reasignar alumno a otro tutor cuando regrese

---

### Escenario C: Sin tutor preservado (Error)
Cuando el alumno no tenía tutor al momento de marcarse inactivo.

**Solicitud cURL**:
```bash
curl -X PATCH "http://localhost:8080/api/alumnos-inactivos/7/resolver-motivo?motivo=BAJA_TEMPORAL" \
  -H "Content-Type: application/json"
```

**Respuesta**:
```json
{
  "status": "success",
  "data": {
    "exitoso": false,
    "mensaje": "No hay tutor preservado para este alumno inactivo",
    "sugerencia": "El alumno no tenía tutor asignado cuando se marcó como inactivo",
    "alumnoInactivoId": 7,
    "alumnoMatricula": "2020007",
    "alumnoNombre": "Roberto Flores",
    "tutorPreservadoId": null,
    "tutorNombre": "N/A"
  },
  "message": "No hay tutor preservado para este alumno inactivo",
  "timestamp": "2025-11-14T19:36:00"
}
```

**Acción recomendada**:
- Este caso es raro, indica un problema de integridad de datos
- Se debe investigar por qué el alumno no tenía tutor

---

## Motivos Disponibles

| Motivo | Preserva Tutor | Libera Cupo | Caso de Uso |
|--------|---|---|---|
| `BAJA_TEMPORAL` | ✓ SÍ | ✗ NO | Estudiante se toma descanso |
| `BAJA_DEFINITIVA` | ✗ NO | ✓ SÍ | Estudiante abandona carrera |
| `EGRESADO` | ✗ NO | ✓ SÍ | Estudiante terminó carrera |
| `MOVILIDAD` | ✓ SÍ | ✗ NO | Estudiante cambió de carrera |
| `SIN_DEFINIR` | ✗ NO | ✓ SÍ | Por defecto (pendiente de resolver) |

---

## Flujo de Resolución Recomendado

### Paso 1: Listar pendientes
```bash
curl -X GET "http://localhost:8080/api/alumnos-inactivos/pendientes" \
  -H "Content-Type: application/json"
```

### Paso 2: Revisar cada alumno
Para cada alumno pendiente, determinar el motivo correcto.

### Paso 3: Resolver motivos
```bash
# Para cada alumno, ejecutar:
curl -X PATCH "http://localhost:8080/api/alumnos-inactivos/{ID}/resolver-motivo?motivo={MOTIVO}"
```

### Paso 4: Ejecutar liberación de cupos
```bash
# En el siguiente ciclo de asignación, los cupos se liberarán automáticamente
POST /api/procesos/iniciar
```

---

## Notas Importantes

1. **Sincronización**: El endpoint sincroniza la carga del tutor antes de validar capacidad
2. **Transaccional**: Todos los cambios son atómicos (se guardan completamente o no se guardan)
3. **Auditoría**: Se registra quién cambió el motivo y cuándo
4. **Inteligente**: Detecta automáticamente las condiciones del tutor y sugiere acciones

---

## Códigos de Error

En caso de error, el código HTTP será diferente:

- **200 OK**: Operación exitosa (incluso si `exitoso: false` en data)
- **404 Not Found**: El alumno inactivo no existe
- **500 Internal Server Error**: Error del servidor

**Ejemplo de error 404**:
```json
{
  "status": "success",
  "data": null,
  "message": "Error al resolver el motivo de inactividad: Alumno inactivo no encontrado: 999",
  "timestamp": "2025-11-14T19:37:00"
}
```

