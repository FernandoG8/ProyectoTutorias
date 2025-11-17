# Servicio de Tutores

Cliente para interactuar con los endpoints de tutores.

## Ubicación

`src/services/tutors-service.ts`

## Métodos Disponibles

### getAll()

Obtiene la lista de todos los tutores.

```typescript
import { tutorService } from '@/services/tutors-service';

const tutores = await tutorService.getAll();
// Retorna: TutorDTO[]
```

### getById(id)

Obtiene un tutor específico.

```typescript
const tutor = await tutorService.getById(1);
// Retorna: TutorDTO
```

### create(data)

Crea un nuevo tutor.

```typescript
const newTutor = await tutorService.create({
  nombre: 'Dr. García',
  apellido: 'López',
  email: 'garcia@example.com',
  especialidades: ['Matemática', 'Física'],
  capacidadMaximaAlumnos: 5
});
// Retorna: TutorDTO
```

### update(id, data)

Actualiza un tutor.

```typescript
const updated = await tutorService.update(1, {
  especialidades: ['Matemática', 'Física', 'Química']
});
// Retorna: TutorDTO
```

### delete(id)

Elimina un tutor.

```typescript
await tutorService.delete(1);
// Retorna: void
```

### getAvailable()

Obtiene tutores disponibles (no saturados).

```typescript
const available = await tutorService.getAvailable();
// Retorna: TutorDTO[]
```

## Uso en Componentes

```typescript
import { useQuery } from '@tanstack/react-query';
import { tutorService } from '@/services/tutors-service';

export function TutorList() {
  const { data: tutores, isLoading } = useQuery({
    queryKey: ['tutores'],
    queryFn: () => tutorService.getAll()
  });

  if (isLoading) return <div>Cargando...</div>;

  return (
    <div className="grid grid-cols-3 gap-4">
      {tutores?.map(tutor => (
        <div key={tutor.id} className="p-4 border rounded">
          <h3>{tutor.nombre} {tutor.apellido}</h3>
          <p>{tutor.email}</p>
          <p>Especialidades: {tutor.especialidades.join(', ')}</p>
        </div>
      ))}
    </div>
  );
}
```

## Tipos de Datos

```typescript
interface TutorDTO {
  id: number;
  nombre: string;
  apellido: string;
  email: string;
  especialidades: string[];
  capacidadMaximaAlumnos: number;
  alumnosAsignados: number;
  estado: 'activo' | 'inactivo';
  fechaCreacion: Date;
  fechaActualizacion: Date;
}
```

---

**Ver también**: [Servicios Alumnos](./04-servicios-alumnos.md) | [Servicios Asignaciones](./06-servicios-asignaciones.md)
