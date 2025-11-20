# REPORTE EJECUTIVO - PRUEBAS INTEGRALES DE SOFTWARE

**Práctica 04 | 2do Parcial | Profesor: Sergio A Noh Puch**

---

## RESUMEN EJECUTIVO

| Métrica | Valor |
|---------|-------|
| **Método Aplicado** | Sandwich (Integración Híbrida) + CI |
| **Tests Ejecutados** | 46 (100% exitosos) |
| **Cobertura Global** | 12% (1,119 / 14,018 instrucciones) |
| **Cobertura Servicios Críticos** | 85-90% (Auth, Semestre) |
| **Clases Analizadas** | 102 |
| **Clases Cubiertas** | 32 (31.4%) |

---

## 1. MÉTODO APLICADO

### Sandwich (Integración Híbrida)

**Por qué se seleccionó:**
- Clean Architecture: Capas independientes = testing independiente
- Bottom-Up: Dominio testeable sin dependencias
- Top-Down: Servicios con mocks, controladores integrados
- Pragmático: Combina tests automatizados + validación manual

**Ventajas para este proyecto:**
✅ Independencia de capas
✅ Paralelización efectiva
✅ Flexible y escalable
✅ Realista (tests + validación manual)

---

## 2. REPORTE DE COBERTURA

### Estadísticas Clave

```
Instrucciones:  1,119 / 14,018  = 12%  ✓
Branches:          54 / 749     = 7%   ✓
Líneas:           391 / 3,052   = 12.8% ✓
Métodos:           81 / 508     = 15.9% ✓
Clases:            32 / 102     = 31.4% ✓
```

### Paquetes Destacados

| Paquete | Cobertura | Estado |
|---------|-----------|--------|
| domain.enums | **92%** | ✅ EXCELENTE |
| application.dto.auth | **77%** | ✅ BUENO |
| domain.entity | **22%** | 🟡 ACEPTABLE |
| application.service.impl | **16%** | 🟠 MEJORA PENDIENTE |
| infrastructure.controller | **0%** | 🔴 CRÍTICO (validado manual) |

### Líneas sin Cobertura por Módulo

| Módulo | LOC sin Cobertura | Impacto |
|--------|-------------------|---------|
| Asignaciones | ~1,200 | 🔴 CRÍTICO |
| Reportes | ~2,100 | 🔴 CRÍTICO |
| Controladores | ~1,200 | 🟠 ALTO (validado manual) |
| Búsqueda | ~400 | 🟠 ALTO |

---

## 3. FORTALEZAS

### Arquitectura
✅ Clean Architecture bien implementada
✅ Bajo acoplamiento entre capas
✅ Testing aislado por capa

### Testing Infrastructure
✅ JUnit 5 + Mockito + H2 + JaCoCo configurados
✅ TestDataBuilder (297 LOC) para fixtures reutilizables
✅ Tests ejecutándose en < 5 segundos

### Calidad del Código
✅ 46 tests: 100% exitosos (0 fallos)
✅ DTOs con validación declarativa
✅ JWT con refresh tokens

### Validación Complementaria
✅ 14 endpoints REST validados con Insomnia
✅ Frontend integrado y funcional
✅ Flujos end-to-end verificados

---

## 4. ÁREAS DE MEJORA

### 🔴 CRÍTICAS

1. **AsignacionesService** (0% cobertura)
   - ~1,200 LOC sin validación
   - Caso de uso central del sistema
   - Acción: 45 test cases en Fase 2

2. **ReporteService** (0% cobertura)
   - ~2,100 LOC de exportación Excel/PDF
   - No validado antes de producción
   - Acción: 25 test cases en Fase 2

3. **Métodos de Búsqueda** (0% cobertura)
   - Ranking y filtros sin especificación
   - Comportamiento ambiguo
   - Acción: 35 test cases en Fase 2

### 🟠 IMPORTANTES

