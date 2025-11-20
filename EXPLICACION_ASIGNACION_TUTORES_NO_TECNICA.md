# 📚 EXPLICACIÓN: CÓMO FUNCIONA LA ASIGNACIÓN DE TUTORES
## Versión para personas sin conocimiento técnico

---

## 🎯 ¿QUÉ ES LA ASIGNACIÓN DE TUTORES?

Es el **proceso automático que empareja cada alumno con su tutor**. Es como un sistema de "matching" inteligente que se asegura de que:
- Cada alumno tenga un tutor asignado
- Los tutores no queden sobrecargados (respetan límite de alumnos)
- Los alumnos que regresan mantengan su tutor anterior si es posible

---

## 📋 PASO 1: PREPARACIÓN DEL ARCHIVO EXCEL

**¿Qué necesitamos?**
Un archivo Excel con la lista de alumnos del nuevo semestre.

**Columnas requeridas:**
| Columna | Ejemplo | Descripción |
|---------|---------|-------------|
| Matrícula | A20230145 | Identificador único del alumno |
| Nombre | Juan Pérez | Nombre completo |
| Carrera | ICA | Carrera del alumno (Ingeniería Civil, etc) |
| Semestre | 5 | Número de semestre (1-12) |

**Validaciones del Excel:**
✓ Todos los campos deben estar completos (sin celdas vacías)
✓ La matrícula debe tener 5-20 caracteres alfanuméricos
✓ El semestre debe ser entre 1 y 12
✓ La carrera debe ser una de las válidas (ICA, IE, IMECA, IME, ISC, ITS)
✓ No puede haber matrículas repetidas en el mismo archivo

**¿Qué pasa si hay errores?**
El sistema registra cada error en un reporte detallado y continúa procesando los alumnos válidos.

---

## 📊 PASO 2: LECTURA Y VALIDACIÓN

**El sistema:**
1. Lee el archivo Excel fila por fila
2. Valida cada alumno contra reglas específicas
3. Crea una lista de alumnos "válidos" para procesar
4. Guarda un registro de los alumnos que tuvieron errores

**Ejemplo:**
```
Fila 2: Juan Pérez - ✅ Válido
Fila 3: Maria Gómez - ❌ Error: Semestre 15 (no válido)
Fila 4: Carlos López - ✅ Válido
...
Resultado: 150 alumnos válidos, 2 errores
```

---

## 🔍 PASO 3: COMPARACIÓN CON LA BASE DE DATOS

**¿Qué hace?**
El sistema compara:
- **Lista anterior**: Alumnos que estaban registrados en el semestre pasado
- **Lista nueva**: Alumnos que vienen en el Excel

**¿Qué pasa con los alumnos que desaparecen?**
Los alumnos que NO aparecen en el nuevo Excel se marcan como **INACTIVOS**.

**Ejemplo:**
```
Semestre anterior:   [Juan, Maria, Carlos, Diego]
Nuevo semestre:      [Juan, Maria, Luis]

Resultado:
- Juan ✓ Sigue
- Maria ✓ Sigue
- Luis ✓ Entra como nuevo
- Carlos ❌ Marcado como inactivo (ya no está)
- Diego ❌ Marcado como inactivo (ya no está)
```

---

## 📦 PASO 4: LIBERACIÓN DE CUPOS

**¿Qué significa "liberar cupos"?**
Cada tutor tiene una **capacidad máxima** de alumnos (por ejemplo: 25 alumnos).

Cuando un alumno se marca como inactivo, su tutor pierde ese "alumno asignado" y queda con cupo disponible para un nuevo alumno.

**Ejemplo:**
```
Tutor García:
- Antes: 25 alumnos asignados (al máximo)
- Se marca Carlos como inactivo
- Después: 24 alumnos asignados (1 cupo disponible)
```

---

## 🔄 PASO 5: REINGRESOS (ALUMNOS QUE REGRESAN)

**Situación especial:**
Un alumno que estaba inactivo (ausente el semestre pasado) ahora reaparece en el Excel.

**¿Qué sucede?**
El sistema es inteligente:

1. **Si estaba en baja temporal o movilidad:**
   - Intenta asignarlo de nuevo a su **tutor anterior**
   - Si el tutor tiene cupos disponibles → ✅ Se le asigna
   - Si el tutor está lleno → ⚠️ Genera alerta (necesita asignación manual)

