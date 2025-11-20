# 📊 Dashboard Rediseño Completo - Sistema de Gestión de Tutorías

**Especificación Visual, UX/UI y Técnica**
**Fecha:** 2025-11-20
**Versión:** 1.0 Final

---

## 🎯 Objetivo del Dashboard

Proporcionar una **visión integral en tiempo real** de:
- ✅ Distribución de tutores por carrera
- ✅ Carga de alumnos por tutor
- ✅ Identificación de tutores saturados
- ✅ Desbalances entre carreras
- ✅ Alertas de capacidad
- ✅ Tendencias y patrones

**Usuarios:** Administradores, Coordinadores de Tutoría

**Momento de uso:** Diario (para monitoreo) y semanal (para reportes)

---

## 📐 Layout General del Dashboard

```
┌─────────────────────────────────────────────────────────────┐
│ SIDEBAR (240px)  │  MAIN CONTENT (responsive)               │
├──────────────────┼──────────────────────────────────────────┤
│                  │                                           │
│ • Inicio        │ HEADER                                    │
│ • Alumnos       │ Dashboard > Período: [2025-2025-FN] ✓     │
│ • Tutores       │                                           │
│ • Asignación    │ ┌──────────────────────────────────────┐  │
│ • Cambio Tutor  │ │ CARDS DE ESTADÍSTICAS (4 columnas)   │  │
│ • Reportes      │ ├──────────────────────────────────────┤  │
│ • Config        │ │ Total    │ Por Carrera │ Saturados  │  │
│                  │ │ Tutores  │ (6 cards)   │ Desbalance │  │
│                  │ ├──────────────────────────────────────┤  │
│                  │ │                                      │  │
│                  │ │ GRID 2 COLUMNAS (Row 1)             │  │
│                  │ ├──────────────────────────────────────┤  │
│                  │ │ 1. DISTRIBUCIÓN POR CARRERA (chart) │  │
│                  │ │    (Bar chart horizontal)           │  │
│                  │ │                                      │  │
│                  │ │ 2. TOP 10 TUTORES SATURADOS (list)  │  │
│                  │ ├──────────────────────────────────────┤  │
│                  │ │                                      │  │
│                  │ │ TABLA FULL WIDTH (Row 2)            │  │
│                  │ ├──────────────────────────────────────┤  │
│                  │ │ 3. DETALLE COMPLETO DE TUTORES      │  │
│                  │ │    (tabla con filtros)              │  │
│                  │ │                                      │  │
│                  │ └──────────────────────────────────────┘  │
│                  │                                           │
└──────────────────┴──────────────────────────────────────────┘
```

---

## 🎨 Secciones del Dashboard (Detallado)

### SECCIÓN 1: Header + Período Selector

```
╔═══════════════════════════════════════════════════════════════╗
║ Dashboard                                                     ║
║ Sistema de Gestión de Tutorías                               ║
║                                                               ║
║ Período: [▼ 2025-2025-FN] [↙ Anterior] [Siguiente ↘] [Hoy] ║
║                                                               ║
║ Última actualización: Hace 2 minutos (auto-refresh c/5min)   ║
╚═══════════════════════════════════════════════════════════════╝
```

**Componentes:**
- Título "Dashboard"
- Descripción breve
- Select de semestres (dropdown mejorado)
- Botones de navegación (Anterior/Siguiente)
- Timestamp de última actualización
- Botón de "Refresh manual"

**Funcionalidad:**
- Select filtra todo el dashboard
- Auto-refresh cada 5 minutos (con indicador visual)
- Manual refresh con loading state

---

### SECCIÓN 2: Tarjetas de Estadísticas (KPIs)

```
┌──────────────────┬──────────────────┬──────────────────┬──────────────────┐
│                  │                  │                  │                  │
│  TOTAL TUTORES   │  TOTAL ALUMNOS   │  PROMEDIO POR    │  DESBALANCE      │
│                  │                  │  TUTOR           │  ENTRE CARRERAS  │
│  25              │  350             │  14              │  ⚠️ ALTO (↑ 32%) │
│  tutores         │  alumnos         │  alumnos/tutor   │  (8-20 alumnos)  │
│                  │                  │                  │                  │
│  ━━━━━━━━━━━━━  │  ━━━━━━━━━━━━━  │  ━━━━━━━━━━━━━  │  ━━━━━━━━━━━━━  │
│  vs 24 (semana)  │  vs 340 (semana) │  vs 14.2         │  vs BAJO (15%)   │
│  +1 tutor ↑      │  +10 alumnos ↑   │  (semana)        │  semana anterior │
│                  │                  │  -0.2 ↓          │                  │
└──────────────────┴──────────────────┴──────────────────┴──────────────────┘
```

