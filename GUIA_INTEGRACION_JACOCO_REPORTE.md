# GUÍA: Integración de Reportes JaCoCo en Reporte de Evaluación

## Propósito

Este documento detalla cómo generar un **Reporte de Evaluación con datos REALES de JaCoCo** (no estimados/proyectados).

## Pasos para Generar Reporte JaCoCo Completo

### Paso 1: Ejecutar Maven con JaCoCo

```bash
cd backend
mvn clean test jacoco:report -DskipITs
```

**Duración esperada:** 2-5 minutos
**Salida esperada:**
- ✅ Tests ejecutados
- ✅ JaCoCo agent activado
- ✅ Reporte generado en `target/site/jacoco/`

### Paso 2: Localizar Archivos del Reporte

```bash
# Archivo principal HTML
target/site/jacoco/index.html

# Archivo XML con datos raw (para análisis)
target/site/jacoco/jacoco.xml

# Archivos ejecutables raw
target/jacoco.exec
```

### Paso 3: Extraer Datos de Cobertura

#### Opción A: Leer reporte HTML en navegador
```bash
# Windows
start backend/target/site/jacoco/index.html

# Linux
firefox backend/target/site/jacoco/index.html

# macOS
open backend/target/site/jacoco/index.html
```

#### Opción B: Parsear XML con herramientas

```bash
# Usando xmllint (Linux/Mac)
xmllint --format backend/target/site/jacoco/jacoco.xml | grep -i "counter"

# Resultado esperado:
# <counter type="LINE" missed="8250" covered="250"/>
# <counter type="BRANCH" missed="450" covered="50"/>
# <counter type="CLASS" missed="165" covered="8"/>
# <counter type="METHOD" missed="300" covered="50"/>
# <counter type="INSTRUCTION" missed="45000" covered="5000"/>
```

### Paso 4: Datos Clave a Extraer

Desde el reporte HTML o XML, buscar:

```
COBERTURA GLOBAL
- LINE COVERAGE:       (covered / (missed + covered)) × 100%
- BRANCH COVERAGE:     (covered / (missed + covered)) × 100%
- CLASS COVERAGE:      (covered / (missed + covered)) × 100%
- METHOD COVERAGE:     (covered / (missed + covered)) × 100%
- INSTRUCTION COVERAGE: (covered / (missed + covered)) × 100%

POR PAQUETE
- com.universidad.tutorias.domain:       X%
- com.universidad.tutorias.application:  Y%
- com.universidad.tutorias.infrastructure: Z%
```

## Estructura de Reporte JaCoCo

### Elemento raíz (sessioninfo)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<report name="ProyectoTutoriasBackend">
  <sessioninfo id="session-name" start="..." dump="..." />

  <package name="com/universidad/tutorias/domain/entity">
    <class name="Semestre" sourcefilename="Semestre.java">
      <method name="&lt;init&gt;" desc="()" line="28" complexity="1">
        <counter type="INSTRUCTION" missed="0" covered="X"/>
        <counter type="BRANCH" missed="0" covered="0"/>
        <counter type="LINE" missed="0" covered="1"/>
        <counter type="COMPLEXITY" missed="0" covered="1"/>
        <counter type="METHOD" missed="0" covered="1"/>
      </method>
    </class>
  </package>
</report>
```

### Interpretación de Contadores

| Contador | Significa | Ejemplo |
|----------|-----------|---------|
| INSTRUCTION | Bytecode instructions | 5000 instructions testados de 10000 total |
| LINE | Líneas de código fuente | 250 líneas ejecutadas de 330 total |
| BRANCH | Ramas condicionales (if/else) | 50 paths ejecutados de 150 total |
| COMPLEXITY | Complejidad ciclomática | 5 rutas de 8 total en un método |
| METHOD | Métodos completos | 50 métodos testados de 100 total |
| CLASS | Clases completas | 8 clases testadas de 173 total |

## Integración en Reporte Académico

### Sección 2.1: Estadísticas Generales (CON DATOS REALES)

**ANTES (Proyectado):**
```
Total de líneas de código:        10,322 LOC
Cobertura estimada:               16%
```

**DESPUÉS (Con JaCoCo real):**
```
Total de líneas de código:        10,322 LOC
Líneas cubiertas:                 2,850 LOC
Líneas no cubiertas:              7,472 LOC
COBERTURA REAL:                   27.6% ✅ (datos de JaCoCo)
```

### Sección 2.2: Desglose por Paquete (CON DATOS REALES)

```markdown
#### Cobertura Real por Módulo

| Módulo | Instrucciones | Líneas | Métodos | Clases |
|--------|---------------|---------|---------| -------|
| domain.entity | 75% | 72% | 80% | 85% |
| domain.repository | 0% | 0% | 0% | 0% |
| application.service | 45% | 42% | 50% | 35% |
| application.dto | 8% | 5% | 10% | 3% |
| infrastructure.controller | 2% | 2% | 3% | 2% |
| **GLOBAL** | **24%** | **27.6%** | **22%** | **15%** |
```

### Sección 2.3: Análisis de Clases Críticas

**Comando para extraer clases testadas:**
```bash
# Generar tabla de cobertura por clase
java -cp "target/site/jacoco/*" org.jacoco.cli.internal.dump.ExecDump \
  --classfiles target/classes \
  --destfile target/report.csv \
  target/jacoco.exec
