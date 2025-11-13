# Sistema de Gestión de Tutorías

## Backend

### Stack Tecnológico
- Java 21
- Spring Boot 3.3
- Spring Data JPA / Hibernate
- Spring Security con JWT en cookies HttpOnly
- Maven
- MySQL (producción) / H2 (tests)

---

## Requisitos Previos (Backend)
- Java 21 (`java -version`)
- Maven 3.9+
- Servidor MySQL 8.x

---

## Instalación Backend
1. Clonar el repositorio.
2. Crear una base de datos vacía en MySQL.
3. Ejecutar los scripts en `backend/docs/sql/` (si alguno falla no afecta).

## Importacion con el dump
1. Ejecutar el dump.sql
2. Se generara el schema y las tablas 
- (Existe el dump, pueden importarlo. Ya incluye la creacion del Schema).
## Importacion con el dump
1. Ejecutar el dumpsinSchema.sql
2. Se generara el schema y las tablas 
- (Existe el dump, pueden importarlo.No  incluye la creacion del Schema deberan crear la base de datos con: **`gestion_tutorias`**).



---

## Configuración Backend
Editar `backend/src/main/resources/application.properties` o usar:

- `application-dev.properties`
- `application-prod.properties`

Variables importantes (llenalos con tus credenciales de tu gestor de base de datos): 
```
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=
jwt.secret=
```

---

## Ejecutar Backend
Desde `backend/`:
```bash
./mvnw spring-boot:run
```

---

## Ejecutar Tests Backend
```bash
./mvnw test
```

---

## Estructura del Proyecto Backend
```
src/main/java/com/universidad/tutorias   Código principal
src/test/java/com/universidad/tutorias   Tests
docs/                                    Documentación e Insomnia
docs/sql/                                Scripts SQL
```

---

## Convenciones Backend
- Uso de `@RequiredArgsConstructor`
- Validaciones con `jakarta.validation`
- `cargaActual` de tutores sincronizada con asignaciones activas  

---

# Frontend (React + Vite + Tailwind)

## Stack Tecnológico
- React 18
- Vite 5
- TypeScript
- TailwindCSS 3.x
- React Router
- TanStack Query
- Zustand
- Axios
- Shadcn/UI

---

## Requisitos Previos (Frontend)
- Node.js 18+
- npm 9+

Verificación:
```bash
node -v
npm -v
```

---

## Instalación del Frontend
```bash
cd frontend/tutoring-frontend
npm install
```

Crear archivo (Verifica si es necesari, creo que solo con e build jala) `.env`:
```
VITE_API_URL=http://localhost:8080
```

---

## Ejecutar el Frontend
```bash
npm run dev
```

Disponible en:  
👉 http://localhost:5173/

---

## Compilar Producción
```bash
npm run build
```

Genera:
```
dist/
```

---

## Estructura del Proyecto Frontend
```
tutoring-frontend/
│ src/
│   pages/           Vistas principales
│   components/      Componentes UI
│   services/        Consumidores API
│   store/           Zustand (auth, UI, sesión)
│   hooks/           Hooks
│   lib/             Utilidades
│   types/           Tipos globales
│   App.tsx          Rutas
```

---

## Autenticación
- Cookies HttpOnly manejadas automáticamente por el navegador
- Axios configurado con `withCredentials: true`
- Manejo de sesión en Zustand

Ejemplo:
```ts
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  withCredentials: true,
});
```

---

## Consideraciones CORS
Si React no puede conectarse:

### Backend debe permitir:
- `Access-Control-Allow-Origin: http://localhost:5173`
- `Access-Control-Allow-Credentials: true`

### Frontend debe usar:
```ts
withCredentials: true
```

---

## Licencia
Proyecto académico — Universidad Autónoma de Campeche.
