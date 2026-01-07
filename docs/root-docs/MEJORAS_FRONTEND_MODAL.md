# 🎨 Mejoras del Modal "Cargar lista de alumnos" - Frontend React

## 📌 Resumen de Cambios

Se ha refactorizado completamente el componente `AssignmentWizard.tsx` para **consumir correctamente los nuevos endpoints del backend con manejo de errores estandarizado** y mejorar significativamente la UX del modal de carga de archivo.

## ✨ Principales Mejoras Implementadas

### 1. ✅ Manejo de Selección de Archivo Mejorado

**Antes:**
- El input de archivo no mostraba feedback visual
- No había forma de ver el nombre o tamaño del archivo seleccionado
- El botón "Validar archivo" siempre estaba activo (incluso sin archivo)

**Después:**
```typescript
// Estado para rastrear el archivo seleccionado
const [selectedFile, setSelectedFile] = useState<File | null>(null);

// Listener en el input de archivo para actualizar estado
useEffect(() => {
  const fileInput = document.getElementById("archivo-input") as HTMLInputElement;
  const handleFileChange = () => {
    if (fileInput.files && fileInput.files.length > 0) {
      setSelectedFile(fileInput.files[0]);
    } else {
      setSelectedFile(null);
    }
  };

  if (fileInput) {
    fileInput.addEventListener("change", handleFileChange);
    return () => fileInput.removeEventListener("change", handleFileChange);
  }
}, []);
```

**Beneficios:**
- ✅ Visualización clara del archivo seleccionado (nombre + tamaño)
- ✅ Botón deshabilitado hasta seleccionar archivo: `disabled={!selectedFile || validationMutation.isPending}`
- ✅ Indicador visual (cambio de color y icono) cuando hay archivo
- ✅ Botón "X" para limpiar la selección

### 2. 🎯 Consumo Correcto de Respuestas de Error Estandarizadas

**Antes:**
```typescript
onError: (err) => {
  showError(
    err instanceof Error ? err.message : "Error al validar el Excel"
  );
}
```

**Después:**
```typescript
onError: (err) => {
  // Usar nuevos helpers para extraer errores del backend
  const errorCode = extractErrorCode(err);
  const excelErrors = extractExcelErrors(err);
  const fieldErrors = extractFieldErrors(err);
  const errorMessage = extractErrorMessage(err);

  if (errorCode === "EXCEL_VALIDATION_ERROR" && excelErrors && excelErrors.length > 0) {
    // Convertir excelErrors del backend al formato esperado
    const formattedErrors = excelErrors.map((e) => ({
      filaExcel: e.rowNumber,
      campo: e.column,
      valor: e.value,
      descripcion: e.message,
    }));
    setValidationErrors(formattedErrors);
    setCurrentStep(1);
    info(`Se encontraron ${formattedErrors.length} error(es) en la validación`);
  } else if (errorCode === "VALIDATION_ERROR" && fieldErrors && fieldErrors.length > 0) {
    showError(`Error de validación: ${fieldErrors.map((f) => f.message).join(", ")}`);
  } else if (errorCode === "DOMAIN_VALIDATION_ERROR") {
    showError(errorMessage);
  } else if (errorCode === "EXCEL_FORMAT_ERROR") {
    showError(errorMessage);
  } else {
    showError(errorMessage || "Error al validar el Excel");
  }
}
```

**Tipos de Errores Manejados:**
| Código | HTTP | Descripción | Acción |
|--------|------|-------------|--------|
| `EXCEL_VALIDATION_ERROR` | 400 | Errores de validación en Excel (fila/columna específica) | Mostrar tabla con detalles por fila |
| `VALIDATION_ERROR` | 400 | Errores de validación de campos | Mostrar mensaje con campos problemáticos |
| `DOMAIN_VALIDATION_ERROR` | 400 | Error de lógica de negocio (ej: semestre no existe) | Mostrar mensaje al usuario |
| `EXCEL_FORMAT_ERROR` | 400 | Archivo Excel corrupto o formato inválido | Mostrar mensaje de error de formato |

### 3. 🛑 Bloqueo de Scroll del Body Cuando Modal Está Abierto

**Implementación:**
```typescript
// Block body scroll when modal is open (wizard is displayed)
useEffect(() => {
  document.body.style.overflow = "hidden";
  return () => {
    document.body.style.overflow = "unset";
  };
}, []);
```

