# Documentación Técnica - Sistema de Tutorías

## Arquitectura
Aplicación Spring Boot 3 con arquitectura hexagonal ligera. Las capas principales son:
- **Application**: servicios, DTOs y lógica de orquestación.
- **Domain**: entidades JPA, enums y reglas de negocio puntuales.
- **Infrastructure**: configuración, controladores REST y adaptadores.

## Modelo de Dominio
- `Alumno`, `Tutor`, `Asignacion`, `AlumnoInactivo`, `ProcesoAsignacion`.
- `Alumno` referencia opcional a `Tutor` como tutor actual.
- `Asignacion` vincula alumno, tutor, semestre y tipo.
- `Tutor` mantiene `capacidadMax` y `cargaActual` (sincronizada con asignaciones activas).

## Módulos del Sistema
- **Auth**: autenticación JWT con cookies HttpOnly.
- **Asignaciones**: carga masiva desde Excel, reasignaciones, liberación de cupos.
- **Tutorías**: operaciones CRUD de tutores y alumnos.
- **Reportes**: exportación de reportes (PDF/Excel).

## Autenticación y Seguridad
- Cookies HttpOnly con JWT para autenticación.
- Endpoints protegidos bajo prefijo `/api`.
- Roles definidos en base de datos.

## API Endpoints
Consultar colección `backend/docs/insomnia_collection.json` para ejemplos de solicitudes y respuestas.

## Reglas de Negocio Relevantes
- `cargaActual` de tutores debe reflejar siempre el número de asignaciones activas.
- Cada asignación es única por alumno, tutor y semestre.
- Validaciones estrictas para evitar exceder `capacidadMax`.
- Reasignaciones incrementan el contador de cambios por alumno.

## Base de Datos
Scripts SQL en `backend/docs/sql` establecen restricciones e índices requeridos. Para sincronizar datos legados ejecutar `03_corregir_carga_actual.sql`.

## Decisiones de Diseño
- Sincronización explícita de `cargaActual` mediante `TutorSincronizacionService` para evitar desfaces.
- Respuesta de asignación incluye lista de errores detallados y estadísticas de carga.
- Auditoría centralizada con `AuditoriaService` usando transacciones independientes.

## Glosario
- **Carga Actual**: número de alumnos asignados actualmente a un tutor.
- **Capacidad Máxima**: cantidad máxima de alumnos que puede atender un tutor.
- **Proceso de Asignación**: ejecución de carga masiva de alumnos desde Excel.
