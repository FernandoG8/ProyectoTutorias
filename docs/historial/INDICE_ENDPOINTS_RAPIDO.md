# 🚀 ÍNDICE RÁPIDO DE ENDPOINTS - SISTEMA DE TUTORÍAS

**Fecha:** 20 de Noviembre de 2024 | **Total:** 72 Endpoints | **Estado:** ✅ Completo

---

## 📍 NAVEGACIÓN RÁPIDA

### Módulo 1: Autenticación (5 endpoints)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/auth/login` | Login con credenciales |
| POST | `/auth/register` | Crear nuevo usuario (admin) |
| POST | `/auth/refresh` | Renovar token |
| POST | `/auth/logout` | Cerrar sesión |
| GET | `/auth/me` | Info usuario actual |

### Módulo 2: Alumnos (8 endpoints)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/alumnos` | Listar con filtros (paginado) |
| GET | `/api/alumnos/semestre-actual` | Solo activos del semestre actual |
| GET | `/api/alumnos/{id}` | Obtener por ID |
| POST | `/api/alumnos` | Crear nuevo |
| PUT | `/api/alumnos/{id}` | Actualizar (PUT full) |
| PATCH | `/api/alumnos/{id}` | Actualizar (PATCH partial) |
| DELETE | `/api/alumnos/{id}` | Eliminar |

### Módulo 2B: Búsqueda de Alumnos (3 endpoints)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/alumnos/search` | Búsqueda avanzada con ranking |
| GET | `/api/alumnos/autocomplete` | Autocomplete (max 10 resultados) |
| GET | `/api/alumnos/by-matricula/{matricula}` | Búsqueda exacta por matrícula |

### Módulo 3: Tutores (7 endpoints)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/tutores` | Listar con filtros |
| GET | `/api/tutores/{id}` | Obtener por ID |
| GET | `/api/tutores/{tutorId}/alumnos` | Alumnos asignados al tutor |
| POST | `/api/tutores` | Crear nuevo |
| PUT | `/api/tutores/{id}` | Actualizar |
| DELETE | `/api/tutores/{id}` | Eliminar |

### Módulo 3B: Búsqueda de Tutores (2 endpoints)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/tutores/search` | Búsqueda avanzada |
| GET | `/api/tutores/autocomplete` | Autocomplete |

### Módulo 4: Semestres (12 endpoints)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/semestres` | Listar todos |
| GET | `/api/semestres/ultimos` | Últimos N semestres |
| GET | `/api/semestres/activo` | Obtener semestre activo |
| GET | `/api/semestres/{id}` | Obtener por ID |
| GET | `/api/semestres/codigo/{codigo}` | Obtener por código |
| GET | `/api/semestres/existe/codigo/{codigo}` | Verificar si existe |
| GET | `/api/semestres/{id}/estadisticas` | Estadísticas del semestre |
| POST | `/api/semestres` | Crear nuevo |
| PUT | `/api/semestres/{id}` | Actualizar |
| POST | `/api/semestres/{id}/activar` | Activar semestre |
| POST | `/api/semestres/{id}/desactivar` | Desactivar semestre |
| DELETE | `/api/semestres/{id}` | Eliminar |

### Módulo 5: Asignaciones (7 endpoints) ⭐ CORE
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/asignaciones/iniciar` | Cargar Excel (background) |
| POST | `/api/asignaciones/validar-excel` | Validar Excel sin ejecutar |
| POST | `/api/asignaciones/ejecutar` | Ejecutar asignación |
| GET | `/api/asignaciones/proceso/{procesoId}` | Estado del proceso |
| GET | `/api/asignaciones/proceso/{procesoId}/alertas` | Alertas del proceso |
| GET | `/api/asignaciones/procesos` | Listar todos procesos |
| POST | `/api/asignaciones/cambio-tutor` | Cambio manual de tutor |

### Módulo 6: Alumnos Inactivos (4 endpoints)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/alumnos-inactivos/pendientes` | Pendientes de motivo |
| GET | `/api/alumnos-inactivos` | Listar todos |
| PUT | `/api/alumnos-inactivos/{id}/motivo` | Asignar motivo |
| PATCH | `/api/alumnos-inactivos/{id}/resolver-motivo` | Resolver inactividad |

