# Servicio de Asignaciones

Cliente para interactuar con los endpoints de asignaciones de tutores.

## Ubicación

`src/services/asignaciones-service.ts`

## Métodos Disponibles

### getAll()

Obtiene todas las asignaciones.

```typescript
import { asignacionService } from '@/services/asignaciones-service';

const asignaciones = await asignacionService.getAll();
// Retorna: AsignacionDTO[]
```

### getById(id)

Obtiene una asignación específica.

```typescript
const asignacion = await asignacionService.getById(1);
// Retorna: AsignacionDTO
```

### create(data)

Crea una nueva asignación.

```typescript
const newAsignacion = await asignacionService.create({
  alumnoId: 1,
  tutorId: 2,
  semestreId: 1,
  motivoAsignacion: 'Bajo rendimiento académico'
});
// Retorna: AsignacionDTO
```

### update(id, data)

Actualiza una asignación.

```typescript
const updated = await asignacionService.update(1, {
  estado: 'completado'
});
// Retorna: AsignacionDTO
```

### delete(id)

Elimina una asignación.

```typescript
await asignacionService.delete(1);
// Retorna: void
```

### getByAlumno(alumnoId)

Obtiene asignaciones de un alumno.

```typescript
const asignaciones = await asignacionService.getByAlumno(1);
// Retorna: AsignacionDTO[]
```

### getByTutor(tutorId)

Obtiene asignaciones de un tutor.

```typescript
const asignaciones = await asignacionService.getByTutor(2);
// Retorna: AsignacionDTO[]
```

### findOptimalTutor(alumnoId, semestreId)

Encuentra el mejor tutor para un alumno.

```typescript
const tutorId = await asignacionService.findOptimalTutor(
  alumnoId,
  semestreId
);
// Retorna: number (tutorId)
```

## Uso en Componentes

### Asignar Tutor

```typescript
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { asignacionService } from '@/services/asignaciones-service';

export function AssignTutorModal({ alumnoId, onClose }) {
  const queryClient = useQueryClient();

  const assignMutation = useMutation({
    mutationFn: (tutorId) => asignacionService.create({
      alumnoId,
      tutorId,
      motivoAsignacion: 'Asignación manual'
    }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['asignaciones'] });
      onClose();
    }
  });

  const handleAssign = (tutorId) => {
    assignMutation.mutate(tutorId);
  };

  return (
    // Modal para seleccionar tutor...
  );
}
```

### Listar Asignaciones

```typescript
import { useQuery } from '@tanstack/react-query';
import { asignacionService } from '@/services/asignaciones-service';

export function AssignmentList() {
  const { data: asignaciones, isLoading } = useQuery({
    queryKey: ['asignaciones'],
    queryFn: () => asignacionService.getAll()
  });

  if (isLoading) return <div>Cargando...</div>;

  return (
    <table>
      <tbody>
        {asignaciones?.map(asignacion => (
          <tr key={asignacion.id}>
            <td>{asignacion.alumno.nombre}</td>
            <td>{asignacion.tutor.nombre}</td>
            <td>{asignacion.estado}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
```

## Tipos de Datos

```typescript
interface AsignacionDTO {
  id: number;
  alumnoId: number;
  alumno: AlumnoDTO;
  tutorId: number;
  tutor: TutorDTO;
  semestreId: number;
  semestre: SemestreDTO;
  motivoAsignacion: string;
  estado: 'activa' | 'completada' | 'cancelada';
  fechaAsignacion: Date;
  fechaFinalizacion?: Date;
  observaciones?: string;
  fechaCreacion: Date;
  fechaActualizacion: Date;
}
```

---

**Ver también**: [Servicios Alumnos](./04-servicios-alumnos.md) | [Servicios Tutores](./05-servicios-tutores.md) | [Servicios Semestres](./07-servicios-semestres.md)