**Card 1: Total Tutores**
```typescript
interface TutoresCard {
  title: "Total Tutores";
  value: 25;
  subtitle: "tutores activos";
  comparison: {
    value: 24;
    period: "semana anterior";
    change: "+1";
    direction: "up"; // color: green
  };
  color: "primary-600"; // #1C3A4B
}
```

**Card 2: Total Alumnos**
```typescript
interface AlumnosCard {
  title: "Total Alumnos";
  value: 350;
  subtitle: "alumnos asignados";
  comparison: {
    value: 340;
    period: "semana anterior";
    change: "+10";
    direction: "up";
  };
  color: "secondary-500"; // #3AAFA9
}
```

**Card 3: Promedio por Tutor**
```typescript
interface PromedioCard {
  title: "Promedio por Tutor";
  value: 14;
  subtitle: "alumnos/tutor";
  comparison: {
    value: 14.2;
    period: "semana anterior";
    change: "-0.2";
    direction: "down"; // color: amber (warning)
  };
  color: "accent-500"; // #22C55E
}
```

**Card 4: Desbalance (CRÍTICA)**
```typescript
interface DesbalanceCard {
  title: "Desbalance Entre Carreras";
  value: "⚠️ ALTO";
  subtitle: "32% (rango 8-20 alumnos)";
  comparison: {
    value: "BAJO";
    period: "semana anterior";
    change: "+17%";
    direction: "up"; // color: red (danger)
  };
  color: "danger-500"; // #EF4444
  alert: true; // Mostrar alerta visual
}
```

**CSS:**
```css
.stat-card {
  @apply bg-white border border-gray-200 rounded-lg p-6
         shadow-xs hover:shadow-md transition-shadow;

  /* Change indicator color */
  .change--positive { @apply text-green-600; }
  .change--negative { @apply text-amber-600; }
  .change--neutral  { @apply text-gray-600; }

  /* Alert styling */
  &.alert {
    @apply border-l-4 border-l-red-500 bg-red-50;
  }
}
```

---

### SECCIÓN 3: Distribución por Carrera (Chart)

```
DISTRIBUCIÓN DE TUTORES POR CARRERA
╔═══════════════════════════════════════════════════════════════╗
║                                                               ║
║ ISC (Ingeniería en Sistemas Computacionales)                ║
║ ████████████████░░░░  8 tutores │ 120 alumnos │ 15 prom.   ║
║                                                               ║
║ IME (Ingeniería Mecánica)                                   ║
║ ████████░░░░░░░░░░░░  5 tutores │  75 alumnos │ 15 prom.   ║
║                                                               ║
║ ITS (Ingeniería en Tecnologías de Software)                 ║
║ ██████████████░░░░░░  7 tutores │  98 alumnos │ 14 prom.   ║
║                                                               ║
║ IE (Ingeniería Eléctrica)                                   ║
║ ████░░░░░░░░░░░░░░░░  2 tutores │  28 alumnos │ 14 prom.   ║
║                                                               ║
║ ICA (Ingeniería en Computación y Automatización)            ║
║ ██████░░░░░░░░░░░░░░  2 tutores │  19 alumnos │ 10 prom.   ║
║                                                               ║
║ IMECA (Ingeniería Mecatrónica)                              ║
║ ██░░░░░░░░░░░░░░░░░░  1 tutor  │  10 alumnos │ 10 prom.   ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
```

**Visualización Mejorada (Horizontal Bar Chart):**

```
ISC     ███████████ 8 tutores | 120 alumnos | 15.0 prom | Balanceado
        #10B981 (color carrera)

IME     ████ 5 tutores | 75 alumnos | 15.0 prom | Balanceado
        #F59E0B

ITS     ██████░ 7 tutores | 98 alumnos | 14.0 prom | Levemente bajo
        #2563EB

IE      ██ 2 tutores | 28 alumnos | 14.0 prom | Bajo
        #8B5CF6

ICA     █ 2 tutores | 19 alumnos | 10.0 prom | Crítico ⚠️
        #F472B6

IMECA   █ 1 tutor | 10 alumnos | 10.0 prom | Crítico ⚠️
        #EF4444
```

**Especificación Técnica:**

