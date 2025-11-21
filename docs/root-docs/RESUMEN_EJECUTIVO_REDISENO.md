# PROMPT 3: Frontend - Análisis, Refactorización y Nuevas Implementaciones

## Contexto del Proyecto

Sistema de tutorías universitarias existente.

**Stack**: React 18+, TypeScript, Vite, TailwindCSS, React Router 6+, Zustand, TanStack Query, Axios

**Rutas del proyecto frontend**:
- Raíz: `frontend/tutoring-frontend/src/`
- Sidebar: `components/layout/Sidebar.tsx`
- Componentes: `components/`
- Páginas: `pages/`
- Servicios: `services/`
- Types: `types/`

**Controladores backend disponibles** (verificar endpoints en estos archivos):
- `AlumnoController` - CRUD y listado de alumnos
- `AlumnoSearchController` - Búsqueda avanzada: `/api/alumnos/search`, `/api/alumnos/autocomplete`
- `TutorController` - CRUD y listado de tutores
- `TutorSearchController` - Búsqueda avanzada: `/api/tutores/search`, `/api/tutores/autocomplete`
- `AsignacionController` - `/api/asignaciones/validar-excel`, `/api/asignaciones/ejecutar`, `/api/asignaciones/cambio-tutor`, `/api/asignaciones/validar-cambio-tutor/{alumnoId}`
- `SemestreController` - `/api/semestres`, `/api/semestres/activo`

---

## IMPORTANTE: Metodología de Trabajo

Para cada tarea debes seguir este orden:
1. **PRIMERO**: Explorar y leer el código existente relacionado
2. **SEGUNDO**: Analizar si la implementación actual cumple con los requisitos
3. **TERCERO**: Reportar hallazgos (qué funciona, qué no, qué falta)
4. **CUARTO**: Solo modificar si hay inconsistencias o falta funcionalidad
5. **QUINTO**: Crear desde cero SOLO lo que NO existe

NO asumas que el código no existe. NO reescribas desde cero lo que ya funciona.

---

## Tarea 1: Auditoría y Ajuste del Sidebar

**Archivo principal**: `components/layout/Sidebar.tsx`

**Estructura final requerida** (solo estas secciones deben existir):

```
📊 Dashboard (Inicio)
👥 Alumnos (gestión de activos/inactivos)
⏳ Pendientes (asignación de motivo de inactividad)
👨‍🏫 Tutores
📋 Asignaciones (wizard Excel en página)
🔄 Cambio de Tutor (proceso paso a paso en página)
📈 Reportes
📅 Semestres
```

**Tu tarea**:
1. Lee el Sidebar actual completo
2. Compara con la estructura requerida
3. **Elimina** solo las secciones que NO están en la lista
4. **Mantén** las que sí están
5. Verifica si el sidebar tiene `sticky` o `fixed` positioning
6. **Si no tiene posicionamiento fijo**: Corrige para que sea sticky/fixed y no deje espacios en blanco al hacer scroll

---

## Tarea 2: Refactorizar Wizard de Asignaciones (EXISTE, tiene problemas con modales)

**Problema reportado**: 
- El wizard actual usa modales que se desbordan o tienen problemas de distribución
- Debe ser un proceso en página completa, NO en modal

**Archivos a revisar**:
- `components/features/AssignmentWizard.tsx`
- Servicios en `services/asignaciones-service.ts`
- Types en `types/asignacion.ts`

**Endpoints involucrados** (ya funcionan, NO modificar backend):
- `POST /api/asignaciones/validar-excel` - Valida Excel sin ejecutar
- `POST /api/asignaciones/ejecutar` - Ejecuta asignación con datos validados
- `GET /api/semestres/activo` - Obtiene semestre activo automáticamente

**DTOs existentes a reutilizar**:
- `ExcelValidacionResponse` - Respuesta de validación
- `EjecucionAsignacionResponse` - Respuesta de ejecución
- `AlumnoValidadoDTO` - Alumno validado
- `ExcelErrorDTO` - Error de validación

**Flujo requerido (4 pasos en PÁGINA, NO modal)**:

