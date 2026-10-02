# 📋 DOCUMENTACIÓN EXHAUSTIVA DE ENDPOINTS - SISTEMA DE TUTORÍAS

**Fecha de Generación:** 20 de Noviembre de 2024
**Estado:** Completo y Funcional
**Total de Endpoints:** 72
**Controladores:** 12
**Framework:** Spring Boot 3.3.1
**Lenguaje:** Java 21

---

## 📑 TABLA DE CONTENIDOS

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Arquitectura General](#arquitectura-general)
3. [Endpoints por Módulo](#endpoints-por-módulo)
4. [Flujos de Negocio](#flujos-de-negocio)
5. [Diccionario de Respuestas](#diccionario-de-respuestas)
6. [Consideraciones de Seguridad](#consideraciones-de-seguridad)

---

## 🎯 RESUMEN EJECUTIVO

### Stack Tecnológico
- **Framework:** Spring Boot 3.3.1
- **Lenguaje:** Java 21
- **ORM:** Hibernate/JPA
- **Seguridad:** JWT en cookies HTTP-only
- **Base de Datos:** MySQL/MariaDB
- **API Docs:** OpenAPI/Swagger
- **Procesamiento de Excel:** Apache POI 5.2.5

### Estadísticas Generales
| Categoría | Cantidad |
|-----------|----------|
| Total Endpoints | 72 |
| REST Controllers | 12 |
| Service Classes | 20+ |
| DTOs | 30+ |
| Repositories | 17 |
| Entidades | 10+ |
| Enums | 8 |

### Módulos Principales
1. **Autenticación** (5 endpoints)
2. **Gestión de Alumnos** (11 endpoints)
3. **Gestión de Tutores** (9 endpoints)
4. **Gestión de Semestres** (12 endpoints)
5. **Asignaciones** (7 endpoints)
6. **Alumnos Inactivos** (4 endpoints)
7. **Mantenimiento** (6 endpoints)
8. **Dashboard** (5 endpoints)
9. **Reportes** (4 endpoints)
10. **Limpieza de Datos** (6 endpoints)

---

## 🏗️ ARQUITECTURA GENERAL

```
┌─────────────────────────────────────────────────┐
│         CAPA DE PRESENTACIÓN (Controllers)      │
│  - AuthController                              │
│  - AlumnoController, AlumnoSearchController    │
│  - TutorController, TutorSearchController      │
│  - AsignacionController                        │
│  - SemestreController                          │
│  - DashboardController, ReporteController      │
│  - Y más...                                     │
└─────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────┐
│      CAPA DE APLICACIÓN (Services/DTOs)        │
│  - AlumnoCrudService, TutorCrudService         │
│  - AsignacionService, SemestreService          │
│  - ExcelValidacionYOrdenaService               │
│  - EjecucionAsignacionService                  │
│  - Y más...                                     │
└─────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────┐
│    CAPA DE DOMINIO (Entities/Repositories)     │
│  - Alumno, Tutor, Semestre, Asignacion        │
│  - AlumnoRepository, TutorRepository, etc      │
└─────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────┐
│         CAPA DE DATOS (Base de Datos)          │
│  - MySQL/MariaDB                               │
└─────────────────────────────────────────────────┘
```

---

## 🔐 MÓDULO 1: AUTENTICACIÓN Y AUTORIZACIÓN

### Controlador: `AuthController`
**Base Path:** `/auth`
**Protección:** Mecanismo JWT con cookies HTTP-only

---

### 1.1 POST /auth/login
**Descripción:** Autentica un usuario y emite sesión segura
**Protección:** Pública (sin autenticación)
**HTTP Status:**
- `200 OK` - Autenticación exitosa
- `401 Unauthorized` - Credenciales inválidas
- `400 Bad Request` - Datos faltantes o inválidos

**Request Body:**
```json
{
  "username": "coordinador",
  "password": "MiPassword123!"
}
```

**Validaciones Realizadas:**
- `username` (required): string, min 3 caracteres
- `password` (required): string, min 8 caracteres

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "message": "Autenticación exitosa"
  },
  "message": "Sesión iniciada correctamente",
  "timestamp": "2024-11-20T10:30:00.000Z"
}
```

**Cookies Emitidas:**
- `tutorias_access_token` (HTTP-only, Secure, SameSite=Strict)
- `tutorias_refresh_token` (HTTP-only, Secure, SameSite=Strict)

**Error Response (401):**
```json
{
  "success": false,
  "message": "Usuario o contraseña inválidos",
  "errors": ["Credenciales no coinciden"],
  "timestamp": "2024-11-20T10:30:00.000Z"
}
```

**Notas Internas:**
- Utiliza AuthService para validación de credenciales
- Emite tokens JWT con expiración configurable
- Las cookies se almacenan de forma segura en el navegador
- No expone el token en la respuesta (solo en cookies)

---

### 1.2 POST /auth/register
**Descripción:** Registra un nuevo usuario (solo coordinadores)
**Protección:** `@PreAuthorize("hasRole('COORDINADOR_TUTORIAS')")`
**HTTP Status:**
- `201 Created` - Usuario creado exitosamente
- `400 Bad Request` - Datos inválidos o usuario duplicado
- `403 Forbidden` - Rol insuficiente

**Request Body:**
```json
{
  "username": "nuevo_usuario",
  "password": "SecurePass123!",
  "nombre": "Juan García",
  "email": "juan@universidad.edu",
  "rol": "SECRETARIO_ACADEMICO"
}
```

**Validaciones Realizadas:**
- `username` (required): 3-50 caracteres, único en BD
- `password` (required): 8+ caracteres, complejidad validada
- `nombre` (required): 2-100 caracteres
- `email` (required): formato válido de email
- `rol` (required): valor de enumeración válida

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "usuarioId": 5,
    "username": "nuevo_usuario",
    "nombre": "Juan García",
    "email": "juan@universidad.edu",
    "rol": "SECRETARIO_ACADEMICO"
  },
  "message": "Usuario registrado exitosamente",
  "timestamp": "2024-11-20T10:31:00.000Z"
}
```

**Error Response (400 - Usuario duplicado):**
```json
{
  "success": false,
  "message": "Validación fallida",
  "errors": ["El username 'nuevo_usuario' ya existe"],
  "timestamp": "2024-11-20T10:31:00.000Z"
}
```

**Roles Disponibles:**
- `COORDINADOR_TUTORIAS` - Acceso total al sistema
- `SECRETARIO_ACADEMICO` - Acceso a reportes y datos académicos
- `ADMIN` - Acceso a mantenimiento y limpieza

---

### 1.3 POST /auth/refresh
**Descripción:** Renueva el token de acceso usando el refresh token
**Protección:** Pública (valida usando refresh token)
**HTTP Status:**
- `200 OK` - Token renovado
- `401 Unauthorized` - Refresh token inválido/expirado
- `400 Bad Request` - Token no proporcionado

**Request (Opción 1 - Desde Cookies):**
Automático si el refresh token está en cookies

**Request (Opción 2 - Body):**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "message": "Token renovado correctamente"
  },
  "message": "Token de acceso renovado correctamente",
  "timestamp": "2024-11-20T10:35:00.000Z"
}
```

**Nuevas Cookies Emitidas:**
- `tutorias_access_token` (actualizado)
- `tutorias_refresh_token` (puede ser renovado)

**Error Response (401):**
```json
{
  "success": false,
  "message": "Token inválido o expirado",
  "errors": ["El refresh token ha expirado"],
  "timestamp": "2024-11-20T10:35:00.000Z"
}
```

**Notas Internas:**
- Tiempo de expiración del access token: 15 minutos
- Tiempo de expiración del refresh token: 7 días
- El sistema mantiene una tabla de refresh tokens revocados

---

### 1.4 POST /auth/logout
**Descripción:** Revoca la sesión actual y limpia las cookies
**Protección:** Requiere autenticación
**HTTP Status:**
- `200 OK` - Logout exitoso
- `401 Unauthorized` - No autenticado

**Request (Opción 1 - Sin body):**
Las cookies se envían automáticamente en headers

**Request (Opción 2 - Con body):**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": null,
  "message": "Sesión cerrada correctamente",
  "timestamp": "2024-11-20T10:40:00.000Z"
}
```

**Cookies Eliminadas:**
- `tutorias_access_token` (eliminada)
- `tutorias_refresh_token` (eliminada)

**Notas Internas:**
- El refresh token se marca como revocado en BD
- El usuario puede volver a hacer login con credenciales

---

### 1.5 GET /auth/me
**Descripción:** Obtiene información del usuario autenticado actual
**Protección:** Requiere autenticación válida
**HTTP Status:**
- `200 OK` - Usuario encontrado
- `401 Unauthorized` - Token inválido/expirado

**Query Parameters:** Ninguno

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "usuarioId": 1,
    "username": "coordinador",
    "nombre": "María Coordinadora",
    "email": "maria@universidad.edu",
    "roles": ["COORDINADOR_TUTORIAS"],
    "activo": true,
    "ultimoLogin": "2024-11-20T09:15:00.000Z"
  },
  "message": "Información obtenida correctamente",
  "timestamp": "2024-11-20T10:45:00.000Z"
}
```

**Error Response (401):**
```json
{
  "success": false,
  "message": "No autenticado",
  "errors": ["Token expirado o inválido"],
  "timestamp": "2024-11-20T10:45:00.000Z"
}
```

---

## 👥 MÓDULO 2: GESTIÓN DE ALUMNOS

### Controlador: `AlumnoController`
**Base Path:** `/api/alumnos`
**Protección:** Requiere autenticación
**Métodos HTTP Soportados:** GET, POST, PUT, PATCH, DELETE

---

### 2.1 GET /api/alumnos
**Descripción:** Lista alumnos con filtros opcionales (paginado)
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Listado exitoso
- `400 Bad Request` - Parámetros inválidos
- `404 Not Found` - Semestre activo no existe

**Query Parameters:**
```
GET /api/alumnos?page=1&limit=20&estado=ACTIVO&carrera=ISC&semestreId=5&soloActivo=false
```

| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `page` | integer | 1 | Número de página (1-based) |
| `limit` | integer | 20 | Registros por página |
| `estado` | enum | null | ACTIVO, INACTIVO (opcional) |
| `carrera` | string | null | Código de carrera (opcional) |
| `semestreId` | integer | null | ID del semestre; si no se especifica usa semestre activo |
| `soloActivo` | boolean | false | Si es true, fuerza estado=ACTIVO |

**Comportamiento Especial:**
- Si `semestreId` es null, busca automáticamente el semestre activo
- Si no hay semestre activo, lista alumnos sin filtro de semestre
- Si `soloActivo=true`, fuerza el filtro a `estado=ACTIVO`

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "matricula": "202401001",
        "nombre": "Carlos López",
        "carrera": "ISC",
        "semestre": 3,
        "estado": "ACTIVO",
        "tutorId": 5,
        "tutorNombre": "Dr. Pérez",
        "semestreId": 2,
        "semestreCodigo": "2024-03"
      },
      {
        "id": 2,
        "matricula": "202401002",
        "nombre": "Ana García",
        "carrera": "ADM",
        "semestre": 1,
        "estado": "ACTIVO",
        "tutorId": 3,
        "tutorNombre": "Mtra. Rodríguez",
        "semestreId": 2,
        "semestreCodigo": "2024-03"
      }
    ],
    "page": 1,
    "limit": 20,
    "totalElements": 145,
    "totalPages": 8
  },
  "message": "Alumnos del semestre 2024-03",
  "timestamp": "2024-11-20T11:00:00.000Z"
}
```

