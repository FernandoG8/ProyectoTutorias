# 🔄 Flujo Completo End-to-End

## Visión General del Proceso

```
USUARIO
   │
   ├─ Selecciona archivo Excel
   │
   ▼
╔═════════════════════════════════════════════════════════════════╗
║ ENDPOINT 1: POST /api/asignaciones/validar-excel               ║
║                                                                 ║
║ Responsabilidades:                                              ║
║ • Leer Excel                                                    ║
║ • Validar CADA fila (reportar TODOS los errores)              ║
║ • Limpiar y normalizar datos                                   ║
║ • Ordenar por semestre (mayores primero)                      ║
║ • Convertir a AlumnoValidadoDTO                               ║
║                                                                 ║
║ Input:  {archivo: File, semestreId: 5}                        ║
║ Output: ExcelValidacionResponse                               ║
╚═════════════════════════════════════════════════════════════════╝
   │
   ├─────────────────────────────────────────────────────────────┐
   │ SI ERRORES                                                  │
   │                                                             │
   ▼                                                             │
┌──────────────────────────┐                                      │
│ ExcelValidacionResponse  │                                      │
│ {                        │                                      │
│   status: "ERROR",       │                                      │
│   message: "5 errores",  │                                      │
│   data: null,            │                                      │
│   errors: [              │                                      │
│     {                    │                                      │
│       filaExcel: 5,      │                                      │
│       campo: "carrera",  │                                      │
│       descripcion: "..." │                                      │
│     },                   │                                      │
│     ...                  │                                      │
│   ]                      │                                      │
│ }                        │                                      │
└──────────────────────────┘                                      │
   │                                                              │
   └─> MOSTRAR ERRORES AL USUARIO                                │
       FIN DEL PROCESO                                           │
                                                                 │
   SI SIN ERRORES: ◄──────────────────────────────────────────┘
   │
   ▼
┌──────────────────────────┐
│ ExcelValidacionResponse  │
│ {                        │
│   status: "OK",          │
│   message: "Validado",   │
│   totalFilas: 150,       │
│   totalValidas: 150,     │
│   data: [                │
│     {                    │
│       matricula: "...",  │
│       nombre: "...",     │
│       carrera: "ICA",    │
│       semestreId: 5,     │
│       orden: 5           │
│     },                   │
│     ...                  │
│   ]                      │
│ }                        │
└──────────────────────────┘
   │
   │ (Cliente extrae "data")
   │
   ▼
╔═════════════════════════════════════════════════════════════════╗
║ ENDPOINT 2: POST /api/asignaciones/ejecutar                    ║
║                                                                 ║
║ Responsabilidades:                                              ║
║ • Validar precondiciones                                        ║
║ • Crear ProcesoAsignacion en BD                                ║
║ • Asignar cada alumno a tutor:                                 ║
║   - Sincronizar cargas                                         ║
║   - Seleccionar tutor disponible                              ║
║   - Crear Asignacion                                           ║
║   - Actualizar carga                                           ║
║   - Registrar en auditoría                                     ║
║   - CAPTURAR ERRORES (no parar)                               ║
║ • Calcular estadísticas                                        ║
║ • Retornar resultado                                           ║
║                                                                 ║
║ Input:  EjecutarAsignacionRequest                             ║
║         {semestreId: 5, alumnosValidados: [...]}              ║
║ Output: EjecucionAsignacionResponse                           ║
╚═════════════════════════════════════════════════════════════════╝
   │
   ├───────────────────────────────────────────────────────────────┐
   │                                                               │
   │ PRECONDICIONES (Si falla → ERROR)                            │
   │ • semestreId válido y existe                                │
   │ • alumnosValidados no vacío                                 │
   │                                                               │
   │ CREAR PROCESO                                                │
   │ • ProcesoAsignacion {estado: ASIGNANDO}                     │
   │                                                               │
   │ SINCRONIZAR CARGAS                                           │
   │ • Recalcular carga real de todos los tutores                │
   │                                                               │
   │ PROCESAR CADA ALUMNO                                         │
   │ ├─ Sincronizar carga tutor                                  │
   │ ├─ Lock pessimista                                          │
   │ ├─ Validar capacidad                                        │
   │ ├─ Seleccionar tutor (si nuevo)                             │
   │ │  ├─ Misma carrera                                         │
   │ │  ├─ Carrera compatible                                    │
   │ │  └─ Menor carga                                           │
   │ ├─ Crear Asignacion (NUEVO_INGRESO o REINGRESO)            │
   │ ├─ Actualizar carga                                         │
   │ ├─ Registrar en auditoría                                   │
   │ │                                                             │
   │ └─ Capturar excepciones:                                     │
   │    ├─ DuplicadoException → agregar error, continuar          │
   │    ├─ CapacidadExcedidaException → agregar error, continuar │
   │    ├─ SinTutorDisponibleException → agregar error, continuar│
   │    └─ Generic Exception → agregar error, continuar           │
   │                                                               │
   │ ACTUALIZAR PROCESO                                           │
   │ • estado = COMPLETADO                                       │
   │ • Guardar estadísticas                                      │
   │                                                               │
   └───────────────────────────────────────────────────────────────┘
   │
   ├──────────────────────────────────────────────────────────────┐
   │                                                              │
   │ CASO 1: TODOS EXITOSOS (status=OK)                         │
   │                                                              │
   ▼                                                              │
┌──────────────────────────────┐                                  │
│ EjecucionAsignacionResponse  │                                  │
│ {                            │                                  │
│   status: "OK",              │                                  │
│   message: "Completada",     │                                  │
│   totalAlumnos: 150,         │                                  │
│   alumnosAsignados: 150,     │                                  │
│   alumnosConError: 0,        │                                  │
│   duracionMs: 2500,          │                                  │
│   porcentajeExito: 100.0,    │                                  │
│   detalles: "150 exitosos",  │                                  │
│   erroresDetalle: []         │                                  │
│ }                            │                                  │
└──────────────────────────────┘                                  │
   │                                                              │
   └─> MOSTRAR ÉXITO                                              │
                                                                 │
   │ CASO 2: ALGUNOS FALLIDOS (status=PARTIAL)                 ◄─┤
   │                                                              │
   ▼                                                              │
┌──────────────────────────────┐                                  │
│ EjecucionAsignacionResponse  │                                  │
│ {                            │                                  │
│   status: "PARTIAL",         │                                  │
│   message: "Parcial",        │                                  │
│   totalAlumnos: 150,         │                                  │
│   alumnosAsignados: 145,     │                                  │
│   alumnosConError: 5,        │                                  │
│   duracionMs: 2500,          │                                  │
│   porcentajeExito: 96.67,    │                                  │
│   detalles: "145 de 150",    │                                  │
│   erroresDetalle: [          │                                  │
│     {                        │                                  │
│       alumnoMatricula: "...",│                                  │
│       error: "Sin capacidad",│                                  │
│       raizCausa: "..."       │                                  │
│     },                       │                                  │
│     ...                      │                                  │
│   ]                          │                                  │
│ }                            │                                  │
└──────────────────────────────┘                                  │
   │                                                              │
   └─> MOSTRAR ÉXITO + ERRORES                                   │
                                                                 │
   │ CASO 3: ERROR TOTAL (status=ERROR)                        ◄─┘
   │
   ▼
┌──────────────────────────────┐
│ EjecucionAsignacionResponse  │
│ {                            │
│   status: "ERROR",           │
│   message: "Error",          │
│   totalAlumnos: 150,         │
│   alumnosAsignados: 0,       │
│   alumnosConError: 150,      │
│   duracionMs: 250,           │
│   porcentajeExito: 0.0,      │
│   detalles: "semestreId...", │
│   erroresDetalle: []         │
│ }                            │
└──────────────────────────────┘
   │
   └─> MOSTRAR ERROR
```

