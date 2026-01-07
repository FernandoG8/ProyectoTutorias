# Changelog

Todos los cambios notables en este proyecto están documentados en este archivo.

## [1.0.0-REFACTORED] - 2025-11-14

### ✨ Nuevas Características

#### Global Exception Handlers
- Agregados 4 nuevos manejadores de excepciones:
  - `CapacidadExcedidaException` → HTTP 409 CONFLICT
  - `DuplicadoException` → HTTP 409 CONFLICT
  - `SinTutorDisponibleException` → HTTP 409 CONFLICT
  - `AlumnoInactivoException` → HTTP 422 UNPROCESSABLE_ENTITY

#### Typed DTOs para Respuestas
- Creados 6 nuevos DTOs reemplazando `Map<String, Object>`:
  - `DiagnosticoResponse` - Estadísticas del sistema
  - `SincronizacionResponse` - Cambios en tutores
  - `PendienteResolucionResponse` - Alumnos pendientes
  - `ResolucionMasivaResponse` - Resultados de resoluciones
  - `LiberacionCuposResponse` - Estado de liberación
  - `IntegridadResponse` - Validación de integridad

#### Standarización ApiResponse
- Todos los endpoints ahora retornan `ApiResponse<T>`
- Respuestas consistentes en todo el backend
- 16 endpoints actualizados en 2 controladores

### 🔧 Cambios

#### MantenimientoController
- `/api/mantenimiento/diagnostico` - Retorna `DiagnosticoResponse`
- `/api/mantenimiento/sincronizar-tutores` - Retorna `SincronizacionResponse`
- `/api/mantenimiento/pendientes-resolucion` - Retorna `List<PendienteResolucionResponse>`
- `/api/mantenimiento/resolver-masivamente` - Retorna `ResolucionMasivaResponse`
- `/api/mantenimiento/liberar-cupos-seguro` - Retorna `LiberacionCuposResponse`
- `/api/mantenimiento/validar-integridad` - Retorna `IntegridadResponse`

#### SemestreController
- Todos los 12 endpoints ahora envueltos en `ApiResponse<T>`
- Mensajes descriptivos en cada respuesta
- Manejo consistente de errores

### 🐛 Bug Fixes

- Eliminación de `Map<String, Object>` en respuestas API
- Corrección de manejo de excepciones en catch blocks
- Validación centralizada de excepciones

### 📚 Documentación

- Creada carpeta centralizada `/docs/`
- Reorganización completa de documentación
- Nuevo README principal con navegación
- Guías de Quick Start, contribución y estándares
- Documentación de API organizada por secciones
- Guías de mantenimiento y despliegue

### 📊 Cambios Estadísticos

- **Archivos DTOs creados**: 6 nuevos
- **Archivos modificados**: 3 controladores
- **Excepciones manejadas**: 4 nuevas
- **Endpoints estandarizados**: 16
- **Build Status**: ✅ SUCCESS

---

## [1.0.0] - 2025-11-12

### ✨ Características Iniciales

- ✅ CRUD completo de alumnos
- ✅ CRUD de tutores con gestión de capacidad
- ✅ Sistema de asignaciones inteligente
- ✅ Gestión de semestres académicos
- ✅ Reportes en Excel y PDF
- ✅ Diagnóstico y mantenimiento del sistema
- ✅ Autenticación JWT + Cookies HttpOnly
- ✅ Validación de integridad de datos
- ✅ Sincronización automática de cargas

### 🔐 Seguridad

- Spring Security con JWT
- Validación con Jakarta Validation
- CORS configurado
- Roles de usuario (ADMIN, USER)

### 🗄️ Base de Datos

- JPA/Hibernate
- Soporte PostgreSQL/MySQL
- Migrations con Flyway
- Scripts SQL incluidos

---

## Notas de Versión

### Breaking Changes en v1.0.0-REFACTORED

Las respuestas API ahora están envueltas en `ApiResponse<T>`:

**Antes**:
```json
{
  "id": 1,
  "nombre": "Juan",
  "...": "..."
}
```

**Ahora**:
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "nombre": "Juan",
    "...": "..."
  },
  "message": "Descripción de la operación"
}
```

### Migración

Frontend debe actualizar el parsing:
```javascript
// Antes
const data = response.data

// Ahora
const data = response.data.data
```

---

## Plan Futuro

### Próximos Cambios Planeados

- [ ] Agregar paginación a TutorController
- [ ] Agregar paginación a DashboardController
- [ ] Agregar paginación a ReporteController
- [ ] Reorganización de carpetas de DTOs
- [ ] Documentación de Swagger/OpenAPI
- [ ] Caché distribuido (Redis)
- [ ] Notifications en tiempo real (WebSocket)
- [ ] Auditoría completa de operaciones

---

## Cómo Reportar Cambios

Para reportar un nuevo cambio o problema:

1. Crea un issue en GitHub
2. Describe el cambio claramente
3. Proporciona ejemplos si es posible
4. Se revisará en la próxima versión

---

**Última actualización**: 2025-11-14
**Mantenedor**: Equipo de Desarrollo
