# 📋 Manejo Unificado de Errores y Validaciones - Backend Spring Boot

## 🎯 Objetivo Logrado

Se ha implementado un **sistema centralizado y consistente** para el manejo de errores en todos los endpoints del backend. Ya no hay respuestas 500 para validaciones, y el frontend puede consumir una estructura estandarizada de errores.

---

## 📐 Estructura Estandarizada de Respuesta de Error

Todos los errores ahora retornan este formato JSON:

```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Hay errores de validación en la petición",
  "path": "/api/asignaciones/validar-excel",
  "timestamp": "2025-11-20T10:15:30.123456Z",
  "fieldErrors": [
    {
      "field": "nombre",
      "message": "No debe estar vacío"
    }
  ],
  "excelErrors": null
}
```

### Campos de Respuesta

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `status` | int | Código HTTP (400, 404, 409, 422, 500, etc.) |
| `error` | string | Tipo de error (ej: `VALIDATION_ERROR`, `EXCEL_VALIDATION_ERROR`) |
| `message` | string | Mensaje legible para mostrar al usuario |
| `path` | string | URI del endpoint que falló |
| `timestamp` | string | Fecha/hora de la excepción (ISO 8601) |
| `fieldErrors` | array | Errores de validación de campos (opcional) |
| `excelErrors` | array | Errores de validación de Excel (opcional) |

---

## 🔴 Códigos de Error HTTP

### 400 BAD_REQUEST - Validaciones esperadas

Se retorna cuando hay errores que se esperan que ocurran (no son errores del sistema):

- `VALIDATION_ERROR`: Error en validación de Bean Validation (@Valid, @NotNull)
- `DOMAIN_VALIDATION_ERROR`: Error en validación de reglas de negocio
- `EXCEL_VALIDATION_ERROR`: Error en validación de archivo Excel (con lista detallada de errores)
- `EXCEL_FORMAT_ERROR`: Formato inválido del archivo Excel
- `INVALID_ARGUMENT`: Argumento inválido

**Ejemplo:**
```json
{
  "status": 400,
  "error": "EXCEL_VALIDATION_ERROR",
  "message": "Se encontraron 5 errores de validación en 100 filas",
  "path": "/api/asignaciones/validar-excel",
  "timestamp": "2025-11-20T10:15:30.123456Z",
  "excelErrors": [
    {
      "rowNumber": 2,
      "column": "MATRICULA_INVALIDA",
      "value": "abc123",
      "message": "Formato de matrícula inválido. Debe tener entre 5-20 caracteres alfanuméricos"
    },
    {
      "rowNumber": 5,
      "column": "CAMPO_VACIO",
      "value": null,
      "message": "El nombre está vacío"
    }
  ]
}
```

### 404 NOT_FOUND - Recurso no encontrado

- `ENTITY_NOT_FOUND`: Entidad genérica no encontrada
- `SEMESTER_NOT_FOUND`: Semestre no encontrado

### 409 CONFLICT - Conflictos de negocio

- `CAPACITY_EXCEEDED`: Capacidad de tutores excedida
- `DUPLICATE_RESOURCE`: Recurso duplicado
- `NO_AVAILABLE_TUTOR`: No hay tutores disponibles

### 422 UNPROCESSABLE_ENTITY - Entidad no procesable

- `INACTIVE_STUDENT`: Alumno está inactivo

### 401/403 - Autenticación/Autorización

- `INVALID_CREDENTIALS`: Credenciales inválidas
- `AUTHENTICATION_FAILED`: Error de autenticación
- `ACCESS_DENIED`: Acceso denegado

### 500 INTERNAL_SERVER_ERROR - Errores inesperados

Se retorna SOLO cuando ocurre un error realmente inesperado (no validación, no regla de negocio):

- `INTERNAL_SERVER_ERROR`: Error genérico inesperado
- `ASSIGNMENT_PROCESS_ERROR`: Error en proceso de asignación

---

## 🔄 Manejo de Excepciones por Tipo

### 1. Excepciones de Validación de Bean Validation

```java
@Valid @NotNull String nombre
@Valid List<Alumno> alumnos
```

**Manejadas por:** `GlobalExceptionHandler.handleMethodArgumentNotValid()`
**Código HTTP:** 400 BAD_REQUEST
**Estructura:** Incluye `fieldErrors[]` con detalles de cada campo

### 2. Excepciones de Validación de Dominio