```typescript
// src/components/dashboard/CarreraDistributionChart.tsx

interface CarreraDistribution {
  carrera: "ITS" | "ISC" | "IME" | "IMECA" | "IE" | "ICA";
  nombreCompleto: string;
  totalTutores: number;
  totalAlumnos: number;
  promedioAlumnos: number;
  capacidadMaxima: number; // 20 alumnos
  color: string;
  status: "balanceado" | "levemente_bajo" | "bajo" | "crítico";
}

const CarreraDistributionChart = ({ data }: { data: CarreraDistribution[] }) => {
  return (
    <Card className="p-6">
      <h3 className="text-lg font-semibold text-gray-900 mb-6">
        Distribución de Tutores por Carrera
      </h3>

      <div className="space-y-6">
        {data.map((carrera) => {
          const barWidth = (carrera.totalTutores / 8) * 100; // 8 = max tutores
          const statusColor = {
            balanceado: "bg-green-500",
            levemente_bajo: "bg-amber-400",
            bajo: "bg-orange-500",
            crítico: "bg-red-500",
          }[carrera.status];

          return (
            <div key={carrera.carrera}>
              {/* Label */}
              <div className="flex items-center justify-between mb-2">
                <div className="flex items-center gap-2">
                  <div
                    className="w-3 h-3 rounded-full"
                    style={{ backgroundColor: carrera.color }}
                  />
                  <span className="font-medium text-gray-900">
                    {carrera.carrera}
                  </span>
                  <span className="text-xs text-gray-600">
                    {carrera.nombreCompleto}
                  </span>
                </div>

                {carrera.status === "crítico" && (
                  <Badge variant="danger">⚠️ Crítico</Badge>
                )}
              </div>

              {/* Bar Chart */}
              <div className="flex items-center gap-2 mb-2">
                <div className="flex-1 h-8 bg-gray-100 rounded-full overflow-hidden">
                  <div
                    className={`h-full transition-all ${statusColor}`}
                    style={{ width: `${barWidth}%` }}
                  />
                </div>
                <span className="text-sm font-medium text-gray-900 w-12">
                  {carrera.totalTutores} tutores
                </span>
              </div>

              {/* Stats */}
              <div className="flex gap-4 text-xs text-gray-600">
                <span>{carrera.totalAlumnos} alumnos</span>
                <span>|</span>
                <span>{carrera.promedioAlumnos.toFixed(1)} prom.</span>
                <span>|</span>
                <span className={carrera.promedioAlumnos > 18 ? "text-red-600" : ""}>
                  {carrera.promedioAlumnos < 12 ? "Bajo" : "Normal"}
                </span>
              </div>
            </div>
          );
        })}
      </div>

      {/* Legenda */}
      <div className="mt-6 pt-6 border-t border-gray-200 flex gap-4 flex-wrap text-xs">
        <div className="flex items-center gap-1">
          <div className="w-2 h-2 rounded-full bg-green-500" />
          <span>Balanceado</span>
        </div>
        <div className="flex items-center gap-1">
          <div className="w-2 h-2 rounded-full bg-amber-400" />
          <span>Levemente Bajo</span>
        </div>
        <div className="flex items-center gap-1">
          <div className="w-2 h-2 rounded-full bg-orange-500" />
          <span>Bajo</span>
        </div>
        <div className="flex items-center gap-1">
          <div className="w-2 h-2 rounded-full bg-red-500" />
          <span>Crítico</span>
        </div>
      </div>
    </Card>
  );
};

export default CarreraDistributionChart;
```

---

### SECCIÓN 4: Top 10 Tutores Saturados (Right Card)

```
TOP 10 TUTORES SATURADOS
╔═════════════════════════════════════════════╗
║                                             ║
║ 1. Dr. García (ISC)        20/20 ▓▓▓▓▓▓▓▓▓▓ ║
║    100% | Capacidad llena  ⚠️ CRÍTICO      ║
║                                             ║
║ 2. Dra. López (IME)        19/20 ▓▓▓▓▓▓▓▓░ ║
║    95% | Casi lleno        ⚠️ ALERTA      ║
║                                             ║
║ 3. Dr. Martín (ITS)        18/20 ▓▓▓▓▓▓░░  ║
║    90% | Bastante lleno    ⚠️ ALERTA      ║
║                                             ║
║ 4. Dra. Ruiz (ISC)         17/20 ▓▓▓▓▓░░░  ║
║    85% | Moderadamente     ✓ Normal       ║
║                                             ║
║ 5. Dr. Fernández (IME)     16/20 ▓▓▓▓░░░░  ║
║    80% | Moderadamente     ✓ Normal       ║
║                                             ║
║ 6-10. [...]                                 ║
║                                             ║
║ [Ver todos los tutores →]                  ║
║                                             ║
╚═════════════════════════════════════════════╝
```

**Especificación Técnica:**

