# FASE 1: Validación de Migración de Base de Datos

## Estado: ✅ COMPLETADA

La Fase 1 de migración de semestres se ha implementado exitosamente. Aquí puedes validar que todo funciona correctamente.

---

## Paso 1: Preparar la Base de Datos

Si tienes datos de prueba antiguos, reinicia la base de datos:

```bash
# Conectar a MySQL
mysql -u root -p

# En MySQL:
DROP DATABASE IF EXISTS gestion_tutorias;
CREATE DATABASE gestion_tutorias CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

---

## Paso 2: Compilar el Proyecto

```bash
cd backend/
./mvnw clean compile -DskipTests
```

**Resultado esperado:** `BUILD SUCCESS`

---

## Paso 3: Iniciar la Aplicación

```bash
./mvnw spring-boot:run
```

**Qué sucede:**
1. Spring Boot inicia
2. Flyway ejecuta automáticamente `V2__crear_tabla_semestre.sql`
3. Se crea tabla `semestres`
4. Se migran datos de `semestre_academico` a `id_semestre` en `asignaciones`
5. Se crean semestres de prueba

**Resultado esperado:** Aplicación arranca sin errores en `http://localhost:8080`

---

## Paso 4: Validar la Base de Datos

```bash
mysql -u root -p gestion_tutorias
```

### Query 1: Ver tabla de semestres creada
```sql
SELECT * FROM semestres ORDER BY fecha_inicio DESC;
```

**Resultado esperado:**
```
id | codigo           | nombre                              | activo
1  | 2025-2026-F1    | Semestre Agosto 2025 - Enero 2026   | 1
...
```

### Query 2: Verificar que NO hay asignaciones sin semestre_id
```sql
SELECT COUNT(*) FROM asignaciones WHERE id_semestre IS NULL;
```

**Resultado esperado:** `0`

### Query 3: Contar asignaciones por semestre
```sql
SELECT
    s.codigo,
    s.nombre,
    COUNT(a.id) as total_asignaciones
FROM semestres s
LEFT JOIN asignaciones a ON s.id = a.id_semestre
GROUP BY s.id
ORDER BY s.fecha_inicio DESC;
```

**Resultado esperado:** Muestra semestres con sus asignaciones

### Query 4: Verificar foreign key
```sql
SHOW INDEXES FROM asignaciones WHERE Key_name = 'uk_asignacion_alumno_tutor_semestre';
```

**Resultado esperado:** Muestra el índice único en (id_alumno, id_tutor, id_semestre)

---

## Paso 5: Ejecutar Tests

```bash
./mvnw test -Dtest=SemestreTest
```

**Resultado esperado:**
```
[INFO] Tests run: 10, Failures: 0, Errors: 0
[INFO] BUILD SUCCESS
```

---

## Paso 6: Validar API

Si todo compiló y se ejecutó correctamente, prueba los endpoints:

### Obtener una asignación (para verificar que incluye semestre)
```bash
curl -X GET http://localhost:8080/api/asignaciones/1
```

**Respuesta esperada (JSON):**
```json
{
  "id": 1,
  "alumno": { "id": 1, ... },
  "tutor": { "id": 1, ... },
  "semestre": {
    "id": 1,
    "codigo": "2025-2026-F1",
    "nombre": "Semestre Agosto 2025 - Enero 2026",
    "activo": true,
    ...
  },
  "semestreAcademico": "2025-2026-F1",  // Legacy property
  "tipoAsignacion": "INICIAL",
  "fechaAsignacion": "2025-11-14T00:00:00"
}
```

---

## Archivos Implementados

### Nuevos Archivos:
- `backend/src/main/java/com/universidad/tutorias/domain/entity/Semestre.java`
- `backend/src/main/java/com/universidad/tutorias/domain/repository/SemestreRepository.java`
- `backend/src/main/resources/db/migration/V2__crear_tabla_semestre.sql`
- `backend/src/test/java/com/universidad/tutorias/domain/entity/SemestreTest.java`

### Archivos Modificados:
- `Asignacion.java` - Agregó relación ManyToOne con Semestre
- `ProcesoAsignacion.java` - Agregó relación ManyToOne con Semestre
- `AsignacionRepository.java` - Nuevos métodos con dual signatures (Long y String)
- `ReporteConsultaService.java` - Actualización de llamadas a repositorio
- `ReporteServiceImpl.java` - Actualización de llamadas a repositorio

---

## Rollback (Si algo falla)

Si necesitas revertir la migración:

```bash
# 1. Detener la aplicación
# 2. Ejecutar en MySQL:
mysql -u root -p gestion_tutorias

DROP TABLE IF EXISTS asignaciones;
DROP TABLE IF EXISTS procesos_asignacion;
DROP TABLE IF EXISTS semestres;
DELETE FROM flyway_schema_history WHERE version = '2';

-- Luego restaurar las tablas originales si tienes backup
```

---

## Próxima Fase

Una vez que valides que FASE 1 está completa, procederemos a:

**FASE 2: Servicios de Negocio**
- Crear `SemestreService` con lógica CRUD
- Implementar validaciones de negocio
- Crear endpoints REST para semestres
- Actualizar servicios existentes para usar `Semestre` en lugar de String

---

## Soporte

Si encuentras errores:

1. **Error de compilación:** Verifica que estés en `backend/` y que `.mvnw` existe
2. **Error de base de datos:** Verifica que MySQL esté corriendo en `localhost:3306`
3. **Error de Flyway:** Revisa los logs de Spring Boot para mensajes específicos
4. **Error de validación:** Revisa que `id_semestre` NO sea NULL en asignaciones

---

**¿Lista la FASE 1? Espera confirmación antes de proceder a FASE 2.**
