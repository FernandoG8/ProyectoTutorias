# Proceso de Mantenimiento Seguro - Sincronización de Cupos

## Descripción General

Este documento describe el proceso **seguro y controlado** para sincronizar la carga de tutores, resolver motivos de inactividad y liberar cupos correctamente.

---

## Endpoints de Mantenimiento

Todos los endpoints están en:
```
/api/mantenimiento
```

---

## PASO 0: Validar Integridad de Datos

**Antes de hacer cualquier cambio**, verifica que el sistema esté en estado íntegro.

### Endpoint
```
GET /api/mantenimiento/validar-integridad
```

### Ejemplo cURL
```bash
curl -X GET "http://localhost:8080/api/mantenimiento/validar-integridad" \
  -H "Content-Type: application/json"
```

### Respuesta Esperada (Sistema Íntegro)
```json
{
  "status": "success",
  "data": {
    "esIntegro": true,
    "totalAnomalias": 0,
    "anomalias": [],
    "mensaje": "Sistema íntegro"
  }
}
```

### Respuesta si hay Anomalías
```json
{
  "status": "success",
  "data": {
    "esIntegro": false,
    "totalAnomalias": 3,
    "anomalias": [
      {
        "tipo": "ASIGNACION_FANTASMA",
        "descripcion": "Existen asignaciones de alumnos inactivos",
        "cantidad": 2,
        "severidad": "CRÍTICO"
      },
      {
        "tipo": "TUTOR_SOBRECARGADO",
        "descripcion": "Tutores con carga superior a capacidad máxima",
        "cantidad": 1,
        "severidad": "CRÍTICO"
      }
    ]
  }
}
```

### Acciones si hay Anomalías Críticas
- **Contactar administrador** si hay anomalías CRÍTICAS
- **NO proceder** hasta resolver anomalías críticas
- Las anomalías ADVERTENCIA se pueden ignorar

---

## PASO 1: Diagnosticar el Sistema

Obtén un diagnóstico completo del estado actual.

### Endpoint
```
GET /api/mantenimiento/diagnostico
```

### Ejemplo cURL
```bash
curl -X GET "http://localhost:8080/api/mantenimiento/diagnostico" \
  -H "Content-Type: application/json"
```

### Respuesta Esperada
```json
{
  "status": "success",
  "data": {
    "totalTutores": 35,
    "totalAlumnos": 500,
    "alumnosActivos": 490,
    "alumnosInactivos": 10,
    "inactivosSinMotivo": 8,
    "asignacionesTotales": 495,
    "tutoresConDiscrepancia": 2,
    "anomalias": [
      "TUTOR Dr. García (ID: 5) tiene discrepancia: carga reportada=15, real=13"
    ],
    "estadoGeneral": "ADVERTENCIA"
  },
  "message": "Diagnóstico completado. Anomalías: 1"
}
```

### Información Importante
- **alumnosInactivos**: Alumnos que no aparecen en el Excel
- **inactivosSinMotivo**: Alumnos inactivos que aún necesitan resolver su motivo
- **tutoresConDiscrepancia**: Tutores cuya carga no coincide con asignaciones reales
- **estadoGeneral**: SALUDABLE | ADVERTENCIA | CRÍTICO

---

## PASO 2: Sincronizar Carga de Tutores

Recalcula la carga real de **TODOS** los tutores basada en asignaciones activas.

### Endpoint
```
POST /api/mantenimiento/sincronizar-tutores
```

### Ejemplo cURL
```bash
curl -X POST "http://localhost:8080/api/mantenimiento/sincronizar-tutores" \
  -H "Content-Type: application/json"
```

### Respuesta Esperada
```json
{
  "status": "success",
  "data": {
    "tutoresProcesados": 35,
    "tutoresCorregidos": 2,
    "cambiosDetallados": [
      {
        "tutorId": 5,
        "tutorNombre": "Dr. García",
        "cargaAnterior": 15,
        "cargaNueva": 13,
        "diferencia": -2,
        "esCorreccion": true
      },
      {
        "tutorId": 8,
        "tutorNombre": "Dra. Martínez",
        "cargaAnterior": 12,
        "cargaNueva": 12,
        "diferencia": 0,
        "esCorreccion": false
      }
    ]
  },
  "message": "Sincronización completada. Tutores corregidos: 2"
}
```