```typescript
// src/components/dashboard/TopSaturatedTutors.tsx

interface TutorSaturation {
  id: number;
  nombre: string;
  carrera: string;
  alumnosActuales: number;
  capacidadMaxima: number;
  porcentajeSaturacion: number;
  estado: "crítico" | "alerta" | "normal" | "bajo";
  color: string; // color carrera
}

const TopSaturatedTutors = ({ data }: { data: TutorSaturation[] }) => {
  const statusConfig = {
    crítico: { color: "bg-red-500", label: "⚠️ CRÍTICO", badge: "danger" },
    alerta: { color: "bg-orange-500", label: "⚠️ ALERTA", badge: "warning" },
    normal: { color: "bg-green-500", label: "✓ Normal", badge: "success" },
    bajo: { color: "bg-blue-500", label: "○ Bajo", badge: "info" },
  };

  return (
    <Card className="p-6">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">
        Top 10 Tutores Saturados
      </h3>

      <div className="space-y-4">
        {data.slice(0, 10).map((tutor, idx) => {
          const config = statusConfig[tutor.estado];
          const porcentaje = (tutor.alumnosActuales / tutor.capacidadMaxima) * 100;

          return (
            <div key={tutor.id} className="border-b border-gray-200 pb-3 last:border-0">
              {/* Rank + Nombre */}
              <div className="flex items-start justify-between mb-2">
                <div className="flex items-center gap-2">
                  <span className="text-sm font-bold text-gray-500 w-5">
                    {idx + 1}.
                  </span>
                  <div>
                    <p className="font-medium text-gray-900">{tutor.nombre}</p>
                    <p className="text-xs text-gray-600">
                      <span
                        className="inline-block w-2 h-2 rounded-full mr-1"
                        style={{ backgroundColor: tutor.color }}
                      />
                      {tutor.carrera}
                    </p>
                  </div>
                </div>
                <Badge variant={config.badge}>
                  {tutor.porcentajeSaturacion.toFixed(0)}%
                </Badge>
              </div>

              {/* Progress Bar */}
              <div className="flex items-center gap-2">
                <div className="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden">
                  <div
                    className={`h-full transition-all ${config.color}`}
                    style={{ width: `${porcentaje}%` }}
                  />
                </div>
                <span className="text-xs font-medium text-gray-700 w-16 text-right">
                  {tutor.alumnosActuales}/{tutor.capacidadMaxima}
                </span>
              </div>

              {/* Status */}
              <p className="text-xs text-gray-600 mt-1">
                {config.label}
              </p>
            </div>
          );
        })}
      </div>

      <div className="mt-4 pt-4 border-t border-gray-200">
        <button className="text-sm text-primary-600 hover:text-primary-700 font-medium">
          Ver todos los tutores →
        </button>
      </div>
    </Card>
  );
};

export default TopSaturatedTutors;
```

---

### SECCIÓN 5: Tabla Completa de Tutores (Full Width)

```
DETALLE COMPLETO DE TUTORES
┌──────┬─────────────────┬────────┬────────────┬─────────────┬──────────┬──────────────┐
│ #    │ Nombre          │Carrera │ Alumnos    │ Capacidad   │ Estado   │ Acciones     │
├──────┼─────────────────┼────────┼────────────┼─────────────┼──────────┼──────────────┤
│ 1    │ Dr. García      │ ISC    │ 20/20      │ ▓▓▓▓▓▓▓▓▓▓  │ 🔴 Lleno │ [Editar]     │
│      │ Carrera #10B981 │        │ 100%       │ 100%        │ Crítico  │ [Ver alumnos]│
├──────┼─────────────────┼────────┼────────────┼─────────────┼──────────┼──────────────┤
│ 2    │ Dra. López      │ IME    │ 19/20      │ ▓▓▓▓▓▓▓▓░░  │ 🟠 Casi  │ [Editar]     │
│      │ Carrera #F59E0B │        │ 95%        │ 95%         │ Alerta   │ [Ver alumnos]│
├──────┼─────────────────┼────────┼────────────┼─────────────┼──────────┼──────────────┤
│ 3    │ Dr. Martín      │ ITS    │ 14/20      │ ▓▓▓▓▓░░░░░  │ 🟢 Normal│ [Editar]     │
│      │ Carrera #2563EB │        │ 70%        │ 70%         │          │ [Ver alumnos]│
├──────┼─────────────────┼────────┼────────────┼─────────────┼──────────┼──────────────┤
│ ...  │ ...             │ ...    │ ...        │ ...         │ ...      │ ...          │
└──────┴─────────────────┴────────┴────────────┴─────────────┴──────────┴──────────────┘

Mostrando 1-10 de 25   [< Anterior] [Siguiente >]
```

**Especificación Técnica:**

