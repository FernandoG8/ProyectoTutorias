# FASE 3 – REDISEÑO DEL MÓDULO DE ASIGNACIONES (WIZARD)

**Fecha:** 19 de Noviembre, 2025
**Estado:** PLAN DE IMPLEMENTACIÓN LISTO

---

## 🎯 OBJETIVO

Convertir la página de Asignaciones en un **flujo por pasos (Wizard)** que:

1. **Paso 1 – Subir Excel:** Usuario sube archivo y se valida
2. **Paso 2 – Ver Errores:** Si hay errores, se muestran en tabla
3. **Paso 3 – Ejecutar:** Se ejecuta la asignación definitiva
4. **Paso 4 – Resultados:** Se muestran estadísticas y historial

---

## 📋 ANÁLISIS DEL BACKEND EXISTENTE

### Endpoints disponibles:

#### 1. POST `/api/asignaciones/validar-excel`
```
Request:
{
  "archivo": File,
  "semestreId": 5
}

Response:
{
  "status": "OK|ERROR|PARTIAL",
  "data": [AlumnoValidadoDTO, ...],
  "totalFilas": 150,
  "totalValidas": 150,
  "totalErrores": 0,
  "errors": [
    { "fila": 2, "campo": "matricula", "error": "Duplicado", ... }
  ]
}
```

**Responsabilidad:**
- Leer Excel
- Validar datos
- Ordenar por semestre DESC
- Reportar TODOS los errores sin parar

---

#### 2. POST `/api/asignaciones/ejecutar`
```
Request:
{
  "semestreId": 5,
  "alumnosValidados": [AlumnoValidadoDTO, ...]
}

Response:
{
  "status": "OK|PARTIAL|ERROR",
  "totalAlumnos": 150,
  "alumnosAsignados": 150,
  "alumnosConError": 0,
  "duracionMs": 2500,
  "porcentajeExito": 100.0,
  "detalles": "150 alumnos asignados exitosamente",
  "erroresDetalle": [
    {
      "alumnoMatricula": "A00123456",
      "alumnoNombre": "Juan Pérez",
      "error": "Sin tutor disponible",
      "raizCausa": "...",
      "tutorIntentado": "..."
    }
  ]
}
```

**Responsabilidad:**
- Ejecutar asignación pura
- Best-effort error handling (captura excepciones, continúa)
- Auditoría completa
- Estadísticas detalladas

---

## 🏗️ ARQUITECTURA DEL WIZARD

### Componente Principal: `AssignmentWizard.tsx`

```
AssignmentWizard
├── Step 1: UploadExcelStep
│   ├── File input (Excel/CSV)
│   ├── Semester selector
│   ├── User input (responsable)
│   └── Upload button
│
├── Step 2: ValidationResultsStep
│   ├── Success card (si status=OK)
│   ├── Error table (si hay errores)
│   ├── Warning card (si status=PARTIAL)
│   └── Back/Continue buttons
│
├── Step 3: ExecutionStep
│   ├── Confirmation message
│   ├── Execute button (deshabilitado si errores)
│   └── Cancel button
│
├── Step 4: ResultsStep
│   ├── Statistics cards
│   ├── Success/Error breakdown
│   ├── Detailed errors table
│   └── Links to history
│
└── Shared State
    ├── currentStep: 1-4
    ├── uploadedFile: File
    ├── validationData: AlumnoValidadoDTO[]
    ├── validationErrors: AsignacionErrorDTO[]
    ├── executionResults: EjecucionAsignacionResponse
    └── semestreId: number
```

---

## 📊 FLUJO DE ESTADOS

