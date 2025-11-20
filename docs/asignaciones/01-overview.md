# 📋 Overview - Módulo de Asignaciones de Tutores

## ¿Qué es este módulo?

El módulo de **Asignaciones de Tutores** es el corazón del sistema de tutoría. Es responsable de:

1. **Validar** archivos Excel con datos de estudiantes
2. **Limpiar y normalizar** los datos (trim, uppercase, validación)
3. **Ordenar** estudiantes por semestre para optimizar asignaciones
4. **Asignar** cada estudiante a un tutor disponible
5. **Registrar** todas las operaciones en auditoría
6. **Reportar** errores sin parar el procesamiento

---

## 🎯 Objetivos Principales

### 1. Separación Clara de Responsabilidades
```
Validación          Asignación         HTTP
   │                   │                │
   └──> Limpiar     ┌──┴──┐           │
       Ordenar      │     │          │
                    ▼     ▼          ▼
            ExcelService  AsignacionService  Controller
```

### 2. Escalabilidad
- Maneja N semestres (1-12+) sin lógica hardcodeada
- Procesa miles de estudiantes eficientemente
- Distribución automática de cargas de tutores

### 3. Robustez (Best-Effort)
- Un error no detiene el proceso
- Todos los errores se reportan
- Auditoría completa de operaciones

### 4. Confiabilidad
- Transacciones ACID
- Locking pesimista para evitar race conditions
- Sincronización de cargas antes de asignar

---

## 📊 Datos que Maneja

### Entrada: Archivo Excel
```
| Matrícula  | Nombre          | Carrera | Semestre |
|------------|-----------------|---------|----------|
| A00123456  | Juan Pérez      | ICA     | 5        |
| A00123457  | María González  | ISC     | 8        |
| ...        | ...             | ...     | ...      |
```

### Salida: Asignaciones en BD
```
Alumno       Tutor            Semestre    Tipo              Fecha
────────────────────────────────────────────────────────────
A00123456    Dr. López        2025-2025-FN NUEVO_INGRESO   2025-11-19
A00123457    Dra. Martínez    2025-2025-FN REINGRESO       2025-11-19
```

---

## 🔄 Flujo de Alto Nivel

```
┌─────────────────────┐
│ Selecciona Excel    │
└──────────┬──────────┘
           │
           ▼
┌──────────────────────────────────┐
│ POST /validar-excel              │
│ (ExcelValidacionYOrdenaService)  │
│                                  │
│ 1. Leer Excel                    │
│ 2. Validar datos (SIN PARAR)     │
│ 3. Si hay errores → ERROR        │
│ 4. Si OK → Limpiar + Ordenar     │
│ 5. Retornar alumnos validados    │
└──────────┬───────────────────────┘
           │
           ├─ ERROR → Mostrar errores, FIN
           │
           └─ OK → Extrae data[]
                   │
                   ▼
         ┌──────────────────────────────────┐
         │ POST /ejecutar                   │
         │ (EjecucionAsignacionService)     │
         │                                  │
         │ 1. Validar precondiciones        │
         │ 2. Crear ProcesoAsignacion       │
         │ 3. Asignar cada alumno:          │
         │    • Sincronizar cargas          │
         │    • Seleccionar tutor           │
         │    • Crear asignación            │
         │    • Actualizar cargas           │
         │    • Registrar en auditoría      │
         │ 4. Reportar resultado            │
         └──────────┬───────────────────────┘
                    │
                    ▼
         ┌──────────────────────────────────┐
         │ Resultado:                       │
         │ - status: OK/PARTIAL/ERROR       │
         │ - estadísticas                   │
         │ - errores detallados             │
         └──────────────────────────────────┘
```

---

## 🏗️ Componentes Principales

### Servicios

| Servicio | Responsabilidad | Ubicación |
|----------|-----------------|-----------|
| **ExcelValidacionYOrdenaService** | Validar, limpiar, ordenar Excel | `application/service` |
| **AsignacionService** | Lógica pura de asignación | `application/service` |
| **EjecucionAsignacionService** | Orquestar /ejecutar endpoint | `application/service` |
| **TutorSincronizacionService** | Sincronizar cargas de tutores | `application/service` |
| **AuditoriaService** | Registrar en auditoría | `application/service` |

### DTOs

| DTO | Propósito | Ubicación |
|-----|-----------|-----------|
| **AlumnoExcelDTO** | Alumno bruto desde Excel | `application/dto` |
| **AlumnoValidadoDTO** | Alumno limpio, validado, ordenado | `application/dto` |
| **EjecutarAsignacionRequest** | Request del endpoint /ejecutar | `application/dto` |
| **EjecucionAsignacionResponse** | Response del endpoint /ejecutar | `application/dto` |
| **ExcelValidacionResponse** | Response del endpoint /validar-excel | `application/dto` |
| **AsignacionErrorDTO** | Error detallado de asignación | `application/dto` |

### Entidades

| Entidad | Propósito | Ubicación |
|---------|-----------|-----------|
| **Asignacion** | Registro de asignación (NUEVO_INGRESO, REINGRESO) | `domain/entity` |
| **Alumno** | Estudiante | `domain/entity` |
| **Tutor** | Profesor/Tutor | `domain/entity` |
| **Semestre** | Período académico | `domain/entity` |
| **LogAuditoria** | Registro de auditoría | `domain/entity` |
| **TutorCambioAuditoria** | Histórico de cambios de tutor | `domain/entity` |

---

## 💡 Conceptos Clave

### Tipos de Asignación

```java
enum TipoAsignacion {
    NUEVO_INGRESO,   // Primer assignment del alumno
    REINGRESO        // Alumno retorna tras inactividad
}
```

- **NUEVO_INGRESO**: Incrementa carga del tutor
- **REINGRESO**: Mantiene tutor anterior, NO incrementa carga

### Ordenamiento por Semestre

```
Semestres ALTOS primero (8°, 7°, 6°)
        ↓ (libera cupos)
Semestres BAJOS después (3°, 2°, 1°)
        ↓ (más cupos disponibles)
NUEVO_INGRESO al final (máximo éxito)
```

**Razón:** Liberar cupos en semestres avanzados primero, para que nuevo ingreso tenga más opciones.

### Status de Respuesta

```
"OK"      = Todos asignados exitosamente
"PARTIAL" = Algunos asignados, otros con error
"ERROR"   = Ninguno asignado o error fatal
```

---

## 🔐 Características de Seguridad

✅ **Validación de precondiciones** - Rechaza datos inválidos
✅ **Best-effort error handling** - Un error no para todo
✅ **Transacciones ACID** - Consistencia garantizada
✅ **Locking pesimista** - Evita race conditions
✅ **Auditoría completa** - Trazabilidad de cambios
✅ **Sincronización** - Cargas correctas siempre

---

## 📈 Rendimiento

- **Tiempo de validación:** O(n) donde n = número de filas
- **Tiempo de asignación:** O(n) con procesamiento por batch
- **Memoria:** O(1) por alumno (batch size = 100)
- **Capacidad:** Miles de alumnos por ejecución

---

## 🚀 Próximos Pasos

1. **Lee [02-arquitectura.md](./02-arquitectura.md)** para entender la estructura
2. **Lee [05-flujo-completo.md](./05-flujo-completo.md)** para ver el flujo end-to-end
3. **Consulta [14-ejemplos-curl.md](./14-ejemplos-curl.md)** para usar los endpoints

---

**Versión:** 4.0.0
**Última actualización:** 19 de Noviembre, 2025
