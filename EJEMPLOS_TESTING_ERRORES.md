# 🧪 Ejemplos de Testing - Respuestas de Error Unificadas

## 📌 Requisitos Previos

```bash
# URLs base (cambiar según ambiente)
BASE_URL="http://localhost:8080"
SEMESTRE_ID=1  # Cambiar según tu BD
```

---

## 1️⃣ POST `/api/asignaciones/validar-excel`

### ✅ Caso Exitoso - Excel Válido

```bash
curl -X POST "${BASE_URL}/api/asignaciones/validar-excel" \
  -F "archivo=@ruta/a/alumnos_validos.xlsx" \
  -F "semestreId=1" \
  -H "Authorization: Bearer TU_TOKEN"
```

**Respuesta esperada:** 200 OK
```json
{
  "success": true,
  "data": {
    "status": "OK",
    "message": "Validación completada exitosamente. Datos listos para asignación.",
    "timestamp": "2025-11-20T10:15:30.123456",
    "totalFilas": 100,
    "totalValidas": 100,
    "totalErrores": 0,
    "porcentajeExito": 100.0,
    "errors": [],
    "data": [
      {
        "alumnoId": null,
        "matricula": "A12345",
        "nombre": "Juan Pérez",
        "carrera": "ISC",
        "semestreId": 1,
        "semestreCodigo": "2025-2025-FN",
        "semestreNumerico": 3,
        "ordenPriority": 3,
        "validado": true,
        "tipoAsignacionPrevisto": null
      }
      // ... más alumnos
    ],
    "resumen": "✓ 100 alumnos validados exitosamente. Ordenados por semestre..."
  }
}
```

---

### ❌ Error de Formato - Excel Corrupto

```bash
curl -X POST "${BASE_URL}/api/asignaciones/validar-excel" \
  -F "archivo=@ruta/a/archivo_corrupto.txt" \
  -F "semestreId=1" \
  -H "Authorization: Bearer TU_TOKEN"
```

**Respuesta esperada:** 400 BAD_REQUEST
```json
{
  "status": 400,
  "error": "EXCEL_FORMAT_ERROR",
  "message": "El formato del archivo Excel no es válido: El archivo no es un formato válido de Excel",
  "path": "/api/asignaciones/validar-excel",
  "timestamp": "2025-11-20T10:15:30.123456Z",
  "fieldErrors": null,
  "excelErrors": null
}
```

---

### ❌ Error de Validación - Excel con Errores

```bash
curl -X POST "${BASE_URL}/api/asignaciones/validar-excel" \
  -F "archivo=@ruta/a/alumnos_con_errores.xlsx" \
  -F "semestreId=1" \
  -H "Authorization: Bearer TU_TOKEN"
```

**Respuesta esperada:** 400 BAD_REQUEST
```json
{
  "status": 400,
  "error": "EXCEL_VALIDATION_ERROR",
  "message": "Se encontraron 3 errores de validación en 100 filas",
  "path": "/api/asignaciones/validar-excel",
  "timestamp": "2025-11-20T10:15:30.123456Z",
  "fieldErrors": null,
  "excelErrors": [
    {
      "rowNumber": 2,
      "column": "MATRICULA_INVALIDA",
      "value": "abc",
      "message": "Formato de matrícula inválido. Debe tener entre 5-20 caracteres alfanuméricos"
    },
    {
      "rowNumber": 5,
      "column": "CARRERA_INVALIDA",
      "value": "XYZ",
      "message": "Carrera no reconocida: XYZ. Carreras válidas: ICA, IE, IMECA, IME, ISC, ITS"
    },
    {
      "rowNumber": 8,
      "column": "CAMPO_VACIO",
      "value": null,
      "message": "El nombre está vacío"
    }
  ]
}
```

---

### ❌ Error de Dominio - Semestre No Existe

```bash
curl -X POST "${BASE_URL}/api/asignaciones/validar-excel" \
  -F "archivo=@ruta/a/alumnos.xlsx" \
  -F "semestreId=99999" \
  -H "Authorization: Bearer TU_TOKEN"
```

**Respuesta esperada:** 400 BAD_REQUEST
```json
{
  "status": 400,
  "error": "DOMAIN_VALIDATION_ERROR",
  "message": "Semestre con ID 99999 no encontrado",
  "path": "/api/asignaciones/validar-excel",
  "timestamp": "2025-11-20T10:15:30.123456Z",
  "fieldErrors": null,
  "excelErrors": null
}
```

---

## 2️⃣ POST `/api/asignaciones/ejecutar`

### ✅ Caso Exitoso - Asignación Completada

```bash
curl -X POST "${BASE_URL}/api/asignaciones/ejecutar" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TU_TOKEN" \
  -d '{
    "semestreId": 1,
    "alumnosValidados": [
      {
        "alumnoId": null,
        "matricula": "A12345",
        "nombre": "Juan Pérez",
        "carrera": "ISC",
        "semestreId": 1,
        "semestreCodigo": "2025-2025-FN",
        "semestreNumerico": 3,
        "ordenPriority": 3,
        "validado": true,
        "tipoAsignacionPrevisto": null
      }
    ]
  }'
```