```typescript
// src/components/dashboard/TutoresCompleteTable.tsx

interface TutorTableRow {
  id: number;
  nombre: string;
  carrera: string;
  alumnosActuales: number;
  capacidadMaxima: number;
  porcentaje: number;
  estado: "crítico" | "alerta" | "normal" | "bajo";
  color: string;
  lastUpdate: Date;
}

interface TutoresCompleteTableProps {
  data: TutorTableRow[];
  isLoading?: boolean;
  onViewAlumnos: (tutorId: number) => void;
  onEdit: (tutorId: number) => void;
}

const TutoresCompleteTable = ({
  data,
  isLoading,
  onViewAlumnos,
  onEdit,
}: TutoresCompleteTableProps) => {
  const [pagination, setPagination] = useState({ page: 1, pageSize: 10 });
  const [sorting, setSorting] = useState({ field: "porcentaje", order: "desc" });

  const columns = [
    {
      accessor: "id",
      header: "#",
      width: "50px",
      cell: (value: number, idx: number) => (
        <span className="font-medium text-gray-700">
          {(pagination.page - 1) * pagination.pageSize + idx + 1}
        </span>
      ),
    },
    {
      accessor: "nombre",
      header: "Nombre",
      width: "200px",
      sortable: true,
      cell: (value: string, row: TutorTableRow) => (
        <div>
          <p className="font-medium text-gray-900">{value}</p>
          <p className="text-xs text-gray-600">
            <span
              className="inline-block w-2 h-2 rounded-full mr-1"
              style={{ backgroundColor: row.color }}
            />
            Carrera {row.carrera}
          </p>
        </div>
      ),
    },
    {
      accessor: "carrera",
      header: "Carrera",
      width: "100px",
      cell: (value: string, row: TutorTableRow) => (
        <CarreraBadge carrera={value} />
      ),
    },
    {
      accessor: "alumnosActuales",
      header: "Alumnos",
      width: "130px",
      sortable: true,
      cell: (value: number, row: TutorTableRow) => (
        <div className="space-y-1">
          <p className="font-medium text-gray-900">
            {value}/{row.capacidadMaxima}
          </p>
          <p className="text-xs text-gray-600">{row.porcentaje.toFixed(0)}%</p>
        </div>
      ),
    },
    {
      accessor: "porcentaje",
      header: "Capacidad",
      width: "120px",
      sortable: true,
      cell: (value: number) => {
        const color =
          value >= 95 ? "bg-red-500" : value >= 80 ? "bg-orange-400" : "bg-green-500";
        return (
          <div className="flex items-center gap-2">
            <div className="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden">
              <div className={`h-full ${color}`} style={{ width: `${value}%` }} />
            </div>
            <span className="text-xs font-medium w-8">{value.toFixed(0)}%</span>
          </div>
        );
      },
    },
    {
      accessor: "estado",
      header: "Estado",
      width: "100px",
      cell: (value: string) => {
        const variants = {
          crítico: "danger",
          alerta: "warning",
          normal: "success",
          bajo: "info",
        };
        const labels = {
          crítico: "🔴 Crítico",
          alerta: "🟠 Alerta",
          normal: "🟢 Normal",
          bajo: "🔵 Bajo",
        };
        return (
          <Badge variant={variants[value as keyof typeof variants]}>
            {labels[value as keyof typeof labels]}
          </Badge>
        );
      },
    },
    {
      accessor: "id",
      header: "Acciones",
      width: "120px",
      cell: (tutorId: number, row: TutorTableRow) => (
        <div className="flex gap-1">
          <Button
            variant="tertiary"
            size="sm"
            icon={<EditIcon />}
            onClick={() => onEdit(tutorId)}
            title="Editar tutor"
          />
          <Button
            variant="tertiary"
            size="sm"
            icon={<EyeIcon />}
            onClick={() => onViewAlumnos(tutorId)}
            title="Ver alumnos"
          />
        </div>
      ),
    },
  ];

  return (
    <Card>
      <div className="p-6 space-y-4">
        <h3 className="text-lg font-semibold text-gray-900">
          Detalle Completo de Tutores
        </h3>

        {/* Filtros opcionales */}
        <div className="flex gap-2">
          <Select
            placeholder="Filtrar por carrera..."
            options={carreraOptions}
          />
          <Select
            placeholder="Filtrar por estado..."
            options={[
              { value: "crítico", label: "🔴 Crítico" },
              { value: "alerta", label: "🟠 Alerta" },
              { value: "normal", label: "🟢 Normal" },
              { value: "bajo", label: "🔵 Bajo" },
            ]}
          />
        </div>

        {/* DataTable */}
        <DataTable
          columns={columns}
          data={data}
          isLoading={isLoading}
          pagination={{
            current: pagination.page,
            pageSize: pagination.pageSize,
            total: data.length,
            onPageChange: (page) => setPagination({ ...pagination, page }),
          }}
          sorting={{
            field: sorting.field,
            order: sorting.order as "asc" | "desc",
            onSort: (field, order) => setSorting({ field, order }),
          }}
        />
      </div>
    </Card>
  );
};

export default TutoresCompleteTable;
```

---

### SECCIÓN 6: Alertas y Notificaciones

```
ALERTAS DEL SISTEMA
┌──────────────────────────────────────────────────────────────┐
│ 🔴 CRÍTICA - Tutores con capacidad completa (3 casos)       │
│    Dr. García (ISC) 20/20                                    │
│    Dr. Pérez (IME) 20/20                                     │
│    Dr. López (ITS) 20/20                                     │
│    → Acción: Reasignar alumnos                               │
│                                                               │
│ 🟠 ALERTA - Carreras con desbalance significativo            │
│    ICA: 2 tutores con 10 alumnos (5 prom.) vs 15 ideal      │
│    IMECA: 1 tutor con 10 alumnos (10 prom.) - crítico       │
│    → Acción: Considerar nueva asignación                     │
│                                                               │
│ 🟡 ADVERTENCIA - Tutores cercanos a capacidad (80%+)        │
│    5 tutores en rango 80-95%                                 │
│    → Acción: Monitorear próxima semana                       │
│                                                               │
└──────────────────────────────────────────────────────────────┘
```

