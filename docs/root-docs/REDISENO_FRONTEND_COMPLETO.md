# 🎨 Propuesta Completa de Rediseño Frontend - Sistema de Gestión de Tutorías

**Fecha:** 2025-11-20
**Versión:** 2.0 - Rediseño Profesional SaaS
**Estado:** Propuesta Arquitectónica

---

## 📋 Tabla de Contenidos

1. [Visión General](#visión-general)
2. [Fundamentos de Diseño](#fundamentos-de-diseño)
3. [Arquitectura de Componentes](#arquitectura-de-componentes)
4. [Rediseño por Módulo](#rediseño-por-módulo)
5. [Patrones de Interacción](#patrones-de-interacción)
6. [Token Mapping y Colores](#token-mapping-y-colores)
7. [Especificación Técnica](#especificación-técnica)
8. [Accesibilidad y WCAG](#accesibilidad-y-wcag)
9. [Hoja de Ruta de Implementación](#hoja-de-ruta-de-implementación)

---

## 🎯 Visión General

### Objetivos Principales

```
┌─────────────────────────────────────────────────────────────┐
│  ACTUAL: Desorganizado, inconsistente, confuso              │
│  OBJETIVO: Profesional, minimalista, intuitivo (Stripe)     │
└─────────────────────────────────────────────────────────────┘

PRINCIPIOS GUÍA:
✅ Minimalismo: Solo lo esencial, máximo 3 colores por vista
✅ Espacios en blanco: Respira, no ahoga
✅ Claridad: El usuario siempre sabe dónde está
✅ Consistencia: Mismo componente = mismo comportamiento
✅ Accesibilidad: WCAG AA mínimo en contraste y navegación
✅ Performance: Scroll controlado, sin desbordes
✅ Microinteracciones: Feedback inmediato y visible
```

### Cambio Conceptual

**De:**
- Tablas abrumadoras
- Modales sin contexto
- Flujos rotos
- Inconsistencia visual

**A:**
- Tablas elegantes con scroll controlado
- Modales contextuales con propósito claro
- Flujos guiados (wizard)
- Sistema de diseño unificado

---

## 🏗️ Fundamentos de Diseño

### 1. Jerarquía Visual

```
Nivel 1 (Principal):
├─ Título de página (H1, #1F2937, 32px, bold)
├─ Acciones críticas (botones primarios)
└─ Contenido principal

Nivel 2 (Secundario):
├─ Subtítulos (H2, #1F2937, 24px)
├─ Filtros/búsquedas
└─ Secundarias (botones secondary)

Nivel 3 (Terciario):
├─ Etiquetas (labels)
├─ Textos descriptivos (#6B7280)
└─ Acciones terciarias (links, text buttons)
```

### 2. Espaciado System

```typescript
// Escala de espaciado coherente (en Tailwind)
xs   = 4px   (gap-1)    → Espacios mínimos
sm   = 8px   (gap-2)    → Entre elementos
md   = 16px  (gap-4)    → Dentro de cards
lg   = 24px  (gap-6)    → Entre secciones
xl   = 32px  (gap-8)    → Márgenes grandes
2xl  = 48px  (gap-12)   → Separación de módulos

// Ejemplo: Card
<Card className="p-6 space-y-4">
  <h2 className="text-xl font-semibold">Título</h2>       {/* gap-4 */}
  <p className="text-sm text-gray-600">Descripción</p>    {/* gap-4 */}
  <div className="flex gap-2">                            {/* elementos-hermanos */}
    <Button>Acción 1</Button>
    <Button variant="secondary">Acción 2</Button>
  </div>
</Card>
```

### 3. Tipografía

```typescript
// Escala tipográfica (base: 16px)
H1: 32px, bold (600),   line-height 1.2  → Títulos de página
H2: 24px, semibold (600), line-height 1.3  → Secciones
H3: 20px, semibold (600), line-height 1.4  → Subsecciones
Body: 14px/16px, regular (400), line-height 1.5  → Párrafos
Small: 12px, regular (400), line-height 1.4  → Labels, hints
```

### 4. Profundidad y Elevación

```
Nivel 0 (Background):
  background: #F4F7F9
  Elemento base, sin interacción

Nivel 1 (Surface):
  background: #FFFFFF
  shadow: 0 1px 2px rgba(0,0,0,0.05)
  border: 1px solid #E2E8F0
  Cards, inputs, dropdowns

Nivel 2 (Floating):
  background: #FFFFFF
  shadow: 0 4px 6px rgba(0,0,0,0.1)
  border: 1px solid #E2E8F0
  Modales, popovers, tooltips

Nivel 3 (Top):
  background: #FFFFFF
  shadow: 0 20px 25px rgba(0,0,0,0.15)
  Dropdowns abiertos, full modales
```

---

## 🧩 Arquitectura de Componentes

### Componentes Base Reutilizables

```typescript
// File: src/components/ui/
├── Button.tsx
│   ├── variant: primary | secondary | tertiary | danger
│   ├── size: sm | md | lg
│   ├── state: default | loading | disabled
│   └── icon: left | right | only
│
├── Card.tsx
│   ├── variant: default | elevated | outlined
│   ├── padding: compact | normal | spacious
│   └── interactive: clickable con hover
│
├── Table.tsx (Nuevo - crucial)
│   ├── sticky headers
│   ├── zebra striping
│   ├── inline actions
│   ├── responsive scrolling
│   ├── row selection
│   └── pagination
│
├── Modal.tsx (Mejorado)
│   ├── size: sm | md | lg | xl
│   ├── with internal scroll
│   ├── close button claro
│   └── backdrop overlay
│
├── Badge.tsx
│   ├── variant: default | success | warning | danger | info
│   ├── size: sm | md
│   └── icon: leadingIcon
│
├── Select.tsx (Dropdown - mejorado)
│   ├── clearable
│   ├── searchable
│   ├── multi-select option
│   ├── grouped options
│   └── custom rendering
│
├── Input.tsx
│   ├── leading/trailing icons
│   ├── error state con mensaje
│   ├── hint text
│   └── label arriba (no placeholder)
│
├── Tabs.tsx
│   ├── underline variant
│   ├── pills variant
│   ├── scroll en mobile
│   └── keyboard navigation
│
├── Breadcrumb.tsx (Nuevo)
│   ├── muestra contexto
│   ├── clickeable
│   └── responsive
│
├── EmptyState.tsx (Nuevo)
│   ├── ilustración
│   ├── mensaje principal
│   ├── mensaje secundario
│   └── call-to-action
│
├── DataTable.tsx (Compuesto)
│   ├── Table base
│   ├── Search + Filter
│   ├── Sorting
│   ├── Pagination
│   └── Actions
│
└── Wizard.tsx (Mejorado)
    ├── StepIndicator (visual)
    ├── StepContent (actual)
    ├── Navigation (prev/next)
    └── Validation gating
```

### Componentes de Composición

```typescript
// File: src/components/composed/
├── PageHeader.tsx
│   ├── Breadcrumbs
│   ├── Título + descripción
│   ├── Actions (botones principales)
│   └── Stats (opcional)
│
├── DataTableView.tsx
│   ├── PageHeader
│   ├── Filters + Search
│   ├── DataTable (con scroll)
│   └── Pagination footer
│
├── WizardContainer.tsx
│   ├── StepIndicator
│   ├── Content section
│   ├── Navigation
│   └── Progress tracking
│
├── ModalWithTable.tsx
│   ├── Modal wrapper
│   ├── DataTable dentro
│   └── Acciones modales
│
├── FormCard.tsx
│   ├── Card container
│   ├── Título + descripción
│   ├── Campos organizados
│   └── Acciones abajo
│
└── ConfirmationStep.tsx
    ├── Icono de éxito/estado
    ├── Mensaje principal
    ├── Resumen de datos
    ├── Acciones siguientes
    └── Timestamp
```

---

## 📱 Rediseño por Módulo

### MÓDULO 1: Alumnos

#### Problema Actual
- Tabla desborda con sidebar
- Búsqueda inefectiva
- Sin scroll controlado
- Visualmente confusa

#### Solución Propuesta

```
┌─────────────────────────────────────────────────────────────┐
│  HEADER                                                      │
│  Alumnos  >  [Filtros]  [Búsqueda]  [+ Nuevo]  [Export]    │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  TABLE (SCROLL INTERNO CONTROLADO)                          │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ # │ Matrícula │ Nombre    │ Carrera │ Semestre │ ...│  │
│  ├───────────────────────────────────────────────────────┤  │
│  │ 1 │ A12345   │ Juan Pérez│ ISC    │ 3       │ ...│  │  (zebra stripe 1)
│  │ 2 │ A12346   │ María...  │ IME    │ 5       │ ...│  │  (zebra stripe 2)
│  │ 3 │ A12347   │ Carlos... │ ITS    │ 2       │ ...│  │  (zebra stripe 1)
│  └───────────────────────────────────────────────────────┘  │
│                                                               │
│  FOOTER                                                      │
│  Mostrando 1-10 de 250  [< Anterior]  [Siguiente >]        │
└─────────────────────────────────────────────────────────────┘
```

#### Especificación Técnica

```typescript
// src/pages/AlumnosPage.tsx
interface AlumnosPageProps {}

const AlumnosPage = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const [filters, setFilters] = useState({ carrera: "", semestre: "" });
  const [pagination, setPagination] = useState({ page: 1, pageSize: 10 });
  const [sortBy, setSortBy] = useState({ field: "matricula", order: "asc" });

  // Query: listAlumnos con filtros
  const { data, isLoading } = useQuery({
    queryKey: ["alumnos", searchTerm, filters, pagination, sortBy],
    queryFn: () => listAlumnos({ ...filters, ...pagination, ...sortBy }),
  });

  return (
    <div className="space-y-6">
      {/* PageHeader */}
      <PageHeader
        title="Alumnos"
        description="Gestiona el registro de estudiantes"
        breadcrumbs={[{ label: "Inicio", href: "/" }, { label: "Alumnos" }]}
        actions={[
          { label: "Exportar", variant: "secondary" },
          { label: "+ Nuevo", variant: "primary" },
        ]}
      />

      {/* Filters + Search */}
      <Card className="p-4">
        <div className="flex gap-4 items-end">
          <Input
            placeholder="Buscar por matrícula, nombre..."
            icon="search"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <Select
            label="Carrera"
            value={filters.carrera}
            onChange={(val) => setFilters({ ...filters, carrera: val })}
            options={carreraOptions}
          />
          <Select
            label="Semestre"
            value={filters.semestre}
            onChange={(val) => setFilters({ ...filters, semestre: val })}
            options={semestreOptions}
          />
        </div>
      </Card>

      {/* DataTable */}
      <DataTable
        columns={[
          { header: "#", accessor: "id", width: "60px" },
          { header: "Matrícula", accessor: "matricula", width: "120px" },
          { header: "Nombre", accessor: "nombre", width: "200px", sortable: true },
          {
            header: "Carrera",
            accessor: "carrera",
            width: "100px",
            cell: (value) => <CarreraBadge carrera={value} />
          },
          { header: "Semestre", accessor: "semestreNumerico", width: "100px" },
          {
            header: "Estado",
            accessor: "estado",
            width: "100px",
            cell: (value) => <EstadoBadge estado={value} />
          },
          {
            header: "Acciones",
            accessor: "id",
            width: "100px",
            cell: (id) => (
              <div className="flex gap-2">
                <Button variant="tertiary" size="sm" icon="edit" />
                <Button variant="tertiary" size="sm" icon="delete" />
              </div>
            ),
          },
        ]}
        data={data?.alumnos || []}
        isLoading={isLoading}
        pagination={{
          current: pagination.page,
          pageSize: pagination.pageSize,
          total: data?.total || 0,
          onPageChange: (page) => setPagination({ ...pagination, page }),
        }}
        sorting={{
          field: sortBy.field,
          order: sortBy.order,
          onSort: (field, order) => setSortBy({ field, order }),
        }}
      />
    </div>
  );
};
```

#### Componente DataTable Specializado

```typescript
// src/components/ui/DataTable.tsx
interface DataTableProps<T> {
  columns: Column<T>[];
  data: T[];
  isLoading?: boolean;
  pagination?: PaginationConfig;
  sorting?: SortingConfig;
  onRowClick?: (row: T) => void;
  rowSelection?: boolean;
}

export const DataTable = <T,>({
  columns,
  data,
  isLoading,
  pagination,
  sorting,
  onRowClick,
  rowSelection,
}: DataTableProps<T>) => {
  const [selectedRows, setSelectedRows] = useState<Set<string>>(new Set());

  return (
    <Card className="overflow-hidden">
      {/* Table Container con scroll controlado */}
      <div className="overflow-x-auto max-h-[600px] overflow-y-auto">
        <table className="w-full border-collapse">
          {/* Sticky Header */}
          <thead className="bg-gray-50 sticky top-0 z-10 border-b border-gray-200">
            <tr>
              {rowSelection && (
                <th className="w-12 px-4 py-3">
                  <input
                    type="checkbox"
                    onChange={(e) => {
                      if (e.target.checked) {
                        setSelectedRows(new Set(data.map((_, i) => String(i))));
                      } else {
                        setSelectedRows(new Set());
                      }
                    }}
                  />
                </th>
              )}
              {columns.map((col) => (
                <th
                  key={col.accessor}
                  style={{ width: col.width }}
                  className="px-4 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                  onClick={() => {
                    if (col.sortable && sorting) {
                      sorting.onSort(
                        col.accessor,
                        sorting.order === "asc" ? "desc" : "asc"
                      );
                    }
                  }}
                >
                  <div className="flex items-center gap-2">
                    {col.header}
                    {col.sortable && sorting?.field === col.accessor && (
                      <Icon name={sorting.order === "asc" ? "chevron-up" : "chevron-down"} />
                    )}
                  </div>
                </th>
              ))}
            </tr>
          </thead>

          {/* Body con zebra striping */}
          <tbody>
            {isLoading ? (
              <tr>
                <td colSpan={columns.length} className="px-4 py-8 text-center">
                  <Spinner />
                </td>
              </tr>
            ) : data.length === 0 ? (
              <tr>
                <td colSpan={columns.length} className="px-4 py-8">
                  <EmptyState
                    title="Sin resultados"
                    description="No se encontraron registros"
                  />
                </td>
              </tr>
            ) : (
              data.map((row, idx) => (
                <tr
                  key={idx}
                  className={`border-b border-gray-200 hover:bg-blue-50 cursor-pointer transition-colors ${
                    idx % 2 === 0 ? "bg-white" : "bg-gray-50"
                  }`}
                  onClick={() => onRowClick?.(row)}
                >
                  {rowSelection && (
                    <td className="w-12 px-4 py-3">
                      <input
                        type="checkbox"
                        checked={selectedRows.has(String(idx))}
                        onChange={(e) => {
                          const newSet = new Set(selectedRows);
                          if (e.target.checked) {
                            newSet.add(String(idx));
                          } else {
                            newSet.delete(String(idx));
                          }
                          setSelectedRows(newSet);
                        }}
                      />
                    </td>
                  )}
                  {columns.map((col) => (
                    <td
                      key={col.accessor}
                      style={{ width: col.width }}
                      className="px-4 py-3 text-sm text-gray-900"
                    >
                      {col.cell
                        ? col.cell((row as any)[col.accessor], row)
                        : (row as any)[col.accessor]}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Footer con Paginación */}
      {pagination && (
        <div className="px-4 py-3 bg-gray-50 border-t border-gray-200 flex items-center justify-between text-sm">
          <span className="text-gray-600">
            Mostrando {(pagination.current - 1) * pagination.pageSize + 1} a{" "}
            {Math.min(pagination.current * pagination.pageSize, pagination.total)} de{" "}
            {pagination.total}
          </span>
          <div className="flex gap-2">
            <Button
              variant="secondary"
              size="sm"
              disabled={pagination.current === 1}
              onClick={() => pagination.onPageChange(pagination.current - 1)}
            >
              ← Anterior
            </Button>
            <span className="px-2 py-1 text-gray-600">
              {pagination.current} / {Math.ceil(pagination.total / pagination.pageSize)}
            </span>
            <Button
              variant="secondary"
              size="sm"
              disabled={pagination.current >= Math.ceil(pagination.total / pagination.pageSize)}
              onClick={() => pagination.onPageChange(pagination.current + 1)}
            >
              Siguiente →
            </Button>
          </div>
        </div>
      )}
    </Card>
  );
};
```

#### Colores Específicos por Carrera

```typescript
// src/constants/carreras.ts
const CARRERA_COLORS = {
  "ITS": { bg: "bg-blue-50", border: "border-blue-200", text: "text-blue-900", accent: "#2563EB" },
  "ISC": { bg: "bg-green-50", border: "border-green-200", text: "text-green-900", accent: "#10B981" },
  "IME": { bg: "bg-amber-50", border: "border-amber-200", text: "text-amber-900", accent: "#F59E0B" },
  "IMECA": { bg: "bg-red-50", border: "border-red-200", text: "text-red-900", accent: "#EF4444" },
  "IE": { bg: "bg-purple-50", border: "border-purple-200", text: "text-purple-900", accent: "#8B5CF6" },
  "ICA": { bg: "bg-pink-50", border: "border-pink-200", text: "text-pink-900", accent: "#F472B6" },
};

export const CarreraBadge = ({ carrera }: { carrera: string }) => {
  const colors = CARRERA_COLORS[carrera as keyof typeof CARRERA_COLORS];
  return (
    <Badge variant="custom" className={`${colors.bg} ${colors.border} ${colors.text}`}>
      {carrera}
    </Badge>
  );
};
```

---

### MÓDULO 2: Tutores

#### Problema Actual
- Botón "Alumnos" sin contexto visual
- Modal desorganizado
- Información abrumadora

#### Solución Propuesta

```
TARJETA DE TUTOR (Grid):
┌──────────────────────────────┐
│ [Avatar] NOMBRE              │
│ Carrera: ISC                 │
│ Capacidad: 5/10              │
│ ▓▓▓░░░░░░░░░ (5 de 10)      │
│                              │
│ [Editar] [Ver alumnos] [X]  │
└──────────────────────────────┘

MODAL AL HACER CLIC EN "VER ALUMNOS":
┌──────────────────────────────┐
│ Alumnos asignados a:         │
│ Juan García Pérez            │
│ ✕ (botón cerrar)             │
├──────────────────────────────┤
│                              │
│ TABLA INTERNA (scroll):      │
│ # │ Matrícula │ Nombre │ ... │
│ ─────────────────────────── │
│ 1 │ A12345   │ María...    │ │
│ 2 │ A12346   │ Carlos...   │ │
│                              │
│ Mostrando 1-5 de 5          │
│                              │
└──────────────────────────────┘
```

#### Especificación Técnica

```typescript
// src/pages/TutoresPage.tsx
const TutoresPage = () => {
  const [selectedTutor, setSelectedTutor] = useState<Tutor | null>(null);
  const { data: tutores, isLoading } = useQuery({
    queryKey: ["tutores"],
    queryFn: listTutores,
  });

  return (
    <div className="space-y-6">
      <PageHeader
        title="Tutores"
        description="Gestiona los tutores y sus asignaciones"
        actions={[{ label: "+ Nuevo", variant: "primary" }]}
      />

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {tutores?.map((tutor) => (
          <TutorCard
            key={tutor.id}
            tutor={tutor}
            onViewAlumnos={() => setSelectedTutor(tutor)}
          />
        ))}
      </div>

      {selectedTutor && (
        <TutorAlumnosModal
          tutor={selectedTutor}
          onClose={() => setSelectedTutor(null)}
        />
      )}
    </div>
  );
};

// src/components/TutorCard.tsx
const TutorCard = ({ tutor, onViewAlumnos }: TutorCardProps) => {
  const capacidadPorcentaje = (tutor.alumnosAsignados / tutor.capacidadMaxima) * 100;
  const colorCapacidad = capacidadPorcentaje > 80 ? "bg-red-500" : "bg-green-500";

  return (
    <Card className="hover:shadow-lg transition-shadow">
      <div className="p-6 space-y-4">
        {/* Header */}
        <div className="flex items-start justify-between">
          <div className="flex gap-3 flex-1">
            <Avatar src={tutor.avatar} name={tutor.nombre} />
            <div>
              <h3 className="font-semibold text-gray-900">{tutor.nombre}</h3>
              <p className="text-sm text-gray-600">{tutor.departamento}</p>
            </div>
          </div>
        </div>

        {/* Info */}
        <div className="space-y-2 border-t border-gray-200 pt-4">
          <div className="flex justify-between text-sm">
            <span className="text-gray-600">Carrera</span>
            <CarreraBadge carrera={tutor.carrera} />
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-gray-600">Capacidad</span>
            <span className="font-medium">{tutor.alumnosAsignados}/{tutor.capacidadMaxima}</span>
          </div>
        </div>

        {/* Progress Bar */}
        <div className="space-y-1">
          <div className="w-full h-2 rounded-full bg-gray-200 overflow-hidden">
            <div
              className={`h-full transition-all ${colorCapacidad}`}
              style={{ width: `${Math.min(capacidadPorcentaje, 100)}%` }}
            />
          </div>
          <p className="text-xs text-gray-500">{capacidadPorcentaje.toFixed(0)}% lleno</p>
        </div>

        {/* Actions */}
        <div className="flex gap-2 pt-2 border-t border-gray-200">
          <Button variant="secondary" size="sm" className="flex-1">
            Editar
          </Button>
          <Button
            variant="secondary"
            size="sm"
            className="flex-1"
            onClick={onViewAlumnos}
          >
            Ver alumnos
          </Button>
        </div>
      </div>
    </Card>
  );
};

// src/components/TutorAlumnosModal.tsx
const TutorAlumnosModal = ({ tutor, onClose }: TutorAlumnosModalProps) => {
  const { data: alumnos, isLoading } = useQuery({
    queryKey: ["tutor-alumnos", tutor.id],
    queryFn: () => getTutorAlumnos(tutor.id),
  });

  return (
    <Modal size="lg" onClose={onClose}>
      <div className="space-y-4">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-gray-200 pb-4">
          <div>
            <h2 className="text-xl font-semibold text-gray-900">
              Alumnos asignados a:
            </h2>
            <p className="text-sm text-gray-600 mt-1">{tutor.nombre}</p>
          </div>
          <Button variant="tertiary" icon="close" onClick={onClose} />
        </div>

        {/* Table with internal scroll */}
        <div className="max-h-[400px] overflow-y-auto">
          <DataTable
            columns={[
              { header: "#", accessor: "id", width: "40px" },
              { header: "Matrícula", accessor: "matricula", width: "100px" },
              { header: "Nombre", accessor: "nombre", width: "200px" },
              {
                header: "Carrera",
                accessor: "carrera",
                width: "100px",
                cell: (value) => <CarreraBadge carrera={value} />
              },
              {
                header: "Acciones",
                accessor: "id",
                width: "80px",
                cell: (id) => (
                  <Button variant="tertiary" size="sm" icon="edit" />
                ),
              },
            ]}
            data={alumnos || []}
            isLoading={isLoading}
          />
        </div>

        {/* Footer */}
        <div className="text-xs text-gray-500 text-center pt-2 border-t border-gray-200">
          {alumnos?.length || 0} alumnos asignados
        </div>
      </div>
    </Modal>
  );
};
```

---

### MÓDULO 3: Cambio de Tutor (WIZARD COMPLETO)

#### Problema Actual
- Flujo desorganizado
- Errores no controlados
- Resultado confuso

#### Solución: Wizard Tipo "Checkout"

```
PASO 1: SELECCIONAR ALUMNO
┌──────────────────────────────────┐
│ 1. Alumno  2. Tutor  3. Confirmar│
├──────────────────────────────────┤
│                                  │
│ Buscar alumno:                   │
│ [___________________________]     │
│                                  │
│ RESULTADOS:                      │
│ ☐ A12345 - Juan Pérez (ISC)     │
│ ☐ A12346 - María García (IME)   │
│ ☐ A12347 - Carlos López (ITS)   │
│                                  │
│              [< Atrás] [Siguiente >]
└──────────────────────────────────┘

PASO 2: MOSTRAR TUTOR ACTUAL + OPCIONES
┌──────────────────────────────────┐
│ 1. Alumno  2. Tutor  3. Confirmar│
├──────────────────────────────────┤
│                                  │
│ Alumno: Juan Pérez (A12345)     │
│                                  │
│ Tutor actual:                    │
│ ┌──────────────────────────────┐ │
│ │ [Avatar] Dr. García          │ │
│ │ Capacidad: 8/10              │ │
│ │ Carrera: ISC                 │ │
│ └──────────────────────────────┘ │
│                                  │
│ Nuevo tutor (disponibles):       │
│ ○ Dr. López  (3/10)             │
│ ● Dr. Martín (5/10)             │  (seleccionado)
│ ○ Dra. Ruiz  (2/10)             │
│                                  │
│              [< Atrás] [Siguiente >]
└──────────────────────────────────┘

PASO 3: CONFIRMACIÓN
┌──────────────────────────────────┐
│ 1. Alumno  2. Tutor  3. Confirmar│
├──────────────────────────────────┤
│                                  │
│ Resumen del cambio:              │
│                                  │
│ Alumno:        Juan Pérez        │
│ Tutor Actual:  Dr. García        │
│ Nuevo Tutor:   Dr. Martín        │
│                                  │
│ ⚠ Este cambio es irreversible   │
│                                  │
│              [< Atrás] [Confirmar]
└──────────────────────────────────┘

PASO 4: RESULTADO (RECIBO)
┌──────────────────────────────────┐
│                                  │
│         ✓ Éxito                  │
│                                  │
│ Cambio de tutor completado:      │
│                                  │
│ Juan Pérez                       │
│ De: Dr. García → A: Dr. Martín  │
│                                  │
│ Fecha: 20/11/2025 10:15          │
│ Referencia: CHT-20251120-001     │
│                                  │
│              [Finalizar] [+ Otro]
└──────────────────────────────────┘
```

#### Especificación Técnica

```typescript
// src/pages/CambioTutorPage.tsx
type CambioTutorStep = "alumno" | "tutor" | "confirmacion" | "resultado";

interface CambioTutorState {
  alumnoId: number | null;
  alumnoData: Alumno | null;
  tutorActualId: number | null;
  nuevoTutorId: number | null;
  resultado: CambioTutorResponse | null;
}

export const CambioTutorPage = () => {
  const [step, setStep] = useState<CambioTutorStep>("alumno");
  const [state, setState] = useState<CambioTutorState>({
    alumnoId: null,
    alumnoData: null,
    tutorActualId: null,
    nuevoTutorId: null,
    resultado: null,
  });

  const mutation = useMutation({
    mutationFn: () =>
      cambiarTutor({
        alumnoId: state.alumnoId!,
        nuevoTutorId: state.nuevoTutorId!,
      }),
    onSuccess: (data) => {
      setState({ ...state, resultado: data });
      setStep("resultado");
    },
  });

  return (
    <div className="space-y-6">
      <PageHeader
        title="Cambio de Tutor"
        description="Reasigna un alumno a un tutor diferente"
      />

      <WizardContainer
        steps={[
          { label: "Alumno", completed: !!state.alumnoId },
          { label: "Tutor", completed: !!state.nuevoTutorId },
          { label: "Confirmar", completed: false },
        ]}
        currentStep={step}
      >
        {step === "alumno" && (
          <PasoSeleccionarAlumno
            onSelect={(alumno) => {
              setState({
                ...state,
                alumnoId: alumno.id,
                alumnoData: alumno,
                tutorActualId: alumno.tutorAsignadoId,
              });
              setStep("tutor");
            }}
          />
        )}

        {step === "tutor" && state.alumnoData && (
          <PasoSeleccionarTutor
            alumno={state.alumnoData}
            tutorActualId={state.tutorActualId!}
            onSelect={(tutorId) => {
              setState({ ...state, nuevoTutorId: tutorId });
              setStep("confirmacion");
            }}
          />
        )}

        {step === "confirmacion" && (
          <PasoConfirmacion
            alumno={state.alumnoData!}
            tutorActualId={state.tutorActualId!}
            nuevoTutorId={state.nuevoTutorId!}
            onConfirm={() => mutation.mutate()}
            loading={mutation.isPending}
          />
        )}

        {step === "resultado" && (
          <PasoResultado
            resultado={state.resultado!}
            onFinish={() => {
              // Reset y back to inicio
              setState({
                alumnoId: null,
                alumnoData: null,
                tutorActualId: null,
                nuevoTutorId: null,
                resultado: null,
              });
              setStep("alumno");
            }}
          />
        )}
      </WizardContainer>
    </div>
  );
};

// PASO 1: Seleccionar Alumno
const PasoSeleccionarAlumno = ({
  onSelect,
}: {
  onSelect: (alumno: Alumno) => void;
}) => {
  const [search, setSearch] = useState("");
  const { data: alumnos, isLoading } = useQuery({
    queryKey: ["alumnos-cambio-tutor", search],
    queryFn: () => searchAlumnos(search),
    enabled: search.length > 2,
  });

  return (
    <Card className="p-6">
      <div className="space-y-4">
        <div>
          <h3 className="text-lg font-semibold text-gray-900 mb-2">
            Selecciona un alumno
          </h3>
          <p className="text-sm text-gray-600">
            Busca por matrícula o nombre
          </p>
        </div>

        <Input
          placeholder="Ej: A12345 o Juan Pérez"
          icon="search"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          autoFocus
        />

        {isLoading && <Spinner />}

        {alumnos && alumnos.length === 0 && (
          <EmptyState title="Sin resultados" description="Intenta otra búsqueda" />
        )}

        <div className="space-y-2 max-h-[400px] overflow-y-auto">
          {alumnos?.map((alumno) => (
            <div
              key={alumno.id}
              className="p-3 border border-gray-200 rounded-lg hover:bg-blue-50 cursor-pointer transition-colors"
              onClick={() => onSelect(alumno)}
            >
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-medium text-gray-900">{alumno.nombre}</p>
                  <p className="text-sm text-gray-600">{alumno.matricula}</p>
                </div>
                <CarreraBadge carrera={alumno.carrera} />
              </div>
            </div>
          ))}
        </div>
      </div>
    </Card>
  );
};

// PASO 2: Seleccionar Nuevo Tutor
const PasoSeleccionarTutor = ({
  alumno,
  tutorActualId,
  onSelect,
}: {
  alumno: Alumno;
  tutorActualId: number;
  onSelect: (tutorId: number) => void;
}) => {
  const { data: tutoresDisponibles } = useQuery({
    queryKey: ["tutores-disponibles", alumno.carrera],
    queryFn: () => getTutoresDisponibles(alumno.carrera),
  });

  const tutorActual = tutoresDisponibles?.find((t) => t.id === tutorActualId);
  const tutoresAlternos = tutoresDisponibles?.filter((t) => t.id !== tutorActualId);

  return (
    <Card className="p-6 space-y-6">
      {/* Context */}
      <div>
        <p className="text-sm text-gray-600">Alumno seleccionado</p>
        <h3 className="text-lg font-semibold text-gray-900">{alumno.nombre}</h3>
        <p className="text-sm text-gray-600">{alumno.matricula}</p>
      </div>

      {/* Current Tutor */}
      <div className="space-y-2 border-t border-gray-200 pt-4">
        <p className="text-sm font-medium text-gray-700">Tutor actual</p>
        {tutorActual && (
          <div className="p-3 bg-gray-50 rounded-lg border border-gray-200">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <Avatar src={tutorActual.avatar} name={tutorActual.nombre} />
                <div>
                  <p className="font-medium">{tutorActual.nombre}</p>
                  <p className="text-sm text-gray-600">
                    {tutorActual.alumnosAsignados}/{tutorActual.capacidadMaxima} alumnos
                  </p>
                </div>
              </div>
              <Badge variant="default">Actual</Badge>
            </div>
          </div>
        )}
      </div>

      {/* Available Tutors */}
      <div className="space-y-2 border-t border-gray-200 pt-4">
        <p className="text-sm font-medium text-gray-700">Tutores disponibles</p>
        <div className="space-y-2 max-h-[300px] overflow-y-auto">
          {tutoresAlternos?.map((tutor) => (
            <div
              key={tutor.id}
              onClick={() => onSelect(tutor.id)}
              className="p-3 border border-gray-200 rounded-lg hover:bg-blue-50 cursor-pointer transition-colors"
            >
              <div className="flex items-center gap-3">
                <Avatar src={tutor.avatar} name={tutor.nombre} />
                <div className="flex-1">
                  <p className="font-medium text-gray-900">{tutor.nombre}</p>
                  <p className="text-sm text-gray-600">
                    {tutor.alumnosAsignados}/{tutor.capacidadMaxima} alumnos
                  </p>
                </div>
                <Badge variant={tutor.alumnosAsignados >= tutor.capacidadMaxima ? "danger" : "success"}>
                  {tutor.alumnosAsignados < tutor.capacidadMaxima ? "Disponible" : "Lleno"}
                </Badge>
              </div>
            </div>
          ))}
        </div>
      </div>
    </Card>
  );
};

// PASO 3: Confirmación
const PasoConfirmacion = ({
  alumno,
  tutorActualId,
  nuevoTutorId,
  onConfirm,
  loading,
}: {
  alumno: Alumno;
  tutorActualId: number;
  nuevoTutorId: number;
  onConfirm: () => void;
  loading: boolean;
}) => {
  const { data: tutores } = useQuery({
    queryKey: ["tutores"],
    queryFn: listTutores,
  });

  const tutorActual = tutores?.find((t) => t.id === tutorActualId);
  const nuevoTutor = tutores?.find((t) => t.id === nuevoTutorId);

  return (
    <Card className="p-6 space-y-6">
      <div className="text-center space-y-2">
        <AlertCircle className="mx-auto h-8 w-8 text-amber-500" />
        <h3 className="text-lg font-semibold text-gray-900">Confirmar cambio</h3>
        <p className="text-sm text-gray-600">
          Este cambio es irreversible. Por favor verifica los datos.
        </p>
      </div>

      <div className="space-y-4 bg-gray-50 rounded-lg p-4">
        <div className="flex justify-between items-center">
          <span className="text-sm text-gray-700">Alumno</span>
          <span className="font-medium text-gray-900">{alumno.nombre}</span>
        </div>
        <div className="flex justify-between items-center">
          <span className="text-sm text-gray-700">Matrícula</span>
          <span className="font-medium text-gray-900">{alumno.matricula}</span>
        </div>
      </div>

      <div className="space-y-2">
        <p className="text-sm font-medium text-gray-700">Cambio de tutor</p>
        <div className="flex items-center gap-2">
          <Avatar src={tutorActual?.avatar} name={tutorActual?.nombre} />
          <div className="flex-1">
            <p className="text-sm font-medium">{tutorActual?.nombre}</p>
          </div>
          <Icon name="arrow-right" className="h-5 w-5 text-gray-400" />
          <Avatar src={nuevoTutor?.avatar} name={nuevoTutor?.nombre} />
          <div className="flex-1">
            <p className="text-sm font-medium">{nuevoTutor?.nombre}</p>
          </div>
        </div>
      </div>

      <div className="flex gap-2 pt-4 border-t border-gray-200">
        <Button
          variant="secondary"
          className="flex-1"
          disabled={loading}
        >
          ← Atrás
        </Button>
        <Button
          variant="primary"
          className="flex-1"
          loading={loading}
          onClick={onConfirm}
        >
          Confirmar cambio
        </Button>
      </div>
    </Card>
  );
};

// PASO 4: Resultado (Recibo)
const PasoResultado = ({
  resultado,
  onFinish,
}: {
  resultado: CambioTutorResponse;
  onFinish: () => void;
}) => {
  return (
    <Card className="p-6 text-center space-y-6">
      <div className="space-y-2">
        <div className="flex justify-center">
          <CheckCircle className="h-12 w-12 text-green-500" />
        </div>
        <h2 className="text-2xl font-bold text-gray-900">
          ¡Cambio completado!
        </h2>
        <p className="text-gray-600">El alumno fue reasignado exitosamente</p>
      </div>

      <div className="space-y-4 bg-gray-50 rounded-lg p-6 text-left">
        <div className="flex justify-between">
          <span className="text-gray-700">Alumno</span>
          <span className="font-medium text-gray-900">{resultado.alumnoNombre}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-gray-700">De</span>
          <span className="font-medium text-gray-900">{resultado.tutorAnterior}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-gray-700">A</span>
          <span className="font-medium text-gray-900">{resultado.tutorNuevo}</span>
        </div>
        <div className="flex justify-between pt-4 border-t border-gray-200">
          <span className="text-gray-700 text-sm">Fecha</span>
          <span className="font-medium text-gray-900 text-sm">
            {dayjs(resultado.timestamp).format("DD/MM/YYYY HH:mm")}
          </span>
        </div>
        <div className="flex justify-between">
          <span className="text-gray-700 text-sm">Referencia</span>
          <span className="font-mono text-gray-900 text-sm">{resultado.referencia}</span>
        </div>
      </div>

      <div className="flex gap-2 pt-4 border-t border-gray-200">
        <Button
          variant="secondary"
          className="flex-1"
          onClick={onFinish}
        >
          ← Atrás
        </Button>
        <Button
          variant="primary"
          className="flex-1"
          onClick={onFinish}
        >
          + Otro cambio
        </Button>
      </div>
    </Card>
  );
};
```

---

### MÓDULO 4: Reportes → Reporte por Tutor

#### Propuesta

```
┌──────────────────────────────────────┐
│ FILTROS                              │
│ Semestre: [▼ 2025-2025-FN]           │  ← Mismo dropdown que wizard
│ Carrera:  [▼ Todas]                  │
│ Buscar:   [_________________]        │
│           [Generar reporte]          │
└──────────────────────────────────────┘

Resultado:
┌──────────────────────────────────────┐
│ REPORTE: Tutores activos             │
│ Período: 2025-2025-FN                │
│                                      │
│ TABLE:                               │
│ Tutor        │ Carrera│ #Alumnos│ ... │
│ Dr. García   │ ISC   │ 8      │ ... │
│ Dra. López   │ IME   │ 5      │ ... │
└──────────────────────────────────────┘
```

#### Dropdown Compartido Propuesto

```typescript
// src/components/ui/SemestreSelect.tsx
interface SemestreSelectProps {
  value: number | null;
  onChange: (semestreId: number) => void;
  label?: string;
  placeholder?: string;
}

export const SemestreSelect = ({
  value,
  onChange,
  label = "Semestre",
  placeholder = "Selecciona un semestre...",
}: SemestreSelectProps) => {
  const { data: semestres } = useQuery({
    queryKey: ["semestres"],
    queryFn: listSemestres,
  });

  return (
    <Select
      label={label}
      value={value?.toString() || ""}
      onChange={(val) => onChange(parseInt(val, 10))}
      placeholder={placeholder}
      options={
        semestres?.map((sem) => ({
          value: sem.id.toString(),
          label: `${sem.codigo} - ${sem.nombre}`,
        })) || []
      }
    />
  );
};
```

---

### MÓDULO 5: Alumnos Inactivos

```
PROBLEMA: Tabla se sobrepone con sidebar

SOLUCIÓN:
┌─────────────────────────────────────┐
│ Alumnos Inactivos                   │
│                                     │
│ Filtro: [Estado ▼]  [Buscar...]   │
│                                     │
│ ┌───────────────────────────────┐   │
│ │ # │ Matrícula │ Nombre │ Carrera│ │ (scroll internal)
│ │───────────────────────────────│ │
│ │ 1 │ A12345   │ Juan  │ ISC   │ │
│ │ 2 │ A12346   │ María │ IME   │ │
│ └───────────────────────────────┘   │
│                                     │
│ Mostrando 1-10 de 50                │
│ [< Anterior] [Siguiente >]          │
└─────────────────────────────────────┘

CLAVE: max-h-[600px] overflow-y-auto en contenedor de tabla
```

---

### MÓDULO 6: Wizard de Asignación (Mejorado)

Ya fue implementado, pero reforzando:

```
PASO 1: Cargar archivo
PASO 2: Validar y mostrar errores (si los hay)
PASO 3: Revisar datos (pre-ejecución)
PASO 4: Ejecutando... (loading con progreso)
PASO 5: Resultado final (recibo con estadísticas)

Características:
✅ Gating: No puedes avanzar con errores
✅ Microinteracciones: Loader, toast, badges
✅ Accesibilidad: focus visible, labels claros
✅ Responsive: Ajusta en mobile
```

---

## 🎨 Patrones de Interacción

### 1. Estados de Botones

```typescript
// src/components/ui/Button.tsx
interface ButtonProps {
  variant: "primary" | "secondary" | "tertiary" | "danger";
  size: "sm" | "md" | "lg";
  state: "default" | "hover" | "active" | "disabled" | "loading";
  icon?: "left" | "right" | "only";
}

// Estilos:
STATES = {
  default: "bg-primary-600 text-white",
  hover: "bg-primary-700 cursor-pointer shadow-md",
  active: "bg-primary-800 ring-2 ring-primary-400",
  disabled: "bg-gray-200 text-gray-500 cursor-not-allowed",
  loading: "opacity-75 cursor-wait",
};
```

### 2. Estados de Input

```typescript
// Estados visuales
DEFAULT:     border-gray-200, bg-white
FOCUSED:     border-primary-500, ring-2 ring-primary-100, shadow-sm
ERROR:       border-red-500, bg-red-50, ring-2 ring-red-100
SUCCESS:     border-green-500, bg-green-50
DISABLED:    border-gray-200, bg-gray-50, cursor-not-allowed
```

### 3. Transiciones y Animaciones

```css
/* Transiciones suaves (150ms) */
button, input, select {
  transition: all 150ms ease-out;
}

/* Loading spinner */
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Fade in modales */
@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}
```

### 4. Scroll Controlado

```typescript
// Para tablas grandes
<div className="overflow-x-auto max-h-[600px] overflow-y-auto">
  <table>
    <thead className="sticky top-0 z-10 bg-gray-50">
      {/* Headers fijos */}
    </thead>
    <tbody>
      {/* Body scrolleable */}
    </tbody>
  </table>
</div>
```

---

## 🎨 Token Mapping y Colores

### Colores Primarios (Tailwind)

```typescript
// src/constants/colors.ts

// Sistema de colores principal
COLORS = {
  primary: {
    50:  "#F0F5F9",
    100: "#E1EBF3",
    200: "#C3D7E7",
    300: "#A5C3DB",
    400: "#87AFCF",
    500: "#689BC3",  // Base
    600: "#1C3A4B",  // Dark (token)
    700: "#162E3A",
    800: "#102229",
    900: "#081118",
  },

  secondary: {
    50:  "#E8F8F7",
    100: "#D1F1EF",
    200: "#A3E3DF",
    300: "#75D5CF",
    400: "#4AC7BF",
    500: "#3AAFA9",  // Base (token)
    600: "#2E8D87",
    700: "#226B65",
    800: "#164943",
    900: "#0A2721",
  },

  accent: {
    50:  "#F0FDF4",
    100: "#DCFCE7",
    200: "#BBF7D0",
    300: "#86EFAC",
    400: "#4ADE80",
    500: "#22C55E",  // Base (token)
    600: "#16A34A",
    700: "#15803D",
    800: "#166534",
    900: "#14532D",
  },

  gray: {
    50:  "#F9FAFB",  // background: #F4F7F9 ≈ 100
    100: "#F3F4F6",
    200: "#E5E7EB",  // border: #E2E8F0 ≈ 200
    300: "#D1D5DB",
    400: "#9CA3AF",
    500: "#6B7280",  // textMuted
    600: "#4B5563",
    700: "#374151",
    800: "#1F2937",  // text
    900: "#111827",
  },

  // Carreras (mantener)
  carrera: {
    ITS: "#2563EB",     // blue-600
    ISC: "#10B981",     // emerald-500
    IME: "#F59E0B",     // amber-500
    IMECA: "#EF4444",   // red-500
    IE: "#8B5CF6",      // violet-500
    ICA: "#F472B6",     // pink-400
  },
};
```

### Uso en Componentes

```typescript
// Button con colores tokens
<Button className="bg-primary-600 hover:bg-primary-700 text-white">
  Acción principal
</Button>

// Card con border
<Card className="border border-gray-200">
  Contenido
</Card>

// Badge por carrera
<Badge className={`bg-carrera-${carrera}`}>
  {carrera}
</Badge>

// Text muted
<p className="text-gray-500">Texto secundario</p>
```

---

## 🔧 Especificación Técnica

### Estructura de Directorios Propuesta

```
src/
├── components/
│   ├── ui/
│   │   ├── Button.tsx                (primario)
│   │   ├── Card.tsx
│   │   ├── Input.tsx
│   │   ├── Select.tsx
│   │   ├── Modal.tsx
│   │   ├── Badge.tsx
│   │   ├── Tabs.tsx
│   │   ├── Table.tsx                 (NUEVO)
│   │   ├── Breadcrumb.tsx             (NUEVO)
│   │   └── EmptyState.tsx             (NUEVO)
│   │
│   ├── composed/
│   │   ├── PageHeader.tsx             (NUEVO)
│   │   ├── DataTable.tsx              (NUEVO)
│   │   ├── WizardContainer.tsx        (MEJORADO)
│   │   ├── ModalWithTable.tsx         (NUEVO)
│   │   └── FormCard.tsx               (NUEVO)
│   │
│   ├── features/
│   │   ├── AssignmentWizard.tsx       (REFACTORIZADO)
│   │   ├── CambioTutorWizard.tsx      (NUEVO)
│   │   ├── TutorCard.tsx              (NUEVO)
│   │   ├── TutorAlumnosModal.tsx      (NUEVO)
│   │   └── CarreraBadge.tsx           (NUEVO)
│   │
│   └── layouts/
│       ├── MainLayout.tsx             (MEJORADO)
│       ├── Sidebar.tsx                (MEJORADO)
│       └── Navbar.tsx                 (MEJORADO)
│
├── pages/
│   ├── AlumnosPage.tsx                (REFACTORIZADO)
│   ├── TutoresPage.tsx                (REFACTORIZADO)
│   ├── CambioTutorPage.tsx            (NUEVO)
│   ├── DashboardPage.tsx              (MEJORADO)
│   ├── ReportesPage.tsx               (MEJORADO)
│   └── AlumnosInactivosPage.tsx       (MEJORADO)
│
├── constants/
│   ├── colors.ts                      (NUEVO - color tokens)
│   ├── carreras.ts                    (existente, mejorado)
│   └── spacing.ts                     (NUEVO)
│
└── types/
    └── index.ts                       (compartido)
```

### Tailwind Config Mejorado

```typescript
// tailwind.config.js
module.exports = {
  theme: {
    extend: {
      colors: {
        primary: {
          50: "#F0F5F9",
          600: "#1C3A4B",
          // ... rest
        },
        secondary: {
          50: "#E8F8F7",
          500: "#3AAFA9",
          // ... rest
        },
        // ... más colores
      },
      spacing: {
        xs: "4px",
        sm: "8px",
        md: "16px",
        lg: "24px",
        xl: "32px",
        "2xl": "48px",
      },
      fontSize: {
        xs: ["12px", "16px"],
        sm: ["14px", "20px"],
        base: ["16px", "24px"],
        lg: ["18px", "28px"],
        xl: ["20px", "28px"],
        "2xl": ["24px", "32px"],
        "3xl": ["32px", "40px"],
      },
      boxShadow: {
        xs: "0 1px 2px rgba(0, 0, 0, 0.05)",
        sm: "0 1px 2px rgba(0, 0, 0, 0.05)",
        md: "0 4px 6px rgba(0, 0, 0, 0.1)",
        lg: "0 10px 15px rgba(0, 0, 0, 0.1)",
        xl: "0 20px 25px rgba(0, 0, 0, 0.15)",
      },
      zIndex: {
        sidebar: "40",
        dropdown: "50",
        modal: "60",
        tooltip: "70",
      },
    },
  },
};
```

---

## ♿ Accesibilidad y WCAG

### Checklist WCAG AA

```
✅ CONTRASTE (4.5:1 para texto pequeño, 3:1 para grande)
   primary-600 (#1C3A4B) sobre white: ✓ 12.5:1
   gray-500 (#6B7280) sobre white: ✗ 4.7:1 (borderline, usar gray-600)

✅ TAMAÑOS MÍNIMOS
   - Botones: 44x44px mínimo (target touch)
   - Input: 40px alto mínimo
   - Font: 14px mínimo para body

✅ NAVEGACIÓN CON TECLADO
   - Tab order logical
   - Focus visible con ring-2
   - Escape cierra modales

✅ ARIA
   - aria-label en botones sin texto
   - aria-disabled en disabled
   - aria-expanded en dropdowns
   - role="table" en tablas

✅ COLORES NO ÚNICOS
   - No confiar en color solo
   - Usar iconos + colores
   - Badges con texto también

✅ SEMÁNTICA HTML
   - <button> para acciones
   - <a> para navegación
   - <label htmlFor> en inputs
   - <table> con <thead>/<tbody>
```

### Implementación

```typescript
// Botón accesible
<button
  className="..."
  aria-label="Cerrar modal"
  onClick={onClose}
>
  {/* O icono o texto */}
</button>

// Input accesible
<div>
  <label htmlFor="search" className="block text-sm font-medium">
    Buscar
  </label>
  <input
    id="search"
    type="text"
    placeholder="Escribe aquí..."
    aria-describedby="search-hint"
  />
  <p id="search-hint" className="text-xs text-gray-600">
    Hint: opcional
  </p>
</div>

// Select accesible
<div>
  <label htmlFor="carrera">Carrera</label>
  <select id="carrera" aria-label="Selecciona una carrera">
    <option value="">Todas</option>
    {/* ... */}
  </select>
</div>

// Modal accesible
<div
  className="..."
  role="dialog"
  aria-labelledby="modal-title"
  aria-modal="true"
>
  <h2 id="modal-title">Título del modal</h2>
  {/* ... */}
</div>
```

---

## 📊 Hoja de Ruta de Implementación

### Fase 1: Base (2-3 semanas)

```
✅ Establecer color tokens en Tailwind
✅ Crear componentes base (Button, Card, Input, Select, Modal)
✅ Crear componentes compuestos (PageHeader, DataTable)
✅ Mejorar Sidebar/Navbar con nueva identidad
✅ Implementar accesibilidad WCAG básica
```

### Fase 2: Módulos Críticos (2-3 semanas)

```
✅ Rediseño Módulo Alumnos (DataTable)
✅ Rediseño Módulo Tutores (Cards + Modal)
✅ Crear Wizard Cambio de Tutor
✅ Mejorar Wizard Asignación
```

### Fase 3: Completar (1-2 semanas)

```
✅ Rediseño Reportes
✅ Rediseño Alumnos Inactivos
✅ Testing y QA
✅ Optimización de performance
```

### Fase 4: Pulido (1 semana)

```
✅ Microinteracciones refinadas
✅ Animaciones suaves
✅ Testing de accesibilidad completo
✅ Documentación de componentes
```

---

## 📐 Wireframe Conceptual - Dashboard Principal

```
┌─────────────────────────────────────────────────────────────┐
│ LOGO    [Alumnos] [Tutores] [Asignación] [Reportes]  👤 ⚙️  │
├──────────────────────────────────────────────────────────────┤
│ Sidebar                    │ MAIN CONTENT                    │
│ • Inicio                   │                                 │
│ • Alumnos                  │ Bienvenido, Admin              │
│ • Tutores                  │                                 │
│ • Asignación               │ ┌──────────────┐ ┌──────────────┐
│ • Cambio de Tutor          │ │ 250 Alumnos  │ │ 25 Tutores   │
│ • Reportes                 │ └──────────────┘ └──────────────┘
│ • Config                   │
│                            │ ┌──────────────┐ ┌──────────────┐
│                            │ │ 100 Activos  │ │ 150 Inactivos│
│                            │ └──────────────┘ └──────────────┘
│                            │
│                            │ Acciones rápidas:
│                            │ [+ Nuevo Alumno] [Cargar archivo]
│                            │
└──────────────────────────────────────────────────────────────┘
```

---

## 🚀 Resumen Ejecutivo

| Aspecto | Mejora |
|---------|--------|
| **Claridad Visual** | De confuso a minimalista tipo SaaS |
| **Consistencia** | Tokens unificados en todos los módulos |
| **Accesibilidad** | WCAG AA garantizado |
| **Performance** | Scroll controlado, sin desbordes |
| **UX** | Flujos guiados con gating de validación |
| **Tiempo Implementación** | 8-10 semanas |
| **Componentes Nuevos** | ~15 reutilizables |
| **Módulos a Rediseñar** | 6 principales |

---

## 📎 Próximos Pasos

1. **Validar propuesta** con stakeholders
2. **Crear design tokens** en Tailwind
3. **Implementar componentes base**
4. **Prototipo de Módulo Alumnos**
5. **Feedback y iteración**
6. **Escalar a otros módulos**

---

**Documento preparado como guía de implementación.**
**Versión 2.0 - Rediseño Profesional**
