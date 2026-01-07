# Servicio de Alumnos

Cliente para interactuar con los endpoints de alumnos.

## Ubicación

`src/services/alumnos-service.ts`

## Métodos Disponibles

### getAll()

Obtiene la lista de todos los alumnos.

```typescript
import { alumnService } from '@/services/alumnos-service';

const alumnos = await alumnService.getAll();
// Retorna: AlumnoDTO[]
```

### getById(id)

Obtiene un alumno específico por ID.

```typescript
const alumno = await alumnService.getById(1);
// Retorna: AlumnoDTO
```

### create(data)

Crea un nuevo alumno.

```typescript
const newAlumno = await alumnService.create({
  nombre: 'Juan',
  apellido: 'Pérez',
  email: 'juan@example.com',
  matricula: 'A001'
});
// Retorna: AlumnoDTO
```

### update(id, data)

Actualiza un alumno existente.

```typescript
const updated = await alumnService.update(1, {
  nombre: 'Juan Carlos',
  apellido: 'Pérez'
});
// Retorna: AlumnoDTO
```

### delete(id)

Elimina un alumno.

```typescript
await alumnService.delete(1);
// Retorna: void
```

### search(criteria)

Busca alumnos según criterios.

```typescript
const results = await alumnService.search({
  nombre: 'Juan',
  estado: 'activo',
  page: 1,
  limit: 10
});
// Retorna: { data: AlumnoDTO[], total: number, pages: number }
```

## Uso en Componentes

### Con React Query

```typescript
import { useQuery } from '@tanstack/react-query';
import { alumnService } from '@/services/alumnos-service';

export function StudentList() {
  const { data: alumnos, isLoading, error } = useQuery({
    queryKey: ['alumnos'],
    queryFn: () => alumnService.getAll()
  });

  if (isLoading) return <div>Cargando...</div>;
  if (error) return <div>Error: {error.message}</div>;

  return (
    <ul>
      {alumnos?.map(alumno => (
        <li key={alumno.id}>{alumno.nombre}</li>
      ))}
    </ul>
  );
}
```

### Con Mutaciones

```typescript
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { alumnService } from '@/services/alumnos-service';

export function CreateStudent() {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: (data) => alumnService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['alumnos'] });
    }
  });

  const handleSubmit = (formData) => {
    mutation.mutate(formData);
  };

  return (
    // Formulario...
  );
}
```

## Tipos de Datos

```typescript
interface AlumnoDTO {
  id: number;
  nombre: string;
  apellido: string;
  email: string;
  matricula: string;
  carrera: string;
  estado: 'activo' | 'inactivo';
  semestreActual: number;
  tutorAsignado?: TutorDTO;
  fechaCreacion: Date;
  fechaActualizacion: Date;
}
```

---

**Ver también**: [Servicios Tutores](./05-servicios-tutores.md) | [Servicios Asignaciones](./06-servicios-asignaciones.md)