```
PASO 1 - Subir Excel:
├─ Obtener semestre activo automáticamente (NO selección manual por usuario)
├─ Input para subir archivo Excel
├─ Al subir, llamar automáticamente a POST /api/asignaciones/validar-excel
├─ Mostrar loading mientras valida
└─ Pasar al paso 2 con el resultado

PASO 2 - Resumen de Validación:
├─ SI status="OK":
│   ├─ Mostrar resumen: totalFilas, totalValidas, porcentajeExito
│   ├─ Botón "Ejecutar Asignación" → Paso 3
│   └─ Botón "Volver" → Paso 1
├─ SI status="ERROR":
│   ├─ Mostrar TODOS los errores en tabla clara y minimalista
│   ├─ Columnas: filaExcel, campo, valor, descripcion, tipoError
│   ├─ Tabla con paginación si hay muchos errores
│   ├─ NO permitir avanzar al paso 3
│   └─ Botón "Volver a subir" → Paso 1
└─ SI status="WARNING": Similar a ERROR pero con opción de continuar

PASO 3 - Ejecutar Asignación:
├─ Mostrar resumen de lo que se va a ejecutar (cantidad de alumnos)
├─ Botón "Confirmar y Ejecutar" → Llama POST /api/asignaciones/ejecutar
├─ Mostrar loading/progreso mientras ejecuta
├─ IMPORTANTE: Los datos se pasan DIRECTAMENTE del paso 2 sin modificar
│   (backend ya los procesó y ordenó)
└─ Al terminar → Paso 4

PASO 4 - Resultado Final:
├─ SI status="OK": Éxito total con estadísticas
├─ SI status="PARTIAL": Éxito parcial + tabla de errores (erroresDetalle)
├─ SI status="ERROR": Error con detalles
├─ Mostrar: totalAlumnos, alumnosAsignados, alumnosConError, porcentajeExito, duracionMs
└─ Botón "Nueva Asignación" → Vuelve al Paso 1
```

**Requisitos de implementación**:
- Stepper visual que muestre los 4 pasos claramente
- Todo en la página de Asignaciones, NO en modal
- Reutilizar los DTOs y servicios existentes
- `semestreId` siempre como número (no string)
- No modificar datos entre validación y ejecución

---

## Tarea 3: Crear Wizard de Cambio de Tutor (NO EXISTE, crear desde cero)

**Ubicación sugerida**: `pages/CambioTutor/` o `components/features/CambioTutorWizard.tsx`

**Endpoints a utilizar**:
- `GET /api/alumnos/autocomplete?q=...&estado=ACTIVO` - Autocomplete de alumnos activos
- `GET /api/tutores/autocomplete?q=...` - Autocomplete de tutores
- `GET /api/asignaciones/validar-cambio-tutor/{alumnoId}` - Validar si puede cambiar
- `POST /api/asignaciones/cambio-tutor` - Ejecutar cambio

**DTO del endpoint de cambio** (verificar campos exactos en backend):
```typescript
// Request
interface CambioTutorRequestDTO {
  alumnoId: number;
  tutorOrigenId: number;
  tutorDestinoId: number;
  motivo: string;              // Texto libre, obligatorio
  usuarioResponsable: string;  // Texto libre, obligatorio
}
```

**Flujo requerido (3 pasos en PÁGINA, NO modal)**:

```
PASO 1 - Seleccionar Alumno:
├─ Buscador con autocomplete de alumnos ACTIVOS del semestre activo
├─ Usar endpoint: GET /api/alumnos/autocomplete?q=...&estado=ACTIVO
├─ Al escribir >= 2 caracteres, mostrar sugerencias
├─ Al seleccionar alumno:
│   ├─ Mostrar info: matrícula, nombre, carrera
│   ├─ Mostrar tutor actual del alumno
│   └─ Llamar validación: GET /api/asignaciones/validar-cambio-tutor/{alumnoId}
├─ SI no puede cambiar (2 cambios previos):
│   ├─ Mostrar mensaje de error claro
│   └─ NO permitir continuar
├─ SI puede cambiar:
│   └─ Botón "Continuar" → Paso 2

PASO 2 - Configurar Cambio:
├─ Información del alumno (solo lectura)
├─ Tutor actual (solo lectura)
├─ Buscador de tutor destino con autocomplete:
│   ├─ Usar endpoint: GET /api/tutores/autocomplete?q=...
│   ├─ Filtrar por misma carrera o compatible
│   ├─ Mostrar capacidad disponible en cada sugerencia
│   └─ No permitir seleccionar tutor sin capacidad
├─ Campo "Motivo de reasignación" (textarea, obligatorio)
├─ Campo "Persona responsable" (input text, obligatorio)
├─ Botón "Volver" → Paso 1
└─ Botón "Confirmar Cambio" (habilitado solo si todo completo) → Ejecuta POST → Paso 3

PASO 3 - Confirmación:
├─ SI éxito:
│   ├─ Mensaje de éxito
│   ├─ Resumen: Alumno, Tutor anterior → Tutor nuevo
│   └─ Botón "Realizar otro cambio" → Paso 1
├─ SI error:
│   ├─ Mensaje de error descriptivo
│   ├─ Mostrar código si aplica (LIMITE_CAMBIOS_ALCANZADO, TUTOR_SIN_CAPACIDAD)
│   └─ Botón "Volver a intentar" → Paso 2
```