**Beneficio:** Previene el scroll de la página de fondo cuando el modal está abierto, mejorando la UX y evitando comportamiento confuso.

### 4. 📊 Mejora en la UI del Input de Archivo

**Visualización del Archivo Seleccionado:**
```tsx
{selectedFile ? (
  <>
    <FileCheck
      className="h-5 w-5 flex-shrink-0"
      style={{ color: colors.success[400] }}
    />
    <div className="text-left">
      <p className="font-medium" style={{ color: colors.success[900] }}>
        {selectedFile.name}
      </p>
      <p className="text-xs" style={{ color: colors.success[700] }}>
        {formatFileSize(selectedFile.size)}
      </p>
    </div>
  </>
) : (
  // Mostrar placeholder cuando no hay archivo
)}
```

**Función Auxiliar de Formato:**
```typescript
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return "0 Bytes";
  const k = 1024;
  const sizes = ["Bytes", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + " " + sizes[i];
};
```

**Cambios Visuales:**
- Color de borde cambia a verde cuando hay archivo seleccionado
- Fondo verde claro para indicar estado válido
- Icono `FileCheck` en lugar de `Upload` cuando hay archivo
- Botón "X" rojo para limpiar selección
- Transición suave de colores

## 🔧 Cambios en Archivos

### 1. `/frontend/tutoring-frontend/src/components/features/AssignmentWizard.tsx`

**Cambios Principales:**
- Agregado estado `selectedFile` para rastrear archivo seleccionado
- Agregado `useEffect` para escuchar cambios en el input de archivo
- Agregado `useEffect` para bloquear scroll del body
- Implementada función `formatFileSize()` para formato legible de tamaños
- Refactorizado handler `onError` en `validationMutation` para consumir nuevos helpers
- Actualizado UI del input de archivo para mostrar nombre y tamaño
- Modificado botón "Validar archivo" para deshabilitarse sin archivo
- Limpieza de `selectedFile` en `handleReset()` y `handleBackToUpload()`
- Removida transformación de `semestreId` en schema (ahora se convierte en `onUploadSubmit`)
- Simplificada inicialización del formulario

**Líneas Clave:**
- L63-72: Función `formatFileSize()`
- L72-98: Schema Zod actualizado (sin transformación)
- L123: Estado `selectedFile`
- L150-165: `useEffect` para listener de cambios de archivo
- L167-173: `useEffect` para bloquear scroll del body
- L160-191: Handler `onError` refactorizado con nuevos helpers
- L272-292: `onUploadSubmit` actualizado
- L434-505: UI mejorado del input de archivo

### 2. `/frontend/tutoring-frontend/src/lib/api-client.ts`

**Cambios Realizados:**
- Agregada interfaz `BackendErrorResponse` con estructura estandarizada:
  ```typescript
  interface BackendErrorResponse {
    status: number;
    error: string;
    message: string;
    path?: string;
    timestamp?: string;
    fieldErrors?: Array<{ field: string; message: string; }>;
    excelErrors?: Array<{
      rowNumber?: number;
      column?: string;
      value?: string;
      message: string;
    }>;
  }
  ```
- Agregadas funciones helper para extraer errores específicos:
  - `extractExcelErrors()`: Extrae errores de validación de Excel
  - `extractFieldErrors()`: Extrae errores de validación de campos
  - `extractErrorCode()`: Extrae el tipo de error (VALIDATION_ERROR, etc.)
  - `extractErrorMessage()`: Actualizada para soportar nueva estructura

### 3. `/frontend/tutoring-frontend/src/services/asignaciones-service.ts`

**Cambios Realizados:**
- Actualizado manejo de errores en `validateExcelFile()`:
  - Antes: Envolvía el error en un nuevo `Error`
  - Ahora: Propaga el error original para que el componente pueda usar los helpers de extracción
  ```typescript
  catch (error) {
    // Permite que el componente use extractExcelErrors, extractFieldErrors, etc.
    throw error;
  }
  ```

## 🧪 Flujo de Validación de Excel