2. **Si tiene baja definitiva o egresó:**
   - No se puede reasignar automáticamente
   - Requiere revisión manual

**Ejemplo:**
```
Situación: Juan estaba inactivo, ahora reaparece

Caso 1: Su tutor (García) tiene cupos
Resultado: Juan se asigna automáticamente a García

Caso 2: Su tutor (García) está al máximo
Resultado: Sistema crea alerta: "Juan necesita nueva asignación"
```

---

## 🎓 PASO 6: ASIGNACIÓN PRINCIPAL

**Este es el paso más importante.**

El sistema necesita asignar CADA alumno a UN tutor, respetando:
- ✓ La capacidad máxima de cada tutor
- ✓ Las carreas (preferencia por tutores de la misma carrera)
- ✓ La compatibilidad entre carreras
- ✓ El equilibrio de carga

### 🏗️ ESTRUCTURA GENERAL

El sistema tiene varios **grupos de tutores** organizados por carrera:
```
GRUPO ICA (Ingeniería Civil):
├─ Tutor García (25 alumnos máx, 15 asignados actualmente)
├─ Tutor Martínez (25 alumnos máx, 22 asignados actualmente)
└─ Tutor López (25 alumnos máx, 20 asignados actualmente)

GRUPO IE (Ingeniería Eléctrica):
├─ Tutor Pérez (25 alumnos máx, 18 asignados)
└─ Tutor Sánchez (20 alumnos máx, 19 asignados)
```

### 🔎 ESTRATEGIA DE SELECCIÓN (3 NIVELES)

Para cada alumno, el sistema sigue esta estrategia de **3 niveles**:

#### NIVEL 1️⃣: Tutor de la MISMA CARRERA
**Prioridad:** Más alta

El sistema busca tutores de la misma carrera del alumno que:
- Estén activos ✓
- Tengan cupos disponibles ✓

**¿Cómo elige entre varios?**
Elige al que tiene **MENOS alumnos asignados** (para equilibrar la carga).

**Ejemplo:**
```
Alumno: Diego (carrera ICA)

Tutores ICA disponibles:
- García: 15 alumnos (✅ ELIGE ESTE)
- Martínez: 22 alumnos
- López: 20 alumnos

Resultado: Diego se asigna a García (menor carga)
```

#### NIVEL 2️⃣: Tutor de CARRERA COMPATIBLE
**Prioridad:** Media

Si NO hay tutores disponibles en la carrera del alumno, el sistema busca en carreras **compatibles** (que tienen afinidad).

**¿Qué significa "compatible"?**
La institución establece que ciertas carreras pueden compartir tutores. Por ejemplo:
- Ingeniería Civil ↔ Ingeniería Eléctrica (compatible)
- Ingeniería Mecánica ↔ Ingeniería de Sistemas (compatible)

**Orden de búsqueda:**
Se respeta un **orden de prioridad** establecido por la institución.

**Ejemplo:**
```
Alumno: Roberto (carrera ICA)

Tutores ICA disponibles: NINGUNO (todos llenos)

Carreras compatibles (en orden):
1. IE (Ingeniería Eléctrica) - 1era opción
2. IMECA (Ingeniería Mecánica) - 2da opción

Resultado: Se asigna a un tutor de IE
⚠️ Alerta: "Asignación cruzada - carrera diferente"
```

#### NIVEL 3️⃣: Tutor con MENOR CARGA GLOBAL
**Prioridad:** Baja (última opción)

Si ningún tutor compatible tiene cupo, el sistema elige al tutor de **CUALQUIER carrera** que tenga menos alumnos.

**Ejemplo:**
```
Alumno: Sofia (carrera ICA)

Tutores ICA: TODOS LLENOS
Tutores compatibles: TODOS LLENOS

Tutor con menor carga en TODO el sistema:
- Tutor Ramírez (ISC): 12 alumnos asignados

Resultado: Sofia se asigna a Ramírez
⚠️ Alerta: "Asignación cruzada - carrera muy diferente"
```

#### ERROR: SIN TUTOR DISPONIBLE
**¿Qué pasa si NO hay tutores disponibles?**

Esto es muy raro, pero si sucede:
- Se registra un **error** en el reporte
- El alumno queda sin asignar
- Requiere intervención manual

```
Si TODOS los tutores están al máximo:
❌ Error: "No hay tutores con capacidad disponible"
→ Requiere agregar más tutores o aumentar capacidad
```

---

## 📝 TIPOS DE ASIGNACIÓN