**Componente AlertSummary:**

```typescript
// src/components/dashboard/AlertsSummary.tsx

interface Alert {
  id: string;
  severity: "crítica" | "alerta" | "advertencia";
  title: string;
  description: string;
  count?: number;
  action?: {
    label: string;
    onClick: () => void;
  };
}

const AlertsSummary = ({ alerts }: { alerts: Alert[] }) => {
  const severityConfig = {
    crítica: { icon: "🔴", color: "bg-red-50", border: "border-l-red-500" },
    alerta: { icon: "🟠", color: "bg-orange-50", border: "border-l-orange-500" },
    advertencia: { icon: "🟡", color: "bg-yellow-50", border: "border-l-yellow-500" },
  };

  return (
    <div className="space-y-3">
      {alerts.map((alert) => {
        const config = severityConfig[alert.severity];
        return (
          <div
            key={alert.id}
            className={`${config.color} ${config.border} border-l-4 rounded p-4`}
          >
            <div className="flex items-start justify-between">
              <div>
                <p className="font-semibold text-gray-900">
                  {config.icon} {alert.title}
                </p>
                <p className="text-sm text-gray-700 mt-1">{alert.description}</p>
              </div>
              {alert.action && (
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={alert.action.onClick}
                  className="flex-shrink-0"
                >
                  {alert.action.label}
                </Button>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default AlertsSummary;
```

---

## 🎨 Esquema de Colores del Dashboard

### Colores por Carrera (Muy Visible)

```typescript
const CARRERA_COLORS = {
  "ISC": {
    bg: "bg-emerald-50",
    border: "border-emerald-300",
    bar: "bg-emerald-500",
    hex: "#10B981",
    name: "Ingeniería en Sistemas Computacionales"
  },
  "IME": {
    bg: "bg-amber-50",
    border: "border-amber-300",
    bar: "bg-amber-500",
    hex: "#F59E0B",
    name: "Ingeniería Mecánica"
  },
  "ITS": {
    bg: "bg-blue-50",
    border: "border-blue-300",
    bar: "bg-blue-500",
    hex: "#2563EB",
    name: "Ingeniería en Tecnologías de Software"
  },
  "IE": {
    bg: "bg-purple-50",
    border: "border-purple-300",
    bar: "bg-purple-500",
    hex: "#8B5CF6",
    name: "Ingeniería Eléctrica"
  },
  "ICA": {
    bg: "bg-pink-50",
    border: "border-pink-300",
    bar: "bg-pink-500",
    hex: "#F472B6",
    name: "Ingeniería en Computación y Automatización"
  },
  "IMECA": {
    bg: "bg-red-50",
    border: "border-red-300",
    bar: "bg-red-500",
    hex: "#EF4444",
    name: "Ingeniería Mecatrónica"
  }
};

// Estados de saturación
const SATURATION_COLORS = {
  crítico: "🔴 #EF4444",     // Rojo - LLENO
  alerta: "🟠 #F59E0B",      // Ámbar - CASI LLENO (>80%)
  normal: "🟢 #22C55E",      // Verde - NORMAL (50-80%)
  bajo: "🔵 #3B82F6"         // Azul - BAJO (<50%)
};
```

---

## 📊 API Endpoints Requeridos

```typescript
// GET /api/dashboard/estadisticas?semestreId=1
{
  success: true,
  data: {
    semestre: "2025-2025-FN",
    semestre_id: 1,

    // Stats generales
    total_tutores: 25,
    total_alumnos: 350,
    promedio_alumnos_por_tutor: 14,

    // Desbalance
    desbalance_porcentaje: 32, // (max - min) / promedio * 100
    desbalance_rango: { min: 8, max: 20 },
    desbalance_estado: "alto",

    // Por carrera
    por_carrera: [
      {
        carrera: "ISC",
        totalTutores: 8,
        totalAlumnos: 120,
        promedioAlumnos: 15,
        estado: "balanceado"
      },
      // ... más carreras
    ],

    // Top saturados
    top_saturados: [
      {
        id: 1,
        nombre: "Dr. García",
        carrera: "ISC",
        alumnosActuales: 20,
        capacidadMaxima: 20,
        porcentajeSaturacion: 100,
        estado: "crítico"
      },
      // ... más tutores
    ],

    // Todos los tutores
    todos_tutores: [
      {
        id: 1,
        nombre: "Dr. García",
        carrera: "ISC",
        alumnosActuales: 20,
        capacidadMaxima: 20,
        porcentajeSaturacion: 100,
        estado: "crítico",
        lastUpdate: "2025-11-20T10:15:00Z"
      },
      // ... más tutores
    ],

    // Alertas
    alertas: [
      {
        id: "alert-1",
        severity: "crítica",
        title: "Tutores con capacidad completa",
        description: "3 tutores tienen 20/20 alumnos",
        count: 3
      },
      // ... más alertas
    ]
  }
}
```

