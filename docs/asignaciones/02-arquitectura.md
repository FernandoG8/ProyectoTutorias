# 🏗️ Arquitectura - Separación de Responsabilidades

## 🎯 Principio de Diseño

**Una responsabilidad = Un servicio**

El módulo se divide en dos servicios independientes:

```
┌─────────────────────────────────────────────────────────────┐
│ EXCEL VALIDATION SERVICE                                    │
│ (ExcelValidacionYOrdenaService)                             │
│                                                             │
│ Responsabilidades:                                          │
│ 1. Leer archivo Excel                                       │
│ 2. Validar datos (reportar TODOS los errores)              │
│ 3. Limpiar y normalizar                                     │
│ 4. Ordenar por semestre                                     │
│ 5. Convertir a DTOs                                         │
│                                                             │
│ Input:  Excel file + semestreId                            │
│ Output: ExcelValidacionResponse (OK o ERROR)               │
└─────────────────────────────────────────────────────────────┘
                           │
                           ├─ ERROR → Fin
                           │
                           └─ OK ↓
┌─────────────────────────────────────────────────────────────┐
│ ASSIGNMENT EXECUTION SERVICE                                │
│ (EjecucionAsignacionService)                               │
│                                                             │
│ Responsabilidades:                                          │
│ 1. Orquestar ejecución                                      │
│ 2. Validar precondiciones                                   │
│ 3. Invocar AsignacionService.asignarAlumnos()              │
│ 4. Transformar resultado a Response                         │
│                                                             │
│ Input:  EjecutarAsignacionRequest (alumnos validados)     │
│ Output: EjecucionAsignacionResponse (resultado)            │
└─────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ CORE ASSIGNMENT SERVICE                                     │
│ (AsignacionService)                                         │
│                                                             │
│ Responsabilidades:                                          │
│ 1. Lógica pura de asignación                               │
│ 2. Seleccionar tutor disponible                            │
│ 3. Crear registros Asignacion                              │
│ 4. Actualizar cargas de tutores                            │
│ 5. Manejar reingreso vs nuevo ingreso                      │
│ 6. Registrar en auditoría                                  │
│ 7. Capturar errores (best-effort)                          │
│                                                             │
│ Input:  List<AlumnoExcelDTO> + semestreId                │
│ Output: ResultadoAsignacion                                │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔄 Flujo de Datos

```
Excel File
    │
    ├─ ExcelReaderService.leerArchivo()
    │  └─> List<AlumnoExcelDTO>
    │
    ├─ limpiarDatos()
    │  └─> List<AlumnoExcelDTO> (limpio)
    │
    ├─ AlumnoValidadorService.validarAlumnos()
    │  └─> ResultadoValidacion (errores/válidos)
    │
    ├─ Si ERROR:
    │  └─> ExcelValidacionResponse {status=ERROR, data=null}
    │
    └─ Si OK:
       ├─ ordenarPorSemestre()
       │  └─> List<AlumnoValidadoDTO>
       │
       ├─ convertirADtos()
       │  └─> List<AlumnoValidadoDTO> (con semestreId)
       │
       └─> ExcelValidacionResponse {status=OK, data=[...]}
           │
           └─> Client extrae data
               │
               └─> POST /ejecutar
                   │
                   ├─ Validar precondiciones
                   │
                   ├─ Crear ProcesoAsignacion
                   │
                   ├─ AsignacionService.asignarAlumnos()
                   │  │
                   │  ├─ Para cada alumno:
                   │  │  ├─ Sincronizar carga tutor
                   │  │  ├─ Seleccionar tutor
                   │  │  ├─ Crear Asignacion
                   │  │  ├─ Actualizar carga
                   │  │  ├─ Registrar auditoría
                   │  │  └─ Capturar error
                   │  │
                   │  └─> ResultadoAsignacion
                   │
                   └─> EjecucionAsignacionResponse {status, errores, stats}
```

---

## 🛠️ Stack Tecnológico por Capa

### Controller Layer
```java
AsignacionController
├─ @PostMapping("/validar-excel")
│  └─ ExcelValidacionYOrdenaService.validarYProcesarExcel()
│
└─ @PostMapping("/ejecutar")
   └─ EjecucionAsignacionService.ejecutar()
```

### Service Layer
```java
ExcelValidacionYOrdenaService
├─ ExcelReaderService
├─ AlumnoValidadorService
├─ limpiarDatos()
├─ ordenarPorSemestre()
└─ convertirADtos()

EjecucionAsignacionService
├─ SemestreService
├─ ProcesoAsignacionRepository
└─ AsignacionService

AsignacionService
├─ AlumnoRepository
├─ TutorRepository
├─ TutorSincronizacionService
├─ AuditoriaService
└─ AlertaProcesoRepository
```

### Domain Layer
```java
Entities:
├─ Asignacion (tipo: NUEVO_INGRESO, REINGRESO)
├─ Alumno
├─ Tutor
├─ Semestre
├─ LogAuditoria
├─ ProcesoAsignacion
└─ TutorCambioAuditoria