### ¿Por qué se necesita sincronizar?
- Alumnos pueden haber sido eliminados sin actualizar la carga del tutor
- Asignaciones antiguas pueden no haberse contado correctamente
- Inactivaciones pueden no haber liberado cupos adecuadamente

### Resultado
Después de este paso, la carga de todos los tutores será **precisa y correcta**.

---

## PASO 3: Listar Alumnos Inactivos Pendientes

Obtén la lista de alumnos que necesitan que se defina su motivo de inactividad.

### Endpoint
```
GET /api/mantenimiento/pendientes-resolucion
```

### Ejemplo cURL
```bash
curl -X GET "http://localhost:8080/api/mantenimiento/pendientes-resolucion" \
  -H "Content-Type: application/json"
```

### Respuesta Esperada
```json
{
  "status": "success",
  "data": [
    {
      "alumnoInactivoId": 1,
      "alumnoId": 101,
      "matricula": "2020001",
      "nombre": "Juan Pérez",
      "carrera": "Ingeniería en Sistemas",
      "tutorId": 5,
      "tutorNombre": "Dr. García",
      "tutorActivo": true,
      "tutorCuposDisponibles": 2
    },
    {
      "alumnoInactivoId": 2,
      "alumnoId": 102,
      "matricula": "2020002",
      "nombre": "María López",
      "carrera": "Ingeniería en Sistemas",
      "tutorId": 5,
      "tutorNombre": "Dr. García",
      "tutorActivo": true,
      "tutorCuposDisponibles": 2
    },
    ...más alumnos
  ],
  "message": "Encontrados 8 alumnos pendientes"
}
```

### Información Importante
- **tutorCuposDisponibles**: Cupos libres que tiene el tutor
  - Si > 0: Pueden regresar sin problema
  - Si = 0: Requieren incrementar capacidad del tutor
  - Si tutor está inactivo: Necesitarán reasignación

---

## PASO 4: Resolver Masivamente Motivos de Inactividad

Define el motivo para **múltiples** alumnos inactivos simultáneamente.

### Endpoint
```
POST /api/mantenimiento/resolver-masivamente
```

### Body Request
```json
{
  "1": "BAJA_TEMPORAL",
  "2": "BAJA_DEFINITIVA",
  "3": "EGRESADO",
  "4": "MOVILIDAD",
  "5": "BAJA_TEMPORAL",
  "6": "BAJA_DEFINITIVA",
  "7": "EGRESADO",
  "8": "MOVILIDAD"
}
```

### Ejemplo cURL
```bash
curl -X POST "http://localhost:8080/api/mantenimiento/resolver-masivamente" \
  -H "Content-Type: application/json" \
  -d '{
    "1": "BAJA_TEMPORAL",
    "2": "BAJA_DEFINITIVA",
    "3": "EGRESADO",
    "4": "MOVILIDAD"
  }'
```

### Respuesta Esperada
```json
{
  "status": "success",
  "data": {
    "totalProcesados": 4,
    "exitosos": 4,
    "fallidos": 0,
    "detalleExitosos": [
      {
        "alumnoInactivoId": 1,
        "motivo": "BAJA_TEMPORAL",
        "exitoso": true
      },
      ...
    ],
    "detalleFallidos": []
  },
  "message": "Resueltos: 4, Fallidos: 0"
}
```

### Motivos Disponibles

| Motivo | Preserva Tutor | Libera Cupo | Cuándo Usar |
|--------|---|---|---|
| `BAJA_TEMPORAL` | ✓ SÍ | ✗ NO | Estudiante se toma descanso, puede regresar |
| `BAJA_DEFINITIVA` | ✗ NO | ✓ SÍ | Estudiante abandona definitivamente |
| `EGRESADO` | ✗ NO | ✓ SÍ | Estudiante terminó la carrera |
| `MOVILIDAD` | ✓ SÍ | ✗ NO | Cambio de carrera o universidad |

### ¿Cómo decidir el motivo?
Basarse en la información disponible:
- Si se fue temporalmente → `BAJA_TEMPORAL`
- Si completó sus estudios → `EGRESADO`
- Si cambió de carrera/universidad → `MOVILIDAD`
- Si simplemente no volvió sin razón aparente → `BAJA_DEFINITIVA`

---