---

## 📊 Tabla de Estados

| Estado | Descripción | Acción |
|--------|-------------|--------|
| **OK** | Todos asignados | ✅ Éxito total |
| **PARTIAL** | Algunos asignados | ⚠️ Revisar errores |
| **ERROR** | Ninguno asignado | ❌ Verificar precondiciones |

---

## 🔍 Detalles de Cada Paso

### ENDPOINT 1: Validación

```
1. Leer Excel
   └─> List<AlumnoExcelDTO> (bruto)

2. Validar estructura
   ├─ Headers presentes?
   ├─ Columnas correctas?
   └─ Si falla → ERROR_FORMATO

3. Validar datos (SIN PARAR)
   Para cada fila:
   ├─ Matrícula no vacía?
   ├─ Nombre no vacío?
   ├─ Carrera válida?
   ├─ Semestre válido (1-12)?
   ├─ Referencias existen en BD?
   └─ Recolectar errores

4. Si hay errores → return ERROR con lista

5. Si no hay errores:
   ├─ Limpiar datos (trim, uppercase)
   ├─ Ordenar por semestre DESC
   │  (8°, 7°, 6°, ..., 1°)
   ├─ Convertir a AlumnoValidadoDTO
   │  (agregar semestreId, codigo, numerico)
   └─ return OK con data ordenada
```

### ENDPOINT 2: Ejecución