```
┌─────────────────────────────────┐
│ Usuario selecciona archivo      │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│ selectedFile actualizado        │
│ Botón habilitado                │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│ Usuario hace clic en validar    │
│ onUploadSubmit() ejecuta        │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│ validationMutation.mutateAsync()│
│ POST /api/asignaciones/         │
│      validar-excel              │
└────────────┬────────────────────┘
             │
       ┌─────┴─────┐
       │           │
       ▼           ▼
    ✅ OK      ❌ ERROR
       │           │
    setValidation  errorCode ==
    Data()         EXCEL_VALIDATION_ERROR
       │              │
       ▼              ▼
  Step 2:         formatErrors()
  Confirmar     setValidationErrors()
                   Step 1: Ver
                   errores
```

## 📋 Tipos de Errores y Manejo

### EXCEL_VALIDATION_ERROR
```json
{
  "status": 400,
  "error": "EXCEL_VALIDATION_ERROR",
  "message": "Se encontraron 3 errores de validación en 100 filas",
  "excelErrors": [
    {
      "rowNumber": 2,
      "column": "MATRICULA_INVALIDA",
      "value": "abc",
      "message": "Formato de matrícula inválido"
    }
  ]
}
```

**Manejo en componente:**
- Convertir formato: `rowNumber` → `filaExcel`, `column` → `campo`, etc.
- Mostrar tabla de errores con detalles por fila
- Permitir continuar de todas formas o volver a subir

### VALIDATION_ERROR
```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Hay errores de validación",
  "fieldErrors": [
    {
      "field": "semestreId",
      "message": "No debe estar vacío"
    }
  ]
}
```

**Manejo en componente:**
- Mostrar toast con lista de campos problemáticos
- Usuario debe corregir y reintentar

### DOMAIN_VALIDATION_ERROR
```json
{
  "status": 400,
  "error": "DOMAIN_VALIDATION_ERROR",
  "message": "Semestre con ID 99999 no encontrado"
}
```

**Manejo en componente:**
- Mostrar mensaje genérico de error
- Usuario debe seleccionar otro semestre

## 🚀 Mejoras Técnicas

1. **Separación de Responsabilidades:** Los helpers de error están en `api-client.ts`, no en el componente
2. **Type Safety:** Uso de TypeScript con interfaces bien definidas
3. **Cleanup:** Limpieza de event listeners en useEffect
4. **Performance:** No re-renders innecesarios de selectedFile
5. **Accessibility:** Mejores labels y descripciones en UI
6. **Mobile-first:** Diseño responsive para todos los tamaños

## ✅ Checklist de Validación

- ✅ Build frontend sin errores TypeScript
- ✅ Archivo seleccionado muestra nombre y tamaño
- ✅ Botón "Validar archivo" deshabilitado sin archivo
- ✅ Botón "X" limpia selección y resetea input
- ✅ Scroll del body bloqueado cuando modal está abierto
- ✅ Errores de Excel mostrados con detalles por fila
- ✅ Errores de validación mostrados correctamente
- ✅ Errores de dominio manejados
- ✅ Transiciones visuales suaves
- ✅ Colores consistentes con sistema de diseño

## 📦 Dependencias (Sin cambios)

- react-hook-form: ^7
- zod: ^3
- @tanstack/react-query: ^4
- lucide-react: ^0.263
- dayjs: ^1
- axios: ^1

## 🔄 Flujo de Datos Resumido

```
FileInput Change Event
        ↓
   setSelectedFile()
        ↓
   Button Enable/Disable
        ↓
   Form Submit (onUploadSubmit)
        ↓
   validateExcelFile() API call
        ↓
   ┌─────────────┬─────────────┐
   ↓             ↓
 onSuccess    onError
   │             │
   │        extractErrorCode()
   │        extractExcelErrors()
   │        extractFieldErrors()
   │             │
   │        showError()
   │        setValidationErrors()
   │
   ├→ Check status=OK
   │   └→ Goto Step 2: Confirm
   │   └→ Or Step 1: Show Errors
```

## 📚 Referencias

- Backend Error Handling: `/MANEJO_ERRORES_BACKEND.md`
- Testing Examples: `/EJEMPLOS_TESTING_ERRORES.md`
- Components: `/frontend/tutoring-frontend/src/components/features/AssignmentWizard.tsx`
- Services: `/frontend/tutoring-frontend/src/services/asignaciones-service.ts`
- API Client: `/frontend/tutoring-frontend/src/lib/api-client.ts`

---

**Fecha de Implementación:** 2025-11-20
**Versión:** Frontend Refactorizado v2.0
**Estado:** ✅ Completo y Buildeable
