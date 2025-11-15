# Documentación del Sistema de Titorías - ProyectoTutoriasBackend

## 📚 Índice de Documentación

Bienvenido a la documentación centralizada del proyecto. Esta carpeta contiene toda la información necesaria para entender, desarrollar y mantener el sistema.

### 🎯 Inicio Rápido

- **[README Principal](../README.md)** - Descripción general del proyecto
- **[Quick Start Guide](./guias/01-quick-start.md)** - Configuración inicial del ambiente

### 🏗️ Arquitectura y Diseño

- **[Arquitectura del Sistema](./arquitectura/01-arquitectura-general.md)** - Estructura general y capas
- **[Mapeo de Arquitectura](./arquitectura/02-mapeo-completo.md)** - Detalle de clases y relaciones
- **[Patrones de Diseño](./arquitectura/03-patrones-dto.md)** - DTOs, servicios y repositorios

### 🔌 API REST

- **[Endpoints de Alumnos](./api/01-alumnos.md)** - Gestión de estudiantes
- **[Endpoints de Tutores](./api/02-tutores.md)** - Gestión de tutores
- **[Endpoints de Semestres](./api/03-semestres.md)** - Gestión de períodos académicos
- **[Endpoints de Asignaciones](./api/04-asignaciones.md)** - Asignación de tutores a alumnos
- **[Endpoints de Mantenimiento](./api/05-mantenimiento.md)** - Diagnóstico y sincronización

### 🔧 Mantenimiento y Operaciones

- **[Proceso de Mantenimiento Seguro](./mantenimiento/01-proceso-seguro.md)** - Pasos para mantenimiento
- **[Corrección de Bugs](./mantenimiento/02-correccion-bugs.md)** - Problemas solucionados
- **[Diagnóstico del Sistema](./mantenimiento/03-diagnostico.md)** - Validación de integridad

### 🚀 Despliegue

- **[Docker Deployment](./deployment/01-docker.md)** - Despliegue con Docker
- **[Configuración Producción](./deployment/02-config-produccion.md)** - Setup para producción
- **[Variables de Entorno](./deployment/03-variables-entorno.md)** - Configuración requerida

### 📊 Cambios Recientes

- **[Refactorización Completada](./guias/refactorizacion-2025.md)** - Actualización de respuestas API
- **[Changelog](./CHANGELOG.md)** - Historial de cambios

### 👨‍💻 Desarrollo

- **[Guía de Contribución](./guias/02-contribucion.md)** - Cómo contribuir
- **[Estándares de Código](./guias/03-estandares.md)** - Convenciones del proyecto
- **[Testing](./guias/04-testing.md)** - Pruebas unitarias e integración

---

## 📋 Estructura de Carpetas

```
docs/
├── README.md                    ← Estás aquí
├── CHANGELOG.md                 ← Registro de cambios
├── guias/                       ← Guías prácticas
│   ├── 01-quick-start.md
│   ├── 02-contribucion.md
│   ├── 03-estandares.md
│   ├── 04-testing.md
│   └── refactorizacion-2025.md
├── arquitectura/                ← Diseño del sistema
│   ├── 01-arquitectura-general.md
│   ├── 02-mapeo-completo.md
│   └── 03-patrones-dto.md
├── api/                         ← Documentación de endpoints
│   ├── 01-alumnos.md
│   ├── 02-tutores.md
│   ├── 03-semestres.md
│   ├── 04-asignaciones.md
│   └── 05-mantenimiento.md
├── mantenimiento/               ← Operaciones y diagnóstico
│   ├── 01-proceso-seguro.md
│   ├── 02-correccion-bugs.md
│   └── 03-diagnostico.md
└── deployment/                  ← Despliegue y configuración
    ├── 01-docker.md
    ├── 02-config-produccion.md
    └── 03-variables-entorno.md
```

---

## 🔍 Búsqueda Rápida

¿Buscas información sobre...?

| Tema | Ubicación |
|------|-----------|
| Crear nuevo alumno | [API Alumnos](./api/01-alumnos.md) |
| Asignar tutor | [Endpoints Asignaciones](./api/04-asignaciones.md) |
| Diagnosticar errores | [Diagnóstico](./mantenimiento/03-diagnostico.md) |
| Desplegar en producción | [Docker Deployment](./deployment/01-docker.md) |
| Entender la arquitectura | [Arquitectura General](./arquitectura/01-arquitectura-general.md) |
| Contribuir al código | [Guía de Contribución](./guias/02-contribucion.md) |
| Ver cambios recientes | [Changelog](./CHANGELOG.md) |

---

## 🆘 Soporte

Si no encuentras lo que buscas:

1. Revisa el [Quick Start](./guias/01-quick-start.md)
2. Consulta la [Arquitectura](./arquitectura/01-arquitectura-general.md)
3. Busca en los [Endpoints](./api/) correspondientes
4. Lee el [Changelog](./CHANGELOG.md) para cambios recientes

---

**Última actualización**: 2025-11-14
**Versión del Sistema**: 1.0.0-REFACTORED