**Error Response (404 - Sin semestre activo):**
```json
{
  "success": false,
  "message": "No hay semestre activo configurado en el sistema",
  "timestamp": "2024-11-20T11:00:00.000Z"
}
```

**Casos de Uso Comunes:**
1. **Listar activos del semestre actual:**
   ```
   GET /api/alumnos?page=1&limit=50&soloActivo=true
   ```

2. **Listar por carrera específica:**
   ```
   GET /api/alumnos?page=1&limit=20&carrera=ISC&estado=ACTIVO
   ```

3. **Listar de un semestre específico:**
   ```
   GET /api/alumnos?page=1&limit=20&semestreId=5
   ```

---

### 2.2 GET /api/alumnos/semestre-actual
**Descripción:** Obtiene solo alumnos ACTIVOS del semestre actual (alias simplificado)
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Listado exitoso
- `400 Bad Request` - No hay semestre activo

**Query Parameters:**
```
GET /api/alumnos/semestre-actual?page=1&limit=20&carrera=ISC
```

| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `page` | integer | 1 | Número de página |
| `limit` | integer | 20 | Registros por página |
| `carrera` | string | null | Filtro por carrera (opcional) |

**Comportamiento Especial:**
- SIEMPRE filtra por `estado=ACTIVO`
- SIEMPRE usa el semestre activo (no requiere semestreId)
- Retorna error si no hay semestre activo configurado

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "matricula": "202401001",
        "nombre": "Carlos López",
        "carrera": "ISC",
        "semestre": 3,
        "estado": "ACTIVO",
        "tutorId": 5,
        "tutorNombre": "Dr. Pérez",
        "semestreId": 2,
        "semestreCodigo": "2024-03"
      }
    ],
    "page": 1,
    "limit": 20,
    "totalElements": 87,
    "totalPages": 5
  },
  "message": "Alumnos activos del semestre 2024-03 - Total: 87",
  "timestamp": "2024-11-20T11:05:00.000Z"
}
```

**Notas de Negocio:**
- Endpoint recomendado para dashboard de estudiantes activos
- Se usa en asignación de tutores
- Filtering automático por semestre activo (no requiere configuración del frontend)

---

### 2.3 GET /api/alumnos/{id}
**Descripción:** Obtiene un alumno específico por ID
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Alumno encontrado
- `404 Not Found` - Alumno no existe

**Path Parameters:**
```
GET /api/alumnos/15
```

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `id` | long | ID único del alumno |

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 15,
    "matricula": "202401015",
    "nombre": "Patricia Martínez",
    "carrera": "ADM",
    "semestre": 2,
    "estado": "ACTIVO",
    "tutorId": 7,
    "tutorNombre": "Lic. Fernández",
    "semestreId": 2,
    "semestreCodigo": "2024-03"
  },
  "message": "Alumno obtenido correctamente",
  "timestamp": "2024-11-20T11:10:00.000Z"
}
```

**Error Response (404):**
```json
{
  "success": false,
  "message": "Alumno no encontrado",
  "errors": ["No existe alumno con ID 15"],
  "timestamp": "2024-11-20T11:10:00.000Z"
}
```

---

### 2.4 POST /api/alumnos
**Descripción:** Crea un nuevo alumno
**Protección:** Autenticado
**HTTP Status:**
- `201 Created` - Alumno creado
- `400 Bad Request` - Datos inválidos
- `409 Conflict` - Matrícula ya existe

**Request Body:**
```json
{
  "matricula": "202401050",
  "nombre": "Roberto Sánchez",
  "carrera": "ISC",
  "semestre": 1,
  "estado": "ACTIVO",
  "semestreId": 2
}
```

**Validaciones Realizadas:**
- `matricula` (required): 6-20 caracteres, único en BD
- `nombre` (required): 2-100 caracteres
- `carrera` (required): 2-50 caracteres
- `semestre` (required): 1-8
- `estado` (required): ACTIVO | INACTIVO
- `semestreId` (required): ID de semestre válido existente

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 150,
    "matricula": "202401050",
    "nombre": "Roberto Sánchez",
    "carrera": "ISC",
    "semestre": 1,
    "estado": "ACTIVO",
    "tutorId": null,
    "tutorNombre": null,
    "semestreId": 2,
    "semestreCodigo": "2024-03"
  },
  "message": "Alumno creado correctamente",
  "timestamp": "2024-11-20T11:15:00.000Z"
}
```

**Error Response (409 - Matrícula duplicada):**
```json
{
  "success": false,
  "message": "Validación fallida",
  "errors": ["Ya existe un alumno con matrícula 202401050"],
  "timestamp": "2024-11-20T11:15:00.000Z"
}
```

**Notas Internas:**
- No se asigna tutor automáticamente (tutorId será null)
- Se requiere ejecutar asignación de tutores después
- El alumno creado hereda el semestre especificado

---

### 2.5 PUT /api/alumnos/{id}
**Descripción:** Reemplaza completamente los datos de un alumno (PUT full)
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Alumno actualizado
- `400 Bad Request` - Datos inválidos
- `404 Not Found` - Alumno no existe

**Path Parameters:**
```
PUT /api/alumnos/15
```

**Request Body (todos los campos requeridos):**
```json
{
  "matricula": "202401015",
  "nombre": "Patricia Elena Martínez",
  "carrera": "ADM",
  "semestre": 3,
  "estado": "ACTIVO",
  "semestreId": 2
}
```

**Validaciones:**
- Mismas que POST /api/alumnos
- `matricula`: único EXCEPTO si es del mismo alumno

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 15,
    "matricula": "202401015",
    "nombre": "Patricia Elena Martínez",
    "carrera": "ADM",
    "semestre": 3,
    "estado": "ACTIVO",
    "tutorId": 7,
    "tutorNombre": "Lic. Fernández",
    "semestreId": 2,
    "semestreCodigo": "2024-03"
  },
  "message": "Alumno actualizado correctamente",
  "timestamp": "2024-11-20T11:20:00.000Z"
}
```

---