**Respuesta esperada:** 200 OK
```json
{
  "success": true,
  "data": {
    "status": "OK",
    "message": "Asignación completada exitosamente",
    "timestamp": "2025-11-20T10:15:31.123456",
    "totalAlumnos": 100,
    "alumnosAsignados": 100,
    "alumnosConError": 0,
    "porcentajeExito": 100.0,
    "detalles": "Se asignaron 100 alumnos correctamente",
    "erroresDetalle": [],
    "duracionMs": 2500
  }
}
```

---

### ❌ Error de Validación - Lista Vacía

```bash
curl -X POST "${BASE_URL}/api/asignaciones/ejecutar" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TU_TOKEN" \
  -d '{
    "semestreId": 1,
    "alumnosValidados": []
  }'
```

**Respuesta esperada:** 400 BAD_REQUEST
```json
{
  "status": 400,
  "error": "DOMAIN_VALIDATION_ERROR",
  "message": "Lista de alumnos validados vacía",
  "path": "/api/asignaciones/ejecutar",
  "timestamp": "2025-11-20T10:15:30.123456Z",
  "fieldErrors": null,
  "excelErrors": null
}
```

---

### ❌ Error de Validación - Semestre Inválido

```bash
curl -X POST "${BASE_URL}/api/asignaciones/ejecutar" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TU_TOKEN" \
  -d '{
    "semestreId": 0,
    "alumnosValidados": [
      {
        "alumnoId": null,
        "matricula": "A12345",
        "nombre": "Juan Pérez",
        "carrera": "ISC",
        "semestreId": 0,
        "semestreCodigo": "2025-2025-FN",
        "semestreNumerico": 3,
        "ordenPriority": 3,
        "validado": true,
        "tipoAsignacionPrevisto": null
      }
    ]
  }'
```

**Respuesta esperada:** 400 BAD_REQUEST
```json
{
  "status": 400,
  "error": "DOMAIN_VALIDATION_ERROR",
  "message": "semestreId inválido: 0",
  "path": "/api/asignaciones/ejecutar",
  "timestamp": "2025-11-20T10:15:30.123456Z",
  "fieldErrors": null,
  "excelErrors": null
}
```

---

### ❌ Error 404 - Semestre No Encontrado

```bash
curl -X POST "${BASE_URL}/api/asignaciones/ejecutar" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TU_TOKEN" \
  -d '{
    "semestreId": 99999,
    "alumnosValidados": [...]
  }'
```

**Respuesta esperada:** 404 NOT_FOUND
```json
{
  "status": 404,
  "error": "ENTITY_NOT_FOUND",
  "message": "Semestre no encontrado con ID: 99999",
  "path": "/api/asignaciones/ejecutar",
  "timestamp": "2025-11-20T10:15:30.123456Z",
  "fieldErrors": null,
  "excelErrors": null
}
```

---

## 3️⃣ POST `/api/alumnos`

### ✅ Caso Exitoso - Alumno Creado

```bash
curl -X POST "${BASE_URL}/api/alumnos" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TU_TOKEN" \
  -d '{
    "matricula": "A99999",
    "nombre": "Nuevo Alumno",
    "carrera": "ISC",
    "semestreActual": 3
  }'
```

**Respuesta esperada:** 201 CREATED
```json
{
  "success": true,
  "data": {
    "id": 1001,
    "matricula": "A99999",
    "nombre": "Nuevo Alumno",
    "carrera": "ISC",
    "semestreActual": 3,
    "estado": "ACTIVO"
  }
}
```

---

### ❌ Error de Validación - Campos Vacíos

```bash
curl -X POST "${BASE_URL}/api/alumnos" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TU_TOKEN" \
  -d '{
    "matricula": "",
    "nombre": "",
    "carrera": "ISC",
    "semestreActual": 3
  }'
```

**Respuesta esperada:** 400 BAD_REQUEST
```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Hay errores de validación en la petición",
  "path": "/api/alumnos",
  "timestamp": "2025-11-20T10:15:30.123456Z",
  "fieldErrors": [
    {
      "field": "matricula",
      "message": "No debe estar vacío"
    },
    {
      "field": "nombre",
      "message": "No debe estar vacío"
    }
  ],
  "excelErrors": null
}
```

---

### ❌ Error 409 - Alumno Duplicado

```bash
curl -X POST "${BASE_URL}/api/alumnos" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TU_TOKEN" \
  -d '{
    "matricula": "A12345",
    "nombre": "Juan Pérez",
    "carrera": "ISC",
    "semestreActual": 3
  }'
```

**Respuesta esperada:** 409 CONFLICT
```json
{
  "status": 409,
  "error": "DUPLICATE_RESOURCE",
  "message": "Ya existe un registro similar: La matrícula A12345 ya existe",
  "path": "/api/alumnos",
  "timestamp": "2025-11-20T10:15:30.123456Z",
  "fieldErrors": null,
  "excelErrors": null
}
```