---

## 🔄 Actualización en Tiempo Real

```typescript
// src/pages/DashboardPage.tsx

const DashboardPage = () => {
  const [semestreId, setSemestreId] = useState<number | null>(null);
  const [lastUpdate, setLastUpdate] = useState<Date>(new Date());

  // Query con auto-refresh cada 5 minutos
  const { data: dashboard, isLoading, isFetching } = useQuery({
    queryKey: ["dashboard", semestreId],
    queryFn: () => getDashboardData(semestreId!),
    enabled: !!semestreId,
    refetchInterval: 5 * 60 * 1000, // 5 minutos
    staleTime: 2 * 60 * 1000, // 2 minutos
    onSuccess: () => setLastUpdate(new Date()),
  });

  const handleManualRefresh = () => {
    queryClient.refetchQueries({ queryKey: ["dashboard", semestreId] });
  };

  return (
    <div className="space-y-6">
      {/* Header con período y refresh */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
          <p className="text-gray-600">Sistema de Gestión de Tutorías</p>
        </div>

        <div className="flex items-center gap-4">
          <SemestreSelect
            value={semestreId}
            onChange={setSemestreId}
          />

          <Button
            variant="secondary"
            loading={isFetching}
            onClick={handleManualRefresh}
            icon={<RefreshIcon />}
          >
            Actualizar
          </Button>

          <span className="text-xs text-gray-600">
            {isFetching ? "Actualizando..." : `Actualizado hace ${getTimeAgo(lastUpdate)}`}
          </span>
        </div>
      </div>

      {!semestreId ? (
        <EmptyState
          title="Selecciona un semestre"
          description="Elige un período académico para ver el dashboard"
        />
      ) : isLoading ? (
        <LoadingState />
      ) : (
        <>
          {/* Alertas */}
          {dashboard?.alertas && dashboard.alertas.length > 0 && (
            <AlertsSummary alerts={dashboard.alertas} />
          )}

          {/* Stats Cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <StatCard
              title="Total Tutores"
              value={dashboard?.total_tutores || 0}
              subtitle="tutores activos"
              color="primary-600"
              // ... comparison data
            />
            <StatCard
              title="Total Alumnos"
              value={dashboard?.total_alumnos || 0}
              subtitle="alumnos asignados"
              color="secondary-500"
              // ... comparison data
            />
            <StatCard
              title="Promedio por Tutor"
              value={dashboard?.promedio_alumnos_por_tutor || 0}
              subtitle="alumnos/tutor"
              color="accent-500"
              // ... comparison data
            />
            <StatCard
              title="Desbalance"
              value={dashboard?.desbalance_estado}
              subtitle={`${dashboard?.desbalance_porcentaje}% (${dashboard?.desbalance_rango.min}-${dashboard?.desbalance_rango.max})`}
              color={dashboard?.desbalance_porcentaje > 25 ? "danger-500" : "warning-500"}
              // ... comparison data
            />
          </div>

          {/* Grid 2 Columnas */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <CarreraDistributionChart data={dashboard?.por_carrera || []} />
            <TopSaturatedTutors data={dashboard?.top_saturados || []} />
          </div>

          {/* Full Width Table */}
          <TutoresCompleteTable
            data={dashboard?.todos_tutores || []}
            isLoading={isLoading}
            onViewAlumnos={handleViewAlumnos}
            onEdit={handleEditTutor}
          />
        </>
      )}
    </div>
  );
};

export default DashboardPage;
```

---

## 🎯 Acciones del Dashboard

### Acciones Contextuales

```
1. Click en Tutor "Dr. García"
   → Abre modal con lista de alumnos del tutor
   → Opción: "Reasignar alumno"

2. Click en "Ver todos los tutores"
   → Navega a página de Tutores
   → Filtra por saturación

3. Click en "Reasignar alumnos" (alerta)
   → Abre Wizard de Cambio de Tutor
   → Pre-selecciona carrera saturada

4. Click en "Actualizar"
   → Refresh manual del dashboard
   → Loading state visible

5. Cambio de Semestre
   → Recarga todo el dashboard
   → Nuevo gráfico, nuevas stats
```

---

## 📱 Responsividad

