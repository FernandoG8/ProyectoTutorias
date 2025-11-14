# FASE 1: Implementación - Migración de Semestres

## Resumen Ejecutivo

Se ha completado la migración de un sistema de gestión de tutorías que utilizaba `semestre_academico` como VARCHAR (texto libre) a una estructura relacional con tabla maestra `semestres` con claves foráneas y validaciones.

**Tiempo de implementación:** ~45 minutos
**Archivos creados:** 4
**Archivos modificados:** 5
**Lineas de código:** ~1,200

---

## 1. Entidad `Semestre.java`

**Ubicación:** `backend/src/main/java/com/universidad/tutorias/domain/entity/Semestre.java`

### Campos principales:
```java
@Id
private Long id;                          // Auto-increment

@NotBlank @Pattern("^\\d{4}-\\d{4}-F[12]$")
@Column(nullable = false, unique = true)
private String codigo;                    // ej: "2025-2026-F1"

@NotBlank
@Column(nullable = false)
private String nombre;                    // ej: "Semestre Agosto 2025 - Enero 2026"

@NotNull
@Column(nullable = false)
private LocalDate fechaInicio;            // Fecha de inicio

@NotNull
@Column(nullable = false)
private LocalDate fechaFin;               // Fecha de fin

@Column(nullable = false)
private Boolean activo;                   // ¿Semestre activo?

@Column(nullable = false, updatable = false)
private LocalDateTime fechaCreacion;      // Timestamp de creación
```

### Relaciones:
```java
@OneToMany(mappedBy = "semestre", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Asignacion> asignaciones;

@OneToMany(mappedBy = "semestre")
private List<ProcesoAsignacion> procesos;
```

### Validaciones:
- **@PrePersist/@PreUpdate:** Valida que `fechaFin > fechaInicio`
- **@Pattern:** Código debe estar en formato `YYYY-YYYY-FN` (ej: 2025-2026-F1)
- **CONSTRAINT chk_semestre_fechas:** Validación a nivel de base de datos

### Métodos útiles:
```java
public boolean estaActivo()           // Retorna true si activo == true
public boolean estaVigente()          // Retorna true si hoy está entre inicio y fin
public Integer getTotalAsignaciones() // Retorna cantidad de asignaciones
```

### Índices:
```sql
INDEX idx_semestre_activo (activo)
INDEX idx_semestre_codigo (codigo)
INDEX idx_semestre_fechas (fecha_inicio, fecha_fin)
```

---

## 2. Modificación de `Asignacion.java`

### Antes:
```java
@Column(name = "semestre_academico", length = 20)
private String semestreAcademico;
```

### Después:
```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "id_semestre", nullable = false,
            foreignKey = @ForeignKey(name = "fk_asignacion_semestre"))
@JsonIgnoreProperties({"asignaciones", "procesos"})
private Semestre semestre;

@Column(name = "semestre_academico", length = 20)
@Deprecated(forRemoval = false, since = "2.0")
private String semestreAcademico;  // Mantenido para compatibilidad
```

### Constraint actualizado:
```java
@UniqueConstraint(name = "uk_asignacion_alumno_tutor_semestre",
                  columnNames = {"id_alumno", "id_tutor", "id_semestre"})
```

### Compatibilidad JSON:
```java
@JsonProperty("semestreAcademico")
public String getSemestreCodigoLegacy() {
    return semestre != null ? semestre.getCodigo() : null;
}
```

**Resultado:** El frontend sigue recibiendo `semestreAcademico` en JSON, pero los datos vienen de `semestre.codigo`.

---

## 3. Modificación de `ProcesoAsignacion.java`

### Agregado:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "id_semestre",
            foreignKey = @ForeignKey(name = "fk_proceso_semestre"))
@JsonIgnoreProperties({"asignaciones", "procesos"})
private Semestre semestre;
```

### Métodos legacy:
```java
@JsonProperty("semestreAcademico")
public String getSemestreCodigoLegacy() {
    return semestre != null ? semestre.getCodigo() : null;
}