```
START
  │
  ├─> STEP 1: UPLOAD EXCEL
  │   ├─ User selects file + semestre
  │   ├─ POST /validar-excel
  │   └─ Receive: AlumnoValidadoDTO[], errors[]
  │
  ├─ VALIDATION CHECK
  │   ├─ If totalErrores === 0
  │   │  └─> Enable "Continuar" button → STEP 3
  │   │
  │   └─ If totalErrores > 0
  │      └─> STEP 2: SHOW ERRORS
  │
  ├─> STEP 2: VALIDATION RESULTS (OPTIONAL)
  │   ├─ Display error table
  │   ├─ Count errors by type
  │   └─ Options:
  │       ├─ Back → STEP 1 (re-upload)
  │       └─ Continue (si quiere intentar con errores)
  │
  ├─> STEP 3: EXECUTE ASSIGNMENT
  │   ├─ Show summary
  │   ├─ Confirm execution
  │   ├─ POST /ejecutar
  │   └─ Receive: EjecucionAsignacionResponse
  │
  ├─> STEP 4: RESULTS
  │   ├─ Display statistics
  │   ├─ Success breakdown
  │   ├─ Error details (if any)
  │   └─ Link to history
  │
  └─> END

Special Cases:
- Back button: Regresa al paso anterior
- Cancel: Regresa a Step 1, limpia estado
- Error en validación: Muestra error, permite reintentar
- Error en ejecución: Muestra error, permite ver historial
```

---

## 🎨 COMPONENTES A CREAR

### 1. `AssignmentWizard.tsx` (Componente Principal)

```tsx
interface WizardState {
  currentStep: 1 | 2 | 3 | 4;
  semestreId: number | null;
  usuario: string;
  uploadedFile: File | null;
  validationData: AlumnoValidadoDTO[] | null;
  validationErrors: any[];
  validationStatus: 'OK' | 'PARTIAL' | 'ERROR';
  executionResults: EjecucionAsignacionResponse | null;
  isLoading: boolean;
  error: string | null;
}

export const AssignmentWizard = () => {
  const [state, setState] = useState<WizardState>({...});

  return (
    <div className="space-y-6">
      <WizardProgressBar currentStep={state.currentStep} />

      {state.currentStep === 1 && <UploadExcelStep {...} />}
      {state.currentStep === 2 && <ValidationResultsStep {...} />}
      {state.currentStep === 3 && <ExecutionStep {...} />}
      {state.currentStep === 4 && <ResultsStep {...} />}
    </div>
  );
};
```

### 2. `WizardProgressBar.tsx`

Componente que muestra: `1 → 2 → 3 → 4` con indicadores visuales

---

### 3. `UploadExcelStep.tsx`

```tsx
// Entrada de usuario:
// - File input (accept .xlsx, .csv)
// - Semester selector (dropdown)
// - User responsible (text input)
// - Upload button
//
// Validaciones:
// - File requerido
// - Semestre requerido
// - Usuario requerido
//
// On Submit:
// POST /api/asignaciones/validar-excel
// → Avanza a Step 2 (si errores) o Step 3 (si OK)
```

---

### 4. `ValidationResultsStep.tsx`

```tsx
// IF status === 'OK':
//   ✓ Green card: "150 alumnos validados sin errores"
//   → [Continuar] button enabled

// IF status === 'PARTIAL':
//   ⚠ Yellow card: "150 alumnos validados, 5 con errores"
//   → Error table: fila, campo, descripción
//   → [Continuar de todas formas] button enabled
//   → [Volver a subir] button

// IF status === 'ERROR':
//   ✗ Red card: "Error en validación"
//   → Error table: detalles
//   → [Volver a subir] button only

// Back button: Regresa a Step 1
```

---

### 5. `ExecutionStep.tsx`

```tsx
// Confirmación:
// - Semestre: {semestreId}
// - Total alumnos: {totalAlumnos}
// - Usuario ejecutor: {usuario}
// - Errores previos: {totalErrores} (si hay)

// Buttons:
// - [Ejecutar Asignación] → POST /ejecutar
// - [Volver] → Step anterior

// Validación:
// - Continuar solo si alumnosValidados no está vacío
```

---

### 6. `ResultsStep.tsx`

```tsx
// Success path:
// - Statistics cards (total, asignados, errores, duración)
// - Progress bar (porcentajeExito)
// - Success message with timestamp
// - Link to assignment history

// Error path:
// - Error message
// - Error details table (if erroresDetalle)
// - Link to assignment history
// - [Retry] button → Step 1

// Both paths:
// - [Done] button → Close wizard, refresh history
```