### 2.6 PATCH /api/alumnos/{id}
**Descripción:** Actualiza parcialmente los datos de un alumno (PATCH)
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Alumno actualizado
- `400 Bad Request` - Datos inválidos
- `404 Not Found` - Alumno no existe

**Path Parameters:**
```
PATCH /api/alumnos/15
```

**Request Body (campos opcionales):**
```json
{
  "nombre": "Patricia Elena Martínez García",
  "semestre": 4
}
```

**Comportamiento:**
- Solo actualiza los campos proporcionados
- Los campos omitidos mantienen su valor anterior
- Los campos permitidos: nombre, carrera, semestre, estado, semestreId

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 15,
    "matricula": "202401015",
    "nombre": "Patricia Elena Martínez García",
    "carrera": "ADM",
    "semestre": 4,
    "estado": "ACTIVO",
    "tutorId": 7,
    "tutorNombre": "Lic. Fernández",
    "semestreId": 2,
    "semestreCodigo": "2024-03"
  },
  "message": "Alumno actualizado correctamente",
  "timestamp": "2024-11-20T11:25:00.000Z"
}
```

---

### 2.7 DELETE /api/alumnos/{id}
**Descripción:** Elimina un alumno del sistema
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Alumno eliminado
- `404 Not Found` - Alumno no existe
- `409 Conflict` - No se puede eliminar (tiene asignaciones)

**Path Parameters:**
```
DELETE /api/alumnos/15
```

**Validaciones Previas:**
- Verificar que el alumno no tenga asignaciones activas
- Verificar que no esté en proceso de inactivación
- Permitir eliminación solo si está en estado limpio

**Response (200 OK):**
```json
{
  "success": true,
  "data": null,
  "message": "Alumno eliminado correctamente",
  "timestamp": "2024-11-20T11:30:00.000Z"
}
```

**Error Response (409 - Tiene asignaciones):**
```json
{
  "success": false,
  "message": "No se puede eliminar el alumno",
  "errors": ["El alumno tiene asignaciones activas. Primero debe desvincularlos."],
  "timestamp": "2024-11-20T11:30:00.000Z"
}
```

---

## 🔍 MÓDULO 2B: BÚSQUEDA AVANZADA DE ALUMNOS

### Controlador: `AlumnoSearchController`
**Base Path:** `/api/alumnos`
**Protección:** Requiere autenticación

---

### 2B.1 GET /api/alumnos/search
**Descripción:** Búsqueda avanzada con ranking y filtros
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Búsqueda exitosa
- `400 Bad Request` - Query inválido

**Query Parameters:**
```
GET /api/alumnos/search?q=carlos&carrera=ISC&estado=ACTIVO&limit=10
```

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `q` | string | Término de búsqueda (nombre, matrícula) |
| `carrera` | string | Filtro por carrera (opcional) |
| `estado` | enum | ACTIVO, INACTIVO (opcional) |
| `limit` | integer | Máx resultados (default: 10) |

**Algoritmo de Ranking:**
1. **Coincidencia exacta de matrícula:** score=1000
2. **Matrícula comienza con:** score=500
3. **Nombre comienza con:** score=300
4. **Nombre contiene:** score=100
5. **Matrícula contiene:** score=50

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "matricula": "202401001",
      "nombre": "Carlos López",
      "carrera": "ISC",
      "semestre": 3,
      "estado": "ACTIVO",
      "tutorId": 5,
      "tutorNombre": "Dr. Pérez",
      "score": 300
    },
    {
      "id": 45,
      "matricula": "202401045",
      "nombre": "Carlos Alberto García",
      "carrera": "ISC",
      "semestre": 2,
      "estado": "ACTIVO",
      "tutorId": 3,
      "tutorNombre": "Mtra. Rodríguez",
      "score": 150
    }
  ],
  "message": "Búsqueda completada",
  "timestamp": "2024-11-20T11:35:00.000Z"
}
```

---

### 2B.2 GET /api/alumnos/autocomplete
**Descripción:** Autocomplete para búsqueda (máx 10 resultados)
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Sugerencias obtenidas
- `400 Bad Request` - Query vacío

**Query Parameters:**
```
GET /api/alumnos/autocomplete?q=car&estado=ACTIVO
```

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `q` | string | Término de búsqueda (mín 2 caracteres) |
| `estado` | enum | ACTIVO, INACTIVO (opcional) |

**Comportamiento:**
- Retorna máximo 10 resultados
- Realiza búsqueda de prefijo en nombre y matrícula
- Ordena por relevancia (ranking)
- Filtra automáticamente por `estado=ACTIVO` si se especifica

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "matricula": "202401001",
      "nombre": "Carlos López",
      "carrera": "ISC",
      "estado": "ACTIVO"
    },
    {
      "id": 45,
      "matricula": "202401045",
      "nombre": "Carlos García",
      "carrera": "ADM",
      "estado": "ACTIVO"
    }
  ],
  "message": "Sugerencias obtenidas",
  "timestamp": "2024-11-20T11:40:00.000Z"
}
```

---

### 2B.3 GET /api/alumnos/by-matricula/{matricula}
**Descripción:** Búsqueda rápida por número de matrícula exacto
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Alumno encontrado
- `404 Not Found` - Matrícula no existe

**Path Parameters:**
```
GET /api/alumnos/by-matricula/202401001
```

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `matricula` | string | Número de matrícula exacto |

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "matricula": "202401001",
    "nombre": "Carlos López",
    "carrera": "ISC",
    "semestre": 3,
    "estado": "ACTIVO",
    "tutorId": 5,
    "tutorNombre": "Dr. Pérez",
    "semestreId": 2,
    "semestreCodigo": "2024-03"
  },
  "message": "Alumno encontrado",
  "timestamp": "2024-11-20T11:45:00.000Z"
}
```

---

## 👨‍🏫 MÓDULO 3: GESTIÓN DE TUTORES

### Controlador: `TutorController`
**Base Path:** `/api/tutores`
**Protección:** Requiere autenticación

---

### 3.1 GET /api/tutores
**Descripción:** Lista tutores con filtros opcionales
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Listado exitoso

**Query Parameters:**
```
GET /api/tutores?carrera=ISC&disponibles=true&activos=true
```

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `carrera` | string | Filtro por carrera (opcional) |
| `disponibles` | boolean | Solo tutores con capacidad disponible (opcional) |
| `sobrecargados` | boolean | Solo tutores sobrecargados (opcional) |
| `activos` | boolean | Solo tutores activos (opcional) |

**Estructura de Respuesta - Entidad Tutor:**
```json
{
  "id": 5,
  "nombre": "Dr. Francisco Pérez",
  "carrera": "ISC",
  "capacidadMax": 20,
  "cargaActual": 18,
  "activo": true,
  "areaAtencion": "Programación",
  "letraEdificio": "B",
  "disponible": true,
  "sobrecargado": false
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 5,
      "nombre": "Dr. Francisco Pérez",
      "carrera": "ISC",
      "capacidadMax": 20,
      "cargaActual": 18,
      "activo": true,
      "areaAtencion": "Programación",
      "letraEdificio": "B",
      "disponible": true,
      "sobrecargado": false
    },
    {
      "id": 3,
      "nombre": "Mtra. Rosa Rodríguez",
      "carrera": "ISC",
      "capacidadMax": 15,
      "cargaActual": 12,
      "activo": true,
      "areaAtencion": "Bases de Datos",
      "letraEdificio": "A",
      "disponible": true,
      "sobrecargado": false
    }
  ],
  "message": "Tutores obtenidos correctamente",
  "timestamp": "2024-11-20T12:00:00.000Z"
}
```

---

### 3.2 GET /api/tutores/{id}
**Descripción:** Obtiene un tutor específico por ID
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Tutor encontrado
- `404 Not Found` - Tutor no existe

**Path Parameters:**
```
GET /api/tutores/5
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 5,
    "nombre": "Dr. Francisco Pérez",
    "carrera": "ISC",
    "capacidadMax": 20,
    "cargaActual": 18,
    "activo": true,
    "areaAtencion": "Programación",
    "letraEdificio": "B",
    "disponible": true,
    "sobrecargado": false
  },
  "message": "Tutor obtenido correctamente",
  "timestamp": "2024-11-20T12:05:00.000Z"
}
```

---

### 3.3 GET /api/tutores/{tutorId}/alumnos
**Descripción:** Obtiene alumnos asignados a un tutor
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Listado exitoso
- `404 Not Found` - Tutor no existe

