# Servicio de Semestres

Cliente para interactuar con los endpoints de semestres.

## Ubicación

`src/services/semestres-service.ts`

## Métodos Disponibles

### getAll()

Obtiene todos los semestres.

```typescript
import { semestreService } from '@/services/semestres-service';

const semestres = await semestreService.getAll();
// Retorna: SemestreDTO[]
```

### getById(id)

Obtiene un semestre específico.

```typescript
const semestre = await semestreService.getById(1);
// Retorna: SemestreDTO
```

### create(data)

Crea un nuevo semestre.

```typescript
const newSemestre = await semestreService.create({
  nombre: 'Primavera 2025',
  fechaInicio: '2025-01-15',
  fechaFin: '2025-05-15',
  estado: 'activo'
});
// Retorna: SemestreDTO
```

### update(id, data)

Actualiza un semestre.

```typescript
const updated = await semestreService.update(1, {
  estado: 'finalizado'
});
// Retorna: SemestreDTO
```

### getActive()

Obtiene el semestre activo actual.

```typescript
const active = await semestreService.getActive();
// Retorna: SemestreDTO | null
```

### setActive(id)

Establece un semestre como activo.

```typescript
await semestreService.setActive(1);
// Retorna: void
```

## Uso en Componentes

### Selector de Semestre

```typescript
import { useQuery, useMutation } from '@tanstack/react-query';
import { semestreService } from '@/services/semestres-service';
import { useQueryClient } from '@tanstack/react-query';

export function SemesterSelector() {
  const queryClient = useQueryClient();

  const { data: semestres } = useQuery({
    queryKey: ['semestres'],
    queryFn: () => semestreService.getAll()
  });

  const { data: activeSemestre } = useQuery({
    queryKey: ['semestres', 'active'],
    queryFn: () => semestreService.getActive()
  });

  const setActiveMutation = useMutation({
    mutationFn: (id) => semestreService.setActive(id),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['semestres', 'active']
      });
    }
  });

  return (
    <select
      value={activeSemestre?.id || ''}
      onChange={(e) => setActiveMutation.mutate(Number(e.target.value))}
    >
      {semestres?.map(semestre => (
        <option key={semestre.id} value={semestre.id}>
          {semestre.nombre}
        </option>
      ))}
    </select>
  );
}
```

## Tipos de Datos

```typescript
interface SemestreDTO {
  id: number;
  nombre: string;
  fechaInicio: Date;
  fechaFin: Date;
  estado: 'activo' | 'inactivo' | 'finalizado';
  descripcion?: string;
  totalAlumnos: number;
  totalTutores: number;
  totalAsignaciones: number;
  fechaCreacion: Date;
  fechaActualizacion: Date;
}
```

---

**Ver también**: [Servicios Alumnos](./04-servicios-alumnos.md) | [Servicios Asignaciones](./06-servicios-asignaciones.md)
