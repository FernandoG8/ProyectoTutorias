# 📊 Dashboard Visual - Quick Reference Guide

**Referencia Rápida de Componentes, Colores y Comportamientos**

---

## 🎨 Paleta de Colores

### Colores por Carrera (CRÍTICOS - Usar en Todo)

```
ISC  → #10B981 (Verde Esmeralda)
       bg-emerald-50 | border-emerald-300 | bar: emerald-500

IME  → #F59E0B (Ámbar)
       bg-amber-50   | border-amber-300   | bar: amber-500

ITS  → #2563EB (Azul Cobalto)
       bg-blue-50    | border-blue-300    | bar: blue-500

IE   → #8B5CF6 (Púrpura)
       bg-purple-50  | border-purple-300  | bar: purple-500

ICA  → #F472B6 (Rosa)
       bg-pink-50    | border-pink-300    | bar: pink-500

IMECA→ #EF4444 (Rojo)
       bg-red-50     | border-red-300     | bar: red-500
```

### Estados de Saturación (CRÍTICOS - Usar Consistentemente)

```
🔴 CRÍTICO  → #EF4444 (Rojo)       → ≥95% capacidad
              bg-red-50, text-red-900, border-red-200

🟠 ALERTA   → #F59E0B (Ámbar)      → 80-95% capacidad
              bg-amber-50, text-amber-900, border-amber-200

🟢 NORMAL   → #22C55E (Verde)      → 50-80% capacidad
              bg-green-50, text-green-900, border-green-200

🔵 BAJO     → #3B82F6 (Azul)       → <50% capacidad
              bg-blue-50, text-blue-900, border-blue-200
```

---

## 📋 Estructura del Dashboard

```
HEADER
├─ Logo/Título: "Dashboard"
├─ Descripción: "Sistema de Gestión de Tutorías"
├─ Período: [Select Semestre] [Anterior] [Siguiente] [Hoy]
└─ Refresh: [Botón] "Actualizado hace X minutos"

CONTENT
├─ SECCIÓN 1: Cards (4 KPIs en grid)
│  ├─ Total Tutores
│  ├─ Total Alumnos
│  ├─ Promedio por Tutor
│  └─ Desbalance (ALERTA)
│
├─ SECCIÓN 2-3: Grid 2 Columnas
│  ├─ (Izq) Distribución por Carrera (Chart)
│  └─ (Der) Top 10 Tutores Saturados (List)
│
└─ SECCIÓN 4: Full Width
   └─ Tabla Completa de Tutores
      ├─ Filtros (Carrera, Estado)
      ├─ Sort columns
      ├─ Paginación
      └─ Acciones (Edit, Ver Alumnos)
```

---

## 🎯 Las 6 Secciones en Detalle

### SECCIÓN 1: Cards de Estadísticas

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────┐ │
│  │ 25 TUTORES     │  │ 350 ALUMNOS    │  │ 14 PROMEDIO  │ │
│  │ vs 24 (+1) ↑   │  │ vs 340 (+10) ↑ │  │ vs 14.2 (↓)  │ │
│  │ primary-600    │  │ secondary-500  │  │ accent-500   │ │
│  └────────────────┘  └────────────────┘  └──────────────┘ │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ ⚠️ DESBALANCE: ALTO (32%)                           │  │
│  │ Rango: 8-20 alumnos (vs ideal 15 ±3)               │  │
│  │ vs Bajo semana anterior (+17%) ↑                    │  │
│  │ danger-500 bg-red-50 border-l-4 border-red-500     │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**Responsividad:**
```
Desktop (1024+): grid-cols-4 gap-4
Tablet (768-1023): grid-cols-2 gap-4
Mobile (<768): grid-cols-1 gap-3
```

---

### SECCIÓN 2: Distribución por Carrera

```
┌────────────────────────────────────────────────────────┐
│ DISTRIBUCIÓN DE TUTORES POR CARRERA                   │
├────────────────────────────────────────────────────────┤
│                                                        │
│ ● ISC                                                 │
│   ██████████░░░░░░░░░░░░░░  8 tutores | 120 alumnos  │
│   15.0 prom | bg-emerald-50 | #10B981                │
│                                                        │
│ ● IME                                                 │
│   █████░░░░░░░░░░░░░░░░░░░  5 tutores | 75 alumnos   │
│   15.0 prom | bg-amber-50 | #F59E0B                  │
│                                                        │
│ ● ITS                                                 │
│   ███████░░░░░░░░░░░░░░░░░  7 tutores | 98 alumnos   │
│   14.0 prom | bg-blue-50 | #2563EB                   │
│                                                        │
│ ● IE                                                  │
│   ██░░░░░░░░░░░░░░░░░░░░░░  2 tutores | 28 alumnos   │
│   14.0 prom | bg-purple-50 | #8B5CF6                 │
│                                                        │
│ ● ICA          ⚠️ CRÍTICO                             │
│   █░░░░░░░░░░░░░░░░░░░░░░  2 tutores | 19 alumnos   │
│   10.0 prom | bg-pink-50 | #F472B6                   │
│                                                        │
│ ● IMECA        ⚠️ CRÍTICO                             │
│   █░░░░░░░░░░░░░░░░░░░░░░  1 tutor | 10 alumnos     │
│   10.0 prom | bg-red-50 | #EF4444                    │
│                                                        │
└────────────────────────────────────────────────────────┘
```

