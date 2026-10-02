# FASE 4 – CHECKLIST DE PRUEBAS MANUALES

**Fecha de Pruebas:** ___________________
**Navegador/Versión:** ___________________
**Resoluciones Probadas:** ___________________

---

## 🎯 GRUPO 1: LAYOUT Y SCROLL

### TC-001: Sidebar y Topbar visibles
- [ ] Sidebar visible con fondo Casal (#315762)
- [ ] Topbar visible con fondo Casal (#315762)
- [ ] Logo TL: Gallery fondo, Casal texto
- [ ] Sidebar texto: Blanco legible
- [ ] Topbar texto: Blanco legible
- [ ] Dropdown semestre: Visible en topbar

**Notas:** ________________________

**Status:** ⬜ NO PROBADO | ✓ PASS | ✗ FAIL

---

### TC-002: Scroll en contenido principal
- [ ] Sidebar permanece fijo al hacer scroll
- [ ] Topbar permanece sticky en parte superior
- [ ] Contenido central scrollea fluidamente
- [ ] Sin scroll horizontal extraño

**Resolución:** __________ px

**Notas:** ________________________

**Status:** ⬜ NO PROBADO | ✓ PASS | ✗ FAIL

---

### TC-003: Tabla con muchas columnas
- [ ] Tabla tiene scroll horizontal (768px tablet)
- [ ] No se desbordan columnas
- [ ] En desktop: tabla completa visible
- [ ] Headers alineados al scrollear

**Resolución:** __________ px

**Notas:** ________________________

**Status:** ⬜ NO PROBADO | ✓ PASS | ✗ FAIL

---

## 🎨 GRUPO 2: PALETA CASAL/GALLERY

### TC-004: Fondo Gallery en páginas

**Dashboard:**
- [ ] Fondo: Gallery (#EFEFEF)
- [ ] Cards: Blanco con bordes sutiles
- [ ] Títulos: Texto oscuro legible

**Gestión de Alumnos:**
- [ ] Fondo: Gallery (#EFEFEF)
- [ ] Cards: Blanco con bordes sutiles
- [ ] Títulos: Texto oscuro legible

**Gestión de Tutores:**
- [ ] Fondo: Gallery (#EFEFEF)
- [ ] Cards: Blanco con bordes sutiles
- [ ] Títulos: Texto oscuro legible

**Asignaciones:**
- [ ] Fondo: Gallery (#EFEFEF)
- [ ] Cards: Blanco con bordes sutiles
- [ ] Títulos: Texto oscuro legible

**Reportes:**
- [ ] Fondo: Gallery (#EFEFEF)
- [ ] Cards: Blanco con bordes sutiles
- [ ] Títulos: Texto oscuro legible

**Notas:** ________________________

**Status:** ⬜ NO PROBADO | ✓ PASS | ✗ FAIL

---

### TC-005: Botones con color Casal
- [ ] Botón primario: Fondo Casal, texto blanco
- [ ] Hover en botón: Cambio de color visible
- [ ] Botones secundarios: Mantienen gris
- [ ] Botón Logout: Rojo con hover
- [ ] Disabled state: Visible (opacidad/gris)

**Notas:** ________________________

**Status:** ⬜ NO PROBADO | ✓ PASS | ✗ FAIL

---

### TC-006: Badges y estados
- [ ] Badge "Iniciado": Info (azul/cyan)
- [ ] Badge "Completado": Success (verde)
- [ ] Badge "Fallido": Danger (rojo)
- [ ] Badge "Liberando cupos": Warning (amarillo)
- [ ] Texto en badges: Legible

**Notas:** ________________________

**Status:** ⬜ NO PROBADO | ✓ PASS | ✗ FAIL

---

## 📋 GRUPO 3: MÓDULO DE ASIGNACIONES

### TC-007: Navegar al módulo de Asignaciones
- [ ] Sidebar: "Asignaciones" destacado cuando está activo
- [ ] Topbar: Muestra "Asignaciones" en blanco
- [ ] Contenido carga sin errores
- [ ] Formulario visible
- [ ] Tabla de historial visible

**Notas:** ________________________

**Status:** ⬜ NO PROBADO | ✓ PASS | ✗ FAIL

---

### TC-008: Subir Excel válido (sin errores)

**Prerequisitos:**
- [ ] Archivo Excel/CSV válido disponible
- [ ] Semestre existente: ___________
- [ ] Usuario: ___________

**Prueba:**
- [ ] Formulario acepta archivo
- [ ] Semestre selector funciona
- [ ] Usuario input funciona
- [ ] Botón "Ejecutar proceso" clickeable
- [ ] Loading spinner aparece
- [ ] Sin errores en consola (F12)
- [ ] Proceso inicia sin errores

**Resultado:**
- [ ] Respuesta exitosa (status OK)
- [ ] Muestra resultados después
- [ ] Estadísticas visibles

**Notas:** ________________________

**Status:** ⬜ NO PROBADO | ✓ PASS | ✗ FAIL

---

### TC-009: Ver resultados de asignación

**After TC-008 (si pasó):**
- [ ] Aparece sección "Asignación completada"
- [ ] Icono de éxito visible (✓)
- [ ] Estadísticas muestran:
  - [ ] Total procesados
  - [ ] Total asignados
  - [ ] Total errores
  - [ ] Duración en ms
- [ ] Progress bar muestra porcentaje
- [ ] Status badge visible
- [ ] Tabla de errores (si los hay)

**Notas:** ________________________

**Status:** ⬜ NO PROBADO | ✓ PASS | ✗ FAIL

---

### TC-010: Historial de asignaciones accesible
- [ ] Tabla visible en "Historial de ejecuciones"
- [ ] Tabla tiene datos (procesos previos)
- [ ] Columns visibles:
  - [ ] Proceso
  - [ ] Estado
  - [ ] Inicio
  - [ ] Fin
  - [ ] Asignados
  - [ ] Errores
  - [ ] Responsable
  - [ ] Seguimiento
- [ ] Scroll horizontal (si es necesario)
- [ ] Botón "Ver progreso" funciona

**Notas:** ________________________

**Status:** ⬜ NO PROBADO | ✓ PASS | ✗ FAIL

---

## 📱 GRUPO 4: RESPONSIVE Y NAVEGACIÓN

### TC-011: Responsive Design en Mobile (375px)
- [ ] Sidebar visible o colapsable
- [ ] Contenido se adapta al ancho
- [ ] No hay horizontal scroll extraño
- [ ] Topbar visible
- [ ] Botones clickeables
- [ ] Tablas scrolleables

**Dispositivo simulado:** ___________

**Notas:** ________________________

**Status:** ⬜ NO PROBADO | ✓ PASS | ✗ FAIL

---

### TC-012: Navegación entre módulos
- [ ] Dashboard: Carga sin errores
- [ ] Alumnos: Carga sin errores
- [ ] Tutores: Carga sin errores
- [ ] Asignaciones: Carga sin errores
- [ ] Reportes: Carga sin errores
- [ ] Semestres: Carga sin errores
- [ ] Topbar actualiza título en cada módulo
- [ ] Sidebar destaca módulo activo

**Notas:** ________________________

**Status:** ⬜ NO PROBADO | ✓ PASS | ✗ FAIL

---

## ♿ GRUPO 5: ACCESIBILIDAD

### TC-013: Contraste texto-fondo

**Blanco sobre Casal (Sidebar/Topbar):**
- [ ] Ratio contraste > 4.5:1 ✓
- [ ] Texto legible sin dificultad

**Oscuro sobre Gallery (Contenido):**
- [ ] Ratio contraste > 4.5:1 ✓
- [ ] Texto legible sin dificultad

**Oscuro sobre Blanco (Cards):**
- [ ] Ratio contraste > 4.5:1 ✓
- [ ] Texto legible sin dificultad

**Herramienta usada:** ___________

**Notas:** ________________________

**Status:** ⬜ NO PROBADO | ✓ PASS | ✗ FAIL

---

### TC-014: Navegación por teclado
- [ ] TAB navega entre campos
- [ ] Focus ring visible en inputs
- [ ] Focus color contrasta con fondo
- [ ] Puedes enviar formularios con Enter
- [ ] Botones clickeables con Space

**Notas:** ________________________

**Status:** ⬜ NO PROBADO | ✓ PASS | ✗ FAIL

---

## 📊 RESUMEN EJECUTIVO

### Pruebas Totales: 14
### Pasadas: ____ | Fallidas: ____ | No Probadas: ____

### Tasa de Éxito: _____ %

---

## 🐛 BUGS ENCONTRADOS

### Bug #1
**ID Prueba:** ____________
**Descripción:**
_______________________________________________

**Pasos para reproducir:**
1. _______________________________________________
2. _______________________________________________
3. _______________________________________________

**Resultado esperado:** _______________________________________________
**Resultado actual:** _______________________________________________
**Severidad:** 🔴 CRÍTICA | 🟠 ALTA | 🟡 MEDIA | 🟢 BAJA

---

### Bug #2
**ID Prueba:** ____________
**Descripción:**
_______________________________________________

**Pasos para reproducir:**
1. _______________________________________________
2. _______________________________________________
3. _______________________________________________

**Resultado esperado:** _______________________________________________
**Resultado actual:** _______________________________________________
**Severidad:** 🔴 CRÍTICA | 🟠 ALTA | 🟡 MEDIA | 🟢 BAJA

---

## 💡 MEJORAS IDENTIFICADAS

### Mejora #1
**Descripción:** _______________________________________________
**Impacto:** Pequeño | Medio | Grande
**Esfuerzo:** 30min | 1h | 2h+ | ---

---

### Mejora #2
**Descripción:** _______________________________________________
**Impacto:** Pequeño | Medio | Grande
**Esfuerzo:** 30min | 1h | 2h+ | ---

---

## ✅ CONCLUSIONES

### Sistema listo para:
- [ ] Producción (sin cambios críticos)
- [ ] Producción con mejoras mínimas
- [ ] Requeriere cambios antes de producción

### Próximas acciones:
1. _______________________________________________
2. _______________________________________________
3. _______________________________________________

---

**Probado por:** ___________________
**Fecha:** ___________________
**Firma:** ___________________

