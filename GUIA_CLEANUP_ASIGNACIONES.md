# Guía de Limpieza de Asignaciones Corruptas

## 📋 Descripción General

Este sistema proporciona un conjunto de herramientas para **detectar y limpiar asignaciones corruptas** en la base de datos del sistema de tutorías. Se creó específicamente para resolver problemas causados por validaciones insuficientes en versiones anteriores.

## 🔍 ¿Qué son Asignaciones Corruptas?

Las asignaciones corruptas se presentan en estos escenarios:

### 1. **Asignaciones Duplicadas**
Un alumno tiene **múltiples asignaciones en el MISMO semestre** (con diferentes tutores):

```
Alumno A - Semestre 2024-F1:
  ├─ Asignación 1: Tutor X (fecha: 10:00)
  └─ Asignación 2: Tutor Y (fecha: 10:05) ← DUPLICADA
```

**Causa:** Validaciones insuficientes durante carga de listas o reasignaciones.

### 2. **Asignaciones Sin Referencias Válidas**
Una asignación apunta a tutor/alumno/semestre que no existe o es NULL.

```
Asignación ID 123:
  ├─ id_alumno: NULL (¡Error!)
  ├─ id_tutor: 456 (tutor eliminado)
  └─ id_semestre: NULL (¡Error!)
```

### 3. **Desincronización Legacy**
Conflicto entre el campo antiguo `semestre_academico` y el nuevo `id_semestre`:

```
Asignación:
  ├─ semestre_academico: "2025-2026-F1"
  └─ id_semestre: 999 → semestre con código "2024-2025-F2" ¡MISMATCH!
```

## 🛠️ Endpoints Disponibles

### 1. **Detectar Corrupción en Semestre Específico**

```bash
GET /api/cleanup/detectar/{semestreId}
```

**Parámetros:**
- `semestreId`: ID del semestre a analizar

**Ejemplo:**
```bash
curl -X GET "http://localhost:8080/api/cleanup/detectar/5" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Respuesta:**
```json
{
  "asignaciones_duplicadas": 3,
  "asignaciones_sin_tutor": 0,
  "asignaciones_sin_alumno": 1,
  "asignaciones_sin_semestre": 0,
  "total_corrupcion": 4,
  "estado": "CORRUPTO",
  "detalle_duplicadas": [
    {
      "asignacion_id": 101,
      "alumno_matricula": "A123456",
      "alumno_nombre": "Juan Pérez",
      "tutor_nombre": "Dr. López",
      "fecha_asignacion": "2024-01-15T10:05:00"
    }
  ]
}
```

### 2. **Detectar Corrupción Global**

```bash
GET /api/cleanup/detectar-global
```

**Ejemplo:**
```bash
curl -X GET "http://localhost:8080/api/cleanup/detectar-global" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Respuesta:**
```json
{
  "total_duplicadas": 15,
  "total_sin_tutor": 2,
  "total_sin_alumno": 3,
  "total_sin_semestre": 1,
  "total_corrupcion": 21,
  "detalles_por_semestre": {
    "5": {
      "codigo_semestre": "2024-2025-F1",
      "duplicadas": 8
    },
    "6": {
      "codigo_semestre": "2025-2026-F1",
      "duplicadas": 7
    }
  }
}
```

---

## 🧹 Limpieza de Asignaciones

### 3. **Limpiar Semestre Específico**

```bash
POST /api/cleanup/limpiar/{semestreId}?confirmar=CONFIRMAR_LIMPIEZA
```

⚠️ **ACCIÓN DESTRUCTIVA** - Elimina registros de la base de datos.

**Parámetros:**
- `semestreId`: ID del semestre a limpiar
- `confirmar`: Debe ser exactamente `CONFIRMAR_LIMPIEZA` (medida de seguridad)

**Proceso de Limpieza:**
1. Identifica asignaciones duplicadas (mantiene la más reciente)
2. Elimina duplicadas antigas
3. Elimina asignaciones sin tutor válido
4. Elimina asignaciones sin alumno válido
5. Registra cada eliminación en auditoría
6. **NO** elimina asignaciones sin semestre (requieren limpieza global)

