# Componentes Comunes

Componentes reutilizables que se utilizan en toda la aplicación.

## Header

Barra superior de navegación con usuario y opciones.

```typescript
import { Header } from '@/components/common/Header';

export function App() {
  return <Header />;
}
```

## Sidebar

Menú lateral de navegación.

```typescript
import { Sidebar } from '@/components/common/Sidebar';

export function Layout({ children }) {
  return (
    <div className="flex">
      <Sidebar />
      <main className="flex-1">{children}</main>
    </div>
  );
}
```

## Footer

Pie de página con información de copyright.

```typescript
import { Footer } from '@/components/common/Footer';

export function App() {
  return <Footer />;
}
```

## Breadcrumb

Navegación de ruta actual.

```typescript
import { Breadcrumb } from '@/components/common/Breadcrumb';

export function Page() {
  const items = [
    { label: 'Home', href: '/' },
    { label: 'Alumnos', href: '/alumnos' },
    { label: 'Detalle' }
  ];

  return <Breadcrumb items={items} />;
}
```

## Pagination

Controles de paginación para listas.

```typescript
import { Pagination } from '@/components/common/Pagination';

export function StudentList() {
  const [page, setPage] = useState(1);

  return (
    <>
      {/* Contenido */}
      <Pagination
        currentPage={page}
        totalPages={10}
        onChange={setPage}
      />
    </>
  );
}
```

## Loading

Indicador de carga.

```typescript
import { Loading } from '@/components/common/Loading';

export function Page() {
  const { isLoading } = useQuery(...);

  if (isLoading) return <Loading />;

  return <div>Contenido</div>;
}
```

## ErrorBoundary

Componente para capturar errores.

```typescript
import { ErrorBoundary } from '@/components/common/ErrorBoundary';

export function App() {
  return (
    <ErrorBoundary>
      <YourComponent />
    </ErrorBoundary>
  );
}
```

## Toast/Notification

Sistema de notificaciones.

```typescript
import { useToast } from '@/hooks/useToast';

export function MyComponent() {
  const { showToast } = useToast();

  const handleSuccess = () => {
    showToast('Operación exitosa', 'success');
  };

  const handleError = () => {
    showToast('Ocurrió un error', 'error');
  };

  return (
    <>
      <Button onClick={handleSuccess}>Éxito</Button>
      <Button onClick={handleError}>Error</Button>
    </>
  );
}
```

---

**Ver también**: [Componentes UI](./02-componentes-ui.md) | [Componentes Módulos](./03-componentes-modulos.md)
