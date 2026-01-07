# ProyectoTutoriasBackend

Sistema integral de gestión de tutorías académicas. Plataforma que conecta estudiantes con tutores mediante asignación inteligente y seguimiento de cargas.

**Versión:** 4.0.0
**Status:** ✅ Fase 4 Completada
**Última actualización:** 19 de Noviembre, 2025

---

## 📁 Estructura del Proyecto

```
ProyectoTutoriasBackend/
├── backend/                 ← Aplicación Spring Boot (Java 21)
│   ├── src/
│   ├── pom.xml
│   └── README.md
├── frontend/                ← Aplicación Angular/React
│   ├── src/
│   ├── package.json
│   └── README.md
├── docs/                    ← Documentación completa
│   ├── README.md
│   ├── asignaciones/        ← Módulo de asignaciones (Fase 4)
│   ├── api/
│   ├── arquitectura/
│   ├── guias/
│   ├── root-docs/           ← Documentos históricos y análisis
│   └── ...
├── .git/                    ← Control de versiones
├── .idea/                   ← Configuración IntelliJ
├── .claude/                 ← Configuración Claude Code
├── .gitignore
├── .gitattributes
└── README.md                ← Este archivo
```

---

## 🚀 Inicio Rápido

### Backend (Spring Boot)
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
→ Backend estará en `http://localhost:8080`

### Frontend
```bash
cd frontend
npm install
npm start
```
→ Frontend estará en `http://localhost:4200`

### Documentación
→ Toda la documentación está en `/docs`
→ Inicio: `docs/README.md`

---

## 📚 Documentación

### 📖 Módulo de Asignaciones (Fase 4 ✅)
La documentación completa del módulo de asignaciones está en `docs/asignaciones/`:

- **[docs/asignaciones/README.md](./docs/asignaciones/README.md)** - Guía principal
- **[docs/asignaciones/01-overview.md](./docs/asignaciones/01-overview.md)** - Visión general
- **[docs/asignaciones/02-arquitectura.md](./docs/asignaciones/02-arquitectura.md)** - Arquitectura
- **[docs/asignaciones/05-flujo-completo.md](./docs/asignaciones/05-flujo-completo.md)** - Flujo end-to-end
- **[docs/asignaciones/09-fase4-ejecucion.md](./docs/asignaciones/09-fase4-ejecucion.md)** - Implementación Fase 4

### 📖 Documentación General
→ **[docs/README.md](./docs/README.md)** - Índice completo de documentación

Incluye:
- Arquitectura del sistema
- API REST endpoints
- Guías de desarrollo
- Despliegue y configuración
- Mantenimiento

### 📖 Documentos Históricos
Análisis y documentos de fases anteriores:
→ **[docs/root-docs/](./docs/root-docs/)** - Documentación de análisis y planificación

---

## 🛠️ Stack Tecnológico

### Backend
- **Spring Boot 3.x** - Framework principal
- **Java 21** - Lenguaje
- **JPA/Hibernate** - ORM
- **PostgreSQL** - Base de datos
- **Maven** - Gestor de dependencias
- **JUnit 5 + Mockito** - Testing

### Frontend
- **Angular/React** - Framework UI
- **TypeScript** - Lenguaje
- **NPM/Yarn** - Gestor de paquetes
- **SCSS** - Estilos

### DevOps
- **Docker** - Containerización
- **Git** - Control de versiones
- **GitHub Actions** - CI/CD (opcional)

---

## 🎯 Fases Implementadas

| Fase | Descripción | Status |
|------|-------------|--------|
| 1 | Lectura de Excel | ✅ Completada |
| 2 | Validación de datos | ✅ Completada |
| 3 | Limpieza y ordenamiento | ✅ Completada |
| 4 | Ejecución de asignaciones | ✅ **IMPLEMENTADA** |
| 5+ | Mejoras futuras | 📋 Planificadas |

---

## 🔌 Endpoints Principales

### Asignaciones
- `POST /api/asignaciones/validar-excel` - Valida y procesa Excel
- `POST /api/asignaciones/ejecutar` - Ejecuta asignaciones validadas

### Otros
- `GET /api/alumnos` - Listar estudiantes
- `GET /api/tutores` - Listar tutores
- `GET /api/semestres` - Listar períodos académicos

→ **Documentación completa:** [docs/api/](./docs/api/)

---

## 👥 Contribución

Para contribuir al proyecto:

1. Lee [docs/guias/02-contribucion.md](./docs/guias/02-contribucion.md)
2. Crea una rama para tu feature: `git checkout -b feature/nombre`
3. Commit tus cambios: `git commit -m "feat: descripción"`
4. Push a tu rama: `git push origin feature/nombre`
5. Abre un Pull Request

→ **Guía completa:** [docs/guias/](./docs/guias/)

---

## 📝 Commit Style

```
feat:  Nueva feature
fix:   Corrección de bug
docs:  Documentación
style: Formato/estilos
refactor: Refactorización
test:  Tests/testing
```

---

## 🔒 Seguridad

- Validación de entrada en todos los endpoints
- Transacciones ACID garantizadas
- Locking pesimista para concurrencia
- Auditoría completa de operaciones
- Sincronización de datos

---

## 📊 Performance

| Operación | Complejidad | Tiempo (150 alumnos) |
|-----------|-------------|----------------------|
| Validación | O(n) | ~500ms |
| Asignación | O(n) | ~2500ms |
| **Total** | **O(n)** | **~3000ms** |

---

## 🆘 Soporte

### Documentación
- **General:** [docs/README.md](./docs/README.md)
- **Asignaciones:** [docs/asignaciones/README.md](./docs/asignaciones/README.md)
- **Troubleshooting:** [docs/asignaciones/24-troubleshooting.md](./docs/asignaciones/24-troubleshooting.md)

### Backend
- **README:** [backend/README.md](./backend/README.md)

### Frontend
- **README:** [frontend/README.md](./frontend/README.md)

---

## 📅 Roadmap

- [x] Fase 1: Lectura de Excel
- [x] Fase 2: Validación de datos
- [x] Fase 3: Limpieza y ordenamiento
- [x] Fase 4: Ejecución de asignaciones
- [ ] Fase 5: Cambio manual de tutor
- [ ] Fase 6: Dashboard y reportes
- [ ] Optimizaciones de performance

---

## 📞 Contacto

Para preguntas o problemas:

1. Consulta la documentación en `/docs`
2. Revisa problemas existentes en issues
3. Crea un nuevo issue con detalles

---

## 📄 Licencia

© 2025 Universidad. Todos los derechos reservados.

---

**Mantente actualizado:**
- 🔔 [Ver cambios recientes](./docs/asignaciones/25-changelog.md)
- 📝 [Ver historial completo](./docs/CHANGELOG.md)

**Última actualización:** 19 de Noviembre, 2025