**Componentes a crear**:
- `CambioTutorWizard.tsx` - Componente principal con stepper
- `PasoSeleccionAlumno.tsx` - Paso 1 (o inline en el wizard)
- `PasoConfiguracionCambio.tsx` - Paso 2 (o inline en el wizard)
- `PasoConfirmacionCambio.tsx` - Paso 3 (o inline en el wizard)
- Reutilizar componentes de búsqueda/autocomplete si ya existen

---

## Tarea 4: Mejorar Sección de Reportes (Dropdowns y Buscadores)

**Problema actual**:
- Semestres se pasan hardcodeados o escribiendo el código manualmente
- Selector de tutores es básico, debería ser buscador con autocomplete

**Archivos a revisar**: Buscar componentes en `pages/Reportes/` o similares

**Mejoras requeridas**:

1. **Dropdown de Semestres**:
   - Obtener lista con `GET /api/semestres`
   - Mostrar dropdown con todos los semestres
   - Formato sugerido: "2025-2026-F1 - Semestre Agosto 2025"
   - Por defecto seleccionar el semestre activo

2. **Buscador de Tutores**:
   - Reemplazar selector simple por buscador con autocomplete
   - Usar `GET /api/tutores/autocomplete?q=...`
   - Mostrar nombre y carrera en las sugerencias

**Tu tarea**:
1. Localiza los componentes de Reportes
2. Identifica dónde se usan semestres hardcodeados → Reemplazar por dropdown dinámico
3. Identifica selectores de tutores básicos → Reemplazar por buscadores con autocomplete
4. Verificar que estos cambios no rompan la funcionalidad existente

---

## Tarea 5: Verificación de Paginación en Tablas

**Tu tarea**:
1. Busca todos los componentes de tabla (DataTable, Table, etc.)
2. Verifica que implementen paginación del lado del servidor
3. Verifica que no se desborden del contenedor (usar overflow-x-auto si necesario)
4. **Solo si hay problemas**: Corrige
5. **Si todo está correcto**: Reporta

---

## Tarea 6: Sincronización de Rutas

**Tu tarea**:
1. Localiza el archivo de rutas (App.tsx o routes/)
2. Asegura que existan rutas para:
   - `/` → Dashboard
   - `/alumnos` → Alumnos
   - `/pendientes` → Pendientes
   - `/tutores` → Tutores
   - `/asignaciones` → Wizard de Asignaciones (refactorizado)
   - `/cambio-tutor` → Wizard de Cambio de Tutor (nuevo)
   - `/reportes` → Reportes
   - `/semestres` → Semestres
3. Añadir ruta `/cambio-tutor` si no existe
4. Eliminar rutas que no correspondan a la estructura del sidebar

---

## Formato de Reporte Esperado

Al finalizar cada tarea, genera un resumen así:

```
### Tarea X: [Nombre]

**Archivos analizados**:
- ruta/archivo1.tsx

**Estado actual**:
- [✓] Requisito 1: Implementado correctamente
- [✗] Requisito 2: Falta implementar
- [⚠] Requisito 3: Parcialmente implementado

**Acciones realizadas**:
- Ninguna (todo correcto) / Se modificó X para corregir Y / Se creó X desde cero

**Archivos creados/modificados**:
- Lista de archivos con descripción breve del cambio
```

---

## Restricciones

- NO crear componentes nuevos si ya existen similares que pueden adaptarse
- NO cambiar la estructura de carpetas existente
- Mantener los estilos de TailwindCSS existentes
- Reutilizar hooks, servicios y types existentes
- Todos los textos en español
- semestreId siempre como número, nunca string
- No modificar los endpoints del backend, solo consumirlos correctamente