**CSS Pattern:**
```css
.carrera-bar {
  @apply flex items-center gap-2 mb-3;

  .label {
    @apply flex items-center gap-2 w-32;
    .color-dot {
      @apply w-3 h-3 rounded-full;
    }
  }

  .progress {
    @apply flex-1 h-8 bg-gray-100 rounded-full overflow-hidden;
    .fill {
      @apply h-full transition-all;
    }
  }
}
```

---

### SECCIÓN 3: Top 10 Tutores Saturados

```
┌────────────────────────────────────────┐
│ TOP 10 TUTORES SATURADOS              │
├────────────────────────────────────────┤
│                                        │
│ 1. Dr. García (ISC)     20/20  100%   │
│    ██████████████████  🔴 CRÍTICO     │
│    #10B981 color bar                  │
│                                        │
│ 2. Dra. López (IME)     19/20   95%   │
│    ███████████████░░   🟠 ALERTA      │
│    #F59E0B color bar                  │
│                                        │
│ 3. Dr. Martín (ITS)     18/20   90%   │
│    ██████████████░░░   🟠 ALERTA      │
│    #2563EB color bar                  │
│                                        │
│ 4. Dra. Ruiz (ISC)      17/20   85%   │
│    █████████████░░░░   🟡 NORMAL      │
│    #10B981 color bar                  │
│                                        │
│ 5. Dr. Fernández (IME)  16/20   80%   │
│    ████████████░░░░░   🟡 NORMAL      │
│    #F59E0B color bar                  │
│                                        │
│ ... (6-10)                            │
│                                        │
│ [Ver todos los tutores →]             │
│                                        │
└────────────────────────────────────────┘
```

**Algoritmo de Colores por Estado:**
```typescript
const getStatusColor = (percentage: number) => {
  if (percentage >= 95) return { emoji: "🔴", color: "bg-red-500", badge: "danger" };
  if (percentage >= 80) return { emoji: "🟠", color: "bg-orange-400", badge: "warning" };
  if (percentage >= 50) return { emoji: "🟢", color: "bg-green-500", badge: "success" };
  return { emoji: "🔵", color: "bg-blue-500", badge: "info" };
};
```

---

### SECCIÓN 4: Tabla Completa de Tutores

```
┌──────┬───────────────────┬────────┬──────────┬────────┬─────────┐
│ #    │ Nombre            │Carrera │ Alumnos  │ %      │Acciones │
├──────┼───────────────────┼────────┼──────────┼────────┼─────────┤
│      │                   │        │          │        │         │
│ 1    │ Dr. García        │ ISC    │ 20/20    │ ●100%  │[✎][👁] │
│      │ #10B981 dot       │ green  │ 100%     │▓▓▓▓▓▓▓│ red bg │
│      │                   │        │ 🔴CRÍTICO│        │         │
├──────┼───────────────────┼────────┼──────────┼────────┼─────────┤
│ 2    │ Dra. López        │ IME    │ 19/20    │ ●95%   │[✎][👁] │
│      │ #F59E0B dot       │ amber  │ 95%      │▓▓▓▓▓░ │ orange  │
│      │                   │        │ 🟠ALERTA │        │         │
├──────┼───────────────────┼────────┼──────────┼────────┼─────────┤
│ 3    │ Dr. Martín        │ ITS    │ 14/20    │ ●70%   │[✎][👁] │
│      │ #2563EB dot       │ blue   │ 70%      │▓▓▓░░░ │ blue    │
│      │                   │        │ 🟢NORMAL │        │         │
├──────┴───────────────────┴────────┴──────────┴────────┴─────────┤
│ ...                                                              │
├──────────────────────────────────────────────────────────────────┤
│ Mostrando 1-10 de 25    [< Anterior] [1/3] [Siguiente >]       │
└──────────────────────────────────────────────────────────────────┘
```