**Path Parameters:**
```
GET /api/tutores/5/alumnos?semestreAcademico=2024-03
```

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `semestreAcademico` | string | Código del semestre (opcional, usa semestre actual si omite) |

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "tutorId": 5,
    "tutorNombre": "Dr. Francisco Pérez",
    "carrera": "ISC",
    "capacidadMax": 20,
    "cargaActual": 18,
    "semestreAcademico": "2024-03",
    "alumnos": [
      {
        "id": 1,
        "matricula": "202401001",
        "nombre": "Carlos López",
        "carrera": "ISC",
        "semestre": 3,
        "estado": "ACTIVO"
      },
      {
        "id": 5,
        "matricula": "202401005",
        "nombre": "David Martínez",
        "carrera": "ISC",
        "semestre": 1,
        "estado": "ACTIVO"
      }
    ]
  },
  "message": "Alumnos obtenidos correctamente",
  "timestamp": "2024-11-20T12:10:00.000Z"
}
```

---

### 3.4 POST /api/tutores
**Descripción:** Crea un nuevo tutor
**Protección:** Autenticado
**HTTP Status:**
- `201 Created` - Tutor creado
- `400 Bad Request` - Datos inválidos

**Request Body:**
```json
{
  "nombre": "Dr. Juan Rodríguez",
  "carrera": "ADM",
  "capacidadMax": 25,
  "areaAtencion": "Finanzas",
  "letraEdificio": "C"
}
```

**Validaciones:**
- `nombre` (required): 3-100 caracteres
- `carrera` (required): 2-50 caracteres
- `capacidadMax` (required): 1-100
- `areaAtencion` (optional): 2-100 caracteres
- `letraEdificio` (optional): 1 letra

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 25,
    "nombre": "Dr. Juan Rodríguez",
    "carrera": "ADM",
    "capacidadMax": 25,
    "cargaActual": 0,
    "activo": true,
    "areaAtencion": "Finanzas",
    "letraEdificio": "C",
    "disponible": true,
    "sobrecargado": false
  },
  "message": "Tutor creado correctamente",
  "timestamp": "2024-11-20T12:15:00.000Z"
}
```

---

### 3.5 PUT /api/tutores/{id}
**Descripción:** Actualiza los datos de un tutor
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Tutor actualizado
- `404 Not Found` - Tutor no existe

**Path Parameters:**
```
PUT /api/tutores/25
```

**Request Body:**
```json
{
  "nombre": "Dr. Juan Carlos Rodríguez",
  "capacidadMax": 30,
  "areaAtencion": "Finanzas y Contabilidad",
  "activo": true
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 25,
    "nombre": "Dr. Juan Carlos Rodríguez",
    "carrera": "ADM",
    "capacidadMax": 30,
    "cargaActual": 5,
    "activo": true,
    "areaAtencion": "Finanzas y Contabilidad",
    "letraEdificio": "C",
    "disponible": true,
    "sobrecargado": false
  },
  "message": "Tutor actualizado correctamente",
  "timestamp": "2024-11-20T12:20:00.000Z"
}
```

---

### 3.6 DELETE /api/tutores/{id}
**Descripción:** Elimina un tutor
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Tutor eliminado
- `404 Not Found` - Tutor no existe
- `409 Conflict` - Tiene alumnos asignados

**Path Parameters:**
```
DELETE /api/tutores/25
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": null,
  "message": "Tutor eliminado correctamente",
  "timestamp": "2024-11-20T12:25:00.000Z"
}
```

---

## 🔍 MÓDULO 3B: BÚSQUEDA AVANZADA DE TUTORES

### Controlador: `TutorSearchController`
**Base Path:** `/api/tutores`

---

### 3B.1 GET /api/tutores/search
**Descripción:** Búsqueda avanzada de tutores con ranking
**Protección:** Autenticado

**Query Parameters:**
```
GET /api/tutores/search?q=pérez&carrera=ISC&limit=10
```

**Algoritmo de Ranking para Tutores:**
1. **Nombre comienza con:** score=300
2. **Nombre contiene:** score=150
3. **Carrera contiene:** score=50

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 5,
      "nombre": "Dr. Francisco Pérez",
      "carrera": "ISC",
      "capacidadMax": 20,
      "cargaActual": 18,
      "activo": true,
      "score": 300
    }
  ],
  "message": "Búsqueda completada",
  "timestamp": "2024-11-20T12:30:00.000Z"
}
```

---

### 3B.2 GET /api/tutores/autocomplete
**Descripción:** Autocomplete para búsqueda de tutores (máx 10)
**Protección:** Autenticado

**Query Parameters:**
```
GET /api/tutores/autocomplete?q=fran&carrera=ISC
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 5,
      "nombre": "Dr. Francisco Pérez",
      "carrera": "ISC",
      "capacidadMax": 20,
      "cargaActual": 18,
      "activo": true
    }
  ],
  "message": "Sugerencias obtenidas",
  "timestamp": "2024-11-20T12:35:00.000Z"
}
```

---

## 📚 MÓDULO 4: GESTIÓN DE SEMESTRES

### Controlador: `SemestreController`
**Base Path:** `/api/semestres`
**Protección:** Requiere autenticación

---

### 4.1 GET /api/semestres
**Descripción:** Lista todos los semestres ordenados por fecha (descendente)
**Protección:** Autenticado

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 3,
      "codigo": "2024-04",
      "nombre": "Semestre Agosto-Diciembre 2024",
      "fechaInicio": "2024-08-15",
      "fechaFin": "2024-12-15",
      "activo": true,
      "totalAlumnos": 287,
      "totalTutores": 15
    },
    {
      "id": 2,
      "codigo": "2024-03",
      "nombre": "Semestre Marzo-Julio 2024",
      "fechaInicio": "2024-03-01",
      "fechaFin": "2024-07-31",
      "activo": false,
      "totalAlumnos": 265,
      "totalTutores": 15
    }
  ],
  "message": "Semestres obtenidos correctamente",
  "timestamp": "2024-11-20T12:40:00.000Z"
}
```

---

### 4.2 GET /api/semestres/ultimos
**Descripción:** Obtiene los últimos N semestres
**Protección:** Autenticado

**Query Parameters:**
```
GET /api/semestres/ultimos?cantidad=5
```

| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `cantidad` | integer | 5 | Número de semestres a retornar |

**Response (200 OK):** Similar a GET /api/semestres

---

### 4.3 GET /api/semestres/activo
**Descripción:** Obtiene el semestre activo actual
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Semestre encontrado
- `404 Not Found` - No hay semestre activo

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 3,
    "codigo": "2024-04",
    "nombre": "Semestre Agosto-Diciembre 2024",
    "fechaInicio": "2024-08-15",
    "fechaFin": "2024-12-15",
    "activo": true,
    "totalAlumnos": 287,
    "totalTutores": 15
  },
  "message": "Semestre activo encontrado",
  "timestamp": "2024-11-20T12:45:00.000Z"
}
```

---

### 4.4 GET /api/semestres/{id}
**Descripción:** Obtiene un semestre específico por ID
**Protección:** Autenticado

**Path Parameters:**
```
GET /api/semestres/3
```

**Response (200 OK):** Similar a GET /api/semestres/activo

---

### 4.5 GET /api/semestres/codigo/{codigo}
**Descripción:** Obtiene un semestre por su código
**Protección:** Autenticado

**Path Parameters:**
```
GET /api/semestres/codigo/2024-04
```

---

### 4.6 GET /api/semestres/existe/codigo/{codigo}
**Descripción:** Verifica si un código de semestre existe
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Semestre existe
- `404 Not Found` - No existe

**Path Parameters:**
```
GET /api/semestres/existe/codigo/2024-04
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": true,
  "message": "El código 2024-04 existe",
  "timestamp": "2024-11-20T12:50:00.000Z"
}
```

---

### 4.7 GET /api/semestres/{id}/estadisticas
**Descripción:** Obtiene estadísticas detalladas del semestre
**Protección:** Autenticado

**Path Parameters:**
```
GET /api/semestres/3/estadisticas
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "semestreId": 3,
    "codigo": "2024-04",
    "nombre": "Semestre Agosto-Diciembre 2024",
    "activo": true,
    "totalAlumnos": 287,
    "alumnosActivos": 280,
    "alumnosInactivos": 7,
    "totalTutores": 15,
    "tutoresActivos": 14,
    "tutoresInactivos": 1,
    "totalAsignaciones": 280,
    "promedioCargaPorTutor": 18.67,
    "tutorMasCargado": {
      "id": 5,
      "nombre": "Dr. Francisco Pérez",
      "carga": 22
    },
    "tutorMenosCargado": {
      "id": 12,
      "nombre": "Lic. Ana García",
      "carga": 14
    },
    "distribucionPorCarrera": {
      "ISC": 120,
      "ADM": 85,
      "IEM": 82
    }
  },
  "message": "Estadísticas obtenidas correctamente",
  "timestamp": "2024-11-20T12:55:00.000Z"
}
```

---

### 4.8 POST /api/semestres
**Descripción:** Crea un nuevo semestre
**Protección:** Autenticado

**Request Body:**
```json
{
  "codigo": "2025-01",
  "nombre": "Semestre Enero-Junio 2025",
  "fechaInicio": "2025-01-15",
  "fechaFin": "2025-06-30"
}
```

**Validaciones:**
- `codigo` (required): 5-20 caracteres, único, formato específico
- `nombre` (required): 5-200 caracteres
- `fechaInicio` (required): fecha válida anterior a fechaFin
- `fechaFin` (required): fecha válida posterior a fechaInicio

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 5,
    "codigo": "2025-01",
    "nombre": "Semestre Enero-Junio 2025",
    "fechaInicio": "2025-01-15",
    "fechaFin": "2025-06-30",
    "activo": false,
    "totalAlumnos": 0,
    "totalTutores": 0
  },
  "message": "Semestre creado correctamente",
  "timestamp": "2024-11-20T13:00:00.000Z"
}
```