**Ejemplo:**
```bash
# Primero: DETECTAR (sin riesgo)
curl -X GET "http://localhost:8080/api/cleanup/detectar/5" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Si hay corrupción, proceder a LIMPIAR
curl -X POST "http://localhost:8080/api/cleanup/limpiar/5?confirmar=CONFIRMAR_LIMPIEZA" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Respuesta:**
```json
{
  "estado": "EXITOSO",
  "asignaciones_eliminadas": 4,
  "fecha_limpieza": "2024-01-20T14:30:45.123456",
  "semestre_id": 5
}
```

### 4. **Limpiar GLOBALMENTE (TODO el Sistema)**

```bash
POST /api/cleanup/limpiar-global?confirmar=CONFIRMAR_LIMPIEZA_GLOBAL
```

⚠️ **ACCIÓN DESTRUCTIVA GLOBAL** - Afecta TODA la base de datos.

**Parámetros:**
- `confirmar`: Debe ser exactamente `CONFIRMAR_LIMPIEZA_GLOBAL` (doble confirmación)

**Proceso:**
1. Recorre TODOS los semestres
2. Ejecuta limpieza en cada uno
3. Elimina asignaciones sin semestre (nivel global)
4. Registra auditoría de todas las eliminaciones

**Ejemplo:**
```bash
# Siempre detectar primero
curl -X GET "http://localhost:8080/api/cleanup/detectar-global" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Ejecutar limpieza global
curl -X POST "http://localhost:8080/api/cleanup/limpiar-global?confirmar=CONFIRMAR_LIMPIEZA_GLOBAL" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Respuesta:**
```json
{
  "estado": "EXITOSO",
  "total_asignaciones_eliminadas": 21,
  "eliminadas_por_semestre": {
    "5": 8,
    "6": 7,
    "7": 6
  },
  "fecha_limpieza_global": "2024-01-20T14:35:22.987654"
}
```

---

## 🔄 Recalcular Cargas de Tutores

Después de limpiar asignaciones, **SIEMPRE** recalcular las cargas de tutores:

```bash
POST /api/cleanup/recalcular?semestreId={semestreId}
```

**Parámetros:**
- `semestreId`: (opcional) ID del semestre. Si no se proporciona, recalcula TODOS.

**Ejemplos:**

```bash
# Recalcular un semestre específico
curl -X POST "http://localhost:8080/api/cleanup/recalcular?semestreId=5" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Recalcular TODOS los tutores
curl -X POST "http://localhost:8080/api/cleanup/recalcular" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Respuesta:**
```json
{
  "estado": "EXITOSO",
  "tutores_actualizados": 12,
  "fecha_recalculo": "2024-01-20T14:40:15.654321"
}
```

---

## 📊 Flujo Completo de Rollback

Sigue estos pasos para limpiar un semestre problemático:

### Paso 1: Análisis Previo
```bash
# Detectar qué hay corrupto
curl -X GET "http://localhost:8080/api/cleanup/detectar/5"
```

### Paso 2: Decisión
- **Si `total_corrupcion == 0`**: El semestre está limpio ✅
- **Si `total_corrupcion > 0`**: Proceder a limpieza

### Paso 3: Backup (Recomendado)
```bash
# Hacer backup manual de la BD antes de limpiar
# (comando específico de tu BD, ej: mysqldump)
mysqldump -u usuario -p base_datos > backup_antes_limpieza_2024-01-20.sql
```

### Paso 4: Limpiar Semestre
```bash
curl -X POST "http://localhost:8080/api/cleanup/limpiar/5?confirmar=CONFIRMAR_LIMPIEZA"
```

### Paso 5: Recalcular Cargas
```bash
curl -X POST "http://localhost:8080/api/cleanup/recalcular?semestreId=5"
```

### Paso 6: Verificar
```bash
# Verificar que la limpieza funcionó
curl -X GET "http://localhost:8080/api/cleanup/detectar/5"