### Módulo 7: Mantenimiento (6 endpoints)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/mantenimiento/diagnostico` | Diagnóstico completo |
| GET | `/api/mantenimiento/validar-integridad` | Validar integridad |
| GET | `/api/mantenimiento/pendientes-resolucion` | Pendientes |
| POST | `/api/mantenimiento/sincronizar-tutores` | Sincronizar cargas |
| POST | `/api/mantenimiento/resolver-masivamente` | Resolver masivo |
| POST | `/api/mantenimiento/liberar-cupos-seguro` | Liberar cupos |

### Módulo 8: Dashboard (5 endpoints)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/dashboard/estadisticas` | Estadísticas generales |
| GET | `/api/dashboard/distribucion-tutores` | Distribución por tutor |
| GET | `/api/dashboard/procesos-recientes` | Últimos procesos |
| GET | `/api/dashboard/semestre-activo` | Info semestre actual |
| GET | `/api/dashboard/health` | Health check |

### Módulo 9: Reportes (5 endpoints)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/reportes/por-carrera` | Reporte por carrera |
| GET | `/api/reportes/tutores/{id}/alumnos/exportar` | Exportar alumnos tutor |
| GET | `/api/reportes/carreras/{codigo}/exportar` | Exportar carrera |
| GET | `/api/reportes/carreras/exportar-todos` | Exportar todos (ZIP) |
| GET | `/api/reportes/tutores/exportar-todos` | Exportar todos los tutores (ZIP) |

### Módulo 10: Limpieza de Datos (6 endpoints) ⚠️ ADMIN
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/cleanup/detectar/{semestreId}` | Detectar corrupción |
| GET | `/api/cleanup/detectar-global` | Detectar global |
| GET | `/api/cleanup/health` | Health check |
| POST | `/api/cleanup/limpiar/{semestreId}` | Limpiar semestre |
| POST | `/api/cleanup/limpiar-global` | Limpiar global |
| POST | `/api/cleanup/recalcular` | Recalcular cargas |

---

## 🔐 SEGURIDAD RESUMIDA

### Autenticación
- **Mecanismo:** JWT en HTTP-only cookies
- **Login:** `POST /auth/login`
- **Logout:** `POST /auth/logout`
- **Refresh:** `POST /auth/refresh`

### Autorización
- **Sin protección:** `/health`, `/auth/login`
- **Autenticados:** Mayoría de endpoints
- **Rol COORDINADOR_TUTORIAS:** `/auth/register`, asignaciones
- **Rol ADMIN:** Endpoints `/cleanup`

---

## 📊 CÓDIGOS HTTP COMUNES

| Código | Significado | Endpoints |
|--------|-------------|-----------|
| **200** | OK | GET, PUT, PATCH, POST exitosos |
| **201** | Created | POST de creación exitosa |
| **202** | Accepted | Procesos en background |
| **204** | No Content | DELETE exitoso |
| **400** | Bad Request | Validación fallida |
| **401** | Unauthorized | Token inválido/expirado |
| **403** | Forbidden | Rol insuficiente |
| **404** | Not Found | Recurso no existe |
| **409** | Conflict | Duplicado o restricción violada |
| **500** | Server Error | Error interno |

---

## 🔄 FLUJOS MÁS COMUNES

### Flujo 1: Asignación Completa
```
1. POST /auth/login
2. POST /api/asignaciones/iniciar (con Excel)
3. GET /api/asignaciones/proceso/{id} (monitorear)
4. GET /api/asignaciones/proceso/{id}/alertas (ver problemas)
5. GET /api/alumnos?semestreId=X (verificar)
```

### Flujo 2: Cambio de Tutor Manual
```
1. GET /api/alumnos/autocomplete?q=nombre (buscar)
2. GET /api/tutores?disponibles=true (opciones)
3. POST /api/asignaciones/cambio-tutor
4. POST /api/mantenimiento/sincronizar-tutores
```

### Flujo 3: Validar antes de Asignar
```
1. POST /api/asignaciones/validar-excel (revisar)
2. POST /api/asignaciones/ejecutar (si está ok)
```

### Flujo 4: Generar Reporte
```
1. GET /api/reportes/por-carrera?semestreAcademico=X
2. GET /api/reportes/tutores/{id}/alumnos/exportar?formato=EXCEL
```

---

## 🛠️ PARÁMETROS FRECUENTES

### Query Parameters Comunes
| Parámetro | Tipo | Ejemplos |
|-----------|------|----------|
| `page` | int | 1, 2, 3 (default: 1) |
| `limit` | int | 20, 50, 100 (default: 20) |
| `estado` | enum | ACTIVO, INACTIVO |
| `carrera` | string | ISC, ADM, IEM |
| `semestreId` | int | 1, 2, 3 |
| `semestreAcademico` | string | 2024-04, 2024-03 |
| `formato` | enum | EXCEL, PDF |
| `severidad` | enum | INFO, WARNING, ERROR |
| `disponibles` | boolean | true, false |
| `activos` | boolean | true, false |

### Enums Importantes
```
EstadoAlumno: ACTIVO, INACTIVO
EstadoTutor: ACTIVO, INACTIVO
EstadoProceso: INICIADO, VALIDANDO, ORDENANDO, ASIGNANDO, COMPLETADO, FALLIDO
SeveridadAlerta: INFO, WARNING, ERROR, CRITICAL
Rol: COORDINADOR_TUTORIAS, SECRETARIO_ACADEMICO, ADMIN
```

---

## 📥 FORMATOS DE REQUEST

### Form-Data (Multipart)
```
POST /api/asignaciones/iniciar
Content-Type: multipart/form-data