---

### 4.9 PUT /api/semestres/{id}
**Descripción:** Actualiza un semestre existente
**Protección:** Autenticado

**Path Parameters:**
```
PUT /api/semestres/5
```

**Request Body:**
```json
{
  "nombre": "Semestre Enero-Junio 2025 (Revisado)",
  "fechaFin": "2025-07-15"
}
```

---

### 4.10 POST /api/semestres/{id}/activar
**Descripción:** Activa un semestre (desactiva los demás)
**Protección:** Autenticado

**Path Parameters:**
```
POST /api/semestres/5/activar
```

**Comportamiento Especial:**
- Busca el semestre activo actual
- Lo desactiva
- Activa el semestre especificado
- Solo puede haber un semestre activo a la vez

---

### 4.11 POST /api/semestres/{id}/desactivar
**Descripción:** Desactiva un semestre
**Protección:** Autenticado

---

### 4.12 DELETE /api/semestres/{id}
**Descripción:** Elimina un semestre
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Eliminado
- `409 Conflict` - Tiene asignaciones

**Validaciones Previas:**
- Verifica que no sea el semestre activo
- Verifica que no tenga asignaciones
- Verifica que no tenga alumnos

---

## 📋 MÓDULO 5: ASIGNACIONES (CORE BUSINESS)

### Controlador: `AsignacionController`
**Base Path:** `/api/asignaciones`
**Protección:** Requiere autenticación

---

### 5.1 POST /api/asignaciones/iniciar
**Descripción:** Inicia un proceso completo de asignación (Excel upload)
**Protección:** Autenticado
**HTTP Status:**
- `202 Accepted` - Proceso iniciado en background
- `200 OK` - Proceso completado rápidamente

**Request (Multipart Form-Data):**
```
POST /api/asignaciones/iniciar

Form Data:
- archivo: [binary file] (Excel .xlsx)
- semestreAcademico: "2024-04"
- usuario: "coordinador"
```

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `archivo` | file | Archivo Excel .xlsx |
| `semestreAcademico` | string | Código del semestre (ej: "2024-04") |
| `usuario` | string | Username del coordinador ejecutando |

**Validaciones:**
- Archivo debe ser .xlsx (Excel)
- Tamaño máximo: 10MB
- semestreAcademico debe existir
- usuario debe existir en BD

**Response (202 Accepted - Background):**
```json
{
  "success": true,
  "data": {
    "mensaje": "Proceso iniciado en segundo plano. Consulte el estado posteriormente.",
    "timestamp": "2024-11-20T13:05:00.000Z"
  },
  "message": "Aceptado",
  "timestamp": "2024-11-20T13:05:00.000Z"
}
```

**Response (200 OK - Quick Completion):**
```json
{
  "success": true,
  "data": {
    "procesoId": 42,
    "estado": "INICIADO",
    "mensaje": "Proceso de asignación iniciado correctamente",
    "timestamp": "2024-11-20T13:05:00.000Z"
  },
  "message": "Proceso iniciado",
  "timestamp": "2024-11-20T13:05:00.000Z"
}
```

**Flujo Interno:**
1. Recibe archivo Excel
2. Crea registro ProcesoAsignacion en BD
3. Lanza OrchestrationService en background
4. Retorna ID del proceso para tracking
5. El proceso ejecuta en paralelo:
   - Validación del Excel
   - Limpieza y normalización
   - Ordenamiento por semestre
   - Ejecución de asignación
   - Generación de alertas/errores

---

### 5.2 POST /api/asignaciones/validar-excel
**Descripción:** Valida y ordena un Excel SIN ejecutar la asignación
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Validación exitosa
- `400 Bad Request` - Excel inválido

**Request (Multipart Form-Data):**
```
POST /api/asignaciones/validar-excel

Form Data:
- archivo: [binary file]
- semestreId: 3
```

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `archivo` | file | Archivo Excel .xlsx |
| `semestreId` | long | ID del semestre (no código) |

**Estructura Esperada del Excel:**
```
Columns:
- Matrícula (A)
- Nombre (B)
- Carrera (C)
- Semestre (D)
- Tutor Asignado (E) - opcional

Rows:
- Fila 1: Headers
- Fila 2+: Datos de alumnos
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "status": "OK",
    "totalValidas": 150,
    "totalConErrores": 5,
    "alumnosOrdenados": [
      {
        "matricula": "202401001",
        "nombre": "Carlos López",
        "carrera": "ISC",
        "semestre": 3,
        "tutorAsignado": "Dr. Pérez",
        "semestreId": 3
      },
      {
        "matricula": "202401002",
        "nombre": "Ana García",
        "carrera": "ADM",
        "semestre": 1,
        "tutorAsignado": null,
        "semestreId": 3
      }
    ],
    "erroresDetectados": [
      {
        "filaExcel": 5,
        "campo": "matricula",
        "valor": "",
        "descripcion": "Matrícula vacía"
      },
      {
        "filaExcel": 8,
        "campo": "carrera",
        "valor": "INVALID",
        "descripcion": "Carrera no válida"
      }
    ],
    "message": "Validación completada con éxito"
  },
  "message": "Excel validado correctamente",
  "timestamp": "2024-11-20T13:10:00.000Z"
}
```

**Validaciones Realizadas:**
1. **Estructura:** Headers obligatorios
2. **Matrícula:** No puede estar vacía, debe ser única en el Excel
3. **Nombre:** No puede estar vacío
4. **Carrera:** Debe existir en BD o ser reconocida
5. **Semestre:** 1-8, válido
6. **Tutor (opcional):** Si se proporciona, debe existir en BD

**Error Response (400 - Errores en Excel):**
```json
{
  "success": false,
  "message": "Validación fallida",
  "errors": [
    "Fila 5: Matrícula vacía",
    "Fila 8: Carrera 'INVALID' no reconocida",
    "Fila 12: Alumno 202401050 ya existe en fila 3"
  ],
  "timestamp": "2024-11-20T13:10:00.000Z"
}
```

---

### 5.3 POST /api/asignaciones/ejecutar
**Descripción:** Ejecuta la asignación de tutores (después de validar)
**Protección:** Autenticado
**HTTP Status:**
- `202 Accepted` - Ejecución en background
- `200 OK` - Completada rápidamente
- `400 Bad Request` - Datos inválidos

**Request Body:**
```json
{
  "semestreId": 3,
  "alumnosAAsignar": [
    {
      "matricula": "202401001",
      "nombre": "Carlos López",
      "carrera": "ISC",
      "semestre": 3,
      "tutorAsignado": null
    },
    {
      "matricula": "202401002",
      "nombre": "Ana García",
      "carrera": "ADM",
      "semestre": 1,
      "tutorAsignado": null
    }
  ],
  "usuario": "coordinador"
}
```

**Algoritmo de Asignación:**
1. Valida que cada alumno no esté ya asignado
2. Busca tutores disponibles para la carrera del alumno
3. Asigna al tutor con menor carga actual
4. Incrementa cargaActual del tutor
5. Verifica sobrecarga (carga > capacidadMax)
6. Genera alertas si hay problemas

**Response (202 Accepted):**
```json
{
  "success": true,
  "data": {
    "mensaje": "Ejecución iniciada en segundo plano",
    "timestamp": "2024-11-20T13:15:00.000Z"
  },
  "message": "Proceso ejecutándose",
  "timestamp": "2024-11-20T13:15:00.000Z"
}
```

---

### 5.4 GET /api/asignaciones/proceso/{procesoId}
**Descripción:** Obtiene el estado actual de un proceso de asignación
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Proceso encontrado
- `404 Not Found` - Proceso no existe

**Path Parameters:**
```
GET /api/asignaciones/proceso/42
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "procesoId": 42,
    "estado": "COMPLETADO",
    "progreso": {
      "porcentaje": 100,
      "alumnosProcesados": 155,
      "alumnosAsignados": 150,
      "errores": 5,
      "warnings": 12
    },
    "fechaInicio": "2024-11-20T13:05:00.000Z",
    "fechaFin": "2024-11-20T13:07:45.000Z",
    "tiempoTranscurridoMs": 165000
  },
  "message": "Estado del proceso",
  "timestamp": "2024-11-20T13:20:00.000Z"
}
```