## PASO 5: Liberar Cupos de Forma Segura

Ejecuta la liberación de cupos con **validaciones previas**.

### Endpoint
```
POST /api/mantenimiento/liberar-cupos-seguro
```

### Parámetros Opcionales
- `procesoId` (query, opcional): ID del proceso de asignación actual

### Ejemplo cURL
```bash
curl -X POST "http://localhost:8080/api/mantenimiento/liberar-cupos-seguro" \
  -H "Content-Type: application/json"

# O con procesoId
curl -X POST "http://localhost:8080/api/mantenimiento/liberar-cupos-seguro?procesoId=1" \
  -H "Content-Type: application/json"
```

### Respuesta Esperada
```json
{
  "status": "success",
  "data": {
    "exitoso": true,
    "cuposLiberados": 2,
    "tutoresSincronizados": 1,
    "advertencias": [
      "Se sincronizaron 1 tutores antes de liberar cupos"
    ],
    "errores": [],
    "detalles": [
      {
        "tutorId": 5,
        "tutorNombre": "Dr. García",
        "alumnosLiberados": 2,
        "cargaAnterior": 15,
        "cargaNueva": 13
      }
    ]
  },
  "message": "Liberación completada. Cupos: 2"
}
```

### Qué Ocurre Automáticamente
1. **Valida integridad** de datos
2. **Sincroniza tutores** si es necesario
3. **Libera cupos** de alumnos marcados con motivos que liberan cupos
4. **Verifica resultados** finales
5. **Genera reporte** detallado

---

## Flujo Completo Recomendado

### Día 1: Preparación
```
1. Validar integridad
   GET /api/mantenimiento/validar-integridad

   → Si hay CRÍTICOS: Resolver primero, no proceder
   → Si todo OK: Continuar
```

### Día 2: Diagnóstico
```
2. Hacer diagnóstico
   GET /api/mantenimiento/diagnostico

   → Anota: alumnos inactivos, discrepancias, estado
```

### Día 3: Sincronización
```
3. Sincronizar tutores
   POST /api/mantenimiento/sincronizar-tutores

   → Verifica cambios realizados
   → Confirma que cargas son correctas
```

### Día 4: Resolución de Motivos
```
4. Listar pendientes
   GET /api/mantenimiento/pendientes-resolucion

   → Revisar cada alumno
   → Determinar motivo apropiado
```

```
5. Resolver masivamente
   POST /api/mantenimiento/resolver-masivamente

   → Procesar todos los motivos
   → Verificar resultados
```

### Día 5: Liberación de Cupos
```
6. Liberar cupos
   POST /api/mantenimiento/liberar-cupos-seguro?procesoId=TU_PROCESO_ID

   → Ejecuta con todas las validaciones
   → Genera reporte detallado
```

### Día 6: Siguiente Ciclo
```
7. Iniciar nuevo proceso de asignación
   POST /api/procesos/iniciar

   → Sistema usa cupos liberados
   → Distribuye alumnos correctamente
```

---

## Escenarios de Manejo de Errores

### Escenario 1: Anomalías Críticas Detectadas
```
GET /api/mantenimiento/validar-integridad

Respuesta: esIntegro = false, anomalias CRÍTICAS
```

**Acción**:
1. NO ejecutar ningún endpoint de cambio
2. Contactar al administrador del sistema
3. Revisar logs para encontrar causa
4. Resolver anomalías manualmente si es necesario

### Escenario 2: Tutores sin Capacidad
```
GET /api/mantenimiento/pendientes-resolucion

Respuesta: tutorCuposDisponibles = 0
```

**Acción**:
1. Incrementar capacidad del tutor:
   ```
   PATCH /api/tutores/{tutorId}
   { "capacidadMax": NUEVO_VALOR }
   ```
2. Re-ejecutar `sincronizar-tutores`
3. Continuar con resolución de motivos

### Escenario 3: Alumno sin Tutor Preservado
```
GET /api/mantenimiento/pendientes-resolucion

Respuesta: tutorNombre = "N/A", tutorActivo = false
```

**Acción**:
1. Resolver con cualquier motivo que NO preserve tutor
2. En próximo ciclo: Será asignado a nuevo tutor automáticamente
3. Si quiere preservar: Asignar tutor manualmente primero

### Escenario 4: Resolución Fallida
```
POST /api/mantenimiento/resolver-masivamente

Respuesta: fallidos > 0
```

