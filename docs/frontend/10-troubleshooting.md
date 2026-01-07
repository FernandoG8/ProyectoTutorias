# Troubleshooting - Solución de Problemas

Guía para resolver problemas comunes en el desarrollo del frontend.

## Problemas de Instalación

### npm install falla

**Síntoma**: Error al ejecutar `npm install`

**Soluciones**:

1. Limpiar caché de npm:
   ```bash
   npm cache clean --force
   ```

2. Eliminar `node_modules` y `package-lock.json`:
   ```bash
   rm -rf node_modules package-lock.json
   npm install
   ```

3. Usar versión específica de Node.js:
   ```bash
   nvm use 18
   npm install
   ```

## Problemas de Desarrollo

### Puerto 5173 ocupado

**Síntoma**: Error al ejecutar `npm run dev` - Puerto ya está en uso

**Soluciones**:

1. Usar otro puerto:
   ```bash
   npm run dev -- --port 5174
   ```

2. Encontrar y matar proceso:
   ```bash
   # En Linux/Mac
   lsof -i :5173 | grep LISTEN | awk '{print $2}' | xargs kill -9

   # En Windows
   netstat -ano | findstr :5173
   taskkill /PID <PID> /F
   ```

### API no responde

**Síntoma**: Error de conexión con el backend

**Soluciones**:

1. Verificar que backend está corriendo:
   ```bash
   curl http://localhost:8080/api/health
   ```

2. Revisar variable de entorno:
   ```bash
   echo $VITE_API_URL  # o en Windows: echo %VITE_API_URL%
   ```

3. Verificar CORS en backend (debe permitir http://localhost:5173)

4. Revisar logs de la consola del navegador (F12)

### Errores de CORS

**Síntoma**: `Cross-Origin Request Blocked`

**Soluciones**:

1. Verificar configuración CORS en backend
2. Asegurar que `withCredentials: true` está configurado en Axios
3. Comprobar que el header `Origin` es correcto

## Problemas de TypeScript

### Error de tipos en componentes

**Síntoma**: Errores de TypeScript al compilar

**Soluciones**:

1. Verificar tipos de Props:
   ```typescript
   interface Props {
     title: string;
     children?: React.ReactNode;
   }
   ```

2. Ejecutar type-check:
   ```bash
   npm run type-check
   ```

3. Limpiar cache:
   ```bash
   rm -rf dist .vite
   npm run build
   ```

### Tipos faltantes

**Síntoma**: `Cannot find module or its corresponding type declarations`

**Soluciones**:

1. Instalar tipos:
   ```bash
   npm install --save-dev @types/package-name
   ```

2. Crear archivo de tipos `src/types/global.d.ts`

## Problemas de Estilos

### TailwindCSS no aplica estilos

**Síntoma**: Las clases de Tailwind no funcionan

**Soluciones**:

1. Verificar que el contenido está en `tailwind.config.js`:
   ```javascript
   content: ["./src/**/*.{js,ts,jsx,tsx}"],
   ```

2. Importar estilos base en `src/main.tsx`:
   ```typescript
   import './index.css'
   ```

3. Limpiar cache de Tailwind:
   ```bash
   rm -rf node_modules/.cache
   npm run dev
   ```

## Problemas de Componentes

### Componente no renderiza

**Síntoma**: Componente no aparece en pantalla

**Soluciones**:

1. Verificar que el componente está importado correctamente
2. Revisar logs en consola (F12)
3. Usar React DevTools para inspeccionar árbol de componentes
4. Verificar condicionales de renderizado

### Props no se actualizan

**Síntoma**: Cambios en props no se reflejan en el componente

**Soluciones**:

1. Verificar que es un cambio de referencia (arrays, objetos)
2. Usar `useEffect` con dependencias correctas
3. Evitar mutations directas de estado

## Problemas de Rendimiento

### Aplicación lenta

**Síntoma**: Lag o retrasos en la UI

**Soluciones**:

1. Profiler de React (React DevTools)
2. Usar `React.memo` para componentes costosos
3. Lazy load con `React.lazy` y `Suspense`
4. Verificar queries con TanStack Query

### Build muy lento

**Síntoma**: `npm run build` tarda mucho

**Soluciones**:

1. Verificar tamaño de paquetes:
   ```bash
   npm run build -- --analyze
   ```

2. Habilitar code splitting
3. Remover dependencias no usadas

## Problemas de Testing

### Tests no se ejecutan

**Síntoma**: Error al ejecutar `npm run test`

**Soluciones**:

1. Verificar configuración de Jest
2. Limpiar cache:
   ```bash
   npm run test -- --clearCache
   ```

3. Instalar dependencias de testing:
   ```bash
   npm install --save-dev @testing-library/react @testing-library/jest-dom
   ```

## Problemas de Build

### Build produce archivo muy grande

**Síntoma**: `dist/` contiene archivos muy grandes

**Soluciones**:

1. Analizar bundle
2. Implementar lazy loading
3. Remover dependencias no usadas
4. Usar tree-shaking

### Errores al hacer build

**Síntoma**: `npm run build` falla

**Soluciones**:

1. Verificar no hay errores de TypeScript:
   ```bash
   npm run type-check
   ```

2. Revisar logs de build
3. Intentar rebuild:
   ```bash
   rm -rf dist node_modules
   npm install
   npm run build
   ```

## Debugging

### Habilitar logs detallados

```typescript
// En src/main.tsx
if (import.meta.env.VITE_LOG_LEVEL === 'debug') {
  console.log('Starting Tutolink...');
}
```

### Usar devtools

- **React DevTools**: Extension para navegador
- **Redux DevTools**: Para state management
- **Network Tab**: Verificar requests/responses

## Contacto y Soporte

Si el problema persiste:

1. Revisar documentación completa: [README.md](./README.md)
2. Abrir issue en GitHub con:
   - Descripción clara del problema
   - Pasos para reproducir
   - Logs de error
   - Versiones (Node.js, npm, etc.)

---

**Última actualización**: 2025-11-17