**Estados Posibles del Proceso:**
- `INICIADO` - Acaba de empezar
- `VALIDANDO` - Validando Excel
- `ORDENANDO` - Ordenando datos
- `ASIGNANDO` - Asignando tutores
- `COMPLETADO` - Finalizado exitosamente
- `FALLIDO` - Error durante ejecución
- `CANCELADO` - Cancelado por usuario

---

### 5.5 GET /api/asignaciones/proceso/{procesoId}/alertas
**Descripción:** Obtiene alertas generadas durante un proceso
**Protección:** Autenticado

**Path Parameters & Query:**
```
GET /api/asignaciones/proceso/42/alertas?severidad=ERROR&resuelta=false
```

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `severidad` | enum | INFO, WARNING, ERROR (opcional) |
| `resuelta` | boolean | true, false (opcional) |

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "proceso_id": 42,
    "total_alertas": 17,
    "alertas": [
      {
        "id": 101,
        "tipo": "TUTOR_SOBRECARGADO",
        "severidad": "WARNING",
        "descripcion": "Tutor Dr. Pérez (ID: 5) tiene 22 alumnos, capacidad máxima: 20",
        "resuelta": false,
        "fechaCreacion": "2024-11-20T13:06:30.000Z"
      },
      {
        "id": 102,
        "tipo": "ALUMNO_NO_ASIGNADO",
        "severidad": "ERROR",
        "descripcion": "Alumno 202401150 no pudo ser asignado (no hay tutores disponibles en carrera)",
        "resuelta": false,
        "fechaCreacion": "2024-11-20T13:06:35.000Z"
      }
    ]
  },
  "message": "Alertas obtenidas",
  "timestamp": "2024-11-20T13:25:00.000Z"
}
```

**Tipos de Alertas:**
- `TUTOR_SOBRECARGADO` - Tutor excedió capacidad
- `ALUMNO_NO_ASIGNADO` - No hay tutor disponible
- `MATRÍCULA_DUPLICADA` - Matrícula repetida en Excel
- `CARRERA_NO_RECONOCIDA` - Carrera del alumno inválida
- `TUTOR_NO_EXISTE` - Tutor especificado no encontrado

---

### 5.6 GET /api/asignaciones/procesos
**Descripción:** Lista todos los procesos de asignación
**Protección:** Autenticado

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 42,
      "estado": "COMPLETADO",
      "archivo_origen": "Alumnos_2024-04.xlsx",
      "usuario_ejecutor": "coordinador",
      "fecha_inicio": "2024-11-20T13:05:00.000Z",
      "fecha_fin": "2024-11-20T13:07:45.000Z",
      "total_procesados": 155,
      "total_asignados": 150,
      "total_errores": 5,
      "total_warnings": 12,
      "tiempo_ms": 165000
    },
    {
      "id": 41,
      "estado": "COMPLETADO",
      "archivo_origen": "Alumnos_2024-03.xlsx",
      "usuario_ejecutor": "coordinador",
      "fecha_inicio": "2024-11-15T10:00:00.000Z",
      "fecha_fin": "2024-11-15T10:03:20.000Z",
      "total_procesados": 142,
      "total_asignados": 140,
      "total_errores": 2,
      "total_warnings": 8,
      "tiempo_ms": 200000
    }
  ],
  "message": "Procesos obtenidos correctamente",
  "timestamp": "2024-11-20T13:30:00.000Z"
}
```

---

### 5.7 POST /api/asignaciones/cambio-tutor
**Descripción:** Cambia manualmente el tutor asignado a un alumno
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Cambio exitoso
- `400 Bad Request` - Datos inválidos
- `404 Not Found` - Alumno o tutor no existe

**Request Body:**
```json
{
  "alumnoId": 15,
  "tutorOrigenId": 5,
  "tutorDestinoId": 7,
  "motivo": "Solicitud del alumno por horarios",
  "usuarioResponsable": "coordinador"
}
```

**Validaciones:**
- alumnoId debe existir
- tutorOrigenId debe ser el tutor actual del alumno
- tutorDestinoId debe existir y tener capacidad disponible
- motivo (required): min 5 caracteres
- usuarioResponsable debe existir

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "alumnoId": 15,
    "alumnoNombre": "Patricia Martínez",
    "tutorOrigenId": 5,
    "tutorOrigenNombre": "Dr. Pérez",
    "tutorDestinoId": 7,
    "tutorDestinoNombre": "Lic. Fernández",
    "motivo": "Solicitud del alumno por horarios",
    "fechaCambio": "2024-11-20T13:35:00.000Z",
    "usuarioResponsable": "coordinador"
  },
  "message": "Reasignación de tutor completada",
  "timestamp": "2024-11-20T13:35:00.000Z"
}
```

**Flujo Interno:**
1. Valida datos
2. Decrementa cargaActual del tutor origen
3. Incrementa cargaActual del tutor destino
4. Actualiza Asignacion.tutorId en BD
5. Registra en LogAuditoria
6. Verifica si tutores quedan sobrecargados
7. Retorna resultado

---

## 🚫 MÓDULO 6: ALUMNOS INACTIVOS

### Controlador: `AlumnoInactivoController`
**Base Path:** `/api/alumnos-inactivos`
**Protección:** Requiere autenticación

---

### 6.1 GET /api/alumnos-inactivos/pendientes
**Descripción:** Lista alumnos inactivos que aún no tienen motivo asignado
**Protección:** Autenticado

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "alumnoId": 45,
      "alumnoNombre": "Jorge Martínez",
      "alumnoMatricula": "202401045",
      "carrera": "ISC",
      "tutorPreservadoId": 5,
      "tutorPreservadoNombre": "Dr. Pérez",
      "motivoInactividad": null,
      "cupoLiberado": false,
      "fechaCreacion": "2024-11-15T10:00:00.000Z"
    }
  ],
  "message": "Alumnos inactivos pendientes",
  "timestamp": "2024-11-20T13:40:00.000Z"
}
```

---

### 6.2 GET /api/alumnos-inactivos
**Descripción:** Lista todos los alumnos inactivos
**Protección:** Autenticado

---

### 6.3 PUT /api/alumnos-inactivos/{alumnoInactivoId}/motivo
**Descripción:** Asigna un motivo de inactividad a un alumno
**Protección:** Autenticado

**Path Parameters:**
```
PUT /api/alumnos-inactivos/1/motivo
```

**Request Body:**
```json
{
  "motivo": "BAJA_TEMPORAL",
  "descripcion": "Alumno se retira temporalmente por razones de salud"
}
```

**Motivos Válidos:**
- `BAJA_TEMPORAL` - Retiro temporal
- `BAJA_DEFINITIVA` - Retiro permanente
- `EGRESADO` - Completó carrera
- `MOVILIDAD` - Estudiante de intercambio
- `CAMBIO_INSTITUCION` - Trasladado a otra universidad
- `OTRO` - Especificar en descripción

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "alumnoId": 45,
    "alumnoNombre": "Jorge Martínez",
    "motivoInactividad": "BAJA_TEMPORAL",
    "descripcion": "Alumno se retira temporalmente por razones de salud",
    "tutorPreservadoId": 5,
    "cupoLiberado": false,
    "fechaResolucion": "2024-11-20T13:45:00.000Z"
  },
  "message": "Motivo asignado correctamente",
  "timestamp": "2024-11-20T13:45:00.000Z"
}
```

---

### 6.4 PATCH /api/alumnos-inactivos/{id}/resolver-motivo
**Descripción:** Resuelve el motivo de inactividad (reactiva alumno)
**Protección:** Autenticado

**Path Parameters:**
```
PATCH /api/alumnos-inactivos/1/resolver-motivo
```

**Request Body:**
```json
{
  "reactivar": true,
  "notas": "Alumno se recuperó de la salud y desea continuar"
}
```

**Comportamiento:**
- Marca inactividad como resuelta
- Opcionalmente reactiva al alumno
- Preserve tutor asignado previamente
- Libera cupo si corresponde

---

## 🛠️ MÓDULO 7: MANTENIMIENTO Y DIAGNÓSTICO

### Controlador: `MantenimientoController`
**Base Path:** `/api/mantenimiento`
**Protección:** Requiere autenticación con rol COORDINADOR_TUTORIAS

---

### 7.1 GET /api/mantenimiento/diagnostico
**Descripción:** Diagnóstico completo del sistema
**Protección:** Autenticado

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "timestamp": "2024-11-20T13:50:00.000Z",
    "estado_general": "SALUDABLE",
    "base_datos": {
      "conexion": "OK",
      "total_alumnos": 450,
      "total_tutores": 15,
      "total_asignaciones": 420,
      "alumnos_sin_tutor": 30,
      "tutores_sobrecargados": 2,
      "semestres_activos": 1
    },
    "procesos": {
      "procesos_completados": 45,
      "procesos_fallidos": 2,
      "procesos_en_ejecucion": 0,
      "ultimo_proceso": {
        "id": 42,
        "estado": "COMPLETADO",
        "fecha": "2024-11-20T13:07:45.000Z"
      }
    },
    "integridad": {
      "asignaciones_huerfanas": 0,
      "alumnos_duplicados": 0,
      "tutores_sin_carrera": 0,
      "estado_general": "OK"
    }
  },
  "message": "Diagnóstico completado",
  "timestamp": "2024-11-20T13:50:00.000Z"
}
```

