# Guía de Testing con H2 - ProyectoTutoriasBackend

## Configuración rápida de H2 para tests

H2 es una base de datos SQL embebida perfecta para testing porque:
- ✅ Sin dependencias externas (MySQL)
- ✅ Base de datos en memoria (muy rápido)
- ✅ Modo MySQL compatible
- ✅ Automático create/drop del schema
- ✅ Ideal para tests paralelos

## Pasos para usar H2

### 1. Verificar que H2 está en pom.xml

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

Estado: ✅ Ya incluido en pom.xml

### 2. Archivo de configuración

Se ha creado: `backend/src/test/resources/application-test.properties`

Contiene:
```properties
spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
```

### 3. Anotar tests para usar H2

En cada archivo de test, agregar:

```java
@SpringBootTest
@ActiveProfiles("test")  // ← Esto activa application-test.properties
class TuTestTest {
    // ... tests aquí
}
```

O para tests de integración:

```java
@DataJpaTest
@ActiveProfiles("test")  // ← Esto activa application-test.properties
class TuRepositoryTest {
    // ... tests aquí
}
```

### 4. Ejecutar tests

```bash
cd backend
mvn clean test
```

O solo una clase:
```bash
mvn test -Dtest=NombreTestClass
```

O una prueba específica:
```bash
mvn test -Dtest=NombreTestClass#nombreTestMethod
```

### 5. Generar reporte de cobertura

```bash
cd backend
mvn clean test
# Luego acceder a: target/site/jacoco/index.html
```

## Ventajas de H2 vs MySQL

| Aspecto | H2 | MySQL |
|--------|----|----- |
| **Instalación** | Automática | Manual requiere instalación |
| **Configuración** | Simple (propiedades) | Compleja (host, usuario, contraseña) |
| **Velocidad** | Ultra rápido (memoria) | Más lento (red, disco) |
| **Paralelismo** | Soporta múltiples tests | Requiere sincronización |
| **Limpieza** | Automática (reinicia cada test) | Manual o complex setup/teardown |
| **Dependencias** | Cero (auto en pom.xml) | Requiere servidor MySQL corriendo |

## Flujo de tests recomendado

```
1. Tests UNITARIOS con Mockito
   - Sin base de datos
   - Muy rápidos (< 1 segundo)
   - Ej: AuthServiceTest (mock de repo)

2. Tests de INTEGRACIÓN con H2
   - Con base de datos H2 en memoria
   - Rápidos (< 5 segundos)
   - Ej: AlumnoRepositoryTest (real repo con H2)

3. Tests E2E (opcional)
   - Contra servidor de pruebas real
   - Más lentos (10+ segundos)
   - Ej: AuthFlowE2ETest
```

## Ejemplo de test con H2

```java
@DataJpaTest
@ActiveProfiles("test")
class AlumnoRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Test
    void testSaveAndFind() {
        // Arrange
        Alumno alumno = new Alumno();
        alumno.setMatricula("12345");
        alumno.setNombre("Juan Pérez");
        em.persistAndFlush(alumno);

        // Act
        Alumno found = alumnoRepository.findById(alumno.getId()).orElse(null);

        // Assert
        assertNotNull(found);
        assertEquals("12345", found.getMatricula());
    }
}
```

## Solución de problemas comunes

### Problema: "No suitable driver found for jdbc:h2:mem:testdb"
**Solución:** Verificar que H2 está en scope `test` en pom.xml

### Problema: "Failed to load ApplicationContext"
**Solución:** Agregar `@ActiveProfiles("test")` a la clase de test

### Problema: "No entity manager found"
**Solución:** Para repositorio tests, usar `@DataJpaTest` en lugar de `@SpringBootTest`

### Problema: "Tables are empty"
**Solución:** Usar `@TestInstance(Lifecycle.PER_METHOD)` y `ddl-auto=create-drop`

## Referencia rápida de comandos

```bash
# Limpiar y ejecutar todos los tests
mvn clean test

# Ejecutar tests y generar reporte JaCoCo
mvn clean test jacoco:report

# Solo tests de una clase
mvn test -Dtest=SemestreTest

# Solo un método de test
mvn test -Dtest=SemestreTest#testValidation

# Ejecutar tests en paralelo (más rápido)
mvn test -DparallelTestCount=4

# Ver logs de los tests
mvn test -X

# Skip tests (si necesitas compilar sin correr tests)
mvn clean install -DskipTests
```

## Próximos pasos

1. ✅ H2 configurado
2. ✅ Properties de test creados
3. ⏳ Crear primer test con H2 (consultar REPORTE_EVALUACION_INTEGRACION.md)
4. ⏳ Ejecutar `mvn clean test`
5. ⏳ Revisar reporte en `target/site/jacoco/index.html`

---

**Nota:** H2 es la solución ideal para tests automatizados. MySQL debe usarse solo en ambiente de desarrollo local si prefieres.

