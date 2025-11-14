# DOCUMENTACIÓN DEL PROCESO DE ASIGNACIÓN DE TUTORÍAS

## Tabla de Contenidos
1. [Visión General](#visión-general)
2. [Flujo Principal del Proceso](#flujo-principal-del-proceso)
3. [Entidades del Dominio](#entidades-del-dominio)
4. [Servicios de Negocio](#servicios-de-negocio)
5. [Endpoints de API](#endpoints-de-api)
6. [Reglas de Negocio](#reglas-de-negocio)
7. [Flujos Detallados](#flujos-detallados)
8. [Manejo de Excepciones](#manejo-de-excepciones)
9. [Ejemplos Prácticos](#ejemplos-prácticos)

---

## Visión General

El sistema de asignación gestiona la distribución inteligente de estudiantes a tutores dentro de un semestre académico específico. Este proceso es fundamental para garantizar que cada estudiante tenga un tutor asignado y que la carga de trabajo se distribuya equitativamente entre los tutores disponibles.

### Objetivos Principales
- Asignar automáticamente estudiantes a tutores de manera eficiente
- Mantener la capacidad máxima de estudiantes por tutor
- Realizar reasignaciones cuando sea necesario
- Rastrear todas las asignaciones y cambios
- Generar alertas para casos especiales (asignaciones cruzadas, capacidad excedida, etc.)

### Actores Principales
- **Alumno**: Estudiante que requiere tutoría
- **Tutor**: Docente que proporciona tutoría a estudiantes
- **Administrador**: Usuario que inicia procesos de asignación
- **Sistema**: Ejecuta la lógica de asignación automática

---

## Flujo Principal del Proceso

### Diagrama de Alto Nivel

```
┌─────────────────────────────────────┐
│  POST /api/asignaciones/iniciar     │
│  (Subir archivo Excel)              │
└────────────┬────────────────────────┘
             │
             v
┌─────────────────────────────────────┐
│  FASE 1: Lectura y Validación       │
│  • Leer archivo Excel               │
│  • Validar datos de estudiantes     │
│  • Registrar errores                │
└────────────┬────────────────────────┘
             │
             v
┌─────────────────────────────────────┐
│  FASE 2: Comparación y Inactivación │
│  • Comparar con BD                  │
│  • Identificar estudiantes inactivos│
│  • Marcar como INACTIVO             │
└────────────┬────────────────────────┘
             │
             v
┌─────────────────────────────────────┐
│  FASE 3: Liberación de Cupos        │
│  • Decrementar carga de tutores     │
│  • de estudiantes inactivos         │
└────────────┬────────────────────────┘
             │
             v
┌─────────────────────────────────────┐
│  FASE 4: Procesamiento de Reingresos│
│  • Reactivar estudiantes que        │
│  • vuelven a inscribirse            │
└────────────┬────────────────────────┘
             │
             v
┌─────────────────────────────────────┐
│  FASE 5: Asignación de Estudiantes  │
│  • Sincronizar cargas de tutores    │
│  • Asignar cada estudiante          │
│  • Generar alertas si aplica        │
└────────────┬────────────────────────┘
             │
             v
┌─────────────────────────────────────┐
│  GET /api/asignaciones/proceso/{id} │
│  (Verificar estado y progreso)      │
└─────────────────────────────────────┘
```

### Fases del Proceso

#### **FASE 1: Lectura y Validación (INICIADO)**
- **Responsable**: `ExcelReaderService` + `AlumnoValidadorService`
- **Entrada**: Archivo Excel con datos de estudiantes
- **Validaciones**:
  - Matrícula no vacía
  - Nombre no vacío
  - Carrera válida
  - Semestre numérico válido
- **Salida**: Lista de estudiantes validados + errores registrados
- **Errores Capturados**:
  - `MATRICULA_INVALIDA`
  - `CAMPO_VACIO`
  - `CARRERA_INVALIDA`
  - `SEMESTRE_INVALIDO`
  - `MATRICULA_DUPLICADA`

#### **FASE 2: Comparación e Inactivación (COMPARANDO)**
- **Responsable**: `ComparadorAlumnosService` + `InactivacionService`
- **Lógica**:
  1. Obtener lista de estudiantes activos en BD
  2. Comparar con estudiantes en el archivo Excel
  3. Estudiantes que no están en Excel → Se marcan como INACTIVO
- **Impacto**: Libera cupos que estos estudiantes ocupaban
- **Salida**: Lista de estudiantes a inactivar

#### **FASE 3: Liberación de Cupos (LIBERANDO_CUPOS)**
- **Responsable**: `InactivacionService.liberarCupos()`
- **Proceso**:
  1. Para cada estudiante inactivado
  2. Obtener su tutor actual
  3. Decrementar `cargaActual` del tutor
- **Propósito**: Disponibilizar capacidad para nuevas asignaciones
- **Ejemplo**:
  - Tutor A tenía 15/15 estudiantes
  - Se inactivan 3 estudiantes de Tutor A
  - Tutor A pasa a 12/15 (3 cupos disponibles)

#### **FASE 4: Procesamiento de Reingresos**
- **Responsable**: `ReingresoService`
- **Lógica**:
  1. Buscar estudiantes INACTIVO que reaparecen en nuevo Excel
  2. Cambiar su estado a ACTIVO
  3. Limpiar contador de cambios de tutor (opcional)
- **Propósito**: Reintegrar estudiantes que se reinscriben

#### **FASE 5: Asignación de Estudiantes (ASIGNANDO)**
- **Responsable**: `AsignacionService`
- **Pre-requisitos**:
  1. Sincronizar cargas de todos los tutores
  2. Verificar que datos son consistentes
- **Algoritmo de Asignación** (ver sección detallada)
- **Salida**: Records de Asignacion creados, alertas generadas

---

## Entidades del Dominio

### Asignacion
**Representa**: La asignación de un estudiante a un tutor en un semestre

```java
@Entity
public class Asignacion {
    @Id
    private Long id;

    @ManyToOne
    private Alumno alumno;

    @ManyToOne
    private Tutor tutor;

    @Enumerated(EnumType.STRING)
    private TipoAsignacion tipoAsignacion; // INICIAL, REASIGNACION, REINGRESO

    private LocalDateTime fechaAsignacion;
    private String semestreAcademico; // e.g., "2024-1"

    // Constraint: UNIQUE (alumno_id, tutor_id, semestre_academico)
}
```

**Tipos de Asignación**:
- `INICIAL`: Primera asignación de un estudiante
- `REASIGNACION`: Cambio de tutor (máximo 2 veces)
- `REINGRESO`: Reactivación tras inactividad

### Alumno
**Representa**: Un estudiante del sistema

```java
@Entity
public class Alumno {
    @Id
    private Long id;

    private String matricula; // Identificador único
    private String nombre;
    private String carrera; // Programa académico
    private Integer semestre; // Nivel académico

    @Enumerated(EnumType.STRING)
    private EstadoAlumno estado; // ACTIVO, INACTIVO

    @ManyToOne
    private Tutor tutorActual;

    private Integer contadorCambiosTutor; // Max 2
    private LocalDateTime fechaRegistro;

    // Métodos helper
    public boolean puedeReasignarse() {
        return contadorCambiosTutor < 2;
    }
}
```

**Estados**:
- `ACTIVO`: Estudiante inscrito en el semestre actual
- `INACTIVO`: Estudiante no inscrito o retirado

### Tutor
**Representa**: Un docente que proporciona tutoría

```java
@Entity
public class Tutor {
    @Id
    private Long id;

    private String nombre;
    private String carrera; // Programa académico
    private Integer capacidadMax; // Máximo de estudiantes
    private Integer cargaActual; // Estudiantes asignados actualmente

    private String areaAtencion;
    private String letraEdificio;
    private Boolean activo;
    private LocalDateTime fechaRegistro;

    @OneToMany(mappedBy = "tutorActual")
    private List<Alumno> alumnosAsignados;

    // Métodos helper
    public boolean tieneCapacidadDisponible() {
        return activo && (cargaActual < capacidadMax);
    }

    public Integer getCapacidadDisponible() {
        return Math.max(0, capacidadMax - cargaActual);
    }
}
```

**Restricción**: Un tutor solo puede aceptar nuevas asignaciones si:
- `activo == true`
- `cargaActual < capacidadMax`

### ProcesoAsignacion
**Representa**: Metadatos y estado de un proceso de asignación en lote

```java
@Entity
public class ProcesoAsignacion {
    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private EstadoProceso estado; // INICIADO, COMPARANDO, LIBERANDO_CUPOS, ASIGNANDO, COMPLETADO, FALLIDO

    private Integer totalAlumnosProcesados;
    private Integer totalAlumnosAsignados;
    private Integer totalErrores;
    private Integer totalWarnings;

    private String archivoOrigen; // Nombre del archivo Excel
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String usuarioEjecutor;
    private String detallesJson; // Información adicional en JSON

    @OneToMany(mappedBy = "proceso", cascade = CascadeType.ALL)
    private List<AlertaProceso> alertas;

    @OneToMany(mappedBy = "proceso", cascade = CascadeType.ALL)
    private List<ErrorValidacion> errores;

    // Métodos helper
    public void incrementarAlumnosProcesados() { ... }
    public void incrementarAlumnosAsignados() { ... }
    public Long getTiempoTranscurridoMs() { ... }
    public Integer getPorcentajeProgreso(int totalEsperado) { ... }
}
```

**Estados del Proceso**:
- `INICIADO`: Acaba de comenzar
- `COMPARANDO`: Comparando estudiantes con BD
- `LIBERANDO_CUPOS`: Decrementando cargas
- `ASIGNANDO`: Asignando estudiantes
- `COMPLETADO`: Terminó exitosamente
- `FALLIDO`: Terminó con error

### AlertaProceso
**Representa**: Advertencia o información importante durante el proceso

```java
@Entity
public class AlertaProceso {
    @Id
    private Long id;

    @ManyToOne
    private ProcesoAsignacion proceso;

    @Enumerated(EnumType.STRING)
    private TipoAlerta tipo;
    // ASIGNACION_CRUZADA, CAPACIDAD_EXCEDIDA, SIN_TUTOR_DISPONIBLE

    @Enumerated(EnumType.STRING)
    private SeveridadAlerta severidad;
    // CRITICO, ERROR, WARNING, INFO

    private String descripcion;

    @ManyToOne
    private Alumno alumno; // Opcional

    @ManyToOne
    private Tutor tutor; // Opcional

    private Boolean resuelta; // Default: false
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaResolucion; // Opcional
}
```

**Tipos de Alerta**:
- `ASIGNACION_CRUZADA`: Estudiante asignado a tutor de carrera diferente
- `CAPACIDAD_EXCEDIDA`: Se intentó asignar cuando tutor estaba lleno
- `SIN_TUTOR_DISPONIBLE`: No hay tutores con capacidad disponible
- `ERROR_FORMATO`: Formato inválido en datos

### MatrizAfinidadCarrera
**Representa**: Compatibilidad entre carreras para asignaciones cruzadas

```java
@Entity
public class MatrizAfinidadCarrera {
    @Id
    private Long id;

    private String carreraOrigen; // Carrera del estudiante
    private String carreraCompatible; // Carrera alternativa
    private Integer prioridad; // Orden de preferencia (1 = más preferida)

    // Constraint: UNIQUE (carrera_origen, carrera_compatible)
}
```

**Ejemplo**:
```
Carrera: Ingeniería en Sistemas
├─ Prioridad 1: Ingeniería en Desarrollo de Software
├─ Prioridad 2: Ingeniería en Informática
└─ Prioridad 3: Ingeniería en Tecnología
```

### ErrorValidacion
**Representa**: Error encontrado durante validación de datos

```java
@Entity
public class ErrorValidacion {
    @Id
    private Long id;

    @ManyToOne
    private ProcesoAsignacion proceso;

    private Integer filaExcel; // Número de fila en el archivo
    private String matricula;

    @Enumerated(EnumType.STRING)
    private TipoError tipoError;
    // MATRICULA_INVALIDA, CAMPO_VACIO, CARRERA_INVALIDA, etc.

    private String descripcion;
    private String datoErroneo; // El valor que causó error
    private LocalDateTime fechaRegistro;
}
```

---

## Servicios de Negocio

### ProcesoOrchestrator
**Ubicación**: `impl/ProcesoOrchestratorImpl.java`

**Responsabilidad**: Orquestar todas las fases del proceso de asignación

```java
@Async("asignacionExecutor")
@Transactional
CompletableFuture<Long> ejecutarProcesoCompleto(
    MultipartFile archivo,
    String semestreAcademico,
    String usuario
)
```

**Flujo de Ejecución**:

```
1. Crear ProcesoAsignacion (INICIADO)
   ↓
2. FASE 1: Leer y Validar Excel
   - excelReaderService.leerArchivo(archivo)
   - alumnoValidadorService.validarRegistros(alumnos)
   - Guardar errores
   ↓
3. FASE 2: Comparar e Inactivar
   - comparadorAlumnosService.compararConBD(alumnos, semestre)
   - inactivacionService.inactivarEstudiantes(faltantes)
   ↓
4. FASE 3: Liberar Cupos
   - inactivacionService.liberarCupos(procesoId)
   ↓
5. FASE 4: Procesar Reingresos
   - reingresoService.procesarReingresos(alumnos, procesoId)
   ↓
6. FASE 5: Asignar Estudiantes
   - tutorSincronizacionService.recalcularCargaTodosLosTutores()
   - asignacionService.asignarAlumnos(alumnos, procesoId, semestre)
   ↓
7. Actualizar ProcesoAsignacion (COMPLETADO)
```

**Manejo de Errores**:
- Si cualquier fase falla: `ProcesoAsignacion.estado = FALLIDO`
- Se registra excepción en logs
- Usuario puede revisar errores via API

### AsignacionService
**Ubicación**: `impl/AsignacionServiceImpl.java`

**Responsabilidad**: Lógica central de asignación de estudiantes a tutores

#### Algoritmo de Asignación

```
Para cada estudiante en la lista:
   1. ¿Existe tutor con capacidad de su carrera?
      SI → Asignar al tutor con MENOR cargaActual
      NO → Ir a paso 2

   2. ¿Existe tutor con capacidad en carrera compatible?
      (según MatrizAfinidadCarrera, respetando prioridad)
      SI → Asignar al tutor con MENOR cargaActual
          → Generar alerta ASIGNACION_CRUZADA (WARNING)
      NO → Ir a paso 3

   3. ¿Existe tutor con capacidad en cualquier carrera?
      SI → Asignar al tutor con MENOR cargaActual
          → Generar alerta ASIGNACION_CRUZADA (WARNING)
      NO → Generar error: SIN_TUTOR_DISPONIBLE
```

**Ejemplo Detallado**:

```
Estudiante: Juan (Ingeniería en Sistemas)

PASO 1: Buscar en carrera "Ingeniería en Sistemas"
   Tutor A (Sistemas):        cargaActual=14/15  ← Elegible
   Tutor B (Sistemas):        cargaActual=15/15  (LLENO)
   Tutor C (Sistemas):        activo=false       (INACTIVO)

   ✓ Asignado a Tutor A (tiene menor carga)

Estudiante: María (Ingeniería en Sistemas)

PASO 1: Buscar en carrera "Ingeniería en Sistemas"
   Tutor A:        cargaActual=15/15  (LLENO - acabamos de asignar Juan)
   Tutor B:        cargaActual=15/15  (LLENO)
   Tutor C:        activo=false       (INACTIVO)

   ✗ No hay capacidad en carrera origen

PASO 2: Buscar en carreras compatibles
   MatrizAfinidadCarrera busca:
   Prioridad 1: "Ingeniería en Desarrollo de Software"
      Tutor D:     cargaActual=12/15  ← Elegible

   ✓ Asignado a Tutor D
   ⚠ Alerta: ASIGNACION_CRUZADA (WARNING)
     "Estudiante de Sistemas asignado a tutor de Desarrollo de Software"

Estudiante: Carlos (Ingeniería en Sistemas)

PASO 1 y 2: Sin opciones (todos llenos)

PASO 3: Buscar en cualquier carrera
   Tutor E (Informática):     cargaActual=8/15   ← Elegible (menor carga)
   Tutor F (Tecnología):      cargaActual=11/15

   ✓ Asignado a Tutor E
   ⚠ Alerta: ASIGNACION_CRUZADA (WARNING)
     "Asignación fallback: Sistemas → Informática"
```

**Batch Processing**:
- Procesa estudiantes en grupos de 100
- Evita problemas de memoria con datasets grandes
- Cada asignación individual usa `REQUIRES_NEW` transaction
- Permite que fallos individuales no impidan asignaciones posteriores

#### Métodos Principales

```java
public ResultadoAsignacion asignarAlumnos(
    List<AlumnoExcelDTO> alumnosValidos,
    Long procesoId,
    String semestreAcademico
)
```

**Retorna**:
```java
public class ResultadoAsignacion {
    int totalProcesados;
    int totalAsignados;
    int totalErrores;
    List<ErrorAsignacionDTO> errores;
    List<AlertaProceso> alertas;
    List<Asignacion> asignaciones;
    Map<String, Integer> estadisticas;
}
```

### TutorReasignacionService
**Ubicación**: `impl/TutorReasignacionServiceImpl.java`

**Responsabilidad**: Cambiar manualmente la asignación de un estudiante

#### Proceso de Reasignación

```
Entrada:
  - alumnoId
  - tutorOrigenId
  - tutorDestinoId
  - motivo
  - semestreAcademico

Validaciones:
  ✓ Alumno existe
  ✓ Ambos tutores existen y son diferentes
  ✓ Alumno está asignado a tutorOrigen
  ✓ Alumno puede reasignarse (contadorCambiosTutor < 2)
  ✓ TutorDestino tiene capacidad disponible

Cambios:
  1. tutorOrigen.cargaActual--
  2. tutorDestino.cargaActual++
  3. alumno.tutorActual = tutorDestino
  4. alumno.contadorCambiosTutor++
  5. Crear Asignacion con tipo=REASIGNACION
  6. Log en tabla de auditoría

Retorna: CambioTutorResponseDTO
```

**Regla de Negocio**: Máximo 2 reasignaciones por estudiante
- 1ª reasignación: permitida (contadorCambiosTutor = 1)
- 2ª reasignación: permitida (contadorCambiosTutor = 2)
- 3ª reasignación: rechazada

### TutorSincronizacionService
**Ubicación**: `impl/TutorSincronizacionServiceImpl.java`

**Responsabilidad**: Mantener sincronizado `cargaActual` con asignaciones reales

#### Problema que Resuelve
```
Escenario problemático:
1. Tutor A tiene cargaActual = 15
2. Se elimina un Alumno (por error o manualmente)
3. Base de datos ahora: 14 Asignaciones para Tutor A
4. Pero cargaActual sigue en 15 (DESINCRONIZADO)
5. Sistema no permite nuevas asignaciones (piensa que está lleno)
```

#### Solución
```java
public void recalcularCargaTutor(Long tutorId) {
    int cargaReal = asignacionRepository.countByTutorId(tutorId);
    tutor.setCargaActual(cargaReal);
    tutorRepository.save(tutor);
}

public void recalcularCargaTodosLosTutores() {
    for (Tutor tutor : tutorRepository.findAll()) {
        recalcularCargaTutor(tutor.getId());
    }
}
```

**Se ejecuta**:
- Al inicio de cada proceso de asignación
- Manualmente si se detecta inconsistencia
- Después de operaciones masivas

---

## Endpoints de API

### 1. Iniciar Proceso de Asignación

```http
POST /api/asignaciones/iniciar
Content-Type: multipart/form-data

archivo: <archivo Excel>
semestreAcademico: "2024-1"
usuario: "admin@universidad.edu"
```

**Respuesta (202 Accepted)**:
```json
{
    "data": {
        "procesoId": 123,
        "estado": "INICIADO",
        "mensaje": "Proceso iniciado correctamente",
        "timestamp": "2024-11-13T10:30:00"
    },
    "status": 202
}
```

**Errores Posibles**:
- `400`: Archivo vacío o formato inválido
- `400`: Semestre inválido
- `500`: Error al procesar archivo

### 2. Consultar Estado del Proceso

```http
GET /api/asignaciones/proceso/123
```

**Respuesta**:
```json
{
    "data": {
        "procesoId": 123,
        "estado": "ASIGNANDO",
        "progreso": {
            "porcentaje": 75,
            "alumnosProcesados": 1500,
            "alumnosAsignados": 1400,
            "errores": 50,
            "warnings": 75
        },
        "fechaInicio": "2024-11-13T10:30:00",
        "fechaFin": null,
        "tiempoTranscurridoMs": 180000
    }
}
```

**Estados Posibles en Respuesta**:
- `INICIADO` - Acaba de comenzar
- `COMPARANDO` - Comparando con BD
- `LIBERANDO_CUPOS` - Liberando capacidad
- `ASIGNANDO` - Asignando estudiantes
- `COMPLETADO` - Finalizó exitosamente
- `FALLIDO` - Hubo un error

### 3. Listar Alertas del Proceso

```http
GET /api/asignaciones/proceso/123/alertas?severidad=WARNING&resuelta=false
```

**Parámetros de Query**:
- `severidad`: CRITICO, ERROR, WARNING, INFO (opcional)
- `resuelta`: true/false (opcional)

**Respuesta**:
```json
{
    "data": [
        {
            "id": 1001,
            "tipo": "ASIGNACION_CRUZADA",
            "severidad": "WARNING",
            "descripcion": "Estudiante de Sistemas asignado a tutor de Desarrollo",
            "alumno": {
                "id": 500,
                "matricula": "2024-0001",
                "nombre": "Juan Pérez"
            },
            "tutor": {
                "id": 30,
                "nombre": "Dra. María García",
                "carrera": "Ing. Desarrollo de Software"
            },
            "resuelta": false,
            "fechaCreacion": "2024-11-13T10:35:00"
        }
    ]
}
```

**Tipos de Alerta**:
- `ASIGNACION_CRUZADA`: Estudiante asignado a tutor de carrera diferente
- `CAPACIDAD_EXCEDIDA`: Tutor alcanzó capacidad máxima
- `SIN_TUTOR_DISPONIBLE`: No hay tutores disponibles
- `ERROR_FORMATO`: Datos con formato inválido

### 4. Listar Todos los Procesos

```http
GET /api/asignaciones/procesos
```

**Respuesta**:
```json
{
    "data": [
        {
            "procesoId": 123,
            "estado": "COMPLETADO",
            "totalAlumnosProcesados": 2000,
            "totalAlumnosAsignados": 1950,
            "totalErrores": 20,
            "totalWarnings": 75,
            "archivoOrigen": "estudiantes_2024-1.xlsx",
            "fechaInicio": "2024-11-13T10:30:00",
            "fechaFin": "2024-11-13T11:15:00",
            "usuarioEjecutor": "admin@universidad.edu"
        }
    ]
}
```

### 5. Cambiar Tutor de Estudiante

```http
POST /api/asignaciones/cambio-tutor
Content-Type: application/json

{
    "alumnoId": 500,
    "tutorOrigenId": 30,
    "tutorDestinoId": 35,
    "motivo": "Cambio solicitado por estudiante",
    "usuario": "admin@universidad.edu",
    "semestreAcademico": "2024-1"
}
```

**Respuesta**:
```json
{
    "data": {
        "alumnoId": 500,
        "tutorAnteriorId": 30,
        "tutorNuevoId": 35,
        "tutorAnteriorNombre": "Dra. María García",
        "tutorNuevoNombre": "Dr. Carlos López",
        "fechaCambio": "2024-11-13T12:00:00",
        "motivo": "Cambio solicitado por estudiante",
        "semestreAcademico": "2024-1"
    }
}
```

**Errores Posibles**:
- `404`: Alumno, tutor origen o tutor destino no encontrado
- `400`: Alumno no está asignado a tutor origen
- `400`: Tutor destino sin capacidad
- `400`: Estudiante ha alcanzado límite de reasignaciones (2)
- `400`: Tutores origen y destino son iguales

---

## Reglas de Negocio

### 1. Capacidad de Tutores

```
Cada tutor tiene:
  - capacidadMax: Número máximo de estudiantes
  - cargaActual: Número actual de asignaciones

Puede aceptar nuevas asignaciones si:
  - activo == true  AND
  - cargaActual < capacidadMax
```

### 2. Unicidad de Asignaciones

```
No puede existir:
  - Dos asignaciones del mismo alumno a mismo tutor
  - En el mismo semestre académico

Constraint: UNIQUE (alumno_id, tutor_id, semestre_academico)
```

### 3. Límite de Reasignaciones

```
Un estudiante puede cambiar de tutor máximo 2 veces por:
  - Decisión del estudiante
  - Redistribución administrativo
  - Cambio de programa

Después de 2 reasignaciones:
  - No se permite cambio adicional
  - Debe escalar a administrador
```

### 4. Estados del Alumno

```
ACTIVO:
  - Inscrito en semestre actual
  - Puede ser asignado a tutor
  - Aparece en reportes activos

INACTIVO:
  - No inscrito o retirado
  - Libera cupo del tutor actual
  - No recibe nuevas asignaciones
  - Aparece en reportes inactivos
```

### 5. Prioridad de Asignación

```
Orden de preferencia (ver Algoritmo de Asignación):
  1. Tutor misma carrera (menor carga)
  2. Tutor carrera compatible (menor carga)
  3. Tutor cualquier carrera (menor carga)

En caso de empate de carga:
  - Seleccionar aleatoriamente entre empatados
  - Evita concentración en mismo tutor
```

### 6. Sincronización de Cargas

```
cargaActual debe reflejar siempre:
  - Número real de Asignaciones activas
  - Para ese tutor
  - En ese semestre

Si se detecta desincronización:
  - Ejecutar recalcularCargaTutor()
  - Validar integridad
```

---

## Flujos Detallados

### Flujo 1: Asignación Inicial Completa

**Escenario**: Nuevo semestre, todos los estudiantes necesitan asignación

**Precondiciones**:
- Base de datos tiene tutores con capacidad
- Archivo Excel con lista de nuevos estudiantes
- MatrizAfinidadCarrera está configurada

**Pasos**:

1. **Subir archivo**
   ```
   POST /api/asignaciones/iniciar
   archivo: estudiantes_2024-1.xlsx
   semestreAcademico: "2024-1"
   ```

2. **Sistema valida**
   - Lectura correcta del Excel
   - 2000 estudiantes válidos
   - 10 estudiantes con errores de formato → Registrados

3. **Sistema compara con BD**
   - Encuentra 500 estudiantes que estaban activos en 2024-2
   - Pero NO aparecen en nuevo Excel de 2024-1
   - → Se marcan como INACTIVO

4. **Sistema libera cupos**
   - Tutor A tenía 4 estudiantes inactivados
   - cargaActual: 15 → 11
   - Ahora tiene 4 cupos disponibles

5. **Sistema procesa reingresos**
   - Encuentra 5 estudiantes INACTIVO que reaparecen
   - Los marca como ACTIVO

6. **Sistema asigna**
   - Sincroniza todas las cargas
   - Para cada estudiante:
     - Tutor mismo programa: ✓ asignado a Tutor A (carga=12)
     - Tutor compatibles: ✓ asignado a Tutor D (carga=13) + ALERTA
     - Fallback: ✓ asignado a Tutor E (carga=9) + ALERTA

7. **Usuario verifica**
   ```
   GET /api/asignaciones/proceso/123
   → COMPLETADO - 1990 asignados, 10 errores

   GET /api/asignaciones/proceso/123/alertas?severidad=WARNING
   → 45 alertas ASIGNACION_CRUZADA para revisar
   ```

---

### Flujo 2: Reasignación Manual por Solicitud

**Escenario**: Estudiante solicita cambio de tutor

**Precondiciones**:
- Estudiante tiene tutor actual
- Tutor destino tiene capacidad
- Estudiante no ha agotado límite de reasignaciones

**Pasos**:

1. **Usuario solicita cambio**
   ```
   POST /api/asignaciones/cambio-tutor
   {
     "alumnoId": 500,
     "tutorOrigenId": 30,
     "tutorDestinoId": 35,
     "motivo": "Horario incompatible",
     "semestreAcademico": "2024-1"
   }
   ```

2. **Sistema valida**
   - Alumno 500 existe: ✓
   - Tutor 30 existe: ✓
   - Tutor 35 existe: ✓
   - Alumno asignado a Tutor 30: ✓
   - Tutor 35 tiene capacidad (cargaActual=12/15): ✓
   - Alumno puede reasignarse (contador=1 < 2): ✓

3. **Sistema realiza cambio**
   - Tutor 30: cargaActual 15 → 14
   - Tutor 35: cargaActual 12 → 13
   - Alumno 500: tutorActual = Tutor 35
   - Alumno 500: contadorCambiosTutor = 2
   - Crear Asignacion(tipo=REASIGNACION)

4. **Sistema responde**
   ```json
   {
     "alumnoId": 500,
     "tutorAnteriorNombre": "Dra. María",
     "tutorNuevoNombre": "Dr. Carlos",
     "fechaCambio": "2024-11-13T12:00:00"
   }
   ```

5. **Resultado**
   - Cambio se registra en histórico
   - Cargas se actualizan
   - Si hay un 3er intento:
     ```
     POST /api/asignaciones/cambio-tutor
     → 400 Bad Request
     "El estudiante ha alcanzado límite de reasignaciones (2)"
     ```

---

### Flujo 3: Manejo de Estudiante sin Tutor Disponible

**Escenario**: Todos los tutores están llenos, no hay capacidad

**Precondiciones**:
- Estudiante necesita asignación
- Todos los tutores de su carrera: cargaActual = capacidadMax
- Todos los tutores de carreras compatibles: LLENOS
- Todos los tutores en todo el sistema: LLENOS

**Pasos**:

1. **Sistema intenta asignar**
   ```
   Alumno: Juan (Ingeniería en Sistemas)

   Paso 1: Buscar Tutores Sistemas
   → Todos llenos (cargaActual = capacidadMax)

   Paso 2: Buscar Tutores compatibles
   → Desarrollo: LLENO
   → Informática: LLENO
   → Tecnología: LLENO

   Paso 3: Buscar cualquier tutor
   → TODO el sistema sin capacidad

   RESULTADO: ERROR
   ```

2. **Sistema registra error**
   ```java
   ErrorAsignacion {
       filaExcel: 50,
       matricula: "2024-0050",
       nombreAlumno: "Juan Pérez",
       tipoError: "SIN_TUTOR_DISPONIBLE",
       mensajeError: "No hay tutores con capacidad disponible"
   }
   ```

3. **Alerta se crea**
   ```java
   AlertaProceso {
       tipo: "SIN_TUTOR_DISPONIBLE",
       severidad: "CRITICO",
       descripcion: "No se pudo asignar Juan (Sistemas) - sin tutores disponibles",
       alumno: Juan,
       resuelta: false
   }
   ```

4. **Usuario debe resolver**
   ```
   Opciones:
   a) Aumentar capacidadMax de algún tutor
   b) Activar tutor inactivo
   c) Contratar nuevo tutor
   d) Retrasar asignación del estudiante

   Una vez resuelto:
   - GET /api/asignaciones/proceso/123/alertas
   - Ver alertas CRITICO sin resolver
   - Marcar como resuelta manualmente
   - Reintentar asignación
   ```

---

### Flujo 4: Inactivación de Estudiantes

**Escenario**: Semestre anterior tuvo 2000 estudiantes, nuevo semestre tiene 1800

**Precondiciones**:
- Archivo nuevo Excel con 1800 estudiantes
- 200 estudiantes del semestre anterior no aparecen

**Pasos**:

1. **Sistema compara**
   ```
   Semestre anterior: 2000 estudiantes ACTIVO
   Nuevo Excel:      1800 estudiantes

   Diferencia: 200 estudiantes no reinscriptos
   ```

2. **Sistema inactiva**
   ```
   Para cada uno de los 200:
     - Alumno.estado = INACTIVO
     - Liberar cupo de tutor

   Ejemplo:
     Alumno Juan:
     - Estado: ACTIVO → INACTIVO
     - Tutor A: cargaActual 15 → 14
   ```

3. **Impacto en tutores**
   ```
   ANTES (Semestre anterior):
   Tutor A: cargaActual = 15/15 (LLENO)
   Tutor B: cargaActual = 15/15 (LLENO)

   DESPUÉS (Inactivación):
   Tutor A: cargaActual = 12/15 (3 cupos disponibles)
   Tutor B: cargaActual = 14/15 (1 cupo disponible)

   Total disponible: 4 cupos para nuevos estudiantes
   ```

4. **Procesamiento de reingresos**
   ```
   De los 200 inactivados:
   - 50 reaparecen en nuevo Excel
   - Se marcan como ACTIVO
   - Se les asigna tutor nuevamente

   - 150 no reaparecen
   - Quedan INACTIVO en el sistema
   ```

---

## Manejo de Excepciones

### Excepciones de Negocio

#### 1. DuplicadoException
```
Causa: Intento de crear asignación duplicada

Escenario:
  alumno.id = 100
  tutor.id = 50
  semestre = "2024-1"

  Ya existe: Asignacion(alumno=100, tutor=50, semestre="2024-1")

Manejo:
  - Registrar en ErrorAsignacion
  - Continuar con siguiente estudiante (no paralizar)
  - Generar alerta informativa
```

#### 2. CapacidadExcedidaException
```
Causa: Tutor alcanzó capacidad máxima

Escenario:
  Tutor A: cargaActual = 15, capacidadMax = 15
  Sistema intenta: asignarAlumno(tutor=A)

Manejo:
  - No crear asignación
  - Intentar con siguiente tutor
  - Si no hay opciones: SinTutorDisponibleException
```

#### 3. SinTutorDisponibleException
```
Causa: No hay tutor con capacidad en ninguna opción

Escenario:
  Alumno carrera X
  Paso 1: Tutores carrera X → todos llenos
  Paso 2: Tutores carrera compatible → todos llenos
  Paso 3: Cualquier tutor → todos llenos

Manejo:
  - Registrar como error CRITICO
  - Crear alerta no resuelta
  - Continuar con siguientes alumnos
  - Usuario debe resolver manualmente
```

### Excepciones de Validación

#### 1. MatriculaInvalidaException
```
Causa: Campo matricula vacío, nulo o inválido

Validaciones:
  - No null
  - No vacío
  - Formato: Debe existir en base de datos

Manejo:
  - Registrar en ErrorValidacion
  - Indicar fila del Excel
  - Saltar alumno
```

#### 2. CampoVacioException
```
Causa: Campo obligatorio está vacío

Campos obligatorios:
  - nombre (no null, no vacío)
  - carrera (no null, no vacío)
  - semestre (no null, numérico)

Manejo:
  - Registrar en ErrorValidacion
  - Especificar qué campo falta
```

#### 3. CarreraInvalidaException
```
Causa: Carrera no existe en sistema

Validaciones:
  - carrera != null
  - carrera debe existir en tabla de carreras

Manejo:
  - Registrar en ErrorValidacion
  - Sugerir carreras similares si aplica
```

### Excepciones de Reasignación

#### 1. AlumnoNoTieneCapacidadException
```
Causa: Alumno agotó límite de reasignaciones

Escenario:
  Alumno.contadorCambiosTutor = 2
  Usuario intenta: reasignarTutor()

Manejo:
  - Lanzar excepción
  - Mensaje: "El estudiante ha alcanzado límite de reasignaciones (2)"
  - HTTP 400 Bad Request
```

#### 2. TutorSinCapacidadException
```
Causa: Tutor destino no tiene cupo disponible

Escenario:
  Tutor destino: cargaActual = 15, capacidadMax = 15

Manejo:
  - Validar antes de cambio
  - HTTP 400 Bad Request
  - Mensaje: "Tutor destino no tiene capacidad disponible"
```

---

## Ejemplos Prácticos

### Ejemplo 1: Asignación Simple

**Datos de entrada** (Excel):
```
Matricula  Nombre          Carrera                    Semestre
2024-0001  Juan Pérez      Ing. Sistemas              1
2024-0002  María García    Ing. Sistemas              1
2024-0003  Carlos López    Ing. Desarrollo Software  1
```

**Estado inicial de tutores**:
```
Tutor A (Ing. Sistemas):            cargaActual = 10/15
Tutor B (Ing. Sistemas):            cargaActual = 14/15
Tutor D (Ing. Desarrollo):          cargaActual = 8/15
```

**Asignación**:
```
Juan Pérez (Sistemas):
  → Opción 1: Tutor A (10/15) vs Tutor B (14/15)
  → Elige Tutor A (menor carga)
  → Asignación exitosa
  → Tutor A ahora: 11/15

María García (Sistemas):
  → Opción 1: Tutor A (11/15) vs Tutor B (14/15)
  → Elige Tutor A (menor carga)
  → Asignación exitosa
  → Tutor A ahora: 12/15

Carlos López (Desarrollo):
  → Opción 1: Tutor D (8/15)
  → Elige Tutor D (disponible)
  → Asignación exitosa
  → Tutor D ahora: 9/15
```

**Resultado**:
```
Total procesados: 3
Total asignados: 3
Errores: 0
Alertas: 0
```

---

### Ejemplo 2: Asignación con Alerta de Carrera Cruzada

**Datos de entrada** (Excel):
```
Matricula  Nombre      Carrera                    Semestre
2024-0010  Pedro Mora  Ing. Desarrollo Software  1
```

**Estado inicial de tutores**:
```
Tutores Ing. Desarrollo: TODOS LLENOS
Tutores Ing. Sistemas: 2/15 disponible
Tutores Informática: 3/15 disponible
Tutores Tecnología: TODOS LLENOS

MatrizAfinidadCarrera:
  Desarrollo → (1) Sistemas, (2) Informática
```

**Asignación**:
```
Pedro Mora (Desarrollo):
  → Paso 1: Buscar Desarrollo
    • Todos los tutores: LLENOS
  → Paso 2: Buscar compatible
    • Prioridad 1: Sistemas (2/15) → DISPONIBLE
    • Elige Tutor C (Sistemas)
  → Asignación exitosa
  → Generar ALERTA: ASIGNACION_CRUZADA (WARNING)
    "Estudiante de Desarrollo asignado a tutor de Sistemas"

Alerta registrada:
{
  tipo: "ASIGNACION_CRUZADA",
  severidad: "WARNING",
  descripcion: "Desarrollo → Sistemas",
  alumno: Pedro Mora,
  tutor: Tutor C (Sistemas),
  resuelta: false
}
```

**Usuario puede**:
- Revisar alertas vía API
- Aumentar capacidad de Tutores Desarrollo
- Marcar alerta como resuelta si es aceptable

---

### Ejemplo 3: Manejo de Inactivaciones

**Semestre anterior 2024-2**:
```
Total estudiantes ACTIVO: 1000
```

**Nuevo semestre 2024-1**:
```
Excel nuevo: 950 estudiantes
Estudiantes inactivados: 50
```

**Estudiantes que se inactivan**:
```
Alumno ID 100: ACTIVO → INACTIVO
  Tutor actual: Tutor A
  Tutor A: cargaActual 15 → 14

Alumno ID 101: ACTIVO → INACTIVO
  Tutor actual: Tutor A
  Tutor A: cargaActual 14 → 13

Alumno ID 102: ACTIVO → INACTIVO
  Tutor actual: Tutor B
  Tutor B: cargaActual 15 → 14

...

(Total 50 inactivaciones)
```

**Impacto total**:
```
ANTES:
Tutor A: 15/15 (LLENO)
Tutor B: 15/15 (LLENO)
Tutor C: 14/15 (1 cupo)

DESPUÉS:
Tutor A: 13/15 (2 cupos liberados)
Tutor B: 14/15 (1 cupo liberado)
Tutor C: 14/15 (sin cambio)

Total capacidad liberada: 3 cupos
```

**Reingresos**:
```
De los 50 inactivados:
- Alumno ID 100 reaparece en nuevo Excel
  → INACTIVO → ACTIVO
  → Asignado nuevamente

- Alumnos ID 101-150: NO reaparecen
  → Permanecen INACTIVO
  → Requieren reactivación manual si regresan después
```

---

## Conclusión

El sistema de asignación proporciona:

✅ **Automatización**: Proceso completo sin intervención manual
✅ **Inteligencia**: Selección de tutores basada en reglas de negocio
✅ **Rastreabilidad**: Registro completo de todas las operaciones
✅ **Flexibilidad**: Reasignaciones manuales cuando es necesario
✅ **Robustez**: Manejo de errores y casos especiales
✅ **Escalabilidad**: Procesamiento de miles de estudiantes eficientemente

Este documento proporciona la base para entender, mantener y extender el sistema de asignaciones.