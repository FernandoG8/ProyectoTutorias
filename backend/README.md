# Sistema de Gestión de Tutorías

## Stack Tecnológico
- Java 21
- Spring Boot 3.3
- Spring Data JPA / Hibernate
- Spring Security con JWT en cookies HttpOnly
- Maven
- MySQL (producción) / H2 (tests)

## Requisitos Previos
- Java 21 instalado (`java -version`)
- Maven 3.9+
- Servidor MySQL 8.x

## Instalación
1. Clonar el repositorio.
2. Crear una base de datos vacía en MySQL.
3. Ejecutar los scripts de `backend/docs/sql` para agregar restricciones e índices.

## Configuración
Actualizar `backend/src/main/resources/application.properties` con las credenciales de la base de datos y los secretos JWT. Para entornos específicos existen los perfiles `application-dev.properties` y `application-prod.properties`.

Variables relevantes:
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `jwt.secret`

## Ejecutar Proyecto
Desde la carpeta `backend/`:
```bash
./mvnw spring-boot:run
```

## Ejecutar Tests
```bash
./mvnw test
```

## Estructura del Proyecto
- `src/main/java/com/universidad/tutorias`: código fuente principal.
- `src/test/java/com/universidad/tutorias`: pruebas unitarias e integración.
- `docs/`: documentación y colecciones de Insomnia.
- `docs/sql/`: scripts SQL para restricciones e índices.

## Convenciones
- Uso intensivo de `@RequiredArgsConstructor` para inyección de dependencias.
- Métodos públicos con nombres descriptivos.
- Validaciones con Bean Validation (`jakarta.validation`).
- `cargaActual` de tutores siempre sincronizada con las asignaciones activas.
