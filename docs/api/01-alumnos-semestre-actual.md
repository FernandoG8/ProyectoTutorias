# Endpoints de Alumnos por Semestre Actual

## Descripción General

Los endpoints de alumnos ahora incluyen soporte automático para obtener el semestre activo del sistema y filtrar alumnos según el semestre actual.

---

## Endpoints Disponibles

### 1. GET `/api/alumnos` - Lista Alumnos (Actualizado)

**Descripción**: Lista alumnos con soporte automático para semestre activo

**URL**:
```
GET /api/alumnos
```

**Parámetros Query**:

| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `page` | int | 1 | Número de página |
| `limit` | int | 20 | Registros por página |
| `estado` | enum | null | ACTIVO, INACTIVO |
| `carrera` | string | null | Nombre de carrera |
| `semestreId` | integer | null | ID semestre (usa semestre activo si no se proporciona) |
| `soloActivo` | boolean | false | Si true, solo muestra alumnos ACTIVOS |

**Ejemplos de Uso**:

#### Ejemplo 1: Listar alumnos del semestre activo (Default)
```bash
curl -X GET "http://localhost:8080/api/alumnos" \
  -H "Content-Type: application/json"
```

**Respuesta**:
```json
{
  "status": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "matricula": "2020001",
        "nombre": "Juan Pérez",
        "carrera": "Ingeniería en Sistemas",
        "semestre": 6,
        "estado": "ACTIVO",
        "tutor": {
          "id": 5,
          "nombre": "Dr. García"
        }
      },
      ...más alumnos
    ],
    "page": 1,
    "limit": 20,
    "totalElements": 150,
    "totalPages": 8
  },
  "message": "Alumnos del semestre 2025-2026-F1"
}
```

#### Ejemplo 2: Solo alumnos activos
```bash
curl -X GET "http://localhost:8080/api/alumnos?soloActivo=true" \
  -H "Content-Type: application/json"
```

**Respuesta**: Solo alumnos con estado ACTIVO

#### Ejemplo 3: Por carrera
```bash
curl -X GET "http://localhost:8080/api/alumnos?carrera=Ingeniería%20en%20Sistemas" \
  -H "Content-Type: application/json"
```

**Respuesta**: Alumnos de Ingeniería en Sistemas del semestre activo

#### Ejemplo 4: Paginación
```bash
curl -X GET "http://localhost:8080/api/alumnos?page=2&limit=50" \
  -H "Content-Type: application/json"
```

**Respuesta**: Página 2 con 50 registros por página

#### Ejemplo 5: Semestre específico (Override)
```bash
curl -X GET "http://localhost:8080/api/alumnos?semestreId=5" \
  -H "Content-Type: application/json"
```

**Respuesta**: Alumnos del semestre con ID 5 (ignora semestre activo)

#### Ejemplo 6: Combinado - Carrera específica y solo activos
```bash
curl -X GET "http://localhost:8080/api/alumnos?carrera=Administración&soloActivo=true&page=1&limit=25" \
  -H "Content-Type: application/json"
```

**Respuesta**: Alumnos activos de Administración del semestre actual, página 1 con 25 por página

---

### 2. GET `/api/alumnos/semestre-actual` - Lista Alumnos del Semestre Actual (NUEVO)

**Descripción**: Endpoint especializado que lista **SOLO alumnos activos** del semestre actual. Es la forma más rápida de obtener una lista actualizada de estudiantes.

**URL**:
```
GET /api/alumnos/semestre-actual
```

**Parámetros Query**:

| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `page` | int | 1 | Número de página |
| `limit` | int | 20 | Registros por página |
| `carrera` | string | null | Filtro por carrera (opcional) |

**Ejemplos de Uso**:

#### Ejemplo 1: Lista completa actualizada
```bash
curl -X GET "http://localhost:8080/api/alumnos/semestre-actual" \
  -H "Content-Type: application/json"
```

**Respuesta**:
```json
{
  "status": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "matricula": "2020001",
        "nombre": "Juan Pérez",
        "carrera": "Ingeniería en Sistemas",
        "semestre": 6,
        "estado": "ACTIVO",
        "tutor": {
          "id": 5,
          "nombre": "Dr. García"
        }
      },
      {
        "id": 2,
        "matricula": "2020002",
        "nombre": "María López",
        "carrera": "Administración",
        "semestre": 4,
        "estado": "ACTIVO",
        "tutor": {
          "id": 8,
          "nombre": "Dra. Martínez"
        }
      },
      ...más alumnos
    ],
    "page": 1,
    "limit": 20,
    "totalElements": 487,
    "totalPages": 25
  },
  "message": "Alumnos activos del semestre 2025-2026-F1 - Total: 487"
}
```

#### Ejemplo 2: Filtrar por carrera
```bash
curl -X GET "http://localhost:8080/api/alumnos/semestre-actual?carrera=Ingeniería%20en%20Sistemas" \
  -H "Content-Type: application/json"
```

**Respuesta**:
```json
{
  "status": "success",
  "data": {
    "content": [
      // Solo alumnos de Ingeniería en Sistemas, estado ACTIVO
    ],
    "page": 1,
    "limit": 20,
    "totalElements": 120,
    "totalPages": 6
  },
  "message": "Alumnos activos del semestre 2025-2026-F1 - Total: 120"
}
```