---

### 7.2 GET /api/mantenimiento/validar-integridad
**Descripción:** Valida integridad de datos
**Protección:** Autenticado

---

### 7.3 GET /api/mantenimiento/pendientes-resolucion
**Descripción:** Lista registros pendientes de resolución
**Protección:** Autenticado

---

### 7.4 POST /api/mantenimiento/sincronizar-tutores
**Descripción:** Sincroniza cargas de tutores
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Sincronización exitosa
- `409 Conflict` - Detectados problemas

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "sincronizados": 15,
    "corregidos": 3,
    "problemas": 0,
    "detalles": [
      {
        "tutorId": 5,
        "tutorNombre": "Dr. Pérez",
        "cargaAnterior": 18,
        "cargaNueva": 18,
        "estado": "OK"
      }
    ]
  },
  "message": "Sincronización completada",
  "timestamp": "2024-11-20T13:55:00.000Z"
}
```

---

### 7.5 POST /api/mantenimiento/resolver-masivamente
**Descripción:** Resuelve inactividades de forma masiva
**Protección:** Autenticado
**HTTP Status:**
- `202 Accepted` - Procesamiento en background
- `400 Bad Request` - Criterios inválidos

**Request Body:**
```json
{
  "motivo": "EGRESADO",
  "estado_target": "INACTIVO",
  "semestreId": 1
}
```

---

### 7.6 POST /api/mantenimiento/liberar-cupos-seguro
**Descripción:** Libera cupos de tutores de forma segura
**Protección:** Autenticado
**HTTP Status:**
- `200 OK` - Cupos liberados
- `409 Conflict` - Verificación fallida

---

## 📊 MÓDULO 8: DASHBOARD Y MÉTRICAS

### Controlador: `DashboardController`
**Base Path:** `/api/dashboard`
**Protección:** Requiere autenticación

---

### 8.1 GET /api/dashboard/estadisticas
**Descripción:** Estadísticas generales del sistema
**Protección:** Autenticado

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "periodo": "2024-04",
    "totalAlumnos": 287,
    "alumnosActivos": 280,
    "alumnosInactivos": 7,
    "totalTutores": 15,
    "tutoresActivos": 14,
    "tutoresInactivos": 1,
    "totalAsignaciones": 280,
    "promedioCargaPorTutor": 18.67,
    "tasaCoberturaAlumnos": 97.6,
    "tutoresDisponibles": 3
  },
  "message": "Estadísticas obtenidas",
  "timestamp": "2024-11-20T14:00:00.000Z"
}
```

---

### 8.2 GET /api/dashboard/distribucion-tutores
**Descripción:** Distribución de alumnos por tutor
**Protección:** Autenticado

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "totalTutores": 15,
    "distribucion": [
      {
        "tutorId": 5,
        "tutorNombre": "Dr. Francisco Pérez",
        "carrera": "ISC",
        "capacidadMax": 20,
        "cargaActual": 22,
        "disponible": false,
        "sobrecargado": true,
        "porcentajeUso": 110
      },
      {
        "tutorId": 3,
        "tutorNombre": "Mtra. Rosa Rodríguez",
        "carrera": "ISC",
        "capacidadMax": 15,
        "cargaActual": 12,
        "disponible": true,
        "sobrecargado": false,
        "porcentajeUso": 80
      }
    ]
  },
  "message": "Distribución obtenida",
  "timestamp": "2024-11-20T14:05:00.000Z"
}
```

---

### 8.3 GET /api/dashboard/procesos-recientes
**Descripción:** Últimos procesos de asignación ejecutados
**Protección:** Autenticado

---

### 8.4 GET /api/dashboard/semestre-activo
**Descripción:** Información del semestre activo
**Protección:** Autenticado

---

### 8.5 GET /api/dashboard/health
**Descripción:** Health check del sistema
**Protección:** Pública

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "status": "UP",
    "timestamp": "2024-11-20T14:10:00.000Z",
    "database": "CONNECTED",
    "message": "Sistema operacional"
  }
}
```

---

## 📈 MÓDULO 9: REPORTES Y EXPORTACIÓN

### Controlador: `ReporteController`
**Base Path:** `/api/reportes`
**Protección:** Requiere autenticación

---

### 9.1 GET /api/reportes/por-carrera
**Descripción:** Reporte consolidado de tutorías por carrera
**Protección:** Autenticado

**Query Parameters:**
```
GET /api/reportes/por-carrera?semestreAcademico=2024-04&carrera=ISC
```

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `semestreAcademico` | string | Código del semestre |
| `carrera` | string | Código de carrera (opcional) |

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "semestreAcademico": "2024-04",
    "fechaGeneracion": "2024-11-20T14:15:00.000Z",
    "semestre": "2024-04",
    "carreras": [
      {
        "carrera": "ISC",
        "totalAlumnos": 120,
        "totalTutores": 7,
        "tutores": [
          {
            "id": 5,
            "nombre": "Dr. Pérez",
            "cargaActual": 18,
            "capacidadMax": 20,
            "alumnos": [
              {
                "id": 1,
                "matricula": "202401001",
                "nombre": "Carlos López"
              }
            ]
          }
        ]
      }
    ]
  },
  "message": "Reporte generado",
  "timestamp": "2024-11-20T14:15:00.000Z"
}
```

---

### 9.2 GET /api/reportes/tutores/{tutorId}/alumnos/exportar
**Descripción:** Exporta lista de alumnos de un tutor (PDF o Excel)
**Protección:** Autenticado

**Path Parameters:**
```
GET /api/reportes/tutores/5/alumnos/exportar?formato=EXCEL&periodo=2024-04
```

| Parámetro | Tipo | Valores | Descripción |
|-----------|------|--------|-------------|
| `formato` | enum | PDF, EXCEL | Formato de descarga |
| `periodo` | string | "2024-04" | Semestre (opcional) |

**Response:**
- Descarga directa de archivo (Content-Type: application/pdf o application/vnd.ms-excel)
- Nombre del archivo: `reporte-tutor-{tutorId}.pdf` o `.xlsx`

---

### 9.3 GET /api/reportes/carreras/{codigo}/exportar
**Descripción:** Exporta reporte de una carrera
**Protección:** Autenticado

**Path Parameters:**
```
GET /api/reportes/carreras/ISC/exportar?formato=EXCEL&periodo=2024-04
```

**Response:**
- Descarga directa de archivo

---

### 9.4 GET /api/reportes/carreras/exportar-todos
**Descripción:** Exporta todos los reportes de carreras en ZIP
**Protección:** Autenticado

**Query Parameters:**
```
GET /api/reportes/carreras/exportar-todos?periodo=2024-04
```

**Response:**
- Descarga de archivo ZIP con múltiples reportes

### 9.5 GET /api/reportes/tutores/exportar-todos
**Descripción:** Exporta en un ZIP los reportes individuales de todos los tutores para el período indicado.
**Protección:** Autenticado (ROL: COORDINADOR_TUTORIAS)

**Query Parameters:**
```
GET /api/reportes/tutores/exportar-todos?formato=EXCEL&periodo=2024-04
```

| Parámetro | Tipo | Valores | Descripción |
|-----------|------|---------|-------------|
| `formato` | enum | PDF, EXCEL | Formato de descarga (default: EXCEL) |
| `periodo` | string | "YYYY-N" | Semestre académico (opcional) |

**Response:**
- Descarga de archivo ZIP con un reporte por tutor (nomenclatura: `REPORTES_TUTORES_{PERIODO}.zip`)

---

## 🧹 MÓDULO 10: LIMPIEZA Y MANTENIMIENTO DE DATOS

### Controlador: `CleanupAsignacionController`
**Base Path:** `/api/cleanup`
**Protección:** Requiere autenticación con rol ADMIN

---

### 10.1 GET /api/cleanup/detectar/{semestreId}
**Descripción:** Detecta asignaciones corruptas en un semestre
**Protección:** Autenticado (ADMIN)

**Path Parameters:**
```
GET /api/cleanup/detectar/3
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "semestreId": 3,
    "analisis": {
      "totalAsignaciones": 280,
      "asignacionesValidas": 275,
      "asignacionesCorruptas": 5,
      "problemas_detectados": [
        {
          "tipo": "TUTOR_NO_EXISTE",
          "asignacionesAfectadas": 2,
          "ids": [45, 67]
        },
        {
          "tipo": "CARGA_INCONSISTENTE",
          "asignacionesAfectadas": 3,
          "ids": [89, 90, 91]
        }
      ]
    }
  },
  "message": "Análisis completado",
  "timestamp": "2024-11-20T14:20:00.000Z"
}
```

---

### 10.2 GET /api/cleanup/detectar-global
**Descripción:** Análisis global de corrupción en todo el sistema
**Protección:** Autenticado (ADMIN)

---

### 10.3 GET /api/cleanup/health
**Descripción:** Estado general de salud del sistema
**Protección:** Pública

---

### 10.4 POST /api/cleanup/limpiar/{semestreId}
**Descripción:** Limpia asignaciones corruptas de un semestre
**Protección:** Autenticado (ADMIN)
**HTTP Status:**
- `200 OK` - Limpieza exitosa
- `400 Bad Request` - Validación fallida

**Path Parameters:**
```
POST /api/cleanup/limpiar/3
```

**Request Body:**
```json
{
  "confirmarLimpieza": true,
  "estrategia": "REASIGNAR_INTELLIGENTE"
}
```

**Estrategias de Limpieza:**
- `REASIGNAR_INTELLIGENTE` - Reasigna automáticamente a otro tutor
- `ELIMINAR` - Elimina la asignación corrupta
- `MARCAR_PENDIENTE` - Marca para resolución manual

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "semestreId": 3,
    "resultados": {
      "asignacionesCorruptasEncontradas": 5,
      "asignacionesLimpiadas": 5,
      "asignacionesReasignadas": 3,
      "asignacionesEliminadas": 2,
      "erroresLimpios": 0
    },
    "detalles": [
      {
        "asignacionId": 45,
        "problema": "TUTOR_NO_EXISTE",
        "accion": "REASIGNADO",
        "tutorDestino": 7
      }
    ]
  },
  "message": "Limpieza completada",
  "timestamp": "2024-11-20T14:25:00.000Z"
}
```