Enums:
├─ TipoAsignacion
├─ EstadoProceso
├─ TipoError
└─ TipoAccion
```

---

## 📦 Paquetes y Responsabilidades

```
com.universidad.tutorias
│
├─ application/
│  ├─ service/
│  │  ├─ ExcelValidacionYOrdenaService
│  │  ├─ AsignacionService
│  │  ├─ EjecucionAsignacionService
│  │  ├─ TutorSincronizacionService
│  │  ├─ AuditoriaService
│  │  └─ impl/
│  │     ├─ ExcelValidacionYOrdenaServiceImpl
│  │     ├─ AsignacionServiceImpl
│  │     ├─ EjecucionAsignacionServiceImpl
│  │     └─ ...
│  │
│  └─ dto/
│     ├─ AlumnoExcelDTO
│     ├─ AlumnoValidadoDTO
│     ├─ EjecutarAsignacionRequest
│     ├─ ExcelValidacionResponse
│     ├─ EjecucionAsignacionResponse
│     ├─ AsignacionErrorDTO
│     └─ ...
│
├─ domain/
│  ├─ entity/
│  │  ├─ Asignacion
│  │  ├─ Alumno
│  │  ├─ Tutor
│  │  ├─ Semestre
│  │  └─ ...
│  │
│  ├─ enums/
│  │  ├─ TipoAsignacion
│  │  ├─ EstadoProceso
│  │  └─ ...
│  │
│  └─ repository/
│     ├─ AsignacionRepository
│     ├─ AlumnoRepository
│     ├─ TutorRepository
│     └─ ...
│
└─ infrastructure/
   ├─ controller/
   │  └─ AsignacionController
   │
   └─ exception/
      ├─ CapacidadExcedidaException
      ├─ DuplicadoException
      └─ ...
```

---

## 🔀 Patrones de Diseño Utilizados

### 1. Separation of Concerns
- Excel parsing → ExcelReaderService
- Validación → AlumnoValidadorService
- Asignación → AsignacionService
- Orquestación → EjecucionAsignacionService

### 2. Service Layer Pattern
- Controlador maneja HTTP
- Servicio maneja lógica
- Repositorio maneja BD

### 3. Data Transfer Object (DTO)
- `AlumnoExcelDTO` - Desde Excel
- `AlumnoValidadoDTO` - Después de validación
- Request/Response DTOs para HTTP

### 4. Repository Pattern
- Abstracción de acceso a datos
- Queries customizadas
- Transacciones

### 5. Transaction Script
- `AsignacionServiceImpl.asignarAlumnoTransaccional()`
- REQUIRES_NEW propagation para aislamiento

### 6. Best-Effort Processing
- Try-catch individual para cada alumno
- Colección de errores
- Continuar procesamiento

---

## 🔒 Consideraciones de Seguridad

### Validación de Entrada
```
Excel → Leer → Validar estructura
              → Validar datos (CADA fila)
              → Normalizar
              → Convertir a DTOs tipados
```

### Locking y Concurrencia
```
ProcesoAsignacion
    ├─ Sincronizar todas las cargas antes de empezar
    │
    ├─ Para cada alumno:
    │  └─ Pessimistic WRITE lock en tutor
    │     ├─ Verificar capacidad
    │     ├─ Incrementar carga
    │     └─ Guardar
    │
    └─ Flush/clear cada 100 registros
```

### Auditoría
```
Cada asignación registra:
├─ logs_auditoria (ASIGNACION)
└─ Cambios manuales en tutor_cambio_auditoria
```

---

## 📊 Base de Datos

### Tablas Principales
```
asignaciones
├─ id_alumno (FK)
├─ id_tutor (FK)
├─ id_semestre (FK)
├─ tipo_asignacion (ENUM: NUEVO_INGRESO, REINGRESO)
├─ fecha_asignacion
└─ semestre_academico (legacy)

logs_auditoria
├─ id_proceso (FK)
├─ tipo_accion (ENUM)
├─ entidad (varchar)
├─ id_entidad (Long)
├─ descripcion
├─ datos_antes, datos_despues (JSON)
└─ fecha_registro

tutor_cambio_auditoria
├─ id_asignacion (FK)
├─ id_tutor_anterior (FK)
├─ id_tutor_nuevo (FK)
├─ usuario_responsable
├─ fecha_hora_cambio
├─ motivo
└─ tipo_cambio
```

### Índices Importantes
```
asignaciones:
├─ idx_alumno_asig
├─ idx_tutor_asig
├─ idx_semestre_asig
└─ uk_asignacion_alumno_tutor_semestre (UNIQUE)

logs_auditoria:
├─ idx_proceso
├─ idx_tipo_accion
└─ idx_fecha

tutor:
├─ idx_carrera_tutor
└─ idx_activo
```

---

## 🚀 Flujo de Scalabilidad

### Para N Semestres
- ✅ Ordenamiento dinámico (no hardcodeado)
- ✅ Funciona con cualquier semestre (1-12+)

### Para N Alumnos
- ✅ Batch processing (size=100)
- ✅ Memory-efficient
- ✅ Flush/clear de EntityManager

### Para N Tutores
- ✅ Agrupar por carrera en memoria
- ✅ Pessimistic locking por tutor
- ✅ Sincronización previa

---

**Versión:** 4.0.0
**Última actualización:** 19 de Noviembre, 2025