El sistema registra **qué tipo** de asignación hizo para cada alumno:

### 🆕 INICIAL
**Significa:** Alumno nuevo que NO estaba en la base de datos
- El alumno se crea y se asigna por primera vez
- La carga del tutor **AUMENTA** en 1

### 🔁 REINGRESO
**Significa:** Alumno que estaba inactivo y regresa con su tutor anterior
- El alumno mantiene su tutor de antes
- La carga del tutor **NO CAMBIA** (ya estaba contada)
- Ejemplo: Juan estuvo de movilidad y regresa

### 🔄 REASIGNACIÓN
**Significa:** Alumno existente que cambia de tutor
- Tutor anterior ya no está disponible o está lleno
- Se asigna a un tutor diferente
- Tutor anterior pierde 1 alumno
- Tutor nuevo gana 1 alumno

---

## 📊 EJEMPLO COMPLETO DEL FLUJO

```
════════════════════════════════════════════════════════════════

INICIO: Semestre 2024-2 Procesado
Estado:
- Alumnos activos: 450
- Tutores: 20 (capacidad total: 500)

════════════════════════════════════════════════════════════════

📁 CARGAMOS EXCEL DEL SEMESTRE 2025-1

════════════════════════════════════════════════════════════════

✅ PASO 1: VALIDACIÓN
- Alumnos en Excel: 520
- Alumnos válidos: 515
- Errores: 5 (matrículas inválidas)

✅ PASO 2: COMPARACIÓN
- Alumnos nuevos: 95
- Alumnos que continúan: 360
- Alumnos que desaparecen: 90

✅ PASO 3: MARCAR INACTIVOS
- Se marcan 90 alumnos como INACTIVOS
  (Son los que ya no están en Excel)

✅ PASO 4: LIBERAR CUPOS
- Tutor García pierde 5 alumnos → 20→15 alumnos
- Tutor Martínez pierde 8 alumnos → 25→17 alumnos
- Tutor López pierde 3 alumnos → 20→17 alumnos
...
- Total cupos liberados: 90

✅ PASO 5: REINGRESOS
- Alumnos que regresan: 25
  - 15 asignados a su tutor anterior ✅
  - 10 necesitan nueva asignación ⚠️

✅ PASO 6: ASIGNACIÓN PRINCIPAL
Procesando 515 alumnos...

Alumno 1 - Juan (ICA)
├─ ¿Tutor ICA disponible? SÍ
├─ Selecciona: Tutor García (15 alumnos) ✅
└─ Tipo: INICIAL

Alumno 2 - Maria (ICA)
├─ ¿Tutor ICA disponible? SÍ
├─ Selecciona: Tutor López (17 alumnos) ✅
└─ Tipo: INICIAL

Alumno 3 - Carlos (ICA)
├─ ¿Tutor ICA disponible? NO (todos llenos)
├─ ¿Tutor compatible? SÍ (IE)
├─ Selecciona: Tutor Pérez (IE, 18 alumnos) ✅
├─ Tipo: INICIAL
└─ ⚠️ Alerta: Asignación cruzada

...
[Procesando 512 alumnos más]
...

════════════════════════════════════════════════════════════════

📊 RESULTADOS FINALES:

ASIGNACIÓN EXITOSA:
✅ Total asignados: 515
├─ Iniciales: 95
├─ Reasignaciones: 25
└─ Reingresos: 395

⚠️ ALERTAS:
- Asignaciones cruzadas: 45
- Reasignaciones forzadas: 8

❌ ERRORES:
- Ninguno

CARGA DE TUTORES:
- García: 25/25 (100% capacidad)
- Martínez: 25/25 (100%)
- López: 24/25 (96%)
- Pérez: 20/25 (80%)
...

PROCESO COMPLETADO: ✅
Tiempo: 2 minutos 34 segundos
Estado: ÉXITO

════════════════════════════════════════════════════════════════
```

---

## 🎯 CONCEPTOS CLAVE

### Capacidad de un Tutor
Es el **número máximo** de alumnos que un tutor puede atender.
```
Tutor García:
- Capacidad máxima: 25 alumnos
- Alumnos actuales: 23
- Cupos disponibles: 2
```

### Carga Actual
Es el **número real** de alumnos asignados en este momento.
```
Si 90 alumnos se inactivan:
- Carga decrece en 90 total
- Se distribuye entre todos los tutores
- Generan cupos nuevos para asignaciones
```