**CSS Grid:**
```css
.tutores-table {
  @apply w-full border-collapse;

  thead {
    @apply bg-gray-50 sticky top-0 z-10;
    th {
      @apply px-4 py-3 text-left text-xs font-semibold
             text-gray-700 uppercase tracking-wider
             border-b border-gray-200;
    }
  }

  tbody {
    tr {
      @apply border-b border-gray-200
             hover:bg-blue-50 transition-colors;

      &:nth-child(even) {
        @apply bg-gray-50;
      }
    }

    td {
      @apply px-4 py-3 text-sm text-gray-900;
    }
  }
}
```

---

## 🔄 Flujos de Interacción

### Flujo 1: Cambiar Semestre

```
Usuario selecciona "Semestre 2025-2025-SP" en Select
    ↓
onSemestreChange triggers
    ↓
Query refetch con new semestreId
    ↓
Loading state visible en cards
    ↓
Dashboard actualiza (3-5 seg)
    ↓
Nueva data con colores y estados actualizados
```

### Flujo 2: Click en Tutor Saturado

```
Usuario hace click en "Dr. García 20/20"
    ↓
Modal abre con TutorAlumnosModal
    ↓
Carga lista de alumnos de Dr. García
    ↓
Usuario puede:
  - Ver detalles de cada alumno
  - Cambiar tutor (→ Wizard)
  - Volver (← cierra modal)
```

### Flujo 3: Auto-Refresh

```
Dashboard se monta
    ↓
Auto-refresh configurado (5 minutos)
    ↓
Timer silencioso refetch cada 5 min
    ↓
Si hay cambios, UI actualiza (smooth)
    ↓
Timestamp actualiza: "Hace 2 minutos"
```

---

## 🎨 Guía de Colores por Contexto

### Contexto 1: Color por Carrera

**Dónde usar:**
- Badge de carrera
- Punto en gráfico
- Barra de progreso
- Fondo de row en tabla

**Ejemplo:**
```typescript
<div className="flex items-center gap-2">
  <div
    className="w-3 h-3 rounded-full"
    style={{ backgroundColor: CARRERA_COLORS[carrera].hex }}
  />
  <span className="font-medium">{carrera}</span>
</div>
```

### Contexto 2: Color por Estado

**Dónde usar:**
- Badge de estado
- Icono de estado
- Barra de progreso de capacidad
- Fondo de alerta

**Ejemplo:**
```typescript
const statusConfig = {
  crítico: { emoji: "🔴", color: "#EF4444", bgClass: "bg-red-50" },
  alerta: { emoji: "🟠", color: "#F59E0B", bgClass: "bg-amber-50" },
  normal: { emoji: "🟢", color: "#22C55E", bgClass: "bg-green-50" },
  bajo: { emoji: "🔵", color: "#3B82F6", bgClass: "bg-blue-50" },
};
```

---

## 📊 Responsive Breakpoints

```
DESKTOP (1024px+)
├─ Stats: grid-cols-4
├─ Charts: grid-cols-2
├─ Table: Full width
└─ Font sizes: Normal

TABLET (768px - 1023px)
├─ Stats: grid-cols-2
├─ Charts: grid-cols-1
├─ Table: Horizontal scroll
└─ Font sizes: -2px

MOBILE (<768px)
├─ Stats: grid-cols-1 (stack)
├─ Charts: Stack vertical
├─ Table: Card layout
└─ Font sizes: -4px
```

---

## 🎯 Checklist de Implementación por Sección

```
SECCIÓN 1: Cards
☑ StatCard component
☑ Colores correctos
☑ Comparativa semanal
☑ Trending arrows (↑↓)

SECCIÓN 2: Chart
☑ CarreraDistributionChart
☑ Colores por carrera
☑ Bar widths proporcionales
☑ Estadísticas por carrera

SECCIÓN 3: Top Tutores
☑ TopSaturatedTutors component
☑ Ranking 1-10
☑ Progress bars coloreados
☑ Badges de estado

SECCIÓN 4: Tabla
☑ DataTable component
☑ Sticky headers
☑ Zebra striping
☑ Sortable columns
☑ Paginación
☑ Acciones (edit, view)

INTEGRACIÓN
☑ DashboardPage wrapper
☑ Query + auto-refresh
☑ Error boundaries
☑ Loading states
☑ Empty states

RESPONSIVE
☑ Mobile (<768px)
☑ Tablet (768-1023px)
☑ Desktop (1024+)

ACCESIBILIDAD
☑ WCAG AA contrast
☑ Focus visible
☑ ARIA labels
☑ Keyboard navigation
```

---

## 🚀 Estado de Implementación

```
MVP READY: ✅ 95%
├─ Diseño finalizado
├─ Componentes especificados
├─ Colores definidos
├─ API endpoints claros
└─ Código TypeScript ready

NEXT STEPS:
1. Implementar componentes
2. Integrar con API
3. Testing responsivo
4. UAT con usuarios
5. Deploy
```

---

**Referencia Rápida Lista para Implementación** ✅