**Acción**:
1. Revisar `detalleFallidos` para ver razones
2. Resolver problemas indicados
3. Re-intentar con solo los fallidos
4. Si persiste: Investigar logs

---

## Validaciones Automáticas

El sistema realiza validaciones en cada paso:

### Validación de Integridad
- ✓ No hay asignaciones de alumnos inactivos
- ✓ Todos los alumnos activos tienen tutor
- ✓ Ningún tutor está sobrecargado
- ✓ Todos los inactivos tienen tutor preservado (o es aceptable)

### Validación de Sincronización
- ✓ Carga de cada tutor coincide con asignaciones reales
- ✓ No hay discrepancias numéricas
- ✓ Capacidades máximas se respetan

### Validación de Liberación
- ✓ Integridad previa es correcta
- ✓ Tutores se sincronizan antes de liberar
- ✓ Cupos se liberan correctamente
- ✓ Estado final es válido

---

## Monitoreo durante el Proceso

### Después de Sincronizar
```
Verificar que:
- tutoresCorregidos < 5 (muy altos números indican problema)
- diferencia en carga es pequeña (no más de ±5 por tutor)
- cambios son lógicos (no saltos raros)
```

### Después de Resolver Motivos
```
Verificar que:
- exitosos ≥ 95% (si hay muchos fallidos, investigar)
- todos los motivos son válidos
- no hay duplicados
```

### Después de Liberar Cupos
```
Verificar que:
- cuposLiberados > 0 (si es 0, verificar que hay inactivos)
- tutoresSincronizados es razonable
- advertencias son informativas, no errores
- errores está vacío
```

---

## Recuperación de Errores

Si algo sale mal:

### Opción 1: Rollback (Si es posible)
```bash
# Si el cambio fue reciente, pueden revertirse cambios
# Contactar administrador para hacer rollback en BD
```

### Opción 2: Corrección Manual
```bash
# Resolver alumno específico manualmente:
PATCH /api/alumnos-inactivos/{id}/resolver-motivo?motivo=MOTIVO_CORRECTO
```

### Opción 3: Reintentar
```bash
# Si fue error temporal, reintentar el endpoint
# Idempotencia: Sistema es seguro para reintentar
```

---

## Checklist de Ejecución Segura

- [ ] Hizo validación de integridad (sin CRÍTICOS)
- [ ] Hizo diagnóstico y anotó resultados
- [ ] Sincronizó tutores
- [ ] Revisó y determinó motivo de cada inactivo
- [ ] Resolvió masivamente con motivos correctos
- [ ] Verificó resultados de resolución
- [ ] Ejecutó liberación de cupos
- [ ] Verificó que cupos se liberaron
- [ ] Está listo para próximo ciclo de asignación

---

##  Preguntas Frecuentes

### P: ¿Qué pasa si no resuelvo los motivos?
R: Los alumnos permanecerán inactivos con motivo SIN_DEFINIR. Su cupo NO se liberará hasta definir el motivo.

### P: ¿Puedo cambiar el motivo posteriormente?
R: Sí. Usa el endpoint individual:
```bash
PATCH /api/alumnos-inactivos/{id}/resolver-motivo?motivo=NUEVO_MOTIVO
```

### P: ¿Qué pasa si hay error durante la liberación?
R: El sistema es **transaccional**. Si hay error, NADA cambia. Es seguro reintentar.

### P: ¿Cuánto tiempo tarda cada paso?
R: Depende de cantidad de tutores/alumnos:
- Validar integridad: < 1 segundo
- Diagnóstico: < 1 segundo
- Sincronizar tutores: 1-5 segundos
- Listar pendientes: < 1 segundo
- Resolver masivamente: 1-10 segundos
- Liberar cupos: 1-5 segundos

### P: ¿Puedo ejecutar en producción?
R: **Sí, pero con cuidado**. Recomendado:
1. Probar en ambiente de desarrollo primero
2. Ejecutar en horario de bajo uso
3. Hacer backup antes
4. Monitorear logs durante ejecución
5. Estar disponible en caso de error

---

## Conclusión

Este proceso es **seguro, controlado y validado** en cada paso. Sigue las recomendaciones y tendrás un sistema de asignación de tutores completamente sincronizado y funcional.

