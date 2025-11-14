# API Endpoints - Sistema de Tutorías

**Versión:** 2.0.0 (Post-refactorización Semestres)  
**Base URL:** `http://localhost:5173/api`  
**Fecha:** 14 de Noviembre, 2025

---

## 🔄 Cambios Importantes vs Versión Anterior

### ⚠️ Breaking Changes

1. **Semestre Académico ahora es obligatorio**
   - Todos los procesos de asignación requieren `semestreAcademico`
   - Formato: `YYYY-YYYY-FN` (ej: `2025-2026-F1`)

2. **Nuevos endpoints de gestión de semestres**
   - `/api/semestres/*` - CRUD completo de semestres
   - `/api/dashboard/*` - Estadísticas en tiempo real

3. **Lógica de asignación mejorada**
   - Mantiene tutor anterior cuando es posible
   - Genera alertas de reasignaciones forzadas
   - Incremento de carga optimizado

---

## 📚 Índice de Recursos

1. [Semestres](#1-semestres) - Gestión de períodos académicos
2. [Dashboard](#2-dashboard) - Estadísticas y métricas
3. [Asignaciones](#3-asignaciones) - Proceso de asignación de tutores
4. [Alumnos](#4-alumnos) - Gestión de estudiantes
5. [Tutores](#5-tutores) - Gestión de docentes
6. [Reportes](#6-reportes) - Generación de documentos
7. [Búsquedas](#7-búsquedas) - Búsqueda avanzada

---

## 1. Semestres

### 1.1 Crear Semestre

```http
POST /api/semestres
Content-Type: application/json
```

**Request Body:**
```json
{
  "codigo": "2025-2026-F1",
  "nombre": "Semestre Agosto 2025 - Enero 2026",
  "fechaInicio": "2025-08-01",
  "fechaFin": "2026-01-31"
}
```

**Response 201 Created:**
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "codigo": "2025-2026-F1",
    "nombre": "Semestre Agosto 2025 - Enero 2026",
    "fechaInicio": "2025-08-01",
    "fechaFin": "2026-01-31",
    "activo": false,
    "estaVigente": true,
    "totalAsignaciones": 0,
    "fechaCreacion": "2025-11-14T10:30:00"
  },
  "message": "Semestre creado exitosamente"
}
```

**Validaciones:**
- `codigo`: Patrón `YYYY-YYYY-FN` (ej: 2025-2026-F1 o 2025-2026-F2)
- `fechaFin` > `fechaInicio`
- `codigo` único (no duplicados)

**Errores:**
- `400` - Formato inválido o código duplicado
- `500` - Error interno

---

### 1.2 Listar Todos los Semestres

```http
GET /api/semestres
```

**Response 200 OK:**
```json
{
  "status": "success",
  "data": [
    {
      "id": 1,
      "codigo": "2025-2026-F1",
      "nombre": "Semestre Agosto 2025 - Enero 2026",
      "fechaInicio": "2025-08-01",
      "fechaFin": "2026-01-31",
      "activo": true,
      "estaVigente": true,
      "totalAsignaciones": 250
    },
    {
      "id": 2,
      "codigo": "2024-2025-F2",
      "nombre": "Semestre Enero 2025 - Julio 2025",
      "fechaInicio": "2025-01-01",
      "fechaFin": "2025-07-31",
      "activo": false,
      "estaVigente": false,
      "totalAsignaciones": 230
    }
  ]
}
```

**Orden:** Fecha de inicio descendente (más reciente primero)

---

### 1.3 Obtener Semestre Activo

```http
GET /api/semestres/activo
```

**Response 200 OK:**
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "codigo": "2025-2026-F1",
    "nombre": "Semestre Agosto 2025 - Enero 2026",
    "activo": true,
    "fechaInicio": "2025-08-01",
    "fechaFin": "2026-01-31"
  }
}
```

**Response 404 Not Found:**
```json
{
  "status": "error",
  "message": "No hay semestre activo configurado"
}
```

**Uso:** Mostrar en header/navbar el semestre actual

---

### 1.4 Activar Semestre

```http
PUT /api/semestres/{id}/activar
```

**Response 200 OK:**
```json
{
  "status": "success",
  "data": "2025-2026-F1",
  "message": "Semestre activado exitosamente"
}
```

**Comportamiento:**
- Desactiva todos los demás semestres
- Solo puede haber 1 semestre activo a la vez

---

### 1.5 Obtener Estadísticas del Semestre

```http
GET /api/semestres/{id}/estadisticas
```

**Response 200 OK:**
```json
{
  "status": "success",
  "data": {
    "semestreId": 1,
    "codigo": "2025-2026-F1",
    "nombre": "Semestre Agosto 2025 - Enero 2026",
    "totalAsignaciones": 250,
    "alumnosActivos": 240,
    "alumnosInactivos": 10,
    "totalTutoresActivos": 15,
    "tutoresConAsignaciones": 14,
    "tutoresConCapacidadLlena": 3,
    "tutoresDisponibles": 11,
    "promedioAlumnosPorTutor": 17.14,
    "distribucionPorCarrera": {
      "Ingeniería en Sistemas": 80,
      "Ingeniería en Informática": 70,
      "Ingeniería en Tecnología": 60,
      "Ingeniería en Desarrollo": 40
    }
  }
}
```

**Uso:** Dashboard detallado, gráficas por semestre

---

### 1.6 Eliminar Semestre

```http
DELETE /api/semestres/{id}
```

**Response 200 OK:**
```json
{
  "status": "success",
  "message": "Semestre eliminado exitosamente"
}
```

**Errores:**
- `400` - No se puede eliminar un semestre activo
- `404` - Semestre no encontrado

**⚠️ ADVERTENCIA:** Eliminación en cascada. Borra todas las asignaciones asociadas.

---

## 2. Dashboard

### 2.1 Estadísticas Generales

```http
GET /api/dashboard/estadisticas
```

**Response 200 OK:**
```json
{
  "status": "success",
  "data": {
    "semestre": "2025-2026-F1",
    "semestre_id": 1,
    "total_alumnos": 250,
    "alumnos_con_tutor": 240,
    "alumnos_sin_tutor": 10,
    "total_tutores": 15,
    "total_asignaciones": 240,
    "promedio_alumnos_por_tutor": 16.0,
    "porcentaje_cobertura": 96.0
  }
}
```

**Uso:** Tarjetas de métricas principales en dashboard

---

### 2.2 Distribución de Tutores

```http
GET /api/dashboard/distribucion-tutores
```

**Response 200 OK:**
```json
{
  "status": "success",
  "data": [
    {
      "tutor_id": 1,
      "tutor_nombre": "Dr. Juan García",
      "tutor_carrera": "Ingeniería en Sistemas",
      "alumnos_asignados": 20,
      "capacidad_max": 25,
      "carga_utilizada": 80.0
    },
    {
      "tutor_id": 2,
      "tutor_nombre": "Dra. María López",
      "tutor_carrera": "Ingeniería en Informática",
      "alumnos_asignados": 18,
      "capacidad_max": 25,
      "carga_utilizada": 72.0
    }
  ]
}
```

**Uso:** Gráfica de barras "Tutores vs Alumnos"

---

### 2.3 Procesos Recientes

```http
GET /api/dashboard/procesos-recientes?limit=5
```

**Query Params:**
- `limit` (opcional, default: 5) - Cantidad de procesos a mostrar

**Response 200 OK:**
```json
{
  "status": "success",
  "data": [
    {
      "id": 1,
      "estado": "COMPLETADO",
      "archivo": "alumnos_2025-2026-F1.xlsx",
      "usuario": "coord_tutorias",
      "fecha_inicio": "2025-11-14T09:00:00",
      "fecha_fin": "2025-11-14T09:05:30",
      "total_procesados": 250,
      "total_asignados": 240,
      "total_errores": 10,
      "tiempo_segundos": 330.0
    }
  ]
}
```

**Estados posibles:**
- `INICIADO`
- `COMPARANDO`
- `LIBERANDO_CUPOS`
- `ASIGNANDO`
- `COMPLETADO`
- `FALLIDO`

**Uso:** Lista de "Alertas Recientes" en dashboard

---

### 2.4 Información del Semestre Activo

```http
GET /api/dashboard/semestre-activo
```

**Response 200 OK:**
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "codigo": "2025-2026-F1",
    "nombre": "Semestre Agosto 2025 - Enero 2026",
    "estaVigente": true,
    "diasRestantes": 45,
    "progreso": 75.0
  }
}
```

---

### 2.5 Health Check

```http
GET /api/dashboard/health
```

**Response 200 OK:**
```json
{
  "status": "success",
  "data": {
    "status": "UP",
    "database": "UP",
    "semestre_activo": true,
    "version": "2.0.0"
  }
}
```

---

## 3. Asignaciones

### 3.1 Iniciar Proceso de Asignación

```http
POST /api/asignaciones/iniciar
Content-Type: multipart/form-data
```

**Form Data:**
```
archivo: <archivo Excel .xlsx o .xls>
semestreAcademico: "2025-2026-F1"  [REQUERIDO]
usuario: "coord_tutorias"
```

**Response 202 Accepted:**
```json
{
  "status": "success",
  "data": {
    "procesoId": 1,
    "estado": "INICIADO",
    "semestreId": 1,
    "semestreCodigo": "2025-2026-F1",
    "mensaje": "Proceso iniciado correctamente",
    "timestamp": "2025-11-14T10:30:00"
  }
}
```

**Validaciones archivo Excel:**
- Formato: `.xlsx` o `.xls`
- Tamaño máximo: 10MB
- Columnas requeridas: `matricula`, `nombre`, `carrera`, `semestre`

**Errores:**
- `400` - Archivo inválido, formato incorrecto, semestre no existe
- `400` - Semestre no está activo
- `500` - Error al procesar archivo

**⚠️ IMPORTANTE:** El proceso es asíncrono. Usar `procesoId` para consultar estado.

---

### 3.2 Consultar Estado del Proceso

```http
GET /api/asignaciones/proceso/{procesoId}
```

**Response 200 OK:**
```json
{
  "status": "success",
  "data": {
    "procesoId": 1,
    "estado": "ASIGNANDO",
    "progreso": {
      "porcentaje": 75,
      "alumnosProcesados": 188,
      "alumnosAsignados": 180,
      "errores": 5,
      "warnings": 12
    },
    "fechaInicio": "2025-11-14T09:00:00",
    "fechaFin": null,
    "tiempoTranscurridoMs": 120000
  }
}
```

**Uso:** Polling cada 2-3 segundos para mostrar progreso

---

### 3.3 Listar Todos los Procesos

```http
GET /api/asignaciones/procesos
```

**Response 200 OK:**
```json
{
  "status": "success",
  "data": [
    {
      "procesoId": 1,
      "estado": "COMPLETADO",
      "semestreCodigo": "2025-2026-F1",
      "totalAlumnosProcesados": 250,
      "totalAlumnosAsignados": 240,
      "totalErrores": 10,
      "archivoOrigen": "alumnos_2025.xlsx",
      "fechaInicio": "2025-11-14T09:00:00",
      "fechaFin": "2025-11-14T09:05:30",
      "usuarioEjecutor": "coord_tutorias"
    }
  ]
}
```

---

### 3.4 Cambio Manual de Tutor

```http
POST /api/asignaciones/cambio-tutor
Content-Type: application/json
```

**Request Body:**
```json
{
  "alumnoId": 100,
  "tutorOrigenId": 5,
  "tutorDestinoId": 8,
  "motivo": "Solicitud del estudiante por incompatibilidad de horarios",
  "usuario": "coord_tutorias",
  "semestreAcademico": "2025-2026-F1"
}
```

**Response 200 OK:**
```json
{
  "status": "success",
  "data": {
    "alumnoId": 100,
    "tutorAnteriorId": 5,
    "tutorNuevoId": 8,
    "tutorAnteriorNombre": "Dr. Juan García",
    "tutorNuevoNombre": "Dra. María López",
    "fechaCambio": "2025-11-14T10:30:00",
    "motivo": "Solicitud del estudiante...",
    "semestreAcademico": "2025-2026-F1"
  },
  "message": "Cambio de tutor realizado exitosamente"
}
```

**Validaciones:**
- Alumno debe estar asignado a tutor origen
- Tutor destino debe tener capacidad disponible
- Alumno no puede haber excedido límite de cambios (máximo 2)

**Errores:**
- `400` - Alumno no asignado a tutor origen
- `400` - Tutor destino sin capacidad
- `400` - Límite de reasignaciones alcanzado (2 máximo)

---

## 4. Alumnos

### 4.1 Listar Alumnos con Filtros

```http
GET /api/alumnos
```

**Query Parameters:**
- `semestreId` (opcional) - ID del semestre (usa activo por defecto)
- `estado` (opcional, default: `ACTIVO`) - `ACTIVO` | `INACTIVO` | `TODOS`
- `carrera` (opcional) - Filtrar por carrera
- `semestreCursado` (opcional) - Filtrar por semestre cursado (1-12)
- `busqueda` (opcional) - Búsqueda por matrícula o nombre
- `page` (default: 0)
- `size` (default: 50)
- `sortBy` (default: `semestre`) - `nombre` | `matricula` | `semestre`
- `direction` (default: `ASC`) - `ASC` | `DESC`

**Response 200 OK:**
```json
{
  "status": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "matricula": "2025001",
        "nombre": "Juan Pérez García",
        "carrera": "Ingeniería en Sistemas",
        "semestre": 7,
        "estado": "ACTIVO",
        "tutorActual": {
          "id": 5,
          "nombre": "Dr. Fernando Silva",
          "carrera": "Ingeniería en Sistemas"
        },
        "contadorCambiosTutor": 0
      }
    ],
    "totalElements": 250,
    "totalPages": 5,
    "currentPage": 0,
    "pageSize": 50,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

**Comportamiento por defecto:**
- Muestra solo `ACTIVOS` del semestre activo
- Ordenados por `semestre` ASC (1, 1, 1, 3, 3, 5, 7...)

**Uso:** Tabla principal de alumnos con paginación

---

### 4.2 Obtener Alumno por ID

```http
GET /api/alumnos/{id}
```

**Response 200 OK:**
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "matricula": "2025001",
    "nombre": "Juan Pérez García",
    "carrera": "Ingeniería en Sistemas",
    "semestre": 7,
    "estado": "ACTIVO",
    "tutorActual": {
      "id": 5,
      "nombre": "Dr. Fernando Silva"
    },
    "contadorCambiosTutor": 0,
    "fechaRegistro": "2024-08-01T10:00:00"
  }
}
```

---

### 4.3 Buscar Alumnos

```http
GET /api/alumnos/buscar?query={texto}
```

**Query Parameters:**
- `query` (requerido) - Texto a buscar (matrícula o nombre)

**Response 200 OK:**
```json
{
  "status": "success",
  "data": [
    {
      "id": 1,
      "matricula": "2025001",
      "nombre": "Juan Pérez García",
      "carrera": "Ingeniería en Sistemas"
    }
  ]
}
```

**Uso:** Autocompletado en formularios

---

## 5. Tutores

### 5.1 Listar Tutores

```http
GET /api/tutores
```

**Query Parameters:**
- `activo` (opcional, default: `true`) - Filtrar por estado
- `carrera` (opcional) - Filtrar por carrera

**Response 200 OK:**
```json
{
  "status": "success",
  "data": [
    {
      "id": 1,
      "nombre": "Dr. Juan García",
      "carrera": "Ingeniería en Sistemas",
      "capacidadMax": 25,
      "cargaActual": 20,
      "areaAtencion": "Redes y Sistemas Distribuidos",
      "letraEdificio": "D",
      "activo": true
    }
  ]
}
```

---

### 5.2 Obtener Alumnos del Tutor

```http
GET /api/tutores/{tutorId}/alumnos
```

**Query Parameters:**
- `semestreId` (opcional) - ID del semestre (usa activo por defecto)

**Response 200 OK:**
```json
{
  "status": "success",
  "data": [
    {
      "id": 1,
      "matricula": "2025001",
      "nombre": "Juan Pérez",
      "semestre": 7,
      "carrera": "Ingeniería en Sistemas"
    }
  ]
}
```

**Orden:** Por `semestre` ASC (agrupados por semestre cursado)

---

## 6. Reportes

### 6.1 Reporte de Alumnos por Tutor (PDF)

```http
GET /api/reportes/tutor/{tutorId}/pdf
```

**Query Parameters:**
- `semestreId` (opcional) - ID del semestre (usa activo por defecto)

**Response:** Archivo PDF

**Headers:**
```
Content-Type: application/pdf
Content-Disposition: attachment; filename="alumnos_Dr_Juan_Garcia_2025-2026-F1.pdf"
```

**Contenido PDF:**
```
Título: Alumnos asignados - DR. JUAN GARCÍA
Periodo: 2025-2026-F1

