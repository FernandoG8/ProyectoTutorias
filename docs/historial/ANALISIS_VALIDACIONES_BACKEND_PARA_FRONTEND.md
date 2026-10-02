# ANÁLISIS: Validaciones del Backend para el Frontend

**Fecha:** 20 de Noviembre, 2025
**Status:** ✅ COMPLETADO
**Propósito:** Documentar todos los formatos, validaciones y precondiciones del backend.

---

## 📋 RESUMEN EJECUTIVO

El backend implementa un flujo de **2 fases** para asignaciones:

1. **POST /api/asignaciones/validar-excel** - Validar sin ejecutar
2. **POST /api/asignaciones/ejecutar** - Ejecutar con datos validados

**Clave:** Frontend DEBE pasar datos exactos en formatos especificados.

---

## 🔍 ANÁLISIS DETALLADO DE DTOs

### 1. REQUEST: Validación de Excel

**Endpoint:** `POST /api/asignaciones/validar-excel`

**Input (FormData):**
```typescript
{
  archivo: File,          // Archivo Excel
  semestreId: number      // ID del semestre (NOT string)
}
```

---

## 2. AlumnoValidadoDTO

```typescript
{
  alumnoId: number | null,
  matricula: string,
  nombre: string,
  carrera: string,
  semestreId: number,           // ✓ NÚMERO, no string
  semestreCodigo: string,
  semestreNumerico: number,     // 1-12
  ordenPriority: number,
  validado: boolean,
  tipoAsignacionPrevisto: string
}
```

---

## 3. REQUEST: Ejecución

**Endpoint:** `POST /api/asignaciones/ejecutar`

```typescript
{
  semestreId: number,                    // ✓ NÚMERO
  alumnosValidados: AlumnoValidadoDTO[], // Del paso anterior (SIN modificar)
  simular?: boolean,                     // Optional
  reporteDetallado?: boolean             // Optional
}
```

---

## ⚠️ ERRORES COMUNES A EVITAR

### ❌ Error 1: string en semestreId
```typescript
// ❌ INCORRECTO
validateExcelFile(file, "2025-1");

// ✓ CORRECTO
validateExcelFile(file, 5);
```

### ❌ Error 2: Modificar datos después de validación
```typescript
// ❌ INCORRECTO
const cleaned = validationData.map(a => ({...a, nombre: a.nombre.trim()}));
executeAssignment(semestreId, cleaned);

// ✓ CORRECTO
executeAssignment(semestreId, validationData);
```

### ❌ Error 3: Reordenar datos
```typescript
// ❌ INCORRECTO
const sorted = validationData.sort(...);
executeAssignment(semestreId, sorted);

// ✓ CORRECTO (backend ya ordena)
executeAssignment(semestreId, validationData);
```

---

## ✅ CHECKLIST IMPLEMENTACIÓN

- [ ] Tipos TypeScript coinciden con DTOs backend
- [ ] semestreId es `number`, no `string`
- [ ] No se modifican datos después de validación
- [ ] Se respeta el orden de alumnos (backend)
- [ ] Se maneja status "OK" vs "ERROR"
- [ ] Se muestra tabla de errores
- [ ] Se valida respuesta del servidor

---