---

## 4️⃣ GET `/api/dashboard/estadisticas`

### ✅ Caso Exitoso - Estadísticas Obtenidas

```bash
curl -X GET "${BASE_URL}/api/dashboard/estadisticas?semestreId=1" \
  -H "Authorization: Bearer TU_TOKEN"
```

**Respuesta esperada:** 200 OK
```json
{
  "success": true,
  "data": {
    "semestre": "2025-2025-FN",
    "semestre_id": 1,
    "total_alumnos": 350,
    "alumnos_con_tutor": 350,
    "alumnos_sin_tutor": 0,
    "total_tutores": 25,
    "tutores_con_asignaciones": 23,
    "total_asignaciones": 350,
    "promedio_alumnos_por_tutor": 15.2,
    "porcentaje_cobertura": 100.0
  }
}
```

---

### ❌ Error 404 - Semestre No Existe

```bash
curl -X GET "${BASE_URL}/api/dashboard/estadisticas?semestreId=99999" \
  -H "Authorization: Bearer TU_TOKEN"
```

**Respuesta esperada:** 404 NOT_FOUND
```json
{
  "status": 404,
  "error": "SEMESTER_NOT_FOUND",
  "message": "Semestre no encontrado con ID: 99999",
  "path": "/api/dashboard/estadisticas",
  "timestamp": "2025-11-20T10:15:30.123456Z",
  "fieldErrors": null,
  "excelErrors": null
}
```

---

## 🔐 Errores de Autenticación

### ❌ 401 UNAUTHORIZED - Credenciales Inválidas

```bash
curl -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "incorrecta"
  }'
```

**Respuesta esperada:** 401 UNAUTHORIZED
```json
{
  "status": 401,
  "error": "INVALID_CREDENTIALS",
  "message": "Usuario o contraseña incorrectos",
  "path": "/auth/login",
  "timestamp": "2025-11-20T10:15:30.123456Z",
  "fieldErrors": null,
  "excelErrors": null
}
```

---

### ❌ 403 FORBIDDEN - Acceso Denegado

```bash
# Token de usuario normal, intentando endpoint de admin
curl -X POST "${BASE_URL}/api/asignaciones/ejecutar" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN_USUARIO_NORMAL" \
  -d '{
    "semestreId": 1,
    "alumnosValidados": [...]
  }'
```

**Respuesta esperada:** 403 FORBIDDEN
```json
{
  "status": 403,
  "error": "ACCESS_DENIED",
  "message": "No tiene permisos para realizar esta acción",
  "path": "/api/asignaciones/ejecutar",
  "timestamp": "2025-11-20T10:15:30.123456Z",
  "fieldErrors": null,
  "excelErrors": null
}
```

---

## 🛡️ Monitoreo de Errores

### Ver logs en tiempo real

```bash
# En el servidor
tail -f logs/spring-boot.log | grep -E "(ERROR|WARN|GlobalExceptionHandler)"
```

### Estructura de logs

```
2025-11-20 10:15:30 WARN [GlobalExceptionHandler] Error de validación de dominio: Semestre no encontrado
2025-11-20 10:15:31 WARN [GlobalExceptionHandler] Error de validación de Excel: 3 errores encontrados
2025-11-20 10:15:32 ERROR [GlobalExceptionHandler] Error inesperado
```

---

## 📊 Tabla de Referencia Rápida

| Escenario | HTTP | Error Code | Retry? |
|-----------|------|-----------|--------|
| Excel válido | 200 | N/A | ✅ |
| Excel inválido | 400 | EXCEL_VALIDATION_ERROR | ✅ Fix & Retry |
| Formato inválido | 400 | EXCEL_FORMAT_ERROR | ✅ Fix & Retry |
| Semestre no existe | 400/404 | DOMAIN/NOT_FOUND | ❌ Verificar |
| Campo vacío | 400 | VALIDATION_ERROR | ✅ Fix & Retry |
| Duplicado | 409 | DUPLICATE_RESOURCE | ❌ Verificar |
| Capacidad excedida | 409 | CAPACITY_EXCEEDED | ❌ Verificar |
| Alumno inactivo | 422 | INACTIVE_STUDENT | ❌ Reactivar |
| Sin permisos | 403 | ACCESS_DENIED | ❌ Verificar rol |
| Credenciales malas | 401 | INVALID_CREDENTIALS | ✅ Reintentar login |
| Error del servidor | 500 | INTERNAL_SERVER_ERROR | ⏳ Esperar & Retry |

---

## 💡 Tips para Testing

1. **Siempre verificar el `error` field:** Ese es el tipo específico de error
2. **Para Excel:** Revisar `excelErrors[].rowNumber` para ubicar el problema
3. **Para campos:** Revisar `fieldErrors[].field` para saber qué completar
4. **Timestamps:** Útil para debugging y correlacionamiento de logs
5. **Path:** Ayuda a verificar que estás en el endpoint correcto

---

**Última actualización:** 2025-11-20