---

### 10.5 POST /api/cleanup/limpiar-global
**Descripción:** Limpieza global del sistema completo
**Protección:** Autenticado (ADMIN)
**⚠️ OPERACIÓN CRÍTICA**

---

### 10.6 POST /api/cleanup/recalcular
**Descripción:** Recalcula cargas de tutores
**Protección:** Autenticado (ADMIN)

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "tutoresActualizados": 15,
    "cargas_corregidas": 3,
    "detalles": [
      {
        "tutorId": 5,
        "tutorNombre": "Dr. Pérez",
        "cargaAnterior": 20,
        "cargaNueva": 18,
        "diferencia": -2
      }
    ]
  },
  "message": "Recalculación completada",
  "timestamp": "2024-11-20T14:30:00.000Z"
}
```

---

## 📚 DICCIONARIO DE RESPUESTAS

### ApiResponse Base
```json
{
  "success": boolean,
  "data": T (genérico),
  "message": string,
  "timestamp": ISO-8601,
  "errors": string[] (opcional)
}
```

### EstadoAlumno Enum
```
ACTIVO
INACTIVO
```

### EstadoTutor Enum
```
ACTIVO
INACTIVO
DISPONIBLE (con capacidad)
SOBRECARGADO (excedió capacidad)
```

### EstadoProceso Enum
```
INICIADO
VALIDANDO
ORDENANDO
ASIGNANDO
COMPLETADO
FALLIDO
CANCELADO
```

### SeveridadAlerta Enum
```
INFO
WARNING
ERROR
CRITICAL
```

### Rol Enum
```
COORDINADOR_TUTORIAS
SECRETARIO_ACADEMICO
ADMIN
```

---

## 🔐 CONSIDERACIONES DE SEGURIDAD

### Autenticación
- **Mecanismo:** JWT en cookies HTTP-only
- **Expiración access token:** 15 minutos
- **Expiración refresh token:** 7 días
- **Transporte:** Solo HTTPS (en producción)

### Autorización
- **Granularidad:** Basada en roles (@PreAuthorize)
- **Roles soportados:** COORDINADOR_TUTORIAS, SECRETARIO_ACADEMICO, ADMIN
- **Endpoints protegidos:** Todos excepto /health, /auth/login

### Validación de Datos
- **Framework:** Jakarta Bean Validation (@Valid, @Validated)
- **Niveles:** Form validation, Domain validation, Business logic validation

### Protección CSRF
- **Mecanismo:** Token validation en cookies
- **CORS:** Configurado con origen específico (producción)

### Logging y Auditoría
- **Nivel:** DEBUG en aplicación, INFO en root
- **Tabla:** LogAuditoria rastrea:
  - Usuario que ejecutó acción
  - Entidad modificada
  - Cambios realizados
  - Timestamp

### Límites de Tasa
- **Upload máximo:** 10MB por archivo
- **Request máximo:** 10MB
- **Paginación máxima:** 100 registros por página

---

## 🔄 FLUJOS DE NEGOCIO PRINCIPALES

### Flujo 1: Asignación de Tutores (Happy Path)
```
1. POST /auth/login                    [Autenticar usuario]
2. POST /api/asignaciones/iniciar      [Cargar Excel]
   - Sistema crea ProcesoAsignacion
   - Valida estructura del Excel
   - Ordena por semestre
   - Ejecuta asignación en background
3. GET /api/asignaciones/proceso/{id}  [Monitorear estado]
4. GET /api/asignaciones/proceso/{id}/alertas  [Ver alertas]
5. GET /api/alumnos?semestreId=X       [Verificar asignaciones]
```

### Flujo 2: Cambio Manual de Tutor
```
1. GET /api/alumnos/autocomplete?q=nombre    [Buscar alumno]
2. GET /api/alumnos/{id}                     [Obtener detalles]
3. GET /api/tutores?carrera=X&disponibles=true  [Listar tutores]
4. POST /api/asignaciones/cambio-tutor       [Reasignar]
5. GET /api/mantenimiento/sincronizar-tutores  [Sincronizar cargas]
```

### Flujo 3: Manejo de Inactividad
```
1. [Sistema detecta alumno inactivo]
2. PUT /api/alumnos-inactivos/{id}/motivo    [Asignar motivo]
3. PATCH /api/alumnos-inactivos/{id}/resolver-motivo  [Resolver]
4. [Tutor asignado se preserva automáticamente]
5. GET /api/alumnos-inactivos/pendientes     [Auditoría]
```

### Flujo 4: Generación de Reportes
```
1. GET /api/semestres/activo           [Obtener periodo actual]
2. GET /api/reportes/por-carrera?semestreAcademico=X  [Generar]
3. GET /api/reportes/tutores/{id}/alumnos/exportar?formato=EXCEL  [Descargar]
```

### Flujo 5: Limpieza de Datos
```
1. GET /api/cleanup/detectar/{semestreId}    [Diagnosticar]
2. [Admin revisa problemas detectados]
3. POST /api/cleanup/limpiar/{semestreId}    [Limpiar]
4. POST /api/cleanup/recalcular              [Recalcular cargas]
5. GET /api/mantenimiento/diagnostico       [Verificar]
```

---

## 📝 NOTAS IMPORTANTES

### Semestre Activo
- Sistema requiere un semestre activo para la mayoría de operaciones
- Endpoints con parámetro `semestreId` null usan automáticamente el semestre activo
- Excepto: búsquedas, listados de semestres, mantenimiento

### Paginación
- **Parámetro page:** 1-based (comienza en 1)
- **Parámetro limit:** Máximo 100 registros
- **Respuesta:** Incluye totalElements, totalPages

### Estados de Alumno
- `ACTIVO`: Cursando/asignado
- `INACTIVO`: Retirado, egresado, o en suspensión

### Carga de Tutores
- `cargaActual`: Cantidad actual de alumnos asignados
- `capacidadMax`: Límite establecido
- `sobrecargado`: true si cargaActual > capacidadMax

### Procesamiento en Background
- Endpoints de larga duración retornan `202 Accepted`
- Cliente debe usar GET /api/asignaciones/proceso/{id} para monitorear
- El proceso ejecuta en paralelo en el servidor

### Validación de Excel
- Estructura esperada: Matrícula, Nombre, Carrera, Semestre
- Detección de errores: Duplicados, tipos inválidos, campos faltantes
- Resultado: Lista de errores por fila o datos válidos ordenados

---

## 🎯 CONCLUSIÓN

Este documento proporciona una documentación exhaustiva de los **72 endpoints** funcionales del sistema de tutorías, organizados en **10 módulos**, con ejemplos detallados de requests/responses, validaciones, comportamientos y flujos de negocio.

**Fecha de Actualización:** 20 de Noviembre de 2024
**Versión Backend:** 3.3.1 (Spring Boot)
**Estado:** ✅ COMPLETO Y VERIFICADO

---

*Generado automáticamente por Sistema de Auditoría de Backends*
*Para consultas técnicas, refer a los comentarios en el código fuente*