| No. | MATRICULA | NOMBRE                  | SEMESTRE | CARRERA |
|-----|-----------|-------------------------|----------|---------|
|  1  | 2025001   | HERNANDEZ VARGAS LUIZ   | 1        | ITS     |
|  2  | 2025002   | CRUZ ARCEO HUMBERTO     | 1        | ITS     |
|  3  | 2025010   | UC AGUILAR CESAR        | 3        | ITS     |
...
```

**Características:**
- ✅ Ordenado por `semestre` ASC
- ✅ Numeración secuencial
- ✅ Agrupación visual por semestre
- ✅ Solo alumnos ACTIVOS

---

### 6.2 Reporte Consolidado por Carrera (Excel)

```http
GET /api/reportes/carrera/{codigoCarrera}/excel
```

**Query Parameters:**
- `semestreId` (opcional) - ID del semestre

**Response:** Archivo Excel

**Headers:**
```
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename="tutorias_ITS_2025-2026-F1.xlsx"
```

---

## 7. Búsquedas

### 7.1 Búsqueda Avanzada de Alumnos

```http
POST /api/busqueda/alumnos
Content-Type: application/json
```

**Request Body:**
```json
{
  "semestreId": 1,
  "estados": ["ACTIVO"],
  "carreras": ["Ingeniería en Sistemas", "Ingeniería en Informática"],
  "semestresCursados": [7, 9],
  "conTutor": true,
  "busqueda": "Juan"
}
```

**Response 200 OK:**
```json
{
  "status": "success",
  "data": [
    {
      "id": 1,
      "matricula": "2025001",
      "nombre": "Juan Pérez",
      "carrera": "Ingeniería en Sistemas",
      "semestre": 7,
      "estado": "ACTIVO",
      "tutorActual": {
        "id": 5,
        "nombre": "Dr. Fernando Silva"
      }
    }
  ]
}
```

---

## 🔐 Autenticación y Autorización

### Headers Requeridos

```
Authorization: Bearer {token}
Content-Type: application/json
```

### Roles de Usuario

- `COORDINADOR_TUTORIAS` - Acceso completo
- `TUTOR` - Acceso limitado a sus alumnos
- `ADMIN` - Acceso total al sistema

---

## 📊 Códigos de Estado HTTP

| Código | Descripción |
|--------|-------------|
| `200` | OK - Operación exitosa |
| `201` | Created - Recurso creado |
| `202` | Accepted - Proceso asíncrono iniciado |
| `204` | No Content - Eliminación exitosa |
| `400` | Bad Request - Datos inválidos |
| `401` | Unauthorized - Sin autenticación |
| `403` | Forbidden - Sin permisos |
| `404` | Not Found - Recurso no encontrado |
| `409` | Conflict - Conflicto de datos (duplicados) |
| `500` | Internal Server Error - Error del servidor |

---

## 🔄 Estructura de Respuestas

### Respuesta Exitosa

```json
{
  "status": "success",
  "data": { ... },
  "message": "Mensaje opcional"
}
```

### Respuesta de Error

```json
{
  "status": "error",
  "message": "Descripción del error",
  "errors": {
    "campo": "Detalle del error"
  },
  "timestamp": "2025-11-14T10:30:00"
}
```

---

## 📝 Notas Importantes para Frontend

### 1. Paginación

Todos los endpoints paginados siguen este formato:

```javascript
{
  content: [],           // Datos de la página actual
  totalElements: 250,    // Total de registros
  totalPages: 5,        // Total de páginas
  currentPage: 0,       // Página actual (0-indexed)
  pageSize: 50,         // Tamaño de página
  hasNext: true,        // ¿Hay siguiente página?
  hasPrevious: false    // ¿Hay página anterior?
}
```

### 2. Fechas

- Formato ISO 8601: `YYYY-MM-DDTHH:mm:ss`
- Timezone: UTC

### 3. Polling para Procesos Asíncronos

```javascript
// Ejemplo en JavaScript
const checkStatus = async (procesoId) => {
  const response = await fetch(`/api/asignaciones/proceso/${procesoId}`);
  const data = await response.json();
  
  if (data.data.estado === 'COMPLETADO' || data.data.estado === 'FALLIDO') {
    // Proceso terminado
    return data;
  } else {
    // Seguir polling cada 2 segundos
    setTimeout(() => checkStatus(procesoId), 2000);
  }
};
```

### 4. Skeleton Loaders Recomendados

- **Tablas:** Mostrar 10 filas skeleton mientras carga
- **Dashboard:** Cards con skeleton en lugar de números
- **Formularios:** Skeleton en selectores hasta que carguen opciones

### 5. Estados Vacíos

Cuando no hay datos, mostrar:
- Ilustración o ícono
- Mensaje descriptivo en español
- Acción sugerida (botón para agregar, etc.)

---

## 🚀 Endpoints de Próxima Implementación

### Planificados para v2.1.0

- [ ] `GET /api/estadisticas/historico` - Histórico de semestres
- [ ] `POST /api/notificaciones/enviar` - Sistema de notificaciones
- [ ] `GET /api/auditoria/cambios` - Log de cambios
- [ ] `POST /api/reportes/personalizado` - Reportes personalizados

---

**Última actualización:** 14 de Noviembre, 2025  
**Documentación completa:** Swagger UI en `http://localhost:5173/swagger-ui.html`
