# ProyectoTutoriasBackend - Sistema de Gestión de Titorías

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Version](https://img.shields.io/badge/version-1.0.0--REFACTORED-blue)
![Java](https://img.shields.io/badge/java-21-orange)
![Spring Boot](https://img.shields.io/badge/spring--boot-3.3.0-green)
![React](https://img.shields.io/badge/react-18-blue)

## 📖 Descripción General

Sistema REST API + Frontend para la gestión integral de titorías académicas. Administra alumnos, tutores, asignaciones, semestres, reportes y diagnóstico del sistema.

**[👉 Ver Documentación Completa →](./docs/README.md)**

---

## ⚡ Inicio Rápido

### Backend

```bash
# 1. Clonar y navegar
git clone <repo-url>
cd ProyectoTutoriasBackend/backend

# 2. Compilar
mvn clean compile

# 3. Ejecutar
mvn spring-boot:run
```

**API disponible en**: `http://localhost:8080`

### Frontend

```bash
# 1. Instalar dependencias
cd frontend/tutoring-frontend
npm install

# 2. Ejecutar desarrollo
npm run dev
```

**Frontend disponible en**: `http://localhost:5173`

**[📖 Ver documentación completa del frontend →](./docs/frontend/README.md)**

### Con Docker

```bash
docker-compose up -d
```

---

## 📚 Documentación Rápida

| Tema | Ubicación |
|------|-----------|
| Documentación Completa | [docs/README.md](./docs/README.md) |
| **Documentación Frontend** | **[docs/frontend/README.md](./docs/frontend/README.md)** |
| Endpoints API | [docs/api/](./docs/api/) |
| Arquitectura | [docs/arquitectura/](./docs/arquitectura/) |
| Guías Prácticas | [docs/guias/](./docs/guias/) |
| Despliegue | [docs/deployment/](./docs/deployment/) |
| Mantenimiento | [docs/mantenimiento/](./docs/mantenimiento/) |

---

## 🏗️ Estructura del Proyecto

```
ProyectoTutoriasBackend/
├── backend/                        ← Spring Boot 3.3, Java 21
│   ├── src/main/java/             Código principal
│   ├── src/test/java/             Tests
│   ├── pom.xml                    Dependencias Maven
│   └── application.properties      Configuración
├── frontend/tutoring-frontend/     ← React 18 + Vite
│   ├── src/
│   ├── package.json
│   └── vite.config.ts
├── docs/                           ← Documentación centralizada
│   ├── api/                        Referencia de endpoints
│   ├── arquitectura/               Diseño del sistema
│   ├── frontend/                   Documentación del frontend React
│   ├── guias/                      Tutoriales
│   ├── mantenimiento/              Operaciones
│   └── deployment/                 Despliegue
└── README.md                       ← Este archivo
```

---

## 🛠️ Stack Tecnológico

### Backend
- **Java 21** - Lenguaje principal
- **Spring Boot 3.3** - Framework
- **Spring Data JPA** - Acceso a datos
- **Spring Security** - Autenticación (JWT + Cookies)
- **Maven** - Gestión de dependencias
- **PostgreSQL/MySQL** - Base de datos
- **Hibernate** - ORM
- **JUnit 5 + Mockito** - Testing

### Frontend
- **React 18** - UI library
- **Vite 5** - Build tool
- **TypeScript** - Type safety
- **TailwindCSS** - Estilos
- **React Router** - Navegación
- **TanStack Query** - State management
- **Zustand** - Global state
- **Axios** - HTTP client

---

## 🔐 Características Principales

✅ **Gestión Completa**
- CRUD de alumnos, tutores y semestres
- Asignación inteligente de tutores
- Seguimiento de capacidad

✅ **Seguridad**
- Autenticación JWT
- Autorización por roles
- Validación de entrada
- CORS configurado

✅ **Reportes**
- Exportación a Excel
- Generación de PDF
- Estadísticas por semestre

✅ **Mantenimiento**
- Diagnóstico del sistema
- Sincronización automática
- Validación de integridad

---

## 📋 Requisitos Previos

### Backend
- Java 21+
- Maven 3.8+
- PostgreSQL 12+ o MySQL 8+

### Frontend
- Node.js 18+
- npm 9+

### Ambos
- Docker (opcional, para despliegue)
- Git

---

## 🚀 Configuración

### Backend
```bash
cd backend
# Editar aplicación.properties con:
spring.datasource.url=jdbc:mysql://localhost:3306/gestion_tutorias
spring.datasource.username=root
spring.datasource.password=tu_contraseña
jwt.secret=tu_jwt_secret_aqui
```

### Frontend
```bash
cd frontend/tutoring-frontend
# El archivo .env es opcional (usa valores por defecto)
echo "VITE_API_URL=http://localhost:8080" > .env
```

---

## 🎯 Endpoints Principales

```
# Alumnos
GET    /api/alumnos
POST   /api/alumnos
GET    /api/alumnos/{id}
PUT    /api/alumnos/{id}
DELETE /api/alumnos/{id}

# Tutores
GET    /api/tutores
POST   /api/tutores
GET    /api/tutores/{id}
PUT    /api/tutores/{id}

# Semestres
GET    /api/semestres
POST   /api/semestres
GET    /api/semestres/activo
POST   /api/semestres/{id}/activar

# Mantenimiento
GET    /api/mantenimiento/diagnostico
GET    /api/mantenimiento/validar-integridad
POST   /api/mantenimiento/sincronizar-tutores

# Reportes
GET    /api/reportes/alumnos
GET    /api/reportes/carreras
POST   /api/reportes/exportar
```

**[Ver referencia completa →](./docs/api/)**

---

## 🧪 Testing

```bash
cd backend

# Ejecutar todos los tests
mvn test

# Tests específicos
mvn test -Dtest=AlumnoControllerTest

# Con cobertura
mvn test jacoco:report
```

---

## 📝 Notas Importantes

### Cambios Recientes (v1.0.0-REFACTORED)
- ✅ Estandarización de respuestas API con `ApiResponse<T>`
- ✅ 4 nuevos manejadores de excepciones
- ✅ DTOs tipificados para todas las respuestas
- ✅ Eliminación de `Map<String, Object>`

[Leer detalles →](./docs/guias/refactorizacion-2025.md)

### Base de Datos
El proyecto incluye scripts SQL en `backend/docs/sql/`:
- Creación de schema y tablas
- Datos iniciales
- Dumps de ejemplo

### Seguridad
- Las cookies JWT se envían como HttpOnly
- Frontend usa `withCredentials: true` en Axios
- CORS está configurado para desarrollo y producción

---

## 🐛 Reportar Problemas

Encontraste un error? Abre un issue en GitHub con:
- Descripción clara
- Pasos para reproducir
- Logs/stacktraces
- Ambiente (OS, versiones)

---

## 🤝 Contribuir

1. Fork del proyecto
2. Crear rama feature (`git checkout -b feature/nueva-feature`)
3. Commit cambios (`git commit -m 'feat: descripción'`)
4. Push (`git push origin feature/nueva-feature`)
5. Abrir Pull Request

Consulta [guía de contribución →](./docs/guias/02-contribucion.md)

---

## 📞 Soporte

- **Documentación**: [./docs/README.md](./docs/README.md)
- **Problemas frecuentes**: [Troubleshooting](./docs/guias/01-quick-start.md#troubleshooting)
- **Issues**: GitHub Issues
- **Email**: soporte@proyecto-tutorias.edu

---

## 📄 Licencia

Proyecto académico - Universidad Autónoma de Campeche

---

<div align="center">

**[📚 Documentación Completa](./docs/README.md)** | **[🔗 Endpoints API](./docs/api/)** | **[🏗️ Arquitectura](./docs/arquitectura/)** | **[⚛️ Frontend](./docs/frontend/README.md)**

Versión 1.0.0-REFACTORED (2025-11-17)

Made with ❤️ para la gestión de titorías académicas

</div>
