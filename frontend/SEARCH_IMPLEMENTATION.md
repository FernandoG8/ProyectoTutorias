# Implementación de Búsqueda Global con Autocompletado

## Resumen de Cambios

Se ha implementado un sistema de búsqueda global con autocompletado reutilizable en las tres páginas principales de búsqueda: **Alumnos**, **Tutores** y **Cambios de Tutor**.

---

## Componentes Nuevos

### 1. **SearchInput.tsx** (`src/components/SearchInput.tsx`)
Componente reutilizable que proporciona:
- **Autocompletado**: Dropdown con hasta 8 sugerencias
- **Navegación con teclado**: ↑ ↓ Enter Escape
- **Debounce**: 300ms para optimizar llamadas API
- **Loading spinner**: Indicador mientras se cargan sugerencias
- **Botón Clear (×)**: Limpia el input rápidamente
- **Estilos consistentes**: Mantiene la paleta de colores del proyecto

**Props:**
```typescript
{
  value: string;                                    // Valor actual del input
  onChange: (value: string) => void;                // Callback al cambiar texto
  onSelect?: (item, type) => void;                  // Callback al seleccionar sugerencia
  suggestions?: (AlumnoResponse | TutorResponse)[];  // Sugerencias a mostrar
  suggestionsType?: "student" | "tutor" | "mixed"; // Tipo de sugerencias
  isLoading?: boolean;                              // Estado de carga
  onClear?: () => void;                             // Callback al limpiar
}
```

---

## Utilidades Nuevas

### 2. **search-rank.ts** (`src/lib/search-rank.ts`)
Sistema de ranking local para búsquedas cuando el backend no soporta búsqueda server-side.

**Funciones:**
- `scoreStudent(student, query)`: Calcula relevancia para estudiantes
  - Prioridad: Matrícula exacta → Matrícula comienza con → Nombre comienza con → Contiene
- `rankStudents(students, query, limit?)`: Rankea y filtra estudiantes
- `scoreTutor(tutor, query)`: Calcula relevancia para tutores
- `rankTutors(tutors, query, limit?)`: Rankea y filtra tutores

---

## Extensiones de Servicios

### 3. **alumnos-service.ts** - Métodos Nuevos
```typescript
searchStudents(params: SearchStudentsParams): Promise<AlumnoPagedResponse>
  // Búsqueda server-side con parámetro 'q'

autocompleteStudents(query: string, limit?: number): Promise<AlumnoResponse[]>
  // Retorna hasta 8 sugerencias para autocompletado
```

### 4. **tutors-service.ts** - Métodos Nuevos
```typescript
searchTutors(params: SearchTutorsParams): Promise<TutorResponse[]>
  // Búsqueda server-side de tutores

autocompleteTutors(query: string, limit?: number): Promise<TutorResponse[]>
  // Retorna hasta 8 sugerencias de tutores
```

---

## Tipos Nuevos

### 5. **types/search.ts**
```typescript
type SearchResultType = "student" | "tutor";

interface StudentSearchResult {
  type: "student";
  item: AlumnoResponse;
}

interface TutorSearchResult {
  type: "tutor";
  item: TutorResponse;
}

type SearchResult = StudentSearchResult | TutorSearchResult;

interface SearchResponse {
  students: AlumnoResponse[];
  tutors: TutorResponse[];
  query: string;
  totalResults: number;
}

interface AutocompleteResult {
  type: SearchResultType;
  id: number;
  displayName: string;
  detail?: string;
}
```

---

## Cambios en Páginas

### 6. **StudentsPage.tsx**
**Comportamiento:**
- Al escribir 2+ caracteres → Entrada en "search mode"
- Badge "🔍 Búsqueda global" se muestra durante búsqueda activa
- Mantiene paginación y filtros existentes
- Al borrar → Vuelve a modo lista normal

**Características:**
- Autocompletado con hasta 8 sugerencias
- Navegación por teclado en dropdown
- Fallback a búsqueda local si API falla (usando `rankStudents`)
- Estados: loading, no results, autocomplete dropdown

### 7. **TutorsPage.tsx**
**Comportamiento:**
- Se agregó primera búsqueda a página de tutores (no tenía antes)
- Al escribir 2+ caracteres → Filtra lista de tutores
- Badge "🔍 Búsqueda global" durante búsqueda activa
- Usa ranking local de tutores

**Características:**
- Autocompletado con sugerencias
- Búsqueda en tiempo real sin paginación (lista completa)
- Fallback a ranking local

