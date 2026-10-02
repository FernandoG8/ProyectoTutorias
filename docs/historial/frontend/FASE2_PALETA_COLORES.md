# FASE 2 – APLICAR PALETA CASAL/GALLERY

**Fecha:** 19 de Noviembre, 2025
**Estado:** PLAN DE IMPLEMENTACIÓN LISTO

---

## 📋 PALETA DE COLORES

### Colores Primarios
- **Casal:** `#315762` (azul-verde oscuro)
  - Uso: Sidebar, Topbar, botones primarios, títulos destacados
  - Variaciones: Mas claro/oscuro según necesidad

- **Gallery:** `#EFEFEF` (gris muy claro)
  - Uso: Fondo principal, fondos secundarios
  - Contraste: Texto oscuro sobre este fondo

---

## 🎨 ESTRATEGIA DE APLICACIÓN

### Nivel 1: Componentes Layout (CRÍTICOS)
1. **Sidebar** → background Casal
2. **Topbar** → background Casal
3. **Contenido principal** → background Gallery (ya está)

### Nivel 2: Componentes Reutilizables
1. **Botones** → Primario: Casal bg, texto blanco
2. **Cards** → blanco con sombra suave
3. **Badges/Tags** → Casal para estados

### Nivel 3: Componentes Específicos
1. **Navlinks activos** → Casal
2. **Formularios** → inputs con border Casal
3. **Estados** → Colores semánticos mantenidos

---

## 📊 CAMBIOS POR COMPONENTE

### Sidebar.tsx
```tsx
// ANTES:
<aside className="... bg-white shadow-lg ...">

// DESPUÉS:
<aside className="... bg-casal text-white shadow-lg ...">

Cambios:
- background: white → casal (#315762)
- text color: dark → white
- Logo: mantener pero con blanco
- Navlinks activos: Ya son Casal, mantener
- Navlinks inactivos: texto gris/blanco, hover suave
- Logout button: Cambiar a rojo (danger) pero mantener estilo
```

### Topbar.tsx
```tsx
// ANTES:
<header className="... bg-white border-b ...">

// DESPUÉS:
<header className="... bg-casal text-white border-b ...">

Cambios:
- background: white → casal
- text: dark → white
- Menu toggle: mantener pero icono blanco
- Título: blanco
- Perfil user: fondo blanco, texto oscuro (para contraste)
```

### Button.tsx (Componente UI)
```tsx
// ANTES:
variant="primary" → color azul genérico (#2C3E50)

// DESPUÉS:
variant="primary" → Casal (#315762)

Cambios:
- primary variant: usar bg-casal
- Mantener secondary, ghost, danger variantes
```

### Card.tsx (Componente UI)
```tsx
// ANTES:
<div className="... bg-white border-border ...">

// DESPUÉS:
<div className="... bg-white border border-casal/20 ...">

Cambios:
- background: mantener white (buena práctica)
- border: usar casal con opacidad baja
- sombra: suave (ya está bien)
```

### Badge/Badge.tsx
```tsx
// ANTES:
variant="primary" → azul genérico

// DESPUÉS:
variant="primary" → Casal (#315762)

Cambios:
- Actualizar colores para cada variante
```

### Colors Constants
```ts
// En src/constants/colors.ts
export const colors = {
  primary: {
    50: "#F0F5F7",    // Casal muy claro
    100: "#D4E3E8",
    200: "#A8C7D0",
    300: "#7CABBA",
    400: "#315762",   ← CASAL (principal)
    500: "#2A4B52",
    600: "#1F3840",
    700: "#152530",
    800: "#0C1318",
  },
  semantic: {
    background: "#EFEFEF",  // Gallery
    surface: "#FFFFFF",
    text: {
      primary: "#0F172A",
      secondary: "#475569",
      muted: "#94A3B8",
    },
  },
  ...resto mantener
}
```

---

## 🔄 ORDEN DE IMPLEMENTACIÓN

### Paso 1: Actualizar Constants/Colors
- Definir variaciones de Casal
- Actualizar primary color system
- Verificar que Gallery está en background

### Paso 2: Actualizar Components Layout
1. Sidebar.tsx
2. Topbar.tsx

### Paso 3: Actualizar Componentes UI
1. Button.tsx
2. Card.tsx
3. Badge.tsx
4. Input.tsx (border)

### Paso 4: Validar Páginas
- Revisar cada página con nuevos colores
- Verificar contraste de accesibilidad
- Ajustes puntuales si es necesario

---

## 📋 CHECKLIST DE IMPLEMENTACIÓN

### Archivos a Modificar

- [ ] `src/constants/colors.ts` - Actualizar paleta Casal/Gallery
- [ ] `src/components/layout/Sidebar.tsx` - Background Casal
- [ ] `src/components/layout/Topbar.tsx` - Background Casal
- [ ] `src/components/ui/Button.tsx` - Primary → Casal
- [ ] `src/components/ui/Card.tsx` - Border actualizado
- [ ] `src/components/ui/Badge.tsx` - Colores Casal
- [ ] `src/components/ui/Input.tsx` - Border Casal focus
- [ ] Verificar otras páginas visualmente

### Verificaciones

- [ ] Build: 0 errores
- [ ] Contraste WCAG AA en textos
- [ ] Sidebar legible (blanco sobre Casal)
- [ ] Topbar legible (blanco sobre Casal)
- [ ] Botones primarios destacados
- [ ] Cards bien diferenciados

---

## 🎯 RESULTADO ESPERADO

**Antes:**
- Sidebar: Blanco
- Topbar: Blanco
- Fondo: Gris claro (#F5F6F8)
- Botones: Azul genérico (#2C3E50)

**Después:**
- Sidebar: Casal (#315762) con texto blanco
- Topbar: Casal (#315762) con texto blanco
- Fondo: Gallery (#EFEFEF)
- Botones: Casal (#315762) con texto blanco
- Cards: Blanco con bordes Casal sutiles

---

**Próxima Acción:** Implementar cambios según checklist