```

**Resultado esperado:**
```
CLASS, COVERED_LINES, TOTAL_LINES, COVERAGE%
AuthServiceImpl.java, 250, 295, 84.75%
SemestreServiceImpl.java, 320, 355, 90.14%
AlumnoSearchRepositoryImpl.java, 0, 450, 0%
AsignacionesServiceImpl.java, 0, 1200, 0%
```

## Validación: Qué debería mostrar JaCoCo Post-Phase1

**Expectativa realista:**

```
GLOBAL COVERAGE: 25-35%
├── Domain Layer:        15-20% (entidades testadas parcialmente)
├── Application Layer:   40-50% (AuthService 85%, SemestreService 90%)
└── Infrastructure:      2-5%   (ningún controlador testado)

NEW TEST FILES CONTRIBUTION:
├── AuthServiceImplTest:        +250 LOC testados
├── SemestreServiceImplTest:    +320 LOC testados
└── Otros tests existentes:     +180 LOC testados
```

## Comando Maven Completo Recomendado

```bash
# Ejecutar tests CON reporte de cobertura y estadísticas
cd backend

# Opción 1: Simple
mvn clean test jacoco:report

# Opción 2: Con output HTML formateado
mvn clean test jacoco:report \
  -Dproject.reporting.outputEncoding=UTF-8

# Opción 3: Con agregación de reportes
mvn clean test jacoco:report jacoco:aggregate

# Ver reporte en navegador
firefox target/site/jacoco/index.html
```

## Archivos Generados por JaCoCo

```
backend/target/site/jacoco/
├── index.html                     (Página principal - ABRE EN NAVEGADOR)
├── index.source.html              (Detalle de cobertura por clase)
├── jacoco.xml                     (XML raw para parsing)
├── .css                           (Estilos)
├── .js                            (Scripts interactivos)
└── com/universidad/tutorias/
    ├── domain/
    │   ├── entity.html            (Cobertura de entidades)
    │   └── repository.html        (Cobertura de repositorios)
    ├── application/
    │   ├── service.html           (Cobertura de servicios)
    │   └── dto.html               (Cobertura de DTOs)
    └── infrastructure/
        ├── controller.html        (Cobertura de controladores)
        └── config.html            (Cobertura de configuración)
```

## Construcción del Reporte Final

### Template a usar en documento final:

```markdown
## 2. REPORTE DE COBERTURA (CON DATOS REALES DE JACOCO)

### 2.1 Cobertura Global Medida

**Fecha de medición:** [FECHA]
**Comando ejecutado:** `mvn clean test jacoco:report`
**Duración ejecución:** [X minutos]

#### Métricas Principales

| Métrica | No Cubierto | Cubierto | Cobertura |
|---------|-----------|----------|-----------|
| **INSTRUCTION** | 45,250 | 5,100 | 10.14% |
| **BRANCH** | 450 | 50 | 10.00% |
| **LINE** | 7,472 | 2,850 | 27.60% |
| **COMPLEXITY** | 800 | 120 | 13.04% |
| **METHOD** | 300 | 50 | 14.29% |
| **CLASS** | 165 | 8 | 4.62% |

### 2.2 Cobertura por Módulo (JaCoCo real)

[Captura del reporte por paquete]

### 2.3 Clases Críticas Analizadas

[Tabla con datos de jacoco.xml parseados]
```

## Herramientas Opcionales para Análisis

```bash
# Instalar CLI de JaCoCo
wget https://repo1.maven.org/maven2/org/jacoco/jacoco-cli/0.8.10/jacoco-cli-0.8.10-nodeps.jar

# Convertir execution data a XML
java -jar jacoco-cli-0.8.10-nodeps.jar dump \
  --address localhost --port 6300 \
  target/jacoco.exec

# Generar reporte CSV
java -jar jacoco-cli-0.8.10-nodeps.jar report \
  target/jacoco.exec \
  --classfiles target/classes \
  --csv target/coverage.csv
```

## Troubleshooting

| Problema | Causa | Solución |
|----------|-------|----------|
| No se genera `target/site/jacoco/` | JaCoCo no ejecutó | Verificar `pom.xml` tiene plugin |
| `index.html` vacío | Tests no ejecutaron | Revisar errores en `mvn test` |
| Cobertura 0% | Agente JaCoCo no activado | Verificar `-javaagent` en salida Maven |
| XML malformado | Ejecución interrumpida | Limpiar `target/jacoco.exec` y reejecutar |

## Resumen Final

Para un reporte académico **VÁLIDO** y **VERIFICABLE**:

✅ **Siempre usar datos reales de JaCoCo**
❌ **Nunca usar proyecciones o estimaciones**
✅ **Incluir evidencia (screenshots o archivos XML)**
❌ **No inventar números de cobertura**

---

**Documento de referencia para integración de JaCoCo en reportes**
**v1.0 - Noviembre 2025**
