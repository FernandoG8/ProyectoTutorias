# Componentes UI Base

Componentes de interfaz de usuario base reutilizables.

## Button

Botón estándar con variantes.

```typescript
import { Button } from '@/components/ui/Button';

export function MyComponent() {
  return (
    <>
      <Button variant="primary">Primario</Button>
      <Button variant="secondary">Secundario</Button>
      <Button variant="danger">Peligro</Button>
      <Button disabled>Deshabilitado</Button>
      <Button loading>Cargando...</Button>
    </>
  );
}
```

## Input

Campo de entrada con validación.

```typescript
import { Input } from '@/components/ui/Input';
import { useState } from 'react';

export function Form() {
  const [email, setEmail] = useState('');

  return (
    <Input
      type="email"
      placeholder="Ingrese email"
      value={email}
      onChange={(e) => setEmail(e.target.value)}
      error="Email inválido"
    />
  );
}
```

## Select

Selector desplegable.

```typescript
import { Select } from '@/components/ui/Select';

export function FilterForm() {
  return (
    <Select
      options={[
        { value: 'active', label: 'Activo' },
        { value: 'inactive', label: 'Inactivo' }
      ]}
      onChange={(value) => console.log(value)}
    />
  );
}
```

## Textarea

Área de texto multilínea.

```typescript
import { Textarea } from '@/components/ui/Textarea';

export function CommentForm() {
  return (
    <Textarea
      placeholder="Escriba su comentario..."
      rows={4}
    />
  );
}
```

## Card

Contenedor de contenido.

```typescript
import { Card } from '@/components/ui/Card';

export function Dashboard() {
  return (
    <Card title="Estadísticas">
      <p>Contenido de la tarjeta</p>
    </Card>
  );
}
```

## Badge

Etiqueta o insignia.

```typescript
import { Badge } from '@/components/ui/Badge';

export function StudentCard() {
  return (
    <div>
      <h2>Juan Pérez</h2>
      <Badge variant="success">Activo</Badge>
      <Badge variant="warning">Pendiente</Badge>
    </div>
  );
}
```

## Modal

Diálogo modal.

```typescript
import { Modal } from '@/components/ui/Modal';
import { useState } from 'react';

export function DeleteConfirm() {
  const [open, setOpen] = useState(false);

  return (
    <>
      <Button onClick={() => setOpen(true)}>Eliminar</Button>
      <Modal
        open={open}
        title="Confirmar eliminación"
        onClose={() => setOpen(false)}
      >
        <p>¿Está seguro de que desea eliminar este elemento?</p>
        <div className="flex gap-2 justify-end mt-4">
          <Button variant="secondary" onClick={() => setOpen(false)}>
            Cancelar
          </Button>
          <Button variant="danger">Eliminar</Button>
        </div>
      </Modal>
    </>
  );
}
```

## Table

Tabla de datos.

```typescript
import { Table } from '@/components/ui/Table';

export function StudentList() {
  const columns = [
    { key: 'nombre', label: 'Nombre' },
    { key: 'email', label: 'Email' },
    { key: 'estado', label: 'Estado' }
  ];

  const data = [
    { id: 1, nombre: 'Juan', email: 'juan@example.com', estado: 'Activo' },
    { id: 2, nombre: 'María', email: 'maria@example.com', estado: 'Activo' }
  ];

  return <Table columns={columns} data={data} />;
}
```

## Tabs

Pestañas para navegación.

```typescript
import { Tabs } from '@/components/ui/Tabs';

export function StudentDetails() {
  return (
    <Tabs
      tabs={[
        {
          label: 'Información',
          content: <div>Datos del estudiante</div>
        },
        {
          label: 'Historial',
          content: <div>Historial académico</div>
        }
      ]}
    />
  );
}
```

## Alert

Mensaje de alerta.

```typescript
import { Alert } from '@/components/ui/Alert';

export function Page() {
  return (
    <>
      <Alert variant="success" title="Éxito">
        Operación realizada correctamente
      </Alert>
      <Alert variant="error" title="Error">
        Ocurrió un problema
      </Alert>
      <Alert variant="warning" title="Advertencia">
        Tenga cuidado
      </Alert>
    </>
  );
}
```

## Spinner

Indicador de carga rotatorio.

```typescript
import { Spinner } from '@/components/ui/Spinner';

export function LoadingComponent() {
  return <Spinner size="lg" />;
}
```

## Form

Formulario con validación.

```typescript
import { Form, FormField } from '@/components/ui/Form';
import { useForm } from 'react-hook-form';

export function StudentForm() {
  const { control, handleSubmit } = useForm();

  return (
    <Form onSubmit={handleSubmit(onSubmit)}>
      <FormField
        control={control}
        name="nombre"
        rules={{ required: 'El nombre es requerido' }}
        render={({ field }) => (
          <Input {...field} placeholder="Nombre completo" />
        )}
      />
    </Form>
  );
}
```

---

**Ver también**: [Componentes Comunes](./01-componentes-comunes.md) | [Componentes Módulos](./03-componentes-modulos.md)
