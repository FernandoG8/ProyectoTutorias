# Configuración del Frontend

Guía de configuración y variables de entorno.

## Variables de Entorno

### Archivo .env

Crea un archivo `.env` en la raíz del proyecto frontend:

```env
# API Configuration
VITE_API_URL=http://localhost:8080

# Application Settings
VITE_APP_NAME=Tutolink
VITE_APP_VERSION=1.0.0
VITE_LOG_LEVEL=info

# Feature Flags
VITE_ENABLE_DEBUG=false
VITE_ENABLE_MOCK_DATA=false
```

### Variables Disponibles

| Variable | Descripción | Valor Defecto | Requerido |
|----------|-----------|---------------|-----------|
| `VITE_API_URL` | URL base de la API backend | `http://localhost:8080` | No |
| `VITE_APP_NAME` | Nombre de la aplicación | `Tutolink` | No |
| `VITE_LOG_LEVEL` | Nivel de logs (debug, info, warn, error) | `info` | No |
| `VITE_ENABLE_DEBUG` | Habilitar modo debug | `false` | No |
| `VITE_ENABLE_MOCK_DATA` | Usar datos mock en desarrollo | `false` | No |

## Configuración Vite

### vite.config.ts

```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.VITE_API_URL || 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
```

## Configuración TailwindCSS

### tailwind.config.js

```javascript
module.exports = {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#3B82F6',
        success: '#10B981',
        warning: '#F59E0B',
        danger: '#EF4444',
        info: '#06B6D4',
      },
    },
  },
  plugins: [],
}
```

## Configuración TypeScript

### tsconfig.json

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"]
    },
    "strict": true,
    "noEmit": true,
    "esModuleInterop": true,
    "moduleResolution": "bundler",
    "allowSyntheticDefaultImports": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "jsx": "react-jsx"
  },
  "include": ["src"],
  "exclude": ["node_modules"]
}
```

## Desarrollo Local

### Instalar Dependencias

```bash
npm install
```

### Ejecutar Servidor de Desarrollo

```bash
npm run dev
```

El servidor estará disponible en: `http://localhost:5173`

### Compilar para Producción

```bash
npm run build
```

Los archivos compilados estarán en la carpeta `dist/`.

### Preview de Build

```bash
npm run preview
```

## Scripts Disponibles

| Script | Descripción |
|--------|-----------|
| `npm run dev` | Inicia servidor de desarrollo |
| `npm run build` | Compila para producción |
| `npm run preview` | Previsualiza build |
| `npm run lint` | Ejecuta linter |
| `npm run test` | Ejecuta tests |
| `npm run test:watch` | Tests en watch mode |
| `npm run test:coverage` | Tests con cobertura |
| `npm run type-check` | Verifica tipos TypeScript |

## Axios Configuration

### src/lib/api-client.ts

```typescript
import axios from 'axios';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor de respuesta
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Redirigir a login
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

## Despliegue en Producción

### Build

```bash
npm run build
```

### Servir con Nginx

```nginx
server {
  listen 80;
  server_name example.com;

  location / {
    root /var/www/tutolink/dist;
    index index.html;
    try_files $uri $uri/ /index.html;
  }

  location /api {
    proxy_pass http://localhost:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
  }
}
```

---

**Ver también**: [README Frontend](./README.md) | [Troubleshooting](./10-troubleshooting.md)