public String getSemestreNombre() {
    return semestre != null ? semestre.getNombre() : null;
}
```

---

## 4. `SemestreRepository.java`

**Ubicación:** `backend/src/main/java/com/universidad/tutorias/domain/repository/SemestreRepository.java`

### Métodos principales:
```java
Optional<Semestre> findByCodigo(String codigo);
Optional<Semestre> findByActivoTrue();
List<Semestre> findAllByOrderByFechaInicioDesc();
List<Semestre> findTop5ByOrderByFechaInicioDesc();
boolean existsByCodigo(String codigo);

@Query("SELECT s FROM Semestre s WHERE s.fechaInicio <= :fecha AND s.fechaFin >= :fecha")
Optional<Semestre> findByFechaVigente(@Param("fecha") LocalDate fecha);

@Query("SELECT s FROM Semestre s WHERE YEAR(s.fechaInicio) = :anio")
List<Semestre> findByAnio(@Param("anio") Integer anio);

@Query("SELECT COUNT(a) FROM Asignacion a WHERE a.semestre.id = :semestreId")
Long countAsignacionesBySemestreId(@Param("semestreId") Long semestreId);

@Modifying
@Query("UPDATE Semestre s SET s.activo = false WHERE s.activo = true")
void desactivarTodos();
```

---

## 5. Actualización de `AsignacionRepository.java`

### Nuevos métodos (con Long semestreId):
```java
List<Asignacion> findByTutorAndSemestre(Long tutorId, Long semestreId);
List<Asignacion> findByCarreraAndSemestre(String carrera, Long semestreId);
List<Asignacion> findBySemestreId(Long semestreId);
Long countBySemestreId(Long semestreId);
Long countActivosBySemestreId(Long semestreId);
List<Asignacion> findBySemestreIdAndTutorId(Long semestreId, Long tutorId);
boolean existsByAlumnoAndTutorAndSemestreId(Long alumnoId, Long tutorId, Long semestreId);
```

### Métodos legacy (para compatibilidad):
```java
List<Asignacion> findByTutorAndSemestreString(Long tutorId, String semestre);
List<Asignacion> findByCarreraAndSemestreString(String carrera, String semestre);
List<Asignacion> findBySemestreString(String semestre);
List<String> findCarrerasDisponibles(String semestre);
boolean existsByAlumnoAndTutorAndSemestre(Long alumnoId, Long tutorId, String semestre);

@Deprecated
List<String> findDistinctSemestreAcademico();
```

---

## 6. Script de Migración Flyway V2

**Ubicación:** `backend/src/main/resources/db/migration/V2__crear_tabla_semestre.sql`

### Pasos principales:

**1. Crear tabla `semestres`:**
```sql
CREATE TABLE IF NOT EXISTS semestres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(15) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_semestre_fechas CHECK (fecha_fin > fecha_inicio),
    INDEX idx_semestre_activo (activo),
    INDEX idx_semestre_codigo (codigo),
    INDEX idx_semestre_fechas (fecha_inicio, fecha_fin)
)
```

**2. Agregar columna `id_semestre` a `asignaciones`:**
```sql
ALTER TABLE asignaciones
ADD COLUMN id_semestre BIGINT NULL AFTER id,
ADD INDEX idx_asignacion_semestre (id_semestre);
```

**3. Migrar datos:**
```sql
INSERT IGNORE INTO semestres (codigo, nombre, fecha_inicio, fecha_fin, activo)
SELECT DISTINCT
    a.semestre_academico AS codigo,
    CONCAT('Semestre ', ...) AS nombre,
    DATE(...) AS fecha_inicio,
    DATE(...) AS fecha_fin,
    (a.semestre_academico = (SELECT MAX(...))) AS activo
