# FASE 1 – DIAGNÓSTICO Y CORRECCIÓN DEL LAYOUT

**Fecha:** 19 de Noviembre, 2025
**Estado:** ANÁLISIS COMPLETADO - LISTO PARA IMPLEMENTACIÓN

---

## 📋 ANÁLISIS TÉCNICO

### 1. Estructura de Layout Actual

#### Componentes involucrados:
```
src/components/layout/
├── MainLayout.tsx   (Contenedor raíz - APP SHELL)
├── Sidebar.tsx      (Navegación lateral)
└── Topbar.tsx       (Header superior)
```

#### Estructura HTML/Flex actual (MainLayout.tsx):
```jsx
<div className="flex min-h-screen bg-background">
  <Sidebar />
  <div className="flex flex-1 flex-col">
    <Topbar />
    <main className="flex-1 overflow-y-auto bg-background px-4 py-6">
      <div className="mx-auto flex max-w-7xl flex-col gap-6">
        <Outlet />
      </div>
    </main>
  </div>
</div>
```

**Calificación:** ✅ **LA ESTRUCTURA BASE ES CORRECTA**

El layout actual sigue el patrón "App Shell" recomendado:
- Raíz: `flex min-h-screen` (flex horizontal, cubre pantalla completa)
- Sidebar: ancho fijo (colapsable vía UI store)
- Contenedor derecho: `flex-1 flex flex-col` (toma espacio restante)
- Topbar: fijo en altura dentro del flex-col
- Main: `flex-1 overflow-y-auto` (toma espacio restante y scrollea)

---

### 2. Problemas Identificados

#### PROBLEMA 1: Sidebar - Comportamiento de `h-screen`
**Ubicación:** `Sidebar.tsx`, línea 47

```jsx
<aside className="... h-screen ... ">
```

**Análisis:**
- `h-screen` = `height: 100vh` (altura de la ventana del navegador)
- En contenedor flex, esto es correcto para evitar desbordamiento
- ✅ **NO REQUIERE CAMBIO** - Está bien implementado

---

#### PROBLEMA 2: Overflow anidado en `main`
**Ubicación:** `MainLayout.tsx`, línea 11

**Análisis del flujo de scroll:**
```
<div className="flex min-h-screen">              ← Root: NO scrollea
  <Sidebar h-screen overflow-y-auto />            ← Sidebar: scrollea internamente
  <div className="flex flex-1 flex-col">          ← Derecha: NO scrollea
    <Topbar className="sticky top-0" />           ← Header: sticky (bien)
    <main className="flex-1 overflow-y-auto">     ← Main: AQUÍ SCROLLEA
      <div className="mx-auto max-w-7xl">         ← Contenido centrado
        <Outlet />
      </div>
    </main>
  </div>
</div>
```

**Estado:** ✅ **CORRECTO** - Un único scroll vertical en la zona main

---

#### PROBLEMA 3: Tablas largas - Posible desbordamiento horizontal
**Ubicación:** Páginas como `AssignmentPage.tsx`, `DashboardPage.tsx`, `StudentsPage.tsx`

**Patrón encontrado:**
```jsx
<Card>
  <div className="space-y-4">
    <DataTable columns={columns} data={data} />  ← SIN CONTENEDOR overflow-x
  </div>
</Card>
```

**Análisis:**
- Las columnas de tablas pueden ser muy anchas (8+ columnas)
- `DataTable` genera un `<div>` con `role="grid"` sin `overflow-x-auto`
- En pantallas pequeñas/tablet: **se produce desbordamiento**
- El topbar y el main NO desbordan, pero la tabla sí

**CRÍTICA:** ⚠️ **REQUIERE CORRECCIÓN**

---

#### PROBLEMA 4: Estilos globales - Inconsistencia de color de fondo
**Ubicación:** `index.css`, línea 11 y 20

```css
:root {
  background-color: #efefef;  ← Gallery
}

body {
  background-color: #efefef;  ← Gallery
}
```

**Ubicación 2:** `tailwind.config.js`, línea 13

```js
background: "#F5F6F8",  ← Diferente (NOT Gallery)
```

**Ubicación 3:** `MainLayout.tsx`, línea 7

```jsx
<div className="... bg-background">  ← Usa Tailwind, no #EFEFEF
```