4. **Controladores sin Tests Automatizados**
   - 13/14 controladores sin tests
   - Mitigación: Validados manualmente con Insomnia
   - Acción: MockMvc tests en Fase 3

5. **Servicios Largos**
   - Métodos > 150 líneas
   - Difíciles de testear
   - Acción: Refactorización en múltiples métodos

---

## 5. RECOMENDACIONES

### FASE 2: Servicios Críticos (3-4 semanas)
```
AsignacionesService    → 45 test cases → 80% cobertura
AlumnoSearchService    → 35 test cases → 75% cobertura
ReporteService         → 25 test cases → 70% cobertura
TutorSincronizacion    → 20 test cases → 65% cobertura

Resultado esperado: 45% cobertura global
```

### FASE 3: Integración Completa (3-4 semanas)
```
Controllers (MockMvc)       → 20+ test cases
Integration Flow Tests      → 10 test cases
Regression Tests            → 15 test cases
Repository Specifications   → 25 test cases

Resultado esperado: 65-70% cobertura global
```

### FASE 4: Mantenimiento Continuo
```
✓ Pipeline CI con umbrales
✓ Bloquear merges si cobertura < 65%
✓ Reporte automático post-commit
```

### Mejoras Arquitectónicas
1. **Refactorizar servicios largos** (AlumnoSearchService)
2. **Centralizar configuraciones** (application.properties)
3. **Test fixtures especializadas** (AlumnoFixtures, SemestreFixtures)
4. **CI/CD pipeline** (Maven + JaCoCo automático)

---

## 6. EVIDENCIAS

### Tests Exitosos
```
[INFO] Tests run: 46, Failures: 0, Errors: 0, Skipped: 0 ✓
[INFO] Tiempo total: < 5 segundos

Desglose:
• AuthServiceImplTest: 18 casos ✓
• SemestreServiceImplTest: 24 casos ✓
• AlumnoSearchServiceImplTest: 4 casos ✓
```

### Validación Manual
```
✓ 14 endpoints REST (Insomnia)
✓ Frontend integrado
✓ Flujos de usuario funcionales
✓ Documentación OpenAPI/Swagger
```

### Reporte JaCoCo
```
Ubicación: backend/target/site/jacoco/index.html
Generador: JaCoCo 0.8.10.202304240956
Timestamp: 15 de Noviembre de 2025
```

### Comparativa Pre-Post
```
Tests Unitarios:        8  → 46  (+475%)
Cobertura Global:      2%  → 12% (+300%)
AuthService:           0%  → 85% (+85%)
SemestreService:       0%  → 90% (+90%)
Clases Cubiertas:      3   → 32  (+967%)
```

---

## 7. CONCLUSIÓN

### Logros
✅ Arquitectura Clean validada
✅ 46 tests unitarios exitosos
✅ Módulos críticos (Auth, Semestre) con cobertura > 80%
✅ Infraestructura de testing lista para escalar
✅ Validación dual (automatizada + manual)

### Déficits
❌ Módulo de Asignaciones: 0% cobertura
❌ Reportes: 0% cobertura
❌ 2,661 líneas aún sin validación

### Próximas Acciones
1. Iniciar **Fase 2** (AsignacionesService, ReporteService)
2. **Refactorizar** servicios largos
3. **Configurar CI/CD** con umbrales automáticos
4. Objetivo: **65-70% cobertura global** en 8-12 semanas

---

## ANEXOS

### Ejecutar Tests Localmente
```bash
# Tests completos con reporte
mvn clean test jacoco:report

# Ver reporte en navegador
open target/site/jacoco/index.html
```

### Archivos del Proyecto
```
Código: backend/src/main/java/com/universidad/tutorias/
Tests: backend/src/test/java/com/universidad/tutorias/
Reporte JaCoCo: backend/target/site/jacoco/index.html
Configuración: backend/pom.xml
```

---

**Fecha:** 15 de Noviembre de 2025
**Profesor:** Sergio A Noh Puch
**Asignatura:** Pruebas Integrales de Software