archivo=[binary]
semestreAcademico=2024-04
usuario=coordinador
```

### JSON Body
```
POST /api/alumnos
Content-Type: application/json

{
  "matricula": "202401001",
  "nombre": "Carlos López",
  "carrera": "ISC",
  "semestre": 3,
  "estado": "ACTIVO",
  "semestreId": 2
}
```

### Cookies de Autenticación
```
Cookie: tutorias_access_token=eyJhbGc...
```

---

## 📤 FORMATO ESTÁNDAR DE RESPUESTA

### Exitosa
```json
{
  "success": true,
  "data": {...},
  "message": "Descripción",
  "timestamp": "2024-11-20T14:00:00.000Z"
}
```

### Error
```json
{
  "success": false,
  "message": "Descripción del error",
  "errors": ["Error 1", "Error 2"],
  "timestamp": "2024-11-20T14:00:00.000Z"
}
```

---

## 📝 NOTAS CRÍTICAS

⚠️ **Semestre Activo:** Requerido para la mayoría de operaciones
⚠️ **Paginación:** page es 1-based, limit máximo 100
⚠️ **Background:** Endpoints /iniciar retornan 202 (monitorear con GET /proceso)
⚠️ **Excel:** Estructura: Matrícula, Nombre, Carrera, Semestre
⚠️ **Carga de Tutores:** Se sincroniza automáticamente en algunos endpoints
⚠️ **Permisos:** ADMIN solo para /cleanup, COORDINADOR para /auth/register

---

## 🔗 REFERENCIAS

**Documentación Completa:** `DOCUMENTACION_COMPLETA_ENDPOINTS_BACKEND.md`

**Por Módulo:**
- Autenticación: Ver sección "MÓDULO 1"
- Alumnos: Ver sección "MÓDULO 2"
- Tutores: Ver sección "MÓDULO 3"
- Semestres: Ver sección "MÓDULO 4"
- Asignaciones: Ver sección "MÓDULO 5"
- Inactividad: Ver sección "MÓDULO 6"
- Mantenimiento: Ver sección "MÓDULO 7"
- Dashboard: Ver sección "MÓDULO 8"
- Reportes: Ver sección "MÓDULO 9"
- Limpieza: Ver sección "MÓDULO 10"

---

## ✅ CHECKLIST DE USO

- [ ] Revisar autenticación (POST /auth/login)
- [ ] Entender flujo de semestre activo
- [ ] Conocer estructura de asignaciones
- [ ] Familiarizarse con códigos HTTP
- [ ] Revisar validaciones por endpoint
- [ ] Entender límites de paginación
- [ ] Configurar Excel para uploads
- [ ] Implementar retry para background jobs
- [ ] Monitorear con /api/cleanup/health
- [ ] Revisar logs en /api/mantenimiento/diagnostico

---

**Última Actualización:** 20 de Noviembre de 2024
**Versión:** 1.0
**Estado:** ✅ LISTO PARA USAR