**CRÍTICA:** ⚠️ **INCONSISTENCIA - Gallery (#EFEFEF) vs Tailwind bg-background (#F5F6F8)**

---

### 3. Configuración Tailwind Actual

**Archivo:** `tailwind.config.js`

```js
theme: {
  extend: {
    colors: {
      primary: "#2C3E50",       ← Azul oscuro (NO Casal)
      secondary: "#34495E",     ← Gris azulado
      accent: "#1ABC9C",        ← Turquesa
      background: "#F5F6F8",    ← Gris claro (NO Gallery)
      surface: "#FFFFFF",       ← Blanco
      border: "#E2E8F0",        ← Gris muy claro
      text: "#0F172A",          ← Gris oscuro
    },
  },
},
```

**Observación:** Los colores NO incluyen `casal` ni `gallery` como nombres explícitos.

---

## 🔧 PLAN DE CORRECCIÓN (FASE 1)

### Paso 1A: Validar Layout (SIN CAMBIOS NECESARIOS)
✅ El layout actual (`MainLayout` → `Sidebar` + `Topbar` + `main`) es correcto.

---

### Paso 1B: Corregir Tablas Largas

**Problema:** DataTable sin contenedor con `overflow-x-auto`

**Solución:** Envolver `DataTable` en contenedor responsive:

```jsx
<div className="overflow-x-auto">
  <DataTable columns={columns} data={data} />
</div>
```

**Páginas afectadas:**
- `AssignmentPage.tsx` (línea 384)
- `DashboardPage.tsx` (múltiples DataTables)
- `StudentsPage.tsx` (si existen tablas)
- `TutorsPage.tsx` (si existen tablas)

---

### Paso 1C: Actualizar Config Tailwind para Casal/Gallery

**Cambio en `tailwind.config.js`:**

```js
theme: {
  extend: {
    colors: {
      casal: "#315762",         ← Nuevo
      gallery: "#EFEFEF",       ← Nuevo
      primary: "#2C3E50",       ← Mantener (será reemplazado en Fase 2)
      secondary: "#34495E",     ← Mantener
      accent: "#1ABC9C",        ← Mantener
      background: "#EFEFEF",    ← Cambiar a Gallery
      surface: "#FFFFFF",       ← Mantener
      border: "#E2E8F0",        ← Mantener
      text: "#0F172A",          ← Mantener
    },
  },
},
```

**Razón:** Preparar sistema para Fase 2 sin romper estilos existentes.

---

### Paso 1D: Validar Estilos Globales

**Archivos a verificar:**
- ✅ `index.css` - Ya usa `#efefef` (Gallery)
- ✅ `App.css` - Mínimo
- ✅ `main.tsx` - Monta raíz sin cambios necesarios

---

## 📊 TABLA DE CAMBIOS FASE 1

| Componente | Línea | Cambio | Razón |
|-----------|-------|--------|-------|
| `tailwind.config.js` | 9-17 | Agregar `casal` y `gallery`, actualizar `background` | Preparar paleta para Fase 2 |
| `AssignmentPage.tsx` | 384 | Envolver DataTable con `overflow-x-auto` | Evitar desbordamiento en tablas |
| `DashboardPage.tsx` | (múltiples) | Envolver DataTables con `overflow-x-auto` | Evitar desbordamiento en tablas |
| Otras páginas | (según sea necesario) | Envolver DataTables con `overflow-x-auto` | Evitar desbordamiento |

---

## ✅ RESULTADO ESPERADO

### Después de Fase 1:
1. ✅ Layout correcto: sidebar fijo, contenido scrolleable, topbar sticky
2. ✅ Tablas con scroll horizontal en pantallas pequeñas
3. ✅ Configuración Tailwind lista para Fase 2
4. ✅ Sin cambios visuales de color (se mantiene paleta actual)
5. ✅ Sin quejas de scroll inconsistente

### Verificación manual:
- Abrir página con tabla larga (AssignmentPage)
- Redimensionar ventana a 768px (tablet)
- Tabla debe tener `overflow-x-auto`, no desbordarse
- Sidebar debe permanecer visible
- Topbar debe ser sticky
- El único scroll vertical debe estar en `<main>`

---

## 📝 NOTAS TÉCNICAS

### Decisión: ¿Por qué NO fijar sidebar con `position: fixed`?

Actualmente: `<aside ... h-screen ...>` dentro del flex principal.

**Ventajas de estructura actual (flex):**
- ✅ Sidebar scrollea si el menú es muy largo
- ✅ Contenido se adapta al ancho del sidebar colapsable
- ✅ Más simple para responsive en móviles

**Si fuera `position: fixed`:**
- ❌ Requeriría padding en main para no superponerse
- ❌ Complicaría responsive en móviles
- ❌ Sidebar no scrollearía si el menú fuera largo

**Conclusión:** Mantener estructura flex actual.

---

## 🎯 PRÓXIMOS PASOS

1. ✅ **Fase 1 Completada** - Plan de corrección lista
2. → **Fase 1 Implementación** - Aplicar cambios en código
3. → **Fase 2** - Aplicar paleta Casal/Gallery
4. → **Fase 3** - Refactorizar módulo de Asignaciones a Wizard
5. → **Fase 4** - Pruebas finales

---

**Preparado por:** Claude Code
**Rama:** ramapruebas
**Próxima Acción:** Revisar análisis, aprobar, e implementar cambios