### Estado de un Alumno
```
ACTIVO       → Tiene tutor y está estudiando
INACTIVO     → No está en el semestre actual
EGRESADO     → Completó la carrera
BAJA_DEFINITIVA → Se retiró del programa
```

### Tipos de Carrera Compatible
```
Por ejemplo, si la institución define:
- ICA (Ingeniería Civil) es compatible con IE (Ingeniería Eléctrica)
- IE es compatible con IMECA (Ingeniería Mecánica)

Entonces puede haber asignaciones cruzadas entre ellas.
```

---

## ⚠️ RESTRICCIONES IMPORTANTES

### 1. No se puede duplicar asignación en semestre
```
❌ Juan NO puede estar asignado a García DOS VECES en el mismo semestre
✅ Juan puede estar asignado a García en 2024-2 y a López en 2025-1
```

### 2. No se puede exceder capacidad
```
Tutor García: 25 máximo
- Si tiene 25 asignados → NO puede recibir más
- Debe esperar a que alguien se retire
```

### 3. Máximo de cambios de tutor
```
Cada alumno puede cambiar de tutor MÁXIMO 2 VECES
- Cambio 1: Permitido
- Cambio 2: Permitido
- Cambio 3: ❌ Bloqueado - Requiere aprobación manual
```

### 4. El tutor debe estar ACTIVO
```
❌ No se puede asignar a un tutor inactivo/jubilado
✅ Solo tutores con estado "activo"
```

---

## 📋 POSIBLES RESULTADOS

### ✅ ÉXITO
Todos los alumnos se asignaron correctamente.
```
Estado: COMPLETADO
Asignados: 515/515
Errores: 0
```

### ⚠️ ÉXITO CON ALERTAS
Se asignaron todos, pero algunos tienen situaciones especiales.
```
Estado: COMPLETADO
Asignados: 515/515
Alertas:
- 45 asignaciones cruzadas
- 8 alumnos sin tutor anterior disponible
```

### ❌ PARCIAL
Algunos alumnos no se pudieron asignar.
```
Estado: COMPLETADO CON ERRORES
Asignados: 510/515
Errores: 5
- 3: Sin tutor disponible en carrera
- 2: Matrícula duplicada en sistema
```

### 💥 ERROR TOTAL
No se pudo completar el proceso.
```
Estado: FALLIDO
Razón: Base de datos no accesible
Acción: Reintentar después
```

---

## 📞 PRÓXIMOS PASOS TÍPICOS

1. **Revisar Alertas**
   - ¿Hay asignaciones cruzadas? Verificar si son aceptables
   - ¿Hay alumnos sin tutor? Asignar manualmente

2. **Revisar Errores**
   - ¿Hay errores de validación? Corregir Excel y reintentar
   - ¿Hay errores de duplicación? Validar datos maestros

3. **Confirmar Asignación**
   - Verificar que la carga está bien distribuida
   - Confirmar que todos tienen tutor
   - Comunicar a los tutores sus alumnos

4. **Iniciar Semestre**
   - Con asignaciones confirmadas
   - Sistema listo para tutoría

---

## 🎓 RESUMEN VISUAL

```
┌────────────────────────────────────────────────────────┐
│           FLUJO DE ASIGNACIÓN DE TUTORES              │
└────────────────────────────────────────────────────────┘

📁 Excel con alumnos
    ↓
✅ Validación (campos correctos)
    ↓
🔍 Comparación (quiénes se van, quiénes quedan)
    ↓
📦 Liberación de cupos (desasignar inactivos)
    ↓
🔄 Reingresos (alumnos que regresan)
    ↓
🎓 ASIGNACIÓN PRINCIPAL
    ├─ Alumno 1 → Tutor compatible
    ├─ Alumno 2 → Tutor misma carrera
    ├─ Alumno 3 → Tutor carrera compatible
    └─ Alumno N → Mejor opción disponible
    ↓
📊 Resultados
    ├─ Asignados: 515
    ├─ Alertas: 45
    └─ Errores: 0
    ↓
✅ COMPLETADO
```

---

**Notas finales:**
- El proceso es **automático e inteligente**
- Respeta **límites de capacidad**
- Busca **equilibrio** en carga de tutores
- Prefiere **misma carrera** cuando es posible
- Genera **alertas** para revisar manualmente
- Mantiene **registro** de todo

¡Así es cómo el sistema asegura que cada alumno tenga un tutor adecuado!