```
DESKTOP (1024px+):
├─ Grid 4 columnas (stats)
├─ Grid 2 columnas (charts)
└─ Full width table

TABLET (768px - 1023px):
├─ Grid 2 columnas (stats)
├─ Grid 1 columna (charts)
└─ Tabla scrolleable

MOBILE (< 768px):
├─ Grid 1 columna (stats)
├─ Stack vertical (charts)
├─ Tabla con scroll horizontal
└─ Botones stacked
```

---

## ✅ Checklist de Implementación

```
FASE 1: Componentes Base
☐ StatCard component
☐ AlertsSummary component
☐ CarreraDistributionChart component
☐ TopSaturatedTutors component
☐ TutoresCompleteTable component

FASE 2: Page Integration
☐ DashboardPage component
☐ Integración con API
☐ Error handling
☐ Loading states

FASE 3: Polish
☐ Responsive design
☐ Animations
☐ Accessibility (WCAG AA)
☐ Performance optimization

FASE 4: Testing
☐ Unit tests (componentes)
☐ Integration tests (page)
☐ E2E tests (flujos)
☐ Accessibility audit
```

---

## 🚀 Prioridad de Implementación

```
MUST HAVE (MVP):
1. ✅ Stats Cards (4)
2. ✅ Carrera Distribution Chart
3. ✅ Top Saturated Tutors List
4. ✅ Complete Tutors Table

NICE TO HAVE (v1.1):
5. Alerts Summary
6. Manual refresh button
7. Semester selector con history
8. Export reports button

FUTURE (v2.0):
9. Real-time WebSocket updates
10. Custom date range filter
11. Prediction analytics
12. Tutor workload comparison charts
```

---

## 📊 Wireframe Completo (ASCII Art)

```
╔════════════════════════════════════════════════════════════════════╗
║  DASHBOARD - Sistema de Gestión de Tutorías                       ║
║                                                                    ║
║  Dashboard > Período: [▼ 2025-2025-FN] [Actualizar]              ║
║                                                                    ║
║  ┌──────────────┬──────────────┬──────────────┬──────────────┐   ║
║  │ 25 Tutores   │ 350 Alumnos  │ 14 Promedio  │ 32% Desbal.  │   ║
║  │ vs 24 (↑1)   │ vs 340 (↑10) │ vs 14.2 (↓)  │ vs Bajo ↑    │   ║
║  └──────────────┴──────────────┴──────────────┴──────────────┘   ║
║                                                                    ║
║  ┌──────────────────────────────────┬──────────────────────────┐  ║
║  │ DISTRIBUCIÓN POR CARRERA        │ TOP 10 SATURADOS       │  ║
║  │                                 │                        │  ║
║  │ ISC ████████░░░░░░░░░░░░░░░░░░ │ 1. Dr. García  100%   │  ║
║  │ IME ████░░░░░░░░░░░░░░░░░░░░░░ │ 2. Dra. López   95%   │  ║
║  │ ITS ██████░░░░░░░░░░░░░░░░░░░░░ │ 3. Dr. Martín   90%   │  ║
║  │ IE  ██░░░░░░░░░░░░░░░░░░░░░░░░ │ 4. Dra. Ruiz    85%   │  ║
║  │ ICA █░░░░░░░░░░░░░░░░░░░░░░░░░ │ 5. Dr. Fernández80%  │  ║
║  │ IMECA█░░░░░░░░░░░░░░░░░░░░░░░░ │ ...                    │  ║
║  │                                 │ [Ver todos →]          │  ║
║  └──────────────────────────────────┴──────────────────────────┘  ║
║                                                                    ║
║  DETALLE COMPLETO DE TUTORES                                     ║
║  ┌──────┬─────────────────┬────────┬──────────┬────────┬────────┐ ║
║  │ #    │ Nombre          │Carrera │ Alumnos  │Estado  │Acciones│ ║
║  ├──────┼─────────────────┼────────┼──────────┼────────┼────────┤ ║
║  │ 1    │ Dr. García      │ ISC    │ 20/20    │🔴Crít. │[✎][👁]│ ║
║  │ 2    │ Dra. López      │ IME    │ 19/20    │🟠Alert │[✎][👁]│ ║
║  │ 3    │ Dr. Martín      │ ITS    │ 14/20    │🟢Norm. │[✎][👁]│ ║
║  │ ... (10 por página)               ...       │   ...  │ ...  │ ║
║  └──────┴─────────────────┴────────┴──────────┴────────┴────────┘ ║
║  Mostrando 1-10 de 25  [< Anterior] [Siguiente >]                ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

## 🎓 Conclusión

Este dashboard proporciona una **vista ejecutiva completa** de:

✅ **Visibilidad:** Qué carrera, qué tutor, cuántos alumnos
✅ **Alertas:** Tutores saturados, desbalances detectados
✅ **Acciones:** Reasignaciones, cambios, monitoreo
✅ **Análisis:** Comparativas, tendencias, proyecciones
✅ **Diseño:** Minimalista, profesional, accesible

**Estado:** Listo para implementación inmediata.