---

## 📦 CAMBIOS EN SERVICIOS

### `asignaciones-service.ts`

Ya existen pero voy a asegurar que están bien tipadas:

```ts
export async function validateExcel(
  file: File,
  semestreId: number
): Promise<ExcelValidacionResponse> {
  // POST /validar-excel
}

export async function executeAssignment(
  semestreId: number,
  alumnosValidados: AlumnoValidadoDTO[]
): Promise<EjecucionAsignacionResponse> {
  // POST /ejecutar
}
```

---

## 🎨 ESTILOS Y PALETA

**Usar Paleta Casal/Gallery de FASE 2:**

- Progreso: Casal (#315762) para indicador activo
- Cards: Blanco con bordes Casal/20
- Botones: Casal primario, rojo para peligro
- Fondos: Gallery (#EFEFEF) general, Cards blanco
- Texto: Dark sobre Gallery, white sobre Casal

---

## 📋 CHECKLIST DE IMPLEMENTACIÓN

### Fase 3A: Estructura Base
- [ ] Crear `AssignmentWizard.tsx` (componente principal)
- [ ] Crear `WizardProgressBar.tsx` (indicador visual)
- [ ] Definir tipos TypeScript para estado
- [ ] Setup state management (useState)

### Fase 3B: Pasos Individuales
- [ ] Implementar `UploadExcelStep.tsx`
- [ ] Implementar `ValidationResultsStep.tsx`
- [ ] Implementar `ExecutionStep.tsx`
- [ ] Implementar `ResultsStep.tsx`

### Fase 3C: Integración
- [ ] Conectar con servicios backend
- [ ] Validar flujo completo (1→2→3→4)
- [ ] Manejo de errores por paso
- [ ] Mensajes de usuario claros

### Fase 3D: Estilo y UX
- [ ] Aplicar paleta Casal/Gallery
- [ ] Responsive design (mobile/tablet/desktop)
- [ ] Loading states
- [ ] Error messages
- [ ] Success animations

### Fase 3E: Validación
- [ ] Build: 0 errores
- [ ] Flujo completo funcional
- [ ] Errores manejados correctamente
- [ ] Historial actualizado después de ejecutar

---

## 🔄 REEMPLAZO EN PÁGINA

**Archivo:** `src/pages/AssignmentPage.tsx`

**Cambio:**
```tsx
// ANTES: Form inline + tabla de historial

// DESPUÉS: AssignmentWizard + tabla de historial en Step 4
```

La página ahora renderiza:
1. `<AssignmentWizard />` (los 4 pasos)
2. `<Card>Historial de ejecuciones</Card>` (tabla con overflow-x-auto)

---

## 🎯 RESULTADO ESPERADO

### Experiencia del Usuario:

1. **Abre Módulo Asignaciones**
   - Ve Step 1 con form para subir Excel

2. **Sube archivo**
   - Elige Excel, semestre, usuario responsable
   - Click "Validar"
   - Espera respuesta (validación)

3. **Ve resultados de validación**
   - Si OK: botón "Continuar" habilitado
   - Si ERROR: botón "Volver" habilitado
   - Si PARTIAL: tabla de errores + botones

4. **Ejecuta asignación**
   - Confirmación con resumen
   - Click "Ejecutar"
   - Progreso (loading state)

5. **Ve resultados finales**
   - Estadísticas
   - Status (OK/PARTIAL/ERROR)
   - Detalles de errores (si hay)
   - Opción de ver historial

---

## 📝 NOTAS TÉCNICAS

### State Management:
- Usar `useState` (suficiente para este wizard)
- No requiere Context/Redux (datos fluyen en secuencia)

### Error Handling:
- Try/catch en cada llamada a API
- Mostrar error en card roja
- Permitir reintentos

### Performance:
- No renderizar todos los pasos simultáneamente
- Lazy load solo el paso actual
- Validaciones cliente-side simples

### Accesibilidad:
- ARIA labels en botones
- Focus states visible
- Navegación por teclado (Tab entre botones)

---

**Preparado por:** Claude Code
**Próxima Acción:** Implementar componentes según checklist