#### Ejemplo 3: Con paginación
```bash
curl -X GET "http://localhost:8080/api/alumnos/semestre-actual?page=3&limit=50" \
  -H "Content-Type: application/json"
```

**Respuesta**: Página 3 con 50 registros por página

#### Ejemplo 4: Carrera + Paginación
```bash
curl -X GET "http://localhost:8080/api/alumnos/semestre-actual?carrera=Derecho&page=1&limit=30" \
  -H "Content-Type: application/json"
```

**Respuesta**: Alumnos activos de Derecho del semestre actual, 30 por página

---

## Diferencias Entre los Endpoints

| Aspecto | `/api/alumnos` | `/api/alumnos/semestre-actual` |
|---------|---|---|
| **Estado Default** | null (todos) | ACTIVO (solo activos) |
| **Semestre Default** | Semestre activo | Semestre activo |
| **Permite Semestre Específico** | ✓ Sí (parámetro `semestreId`) | ✗ No (siempre semestre activo) |
| **Caso de Uso** | Búsqueda flexible | Lista actualizada rápida |
| **URL Más Corta** | No | ✓ Sí |
| **Ideal Para** | Dashboard completo | Dropdown/Lista principal |

---

## Casos de Uso Comunes

### Caso 1: Mostrar lista actualizada en dashboard
```bash
# Forma 1: Endpoint especializado (recomendado)
curl -X GET "http://localhost:8080/api/alumnos/semestre-actual?limit=100"

# Forma 2: Endpoint general con parámetro
curl -X GET "http://localhost:8080/api/alumnos?soloActivo=true&limit=100"
```

### Caso 2: Dropdown de alumnos por carrera
```bash
curl -X GET "http://localhost:8080/api/alumnos/semestre-actual?carrera=Ingeniería%20en%20Sistemas&limit=1000"
```

### Caso 3: Búsqueda de alumnos con filtros complejos
```bash
curl -X GET "http://localhost:8080/api/alumnos?estado=ACTIVO&carrera=Administración&page=1&limit=50"
```

### Caso 4: Auditoría - Ver alumnos inactivos del semestre anterior
```bash
curl -X GET "http://localhost:8080/api/alumnos?semestreId=4&estado=INACTIVO"
```

---

## Respuestas por Escenario

### Escenario 1: No hay semestre activo
```json
{
  "status": "success",
  "data": null,
  "message": "No hay semestre activo configurado en el sistema"
}
```

**Acción**: Configurar un semestre como activo en `/api/semestres`

### Escenario 2: No hay alumnos en el semestre activo
```json
{
  "status": "success",
  "data": {
    "content": [],
    "page": 1,
    "limit": 20,
    "totalElements": 0,
    "totalPages": 0
  },
  "message": "Alumnos activos del semestre 2025-2026-F1 - Total: 0"
}
```

### Escenario 3: Error en servidor
```json
{
  "status": "success",
  "data": null,
  "message": "Error: [Descripción del error]"
}
```

---

## Notas Importantes

1. **Semestre Activo Automático**:
   - Los endpoints obtienen automáticamente el semestre activo
   - No es necesario especificar el ID del semestre
   - Reduce errores de parámetros incorrectos

2. **Filtrado Correcto**:
   - El parámetro `soloActivo=true` fuerza estado = ACTIVO
   - No hay alumnos INACTIVOS en `/api/alumnos/semestre-actual`
   - Los alumnos inactivos se muestran con `/api/alumnos?estado=INACTIVO`

3. **Performance**:
   - El límite por defecto es 20, pero puede aumentarse hasta 1000
   - Para listas grandes, usar `limit` apropiado
   - Usar paginación para manejo eficiente de memoria

4. **Relaciones**:
   - Los alumnos incluyen información del tutor asignado
   - Si un alumno no tiene tutor, la relación será null
   - Se muestran todos los campos del DTO `AlumnoResponseDTO`

---

## Integración en Frontend

### Ejemplo en JavaScript/React

```javascript
// Obtener lista actualizada de alumnos
async function obtenerAlumnosActuales() {
  const response = await fetch('http://localhost:8080/api/alumnos/semestre-actual?limit=100');
  const result = await response.json();
  return result.data.content;
}

// Con filtro de carrera
async function obtenerAlumnosPorCarrera(carrera) {
  const response = await fetch(
    `http://localhost:8080/api/alumnos/semestre-actual?carrera=${encodeURIComponent(carrera)}&limit=100`
  );
  const result = await response.json();
  return result.data.content;
}

// Con paginación
async function obtenerAlumnosPaginados(page, limit) {
  const response = await fetch(
    `http://localhost:8080/api/alumnos/semestre-actual?page=${page}&limit=${limit}`
  );
  const result = await response.json();
  return result.data;
}
```

---

## Conclusión

Con estos endpoints actualizados, puedes:

✓ Obtener automáticamente el semestre activo
✓ Listar alumnos del semestre actual de forma simple
✓ Filtrar por carrera, estado, etc.
✓ Implementar paginación eficiente
✓ Mantener una vista actualizada y consistente

El endpoint `/api/alumnos/semestre-actual` es la forma recomendada para obtener una **lista actualizada de estudiantes activos** en el semestre actual.

