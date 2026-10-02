# Backend – Proceso de Asignación (Resumen 2025)

## 1. Tecnologías y base
- **Spring Boot 3 / Java 21** con JPA/Hibernate (PostgreSQL).
- DTOs tipados (records/clases) para requests/responses.
- Seguridad: roles, endpoints protegidos (`COORDINADOR_TUTORIAS`, `SECRETARIO_ACADEMICO`).

## 2. Flujo concreto (end-to-end) de asignación masiva
1) **Validar Excel** (`POST /api/asignaciones/validar-excel`)
   - Parseo + normalización local (carrera, matrícula, nombre, semestre).
   - Sin escrituras en BD.
   - Respuesta:
     - `status`: `OK` (solo entonces hay `data`), `ERROR` o `WARNING`.
     - `data`: `AlumnoValidadoDTO[]` listos para ejecutar.
     - `errors`: `excelErrors` con (`rowNumber`, `column`, `message`, `value`).
   - Errores 400 se devuelven con código `EXCEL_VALIDATION_ERROR` y `excelErrors` para UI.

2) **Ejecutar** (`POST /api/asignaciones/ejecutar`)
   - Entrada: `AlumnoValidadoDTO[]` validados + `semestreId`.
   - Pre-paso: `tutorSincronizacionService.recalcularCargaTodosLosTutores()` y agrupado de tutores disponibles por carrera.
   - Orden: alumnos ordenados por carrera y matrícula (stable).
   - Asignación uno a uno con transacciones REQUIRES_NEW:
     - Selección de tutor:
       1. Tutor de misma carrera con capacidad (`findDisponibles` agrupado).
       2. Tutor de carrera compatible (`MatrizAfinidad`), genera alerta de asignación cruzada.
       3. Tutor con menor carga global (fallback), genera alerta.
       4. Si ninguno: `SinTutorDisponibleException`.
     - Alumno existente:
       - Bloquea por semáforo de asignación (findByAlumnoAndSemestreId → `DuplicadoException` si ya tiene asignación).
       - Intenta mantener tutor previo si está activo y con cupo (lock `findByIdForUpdate`); si no, reasigna y genera alerta de reasignación forzada.
     - Alumno nuevo: se crea entidad `Alumno`, estado ACTIVO, carrera normalizada.
     - Crea `Asignacion` con `Semestre` (relación) + `semestreAcademico` (legacy).
     - Actualiza carga de tutor solo si no mantuvo tutor previo; valida que `cargaActual < capacidadMax` o lanza `CapacidadExcedidaException`.
     - Auditoría: `auditoriaService.registrarLog` por asignación (ids de tutor/semestre, tipo de asignación).
   - Lotes: cada 100 se hace `flush/clear` y se actualiza el registro de proceso.
   - Respuesta agregada `ResultadoAsignacion`: totales, errores, alertas, asignaciones, estadísticas (por tutor).
   - Errores manejados por alumno: duplicado, capacidad excedida, sin tutor disponible, error inesperado (log + `errorValidacion`).

3) **Cambio de tutor** (`POST /api/asignaciones/cambio-tutor`)
   - Valida tutor destino, capacidad, y bloquea por alumno/semestre si aplica.
   - Ajusta carga de tutor origen/destino y registra auditoría.

## 3. Capas principales
- **Controller** (`AsignacionController`, `CambioTutorController`): expone endpoints y códigos de estado; soporta errores 400 estructurados (`excelErrors`, `fieldErrors`).
- **Service**:
  - `AsignacionService`: flujo de validación y ejecución; usa repositorios para tutores, alumnos, asignaciones; recalcula cargas.
  - `InactivacionService`/`ReasignacionService` (relacionado): preserva capacidad y valida restricciones cuando hay inactivaciones.
- **Repository**:
  - `AsignacionRepository`: consultas por semestre, carrera, tutor; detección de tutores con cupo.
  - `TutorRepository`: tutores disponibles/activos, ids, locking pesimista para operaciones críticas.
- **DTOs principales**:
  - `AlumnoValidadoDTO`: alumno listo para asignar (semestreId numérico, carrera normalizada).
  - `ExcelErrorDTO`: detalle de error por fila/columna.
  - `EjecucionAsignacionResponse`: resumen y errores por alumno si aplica.

## 4. Validación y errores (profundo)
- 400 `EXCEL_VALIDATION_ERROR`: `excelErrors[{rowNumber, column, message, value}]` para UI; el interceptor frontend mapea a una tabla de errores.
- 422 `fieldErrors`: request inválido (semestreId, archivo, tipos).
- 409/422 negocio: `CapacidadExcedidaException`, `SinTutorDisponibleException`, `DuplicadoException` (alumno ya asignado en semestre).
- Alertas de proceso: asignación cruzada, reasignación forzada, capacidad; se almacenan y se devuelven en el resultado.

## 5. Auditoría y métricas
- Se registran procesos de asignación con inicio/fin, archivo origen, usuario ejecutor, totales procesados y errores.
- Cargas de tutor se recalculan para mantener consistencia.

## 6. Reglas clave de capacidad y consistencia
- No asignar si `cargaActual >= capacidadMax` (se valida después de lock `findByIdForUpdate`).
- Antes de cada ejecución: `recalcularCargaTodosLosTutores` y, por tutor, `recalcularCargaTutor(id)` tras lock para datos frescos.
- Mantener tutor previo solo si sigue activo y con cupo; de lo contrario, reasignar y registrar alerta.
- Consultas que excluyen egresados/baja definitiva para conteos cuando aplica (repositorio dedicado).

## 7. Reutilización para reportes
- `ReporteConsultaService` obtiene asignaciones filtradas por tutor/carrera y semestre (con/sin filtro `semestreAcademico`), mapea a `ReporteAlumnoDTO` y arma `ReporteContexto` para Strategy PDF/Excel.

## 8. Extensibilidad y puntos de anclaje
- Nuevas validaciones/columnas de Excel: agregar en mapper/DTO y devolver en `excelErrors` para visibilidad inmediata.
- Nuevos criterios de selección de tutor: extender `seleccionarTutorDisponible` (respeta orden carrera → afinidad → menor carga).
- Nuevos formatos de reporte: añadir Strategy en `ReporteStrategyFactory`.
- Monitoreo: ajustar tamaño de lote `BATCH_SIZE`, logs y métricas en `ProcesoAsignacion`.