```java
throw new DomainValidationException("Semestre no encontrado");
```

**Manejadas por:** `GlobalExceptionHandler.handleDomainValidation()`
**Código HTTP:** 400 BAD_REQUEST
**Estructura:** `message` con descripción del error

### 3. Excepciones de Validación de Excel

```java
throw new ExcelValidationException("Se encontraron 5 errores", listaErrores);
```

**Manejadas por:** `GlobalExceptionHandler.handleExcelValidation()`
**Código HTTP:** 400 BAD_REQUEST
**Estructura:** Incluye `excelErrors[]` con todos los errores encontrados

```java
public record ExcelValidationErrorDetail(
    Integer rowNumber,    // Número de fila en Excel
    String column,        // Tipo de error (MATRICULA_INVALIDA, CAMPO_VACIO, etc.)
    String value,         // Valor incorrecto
    String message        // Descripción del error
) {}
```

### 4. Excepciones de Entidad No Encontrada

```java
throw new EntityNotFoundException("Proceso no encontrado");
```

**Manejadas por:** `GlobalExceptionHandler.handleEntityNotFound()`
**Código HTTP:** 404 NOT_FOUND

### 5. Excepciones de Negocio (Capacidad, Duplicados, etc.)

```java
throw new CapacidadExcedidaException("Tutor alcanzó capacidad máxima");
throw new DuplicadoException("Alumno ya tiene asignación");
throw new SinTutorDisponibleException("No hay tutores disponibles");
```

**Manejadas por:** `GlobalExceptionHandler` (múltiples handlers)
**Código HTTP:** 409 CONFLICT

### 6. Excepciones de Alumno Inactivo

```java
throw new AlumnoInactivoException("El alumno está inactivo");
```

**Manejadas por:** `GlobalExceptionHandler.handleAlumnoInactivo()`
**Código HTTP:** 422 UNPROCESSABLE_ENTITY

---

## 📝 Cambios en Endpoints Principales

### POST `/api/asignaciones/validar-excel`

**Validación anterior:** Retornaba respuesta con estructura propia
**Validación nueva:** Lanza `ExcelValidationException` si hay errores

**Comportamiento:**
- ✅ Si el Excel es válido: Retorna 200 OK con datos ordenados
- ❌ Si hay errores de formato: Lanza `ExcelFormatoException` → 400 BAD_REQUEST
- ❌ Si hay errores de validación: Lanza `ExcelValidationException` → 400 BAD_REQUEST con `excelErrors[]`
- ❌ Si semestre no existe: Lanza `DomainValidationException` → 400 BAD_REQUEST

**Respuesta de Error (Ejemplo):**
```json
{
  "status": 400,
  "error": "EXCEL_VALIDATION_ERROR",
  "message": "Se encontraron 3 errores de validación en 100 filas",
  "path": "/api/asignaciones/validar-excel",
  "timestamp": "2025-11-20T10:15:30.123456Z",
  "excelErrors": [
    {
      "rowNumber": 2,
      "column": "MATRICULA_INVALIDA",
      "value": "inv",
      "message": "Formato de matrícula inválido. Debe tener entre 5-20 caracteres alfanuméricos"
    },
    {
      "rowNumber": 5,
      "column": "CARRERA_INVALIDA",
      "value": "XYZ",
      "message": "Carrera no reconocida: XYZ. Carreras válidas: ICA, IE, IMECA, IME, ISC, ITS"
    },
    {
      "rowNumber": 10,
      "column": "CAMPO_VACIO",
      "value": null,
      "message": "El nombre está vacío"
    }
  ]
}
```

### POST `/api/asignaciones/ejecutar`

**Cambio:** Validaciones ahora lanzan excepciones en lugar de retornar respuestas de error

**Comportamiento:**
- ✅ Si validación de precondiciones falla: Lanza `DomainValidationException` → 400 BAD_REQUEST
- ✅ Si semestre no existe: Lanza `EntityNotFoundException` → 404 NOT_FOUND
- ❌ Si hay errores durante ejecución: Retorna respuesta con `status="ERROR"` → 500 INTERNAL_SERVER_ERROR

---

## 🔧 Cómo Consumir Errores en Frontend

### Ejemplo: Validación de Excel