FROM asignaciones a
WHERE a.semestre_academico IS NOT NULL
```

**4. Poblar `id_semestre`:**
```sql
UPDATE asignaciones a
INNER JOIN semestres s ON a.semestre_academico = s.codigo
SET a.id_semestre = s.id
WHERE a.semestre_academico IS NOT NULL;
```

**5. Hacer `id_semestre` NOT NULL y agregar FK:**
```sql
ALTER TABLE asignaciones
    MODIFY COLUMN id_semestre BIGINT NOT NULL,
    ADD CONSTRAINT fk_asignacion_semestre
        FOREIGN KEY (id_semestre)
        REFERENCES semestres(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE;
```

**6. Actualizar constraint de unicidad:**
```sql
ALTER TABLE asignaciones
    DROP INDEX IF EXISTS uk_asignacion_unica;

ALTER TABLE asignaciones
    ADD CONSTRAINT uk_asignacion_alumno_tutor_semestre
        UNIQUE (id_alumno, id_tutor, id_semestre);
```

**7. Agregar soporte en `procesos_asignacion`:**
```sql
ALTER TABLE procesos_asignacion
ADD COLUMN id_semestre BIGINT NULL,
ADD INDEX idx_proceso_semestre (id_semestre),
ADD CONSTRAINT fk_proceso_semestre
    FOREIGN KEY (id_semestre)
    REFERENCES semestres(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE;
```

---

## 7. Correcciones de Compatibilidad

### ReporteConsultaService.java

**Cambio:**
```java
// Antes:
return asignacionRepository.findByTutorAndSemestre(tutorId, periodo);
return asignacionRepository.findByCarreraAndSemestre(carrera, periodo);

// Después:
return asignacionRepository.findByTutorAndSemestreString(tutorId, periodo);
return asignacionRepository.findByCarreraAndSemestreString(carrera, periodo);
```

**Razón:** Los métodos nuevos aceptan Long, pero los servicios aún trabajan con String `periodo`.

### ReporteServiceImpl.java

**Cambio similar:** Se actualizaron las llamadas para usar los métodos `String` por compatibilidad.

---

## 8. Tests Unitarios

**Ubicación:** `backend/src/test/java/com/universidad/tutorias/domain/entity/SemestreTest.java`

### Test cases (10 tests):
1. `crearSemestre_ConDatosValidos_DebeCrearCorrectamente()` ✅
2. `validarFechas_ConFechaFinAnterior_DebeLanzarExcepcion()` ✅
3. `estaVigente_ConFechaActualDentroDelRango_DebeRetornarTrue()` ✅
4. `estaVigente_ConFechaActualAnteriorAlRango_DebeRetornarFalse()` ✅
5. `estaVigente_ConFechaActualPosteriorAlRango_DebeRetornarFalse()` ✅
6. `estaVigente_ConFechasNulas_DebeRetornarFalse()` ✅
7. `estaActivo_ConActivoTrue_DebeRetornarTrue()` ✅
8. `estaActivo_ConActivoFalse_DebeRetornarFalse()` ✅
9. `getTotalAsignaciones_ConAsignacionesVacias_DebeRetornarCero()` ✅
10. `toString_ConDatosCompletos_DebeContenerInformacionRelevante()` ✅

---

## Diagrama de Cambios

```
ANTES:
┌────────────────┐
│  asignaciones  │
├────────────────┤
│ id_alumno   FK │
│ id_tutor    FK │
│ semestre_ac  ← VARCHAR(texto libre)
│ tipo          │
│ fecha_asig    │
└────────────────┘

DESPUÉS:
┌────────────────┐      ┌─────────────────┐
│  asignaciones  │      │   semestres     │
├────────────────┤      ├─────────────────┤
│ id_alumno   FK │      │ id           PK │
│ id_tutor    FK │      │ codigo    UNIQUE│
│ id_semestre FK────────│ nombre          │
│ tipo          │      │ fecha_inicio    │
│ fecha_asig    │      │ fecha_fin       │
│ semestre_ac ✓ │ dep  │ activo          │
└────────────────┘      │ fecha_creacion  │
      ↓ CASCADE          └─────────────────┘
   orphan removal        ↑
                     ON DELETE CASCADE
```

---

## Ventajas de la Migración

### Antes (VARCHAR):
- ❌ Inconsistencias de formato ("2025-F2" vs "2025-f2" vs "2025F2")
- ❌ Sin validación de integridad referencial
- ❌ Imposible eliminar periodos de forma controlada
- ❌ Dificultad para calcular estadísticas por periodo
- ❌ Dashboard muestra datos de múltiples semestres mezclados

### Después (Relación):
- ✅ Formato controlado con UNIQUE constraint
- ✅ Integridad referencial con foreign key
- ✅ Eliminación en cascada (DELETE CASCADE)
- ✅ Estadísticas precisas con GROUP BY semestre_id
- ✅ Control de datos activos/inactivos por semestre
- ✅ Validaciones de fecha a nivel de entidad
- ✅ Métodos helper para negocio (estaActivo(), estaVigente())

---

## Compatibilidad Mantenida

```java
// Legacy: Servicios antiguos siguen funcionando
asignacionRepository.findBySemestreString("2025-2026-F1");

// Nuevo: Servicios modernos usan IDs
asignacionRepository.findBySemestreId(1L);

// JSON: Frontend sigue recibiendo semestreAcademico
{
  "semestre": { "id": 1, "codigo": "2025-2026-F1", ... },
  "semestreAcademico": "2025-2026-F1"  // @JsonProperty
}
```

---

## Checklist de Validación Post-Implementación

- [x] Entidad Semestre creada con validaciones
- [x] Relaciones ManyToOne agregadas a Asignacion y ProcesoAsignacion
- [x] SemestreRepository implementado
- [x] AsignacionRepository actualizado (dual signatures)
- [x] Script de migración Flyway V2 creado
- [x] Tests unitarios para Semestre
- [x] Compilación sin errores: `BUILD SUCCESS`
- [x] Compatibilidad hacia atrás mantenida
- [x] Servicios existentes actualizados
- [x] Índices de base de datos optimizados
- [x] Validaciones a nivel de entidad y BD
- [x] Documentación completa

---

## Próximas Fases

**FASE 2: Servicios de Negocio**
- [ ] Crear `SemestreService` (CRUD)
- [ ] Implementar validaciones de negocio
- [ ] Crear endpoints REST (`@RestController`)
- [ ] Actualizar servicios existentes

**FASE 3: API REST**
- [ ] Endpoints de semestres (GET, POST, PUT, DELETE)
- [ ] Filtros y búsqueda avanzada
- [ ] Documentación Swagger
- [ ] DTOs para request/response

**FASE 4: Frontend (React)**
- [ ] Actualizar componentes para usar IDs de semestre
- [ ] Crear selector de semestres
- [ ] Formularios de gestión de semestres
- [ ] Validaciones en frontend

---

## Notas Técnicas

### Convenciones utilizadas:
- **Nombres de tabla:** Plural en español (`semestres`)
- **Nombres de columna:** Snake_case (`id_semestre`, `fecha_inicio`)
- **Constraint de unicidad:** Prefijo `uk_`
- **Foreign key:** Prefijo `fk_`
- **Índices:** Prefijo `idx_`

### Standards aplicados:
- ✅ Jakarta Persistence (javax → jakarta)
- ✅ Lombok para reducir boilerplate
- ✅ Bean Validation (@NotNull, @Pattern, etc.)
- ✅ Lazy loading en relaciones
- ✅ Cascade delete para integridad referencial
- ✅ JSON serialization con @JsonProperty

---

**Documento de Implementación - FASE 1**
Fecha: 2025-11-14
Desarrollador: Claude Code
Estado: ✅ COMPLETADO
