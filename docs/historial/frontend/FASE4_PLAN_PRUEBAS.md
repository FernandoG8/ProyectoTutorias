# FASE 4 – PRUEBAS MANUALES Y AJUSTES FINALES

**Fecha:** 19 de Noviembre, 2025
**Estado:** PLAN DE PRUEBAS

---

## 🎯 OBJETIVO

Validar que el sistema completo (Backend + Frontend + Paleta Casal/Gallery) funciona correctamente en los casos de uso principales.

---

## 📋 CASOS DE PRUEBA

### GRUPO 1: LAYOUT Y SCROLL

#### TC-001: Sidebar y Topbar visibles
**Descripción:** Verificar que Sidebar (Casal) y Topbar (Casal) se renderizan correctamente
**Pasos:**
1. Abre la aplicación (login si es necesario)
2. Navega al Dashboard
3. Verifica:
   - ✓ Sidebar izquierdo: Fondo Casal (#315762), texto blanco
   - ✓ Topbar: Fondo Casal (#315762), texto blanco
   - ✓ Logo TL en sidebar: Gallery fondo, Casal texto
   - ✓ Dropdown de semestre en topbar: Texto blanco/70
   - ✓ Perfil de usuario: Avatar blanco con Casal texto

**Resultado esperado:** Ambas barras visibles, colores aplicados correctamente

---

#### TC-002: Scroll en contenido principal
**Descripción:** Verificar que solo el contenido principal scrollea, no el sidebar
**Pasos:**
1. Abre AssignmentPage (muy larga con tabla)
2. Haz scroll en el contenido
3. Verifica:
   - ✓ Sidebar permanece fijo (no se mueve)
   - ✓ Topbar permanece sticky en la parte superior
   - ✓ Solo el contenido central scrollea
   - ✓ Tabla tiene scroll horizontal en pantalla pequeña

**Resultado esperado:** Scroll fluido, sin desbordes extraños

---

#### TC-003: Tabla con muchas columnas
**Descripción:** Verificar que tablas largas tienen scroll horizontal sin desbordarse
**Pasos:**
1. Abre la tabla de "Historial de ejecuciones" en AssignmentPage
2. Redimensiona la ventana a 768px (tablet)
3. Verifica:
   - ✓ Tabla tiene `overflow-x-auto`
   - ✓ Puedes scrollear horizontalmente
   - ✓ No se desbordan elementos
   - ✓ En desktop: tabla completa visible

**Resultado esperado:** Tabla responde correctamente a ancho de pantalla

---

### GRUPO 2: PALETA CASAL/GALLERY

#### TC-004: Fondo Gallery en páginas
**Descripción:** Verificar que Gallery (#EFEFEF) es el fondo de todas las páginas
**Pasos:**
1. Navega entre diferentes páginas:
   - Dashboard
   - Gestión de Alumnos
   - Gestión de Tutores
   - Asignaciones
   - Reportes
2. Verifica en cada una:
   - ✓ Fondo general: Gallery (#EFEFEF)
   - ✓ Cards: Blanco con bordes sutiles
   - ✓ Títulos: Texto oscuro legible

**Resultado esperado:** Consistencia visual en todas las páginas

---

#### TC-005: Botones con color Casal
**Descripción:** Verificar que botones primarios usan Casal (#315762)
**Pasos:**
1. Abre cualquier página con formularios o botones
2. Verifica:
   - ✓ Botón primario: Fondo Casal, texto blanco
   - ✓ Hover: Casal más oscuro o transparencia
   - ✓ Botones secundarios: Mantienen su estilo gris
   - ✓ Botones peligro: Rojo (logout)

**Resultado esperado:** Botones tienen colores correctos y son clickeables

---

#### TC-006: Badges y estados
**Descripción:** Verificar que badges y estados muestran colores consistentes
**Pasos:**
1. Abre el historial de asignaciones
2. Verifica badges de estado:
   - ✓ "Iniciado" (info)
   - ✓ "Completado" (success - verde)
   - ✓ "Fallido" (danger - rojo)
3. Verifica que texto sea legible

**Resultado esperado:** Estados visibles y fáciles de diferenciar

---

### GRUPO 3: MÓDULO DE ASIGNACIONES

#### TC-007: Navegar al módulo de Asignaciones
**Descripción:** Acceder al módulo y ver estructura
**Pasos:**
1. Abre el menú sidebar
2. Click en "Asignaciones" (o módulo equivalente)
3. Verifica:
   - ✓ Topbar muestra "Asignaciones" en blanco
   - ✓ Sidebar muestra sección "Asignaciones" destacada
   - ✓ Se cargan contenidos (formulario, tabla de historial)

**Resultado esperado:** Módulo accesible y bien etiquetado

---

#### TC-008: Subir Excel sin validar primero
**Descripción:** Verificar flujo actual de subida de archivo
**Pasos:**
1. En AssignmentPage, llena el formulario:
   - Semestre: "2025-1"
   - Usuario: "coord_tutorias"
   - Archivo: Selecciona CSV/Excel válido
2. Click en botón de envío
3. Verifica:
   - ✓ Aparece loading spinner
   - ✓ Topbar mostrando progreso
   - ✓ Sin errores en consola

**Resultado esperado:** Archivo se procesa sin errores

---

#### TC-009: Ver resultados de asignación
**Descripción:** Verificar que se muestran resultados correctos
**Pasos:**
1. Después de TC-008, espera a que se complete el proceso
2. Verifica:
   - ✓ Aparece sección de resultados
   - ✓ Muestra estadísticas (asignados, errores, duración)
   - ✓ Status visible (OK/PARTIAL/ERROR)
   - ✓ Tabla de errores (si los hay)

**Resultado esperado:** Resultados claros y legibles con Casal/Gallery

---

#### TC-010: Historial de asignaciones accesible
**Descripción:** Verificar que el historial es accesible y scrolleable
**Pasos:**
1. Abre AssignmentPage
2. Desplázate a la tabla de "Historial de ejecuciones"
3. Verifica:
   - ✓ Tabla carga datos
   - ✓ Tiene scroll horizontal si es necesario
   - ✓ Puedes ver todas las columnas
   - ✓ Puedes hacer click en "Ver progreso"

**Resultado esperado:** Historial funcional y navegable

---

### GRUPO 4: NAVEGACIÓN Y RESPONSIVE

#### TC-011: Sidebar colapsable en mobile
**Descripción:** Verificar que sidebar se comporta bien en móvil
**Pasos:**
1. Redimensiona ventana a 375px (móvil)
2. Verifica:
   - ✓ Sidebar aún visible (colapsado)
   - ✓ Contenido se adapta
   - ✓ Botón de toggle funciona
   - ✓ No hay horizontal scroll extraño

**Resultado esperado:** Responsive design funciona correctamente

---

#### TC-012: Navegación entre módulos
**Descripción:** Verificar que puedes navegar sin errores
**Pasos:**
1. Click en cada opción del sidebar:
   - Dashboard
   - Alumnos
   - Tutores
   - Asignaciones
   - Reportes
   - Semestres
2. Verifica:
   - ✓ Navega sin errores
   - ✓ Topbar actualiza título
   - ✓ Sidebar destaca opción activa
   - ✓ Contenido carga correctamente

**Resultado esperado:** Navegación fluida sin errores

---

### GRUPO 5: CONTRASTE Y ACCESIBILIDAD

#### TC-013: Contraste texto-fondo
**Descripción:** Verificar que el texto es legible en todos lados
**Pasos:**
1. Verifica en diferentes secciones:
   - ✓ Blanco sobre Casal (sidebar, topbar): Alto contraste
   - ✓ Oscuro sobre Gallery (fondo): Legible
   - ✓ Oscuro sobre blanco (cards): Legible
2. Usa herramienta de contraste si está disponible

**Resultado esperado:** WCAG AA compliant (ratio > 4.5:1)

---

#### TC-014: Focus visible en inputs
**Descripción:** Verificar que inputs tienen focus visible para accesibilidad
**Pasos:**
1. Abre un formulario (ej: subir archivo)
2. Usa TAB para navegar entre campos
3. Verifica:
   - ✓ Focus ring visible alrededor de inputs
   - ✓ Color de focus es Casal o contraste similar
   - ✓ Puedes navegar con teclado

**Resultado esperado:** Navegación por teclado funciona

---

## 📊 MATRIZ DE PRUEBAS

| ID | Descripción | Tipo | Prioridad | Estado |
|-----|-------------|------|-----------|--------|
| TC-001 | Sidebar y Topbar | Layout | ALTA | ⬜ |
| TC-002 | Scroll en contenido | Layout | ALTA | ⬜ |
| TC-003 | Tabla con scroll | Layout | ALTA | ⬜ |
| TC-004 | Fondo Gallery | Paleta | MEDIA | ⬜ |
| TC-005 | Botones Casal | Paleta | MEDIA | ⬜ |
| TC-006 | Badges y estados | Paleta | MEDIA | ⬜ |
| TC-007 | Acceder a Asignaciones | Módulo | ALTA | ⬜ |
| TC-008 | Subir Excel | Módulo | ALTA | ⬜ |
| TC-009 | Ver resultados | Módulo | ALTA | ⬜ |
| TC-010 | Historial accesible | Módulo | MEDIA | ⬜ |
| TC-011 | Responsive mobile | Responsive | MEDIA | ⬜ |
| TC-012 | Navegación | Responsive | ALTA | ⬜ |
| TC-013 | Contraste | A11y | MEDIA | ⬜ |
| TC-014 | Focus visible | A11y | MEDIA | ⬜ |

---

## 🔧 HERRAMIENTAS RECOMENDADAS

1. **Navegador DevTools**
   - Responsive Design Mode (F12 → Toggle device toolbar)
   - Console (buscar errors/warnings)
   - Network tab (ver request/response)

2. **Herramientas de Accesibilidad**
   - axe DevTools (extensión Chrome)
   - WAVE (Wave.webaim.org)
   - Lighthouse (DevTools → Audits)

3. **Color Checking**
   - Color Contrast Analyzer
   - WebAIM Contrast Checker

---

## 📝 FORMATO DE REPORTE

Para cada prueba completada:

```
TC-XXX: [Nombre de la prueba]
├─ Estado: ✓ PASS / ✗ FAIL
├─ Observaciones: [Detalles si es necesario]
└─ Pantalla: [Resolución probada]
```

---

## 🚀 PRÓXIMOS PASOS

Después de las pruebas:

1. **Si TODO pasa:**
   - Sistema listo para producción
   - Preparar para Wizard mejorado (FASE 3B)

2. **Si hay fallos:**
   - Documentar en "Bugs encontrados"
   - Priorizar por severidad
   - Crear issues específicos

3. **Mejoras identificadas:**
   - Documentar recomendaciones
   - Priorizarvara próximas iteraciones

---

**Responsable:** Manual testing
**Duración estimada:** 1-2 horas
**Fecha:** 19 de Noviembre, 2025