```
1. Validar entrada
   ├─ semestreId válido?
   ├─ alumnosValidados no vacío?
   └─ Semestre existe?

2. Crear ProcesoAsignacion
   └─ estado = ASIGNANDO

3. Sincronizar cargas
   └─ Recalcular carga real de todos tutores

4. Para cada alumno (en orden):
   ├─ Sincronizar carga tutor
   ├─ Lock pessimista
   ├─ ¿Tutor anterior disponible?
   │  ├─ Sí → REINGRESO
   │  └─ No → NUEVO_INGRESO
   ├─ Seleccionar tutor
   │  ├─ Mismo carrera disponible?
   │  ├─ Compatible disponible?
   │  └─ Menor carga?
   ├─ Crear Asignacion
   ├─ Actualizar cargas
   ├─ Registrar auditoría
   └─ Capturar errores

5. Calcular estadísticas
   ├─ Total procesados
   ├─ Total exitosos
   ├─ Total errores
   ├─ Duración ms
   └─ Porcentaje éxito

6. Retornar resultado
```

---

## 💡 Casos de Uso Comunes

### Caso 1: Asignación de Nuevo Semestre
```
Input:  150 alumnos nuevos, semestre 5
Process: Todos NUEVO_INGRESO
         Distribuido entre tutores de ICA
Output:  OK, 150/150
```

### Caso 2: Asignación Mixta (Nuevo + Reingreso)
```
Input:  100 nuevos + 50 reingresos
Process: Reingreso mantiene tutor anterior (no incrementa carga)
         Nuevo busca tutor disponible
Output:  OK, 150/150
```

### Caso 3: Con Algunos Errores
```
Input:  150 alumnos
Process: 145 asignados exitosamente
         5 fallan por capacidad excedida
Output:  PARTIAL, 145/150, 5 errores
```

### Caso 4: Sin Tutores Disponibles
```
Input:  150 alumnos
Process: ICA al 100% de capacidad
         ISC al 100% de capacidad
         Sin opciones cruzadas
Output:  ERROR, 0/150 (todos sin asignar)
```

---

## 📊 Bases de Datos Afectadas

### Escritura
```
Tabla: asignaciones
├─ INSERT: nuevo registro para cada alumno
├─ Campos: id_alumno, id_tutor, id_semestre, tipo_asignacion, fecha
└─ Constraint: UNIQUE(id_alumno, id_tutor, id_semestre)

Tabla: tutores
├─ UPDATE: carga_actual (solo si NUEVO_INGRESO)
├─ Campo: carga_actual += 1
└─ Donde: id_tutor = X

Tabla: logs_auditoria
├─ INSERT: registro para cada asignación
├─ Campos: id_proceso, tipo_accion, datos_antes/después, usuario, fecha
└─ Referencia: tipo_accion = ASIGNACION

Tabla: procesos_asignacion
├─ INSERT: nuevo proceso
├─ UPDATE: estado = COMPLETADO, fecha_fin, estadísticas
└─ Campos: estado, total_procesados, total_asignados, total_errores
```

### Lectura
```
Tabla: alumnos
├─ SELECT: buscar si existe
└─ Validar si ya tiene asignación en semestre

Tabla: tutores
├─ SELECT: buscar disponibles
├─ SELECT: buscar por carrera
└─ SELECT: obtener carga actual

Tabla: semestres
├─ SELECT: validar semestreId existe
└─ SELECT: obtener código

Tabla: matriz_afinidad_carrera
├─ SELECT: carreras compatibles
└─ Para selección cruzada
```

---

## 🔐 Transacciones y Locks

```
ProcesoAsignacion {
    estado = ASIGNANDO
    ↓
    Para cada alumno:
        ├─ BEGIN TRANSACTION (REQUIRES_NEW)
        │
        ├─ SELECT Tutor FOR UPDATE (LOCK PESSIMISTA)
        │
        ├─ UPDATE Tutor SET carga_actual = carga_actual + 1
        │
        ├─ INSERT Asignacion
        │
        ├─ INSERT LogAuditoria
        │
        └─ COMMIT o ROLLBACK
    ↓
    estado = COMPLETADO
    Guardar estadísticas
}
```

---

## 📝 Auditoría Registrada

Para cada asignación exitosa:
```json
{
  "id_proceso": 123,
  "tipo_accion": "ASIGNACION",
  "entidad": "ALUMNO",
  "id_entidad": 456,
  "descripcion": "Alumno A00123 asignado a Dr. López (NUEVO_INGRESO)",
  "datos_antes": null,
  "datos_despues": {
    "id_tutor": 789,
    "id_semestre": 5,
    "tipo": "NUEVO_INGRESO",
    "semestre": "2025-2025-FN"
  },
  "usuario": "SISTEMA",
  "fecha_registro": "2025-11-19 23:30:45"
}
```

---

## ⏱️ Timing Estimado

| Operación | Alumnos | Tutores | Tiempo |
|-----------|---------|---------|--------|
| Validación | 150 | - | ~500ms |
| Asignación | 150 | 20 | ~2500ms |
| Auditoría | 150 | - | ~1000ms |
| **TOTAL** | **150** | **20** | **~4000ms** |

---

**Versión:** 4.0.0
**Última actualización:** 19 de Noviembre, 2025