```javascript
// POST a /api/asignaciones/validar-excel
fetch('/api/asignaciones/validar-excel', {
  method: 'POST',
  body: formData
})
.then(response => {
  if (response.ok) {
    return response.json(); // OK - datos válidos
  } else if (response.status === 400) {
    return response.json().then(error => {
      if (error.error === 'EXCEL_VALIDATION_ERROR') {
        // Mostrar errores de Excel detallados
        mostrarErroresExcel(error.excelErrors);
      } else if (error.error === 'VALIDATION_ERROR') {
        // Mostrar errores de campos
        mostrarErroresCampos(error.fieldErrors);
      } else {
        // Mostrar mensaje genérico
        mostrarError(error.message);
      }
      throw error;
    });
  } else if (response.status === 404) {
    mostrarError('Semestre no encontrado');
    throw new Error('Not Found');
  } else {
    mostrarError('Error inesperado del servidor');
    throw new Error('Server Error');
  }
})
.catch(error => console.error(error));

function mostrarErroresExcel(excelErrors) {
  excelErrors.forEach(err => {
    console.log(`Fila ${err.rowNumber}, ${err.column}: ${err.message}`);
    // Mostrar en tabla/modal con detalles
  });
}

function mostrarErroresCampos(fieldErrors) {
  fieldErrors.forEach(err => {
    console.log(`Campo ${err.field}: ${err.message}`);
  });
}
```

### Mapeo de Códigos HTTP

| HTTP | Error Code | Acción |
|------|------------|--------|
| 400 | VALIDATION_ERROR | Mostrar errores de campos |
| 400 | DOMAIN_VALIDATION_ERROR | Mostrar mensaje de error |
| 400 | EXCEL_VALIDATION_ERROR | Mostrar tabla con errores de Excel |
| 404 | ENTITY_NOT_FOUND | Mostrar "No encontrado" |
| 409 | CAPACITY_EXCEEDED | Mostrar "Capacidad excedida" |
| 409 | DUPLICATE_RESOURCE | Mostrar "Ya existe" |
| 422 | INACTIVE_STUDENT | Mostrar "Alumno inactivo" |
| 500 | INTERNAL_SERVER_ERROR | Mostrar "Error del servidor" |

---

## 📊 Características Principales

✅ **Sin errores 500 para validaciones:** Todas las validaciones retornan 400 BAD_REQUEST
✅ **Estructura consistente:** Todos los errores usan el mismo formato
✅ **Errores completos de Excel:** Se acumulan TODOS los errores, no solo el primero
✅ **Detalles de validación:** `fieldErrors` y `excelErrors` con información por campo/fila
✅ **Timestamps:** Cada error incluye fecha/hora precisa
✅ **Path tracking:** Cada respuesta incluye la URI del endpoint que falló
✅ **Logging centralizado:** GlobalExceptionHandler registra todos los errores

---

## 🔍 Debugging

### Ver logs del GlobalExceptionHandler

```bash
# En application.properties
logging.level.com.universidad.tutorias.infrastructure.exception.GlobalExceptionHandler=DEBUG
```

### Estructura de Logs

```
2025-11-20 10:15:30 WARN [GlobalExceptionHandler] Error de validación de dominio: Semestre no encontrado
2025-11-20 10:15:31 WARN [GlobalExceptionHandler] Error de validación de Excel: 5 errores encontrados
2025-11-20 10:15:32 ERROR [GlobalExceptionHandler] Error inesperado
```

---

## 📚 Referencias

- **GlobalExceptionHandler:** `/backend/src/main/java/com/universidad/tutorias/infrastructure/exception/GlobalExceptionHandler.java`
- **ApiErrorResponse:** `/backend/src/main/java/com/universidad/tutorias/infrastructure/controller/response/ApiErrorResponse.java`
- **DomainValidationException:** `/backend/src/main/java/com/universidad/tutorias/domain/exception/DomainValidationException.java`
- **ExcelValidationException:** `/backend/src/main/java/com/universidad/tutorias/infrastructure/exception/ExcelValidationException.java`

---

## ✨ Beneficios para el Frontend

1. **Respuestas predecibles:** Todos los errores siguen el mismo formato
2. **Información detallada:** Sabe exactamente cuál campo/fila tiene error
3. **Sin sorpresas:** No hay errores 500 inesperados por validaciones
4. **Mejor UX:** Puede mostrar errores específicos por fila de Excel
5. **Debugging fácil:** Cada error incluye path y timestamp
6. **Mensajes claros:** Todos los mensajes son legibles para el usuario

---

**Implementado:** 2025-11-20
**Versión:** Backend Spring Boot 3 Refactorizado