# Esperado: total_corrupcion == 0 y estado == "LIMPIO"
```

---

## 📈 Auditoría y Logs

Todas las operaciones de limpieza se registran en:

### Tabla `logs_auditoria`

```sql
SELECT * FROM logs_auditoria
WHERE tipo_accion = 'ELIMINACION_ASIGNACION'
ORDER BY fecha_registro DESC;
```

**Campos relevantes:**
- `tipo_accion`: `ELIMINACION_ASIGNACION`
- `entidad`: `ASIGNACION`
- `id_entidad`: ID de la asignación eliminada
- `descripcion`: Razón de eliminación
- `usuario`: `CLEANUP_SYSTEM`
- `fecha_registro`: Cuándo se ejecutó

---

## ⚠️ Notas Importantes

### 1. **Permisos Requeridos**
- Rol: `ADMIN`
- Estos endpoints están protegidos por `@PreAuthorize("hasRole('ADMIN')")`

### 2. **Confirmación Requerida**
- No se puede ejecutar limpieza sin incluir el parámetro `confirmar`
- Esto previene eliminaciones accidentales

### 3. **Transacciones Atómicas**
- Cada operación de limpieza es una transacción completa
- Si falla, **todo se revierte** (rollback automático)

### 4. **Auditoría Permanente**
- Los logs de auditoría NO se eliminan
- Siempre hay registro de qué se limpió y cuándo

### 5. **Resincronización Automática**
- Los tutores se recalculan automáticamente
- No queda inconsistencia de cargas

---

## 🆘 Solución de Problemas

### Problema: "Error: No confirmar=CONFIRMAR_LIMPIEZA"

**Solución:** Agregue exactamente el parámetro requerido:
```bash
# ❌ Incorrecto
POST /api/cleanup/limpiar/5?confirmar=true

# ✅ Correcto
POST /api/cleanup/limpiar/5?confirmar=CONFIRMAR_LIMPIEZA
```

### Problema: "Acceso denegado"

**Solución:** El usuario debe tener rol ADMIN:
```bash
# Verificar roles del usuario
curl -X GET "http://localhost:8080/api/usuarios/mi-perfil" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Debe incluir: "rol": "ADMIN"
```

### Problema: "Error durante limpieza"

**Solución:** Revisar logs:
```bash
# Buscar en logs de la aplicación
tail -f logs/aplicacion.log | grep "CLEANUP\|ERROR"
```

---

## 📊 Ejemplo Completo: Recuperación de un Semestre Corrupto

```bash
# 1. Detectar problema
curl -X GET "http://localhost:8080/api/cleanup/detectar/5" \
  -H "Authorization: Bearer admin_token"

# Salida:
# {
#   "total_corrupcion": 12,
#   "estado": "CORRUPTO"
# }

# 2. Hacer backup
mysqldump -u root -p tutoria_db > /backups/backup_2024-01-20_previa_limpieza.sql

# 3. Limpiar
curl -X POST "http://localhost:8080/api/cleanup/limpiar/5?confirmar=CONFIRMAR_LIMPIEZA" \
  -H "Authorization: Bearer admin_token"

# 4. Recalcular cargas
curl -X POST "http://localhost:8080/api/cleanup/recalcular?semestreId=5" \
  -H "Authorization: Bearer admin_token"

# 5. Verificar
curl -X GET "http://localhost:8080/api/cleanup/detectar/5" \
  -H "Authorization: Bearer admin_token"

# Salida esperada:
# {
#   "total_corrupcion": 0,
#   "estado": "LIMPIO"
# }
```

---

## 📞 Soporte y Reporte de Problemas

Si la limpieza no funciona como se espera:

1. Recolectar logs: `logs/aplicacion.log`
2. Ejecutar diagnóstico: `GET /api/cleanup/detectar-global`
3. Documentar: ID de semestres problemáticos, cantidad de corrupción
4. Reportar con detalles completos

---

## 🔗 Servicios Relacionados

- `CleanupAsignacionService`: Lógica de detección y limpieza
- `CleanupAsignacionController`: Endpoints REST
- `TutorSincronizacionService`: Recalcular cargas
- `AuditoriaService`: Registrar cambios

---

**Última actualización:** 2024-01-20
**Versión:** 1.0