### 8. **TutorChangePage.tsx**
**Cambios:**
- Reemplazó `<Input>` con `<SearchInput>` para búsqueda de alumnos
- Mejoró UX con autocompletado (antes solo filtrado manual)
- Mantiene comportamiento de selección de alumnos
- Debounce optimizado a 300ms (era 400ms)

---

## Flujo de Búsqueda

### Modo Autocompletado (2+ caracteres)
```
Usuario escribe → Debounce 300ms → Query API autocomplete
                                   ↓
                            Retorna 8 sugerencias
                                   ↓
                        Dropdown muestra sugerencias
                                   ↓
                   Usuario selecciona → Ejecuta búsqueda completa
```

### Modo Búsqueda Global (⏎ o selección)
```
Usuario presiona Enter / selecciona → isSearchMode = true
                                    ↓
                        queryKey incluye isSearchMode
                                    ↓
                        API llamada con parámetro 'q'
                                    ↓
                        Resultados en paginación
                                    ↓
                     Badge "🔍 Búsqueda global" visible
                                    ↓
                  Presiona Escape o limpia → isSearchMode = false
```

---

## Casos de Uso

### Búsqueda de Estudiante
1. **Input**: "30252" → Muestra matrícula exacta primero
2. **Input**: "cerv" → Ranking: Nombre comienza con > contiene
3. **Input**: "202501" → Matricula comienza con > contiene
4. **Filtros + Búsqueda**: Estado + Carrera + Búsqueda simultáneamente
5. **Clear**: Presiona ✕ o Escape → Vuelve a lista normal

### Búsqueda de Tutor
1. **Input**: "María" → Sugiere "María Pérez"
2. **Input**: "Ing" → Filtra por nombre o carrera contiene
3. **Selection**: Selecciona → Badge de búsqueda global visible
4. **Ranking local**: Si API falla, usa scoring local

### Reasignación de Tutor (Cambios)
1. **Input**: "202501" → Autocompletado con alumnos activos
2. **Selection**: Click en estudiante → Autoselecciona en formulario
3. **Fallback**: Si autocomplete falla, usa ranking local de 150 alumnos cargados

---

## Restricciones y Consideraciones

✅ **Mantenidas:**
- No se rompió funcionalidad existente
- No se cambiaron estilos globales
- Mismo patrón en las 3 páginas
- SearchInput reutilizable

❌ **Limitaciones:**
- Backend debe soportar parámetro `q` para búsqueda
- Autocompletado limitado a 8 resultados (escalable)
- Fallback local solo funciona con datos cargados

---

## Testing Manual

### Requerimientos
- Backend corriendo en `http://localhost:8080`
- Frontend en `http://localhost:5173`

### Casos de Prueba

#### StudentsPage
- [ ] Escribir "30" → Muestra sugerencias de matrículas
- [ ] Seleccionar en dropdown → Ejecuta búsqueda global
- [ ] Presionar Escape → Cancela búsqueda
- [ ] Clic en ✕ → Limpia input
- [ ] Filtro Estado + Búsqueda → Ambos aplicados
- [ ] Navegar con ↑↓ → Selecciona en dropdown

#### TutorsPage
- [ ] Escribir nombre → Filtra tutores
- [ ] Autocompletado muestra datos (nombre, carrera, capacidad)
- [ ] Selection → Badge visible
- [ ] Sin datos en backend → Fallback local

#### TutorChangePage
- [ ] Escribir matrícula → Muestra alumnos activos
- [ ] Seleccionar en dropdown → Autoselecciona en formulario
- [ ] Loading spinner mientras carga
- [ ] "Ver alumnos" botón aún funciona

---

## Notas de Implementación

1. **Debounce**: 300ms (optimizado para UX)
2. **Límite de sugerencias**: 8 (mostrar muchas ralentiza el dropdown)
3. **Fallback local**: `rankStudents()` y `rankTutors()` con scoring
4. **Type-safe**: Completo con TypeScript
5. **Accesibilidad**: Navegación completa con teclado
6. **CSS**: TailwindCSS inline, sin archivos separados
7. **Estado**: No requiere nueva estructura de store (solo useState)

---

## Próximas Mejoras (Opcional)

- [ ] Expandir búsqueda global a más entidades
- [ ] Agregar historial de búsquedas recientes
- [ ] Mostrar coincidencias destacadas en resultados
- [ ] Categorizar resultados por tipo (Alumnos, Tutores)
- [ ] Búsqueda avanzada con filtros complejos
- [ ] Exportar resultados de búsqueda
