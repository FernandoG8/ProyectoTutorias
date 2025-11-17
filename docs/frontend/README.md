# Documentación Frontend - Sistema de Titorías

Bienvenido a la documentación completa del frontend de **Tutolink**, la plataforma web para gestión integral de titorías académicas.

## 📚 Índice Rápido

- **[Inicio Rápido](#inicio-rápido)** - Configuración del ambiente
- **[Arquitectura](#arquitectura)** - Estructura y patrones
- **[Componentes](#componentes)** - Catálogo de componentes UI
- **[Servicios API](#servicios-api)** - Integración con backend
- **[Desarrollo](#desarrollo)** - Guías de desarrollo
- **[Testing](#testing)** - Estrategia de pruebas

---

## 🚀 Inicio Rápido

### Requisitos Previos

- Node.js 18+
- npm 9+
- Git

### Instalación

```bash
# 1. Clonar el repositorio
git clone <repo-url>
cd ProyectoTutoriasBackend/frontend/tutoring-frontend

# 2. Instalar dependencias
npm install

# 3. Configurar variables de entorno (opcional)
echo "VITE_API_URL=http://localhost:8080" > .env

# 4. Ejecutar en desarrollo
npm run dev
```

**El frontend estará disponible en**: `http://localhost:5173`

### Compilar para Producción

```bash
npm run build
npm run preview
```

---

## 🏗️ Arquitectura

### Stack Tecnológico

| Categoría | Tecnología | Versión |
|-----------|-----------|---------|
| Framework | React | 18+ |
| Build Tool | Vite | 5+ |
| Lenguaje | TypeScript | 5.6+ |
| Estilos | TailwindCSS | 3+ |
| Routing | React Router | 6+ |
| State Management | Zustand + TanStack Query | - |
| HTTP Client | Axios | 1.6+ |

### Estructura de Carpetas

```
frontend/tutoring-frontend/
├── src/
│   ├── components/          # Componentes reutilizables
│   │   ├── common/          # Componentes genéricos
│   │   ├── layouts/         # Layouts principales
│   │   ├── modules/         # Módulos específicos
│   │   └── ui/              # Componentes UI base
│   ├── pages/               # Páginas/vistas principales
│   ├── services/            # Servicios API
│   ├── store/               # Estado global (Zustand)
│   ├── lib/                 # Utilidades y helpers
│   ├── constants/           # Constantes del proyecto
│   ├── types/               # Definiciones TypeScript
│   ├── hooks/               # Custom React hooks
│   ├── App.tsx              # Componente raíz
│   └── main.tsx             # Punto de entrada
├── public/                  # Archivos estáticos
├── vite.config.ts           # Configuración Vite
├── tsconfig.json            # Configuración TypeScript
├── tailwind.config.js       # Configuración TailwindCSS
├── package.json             # Dependencias
└── README.md                # Documentación de módulo
```

### Capas de la Aplicación

```
┌─────────────────────────────────────┐
│          Pages/Vistas               │ ← Componentes de página
├─────────────────────────────────────┤
│        Componentes Complejos         │ ← Módulos feature
├─────────────────────────────────────┤
│        Componentes UI Base           │ ← Reutilizables
├─────────────────────────────────────┤
│      Servicios API + Hooks           │ ← Lógica de negocio
├─────────────────────────────────────┤
│    Store (Zustand) + Queries         │ ← Estado global
├─────────────────────────────────────┤
│           Backend API                │ ← http://localhost:8080
└─────────────────────────────────────┘
```

---

## 🧩 Componentes

### Categorías

- **[Componentes Comunes](./01-componentes-comunes.md)** - Header, Footer, Sidebar, etc.
- **[Componentes UI](./02-componentes-ui.md)** - Botones, Inputs, Cards, etc.
- **[Componentes Módulos](./03-componentes-modulos.md)** - Alumnos, Tutores, Asignaciones, etc.

### Ejemplo de Uso

```typescript
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';

export function MyComponent() {
  return (
    <Card>
      <h2>Título</h2>
      <p>Contenido</p>
      <Button variant="primary">Aceptar</Button>
    </Card>
  );
}
```

---

## 🔌 Servicios API

### Cliente HTTP (Axios)

Todos los servicios utilizan una instancia configurada de Axios en `src/lib/api-client.ts`:

```typescript
import { apiClient } from '@/lib/api-client';

// GET request
const response = await apiClient.get('/api/alumnos');

// POST request
const newAlumno = await apiClient.post('/api/alumnos', {
  nombre: 'Juan',
  apellido: 'Pérez'
});

// PUT request
const updated = await apiClient.put(`/api/alumnos/${id}`, data);

// DELETE request
await apiClient.delete(`/api/alumnos/${id}`);
```

### Servicios Disponibles

- **[Alumnos Service](./04-servicios-alumnos.md)** - CRUD y búsqueda
- **[Tutores Service](./05-servicios-tutores.md)** - Gestión de tutores
- **[Asignaciones Service](./06-servicios-asignaciones.md)** - Asignar tutores
- **[Semestres Service](./07-servicios-semestres.md)** - Períodos académicos
- **[Reportes Service](./08-servicios-reportes.md)** - Exportación de datos

---

## 👨‍💻 Desarrollo

### Convenciones de Código

- **Componentes**: PascalCase (e.g., `UserCard.tsx`)
- **Funciones/variables**: camelCase (e.g., `getUserData()`)
- **Constantes**: UPPER_SNAKE_CASE (e.g., `API_BASE_URL`)
- **Tipos/Interfaces**: PascalCase (e.g., `UserDTO`, `ApiResponse`)

### Crear Nuevo Componente

```bash
# Crear estructura de componente
mkdir -p src/components/features/YourFeature
touch src/components/features/YourFeature/{YourComponent.tsx,index.ts,types.ts}
```

### Crear Nuevo Servicio

```typescript
// src/services/my-service.ts
import { apiClient } from '@/lib/api-client';

export const myService = {
  async getItems() {
    const { data } = await apiClient.get('/api/items');
    return data.data; // Desempacar ApiResponse<T>
  },

  async createItem(item: ItemDTO) {
    const { data } = await apiClient.post('/api/items', item);
    return data.data;
  }
};
```

### Usar Custom Hooks

```typescript
import { useQuery } from '@tanstack/react-query';
import { alumnService } from '@/services/alumnos-service';

export function StudentList() {
  const { data, isLoading, error } = useQuery({
    queryKey: ['alumnos'],
    queryFn: () => alumnService.getAll()
  });

  if (isLoading) return <div>Cargando...</div>;
  if (error) return <div>Error: {error.message}</div>;

  return <div>{/* Renderizar datos */}</div>;
}
```

---

## 🧪 Testing

### Estructura de Tests

```
src/
├── components/
│   └── Button.tsx
│   └── Button.test.tsx
├── services/
│   └── alumnos-service.ts
│   └── alumnos-service.test.ts
└── hooks/
    └── useAlumnos.ts
    └── useAlumnos.test.ts
```

### Ejecutar Tests

```bash
# Ejecutar todos los tests
npm run test

# Tests en watch mode
npm run test:watch

# Con cobertura
npm run test:coverage
```

### Ejemplo de Test

```typescript
import { render, screen } from '@testing-library/react';
import { Button } from './Button';

describe('Button', () => {
  it('should render with text', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByText('Click me')).toBeInTheDocument();
  });

  it('should call onClick when clicked', () => {
    const handleClick = jest.fn();
    render(<Button onClick={handleClick}>Click</Button>);
    screen.getByRole('button').click();
    expect(handleClick).toHaveBeenCalled();
  });
});
```

---

## 🎨 Sistema de Diseño

### Colores Semánticos

```typescript
// src/constants/colors.ts
const semanticColors = {
  primary: '#3B82F6',      // Azul
  success: '#10B981',      // Verde
  warning: '#F59E0B',      // Ámbar
  danger: '#EF4444',       // Rojo
  info: '#06B6D4'          // Cian
};
```

### Componentes Básicos

- **Button**: Variantes primary, secondary, danger
- **Input**: Text, email, password, number
- **Card**: Container principal
- **Badge**: Labels y tags
- **Modal**: Diálogos modales
- **Toast**: Notificaciones emergentes
- **Table**: Tablas de datos

---

## 📖 Documentación Detallada

- **[01-componentes-comunes.md](./01-componentes-comunes.md)** - Header, Footer, Sidebar
- **[02-componentes-ui.md](./02-componentes-ui.md)** - Elementos UI base
- **[03-componentes-modulos.md](./03-componentes-modulos.md)** - Módulos específicos
- **[04-servicios-alumnos.md](./04-servicios-alumnos.md)** - API Alumnos
- **[05-servicios-tutores.md](./05-servicios-tutores.md)** - API Tutores
- **[06-servicios-asignaciones.md](./06-servicios-asignaciones.md)** - API Asignaciones
- **[07-servicios-semestres.md](./07-servicios-semestres.md)** - API Semestres
- **[08-servicios-reportes.md](./08-servicios-reportes.md)** - Reportes
- **[09-configuracion.md](./09-configuracion.md)** - Variables de entorno
- **[10-troubleshooting.md](./10-troubleshooting.md)** - Solución de problemas

---

## 🔧 Configuración

### Variables de Entorno

```env
# .env
VITE_API_URL=http://localhost:8080
VITE_APP_NAME=Tutolink
VITE_LOG_LEVEL=info
```

### Configuración Vite

Ver `vite.config.ts` para configuración de build y desarrollo.

---

## 🤝 Contribuir

1. Crea una rama desde `main`: `git checkout -b feature/tu-feature`
2. Realiza cambios y sigue las convenciones de código
3. Asegúrate de que los tests pasen: `npm run test`
4. Haz commit con mensaje descriptivo: `git commit -m "feat: descripción"`
5. Abre un Pull Request

---

## 📞 Soporte

- **Documentación Backend**: [../README.md](../README.md)
- **Documentación General**: [../../README.md](../../README.md)
- **Issues**: GitHub Issues
- **Email**: soporte@proyecto-tutorias.edu

---

**Última actualización**: 2025-11-17
**Versión**: 1.0.0

