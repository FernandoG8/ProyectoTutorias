# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java/com/universidad/tutorias` alberga capas `config`, `domain`, `application`, `infraestructure` y controladores REST.
- `src/main/resources` contiene `application.properties` y perfiles `application-*.properties`; copia valores sensibles vía variables de entorno.
- `src/test/java/com/universidad/tutorias` replica el árbol principal para pruebas unitarias e integración con H2; los datos semilla viven en `src/test/resources`.
- `docs/` centraliza colecciones Insomnia y los SQL de `docs/sql` que definen índices, restricciones y migraciones manuales.

## Build, Test & Local Development Commands
```bash
./mvnw spring-boot:run        # arranca la API con perfiles por defecto
./mvnw clean verify           # compila, ejecuta pruebas y valida MapStruct/Lombok
./mvnw test -Dspring.profiles.active=test  # usa H2 y beans de prueba
./mvnw package -DskipTests    # genera el JAR listo para despliegue
```
## Endpoint Design & API Practices
- Controladores viven en `src/main/java/com/universidad/tutorias/infrastructure/controller`; agrupa rutas por recurso (`/api/alumnos`, `/api/asignaciones`) y usa verbos HTTP consistentes.
- Envuelve respuestas en `ApiResponse` o `PagedResponse`, valida DTOs con `@Valid` y registra `log.info` antes de operaciones críticas.
- Expone CORS con `@CrossOrigin`, documenta con `@Operation`/`@ApiResponse` y protege endpoints sensibles vía `@PreAuthorize` + `@SecurityRequirement`.

## Coding Style & Naming Conventions
- Java 21, 4 espacios, llaves en línea nueva para clases y métodos.
- Clases `UpperCamelCase`, beans y servicios `lowerCamelCase`, constantes `SCREAMING_SNAKE_CASE`.
- Prefiere `@RequiredArgsConstructor` para inyección, `record` o DTO inmutables, y valida entradas con `jakarta.validation`.
- Expone endpoints con verbos HTTP claros (`/tutores/{id}/carga`); usa MapStruct mappers por módulo.

## Testing Guidelines
- Framework principal: JUnit 5 + Spring Boot Test; mockea integraciones externas con `@MockBean`.
- Nombra casos como `Metodo_Escenario_Resultado` y anota integraciones con `@SpringBootTest`.
- Mantén cobertura lógica crítica (asignación de tutores, sincronización `cargaActual`) respaldada por pruebas que verifiquen transacciones y eventos de dominio.

## Commit & Pull Request Guidelines
- Sigue el tono del historial git: mensajes cortos en imperativo (“Fix cargaActual sync”) y, si aplica, etiqueta el módulo (`assignment: ensure liberarCupos persists inactive students`).
- Cada PR debe incluir: descripción del cambio, pasos de validación local (`./mvnw test`), referencias a issues y evidencia visual para endpoints nuevos (capturas Insomnia en `docs/`).
- Rebase sobre `master` antes de solicitar revisión y verifica que no queden archivos generados en `target/`.

## Security & Configuration Tips
- No compartas secretos en los `.properties`; usa variables de entorno o `application-prod.properties` ignorado.
- Ejecuta los SQL de `docs/sql` antes de probar en MySQL.
- Habilita JWT solo vía cookies HttpOnly y revisa expiraciones en `jwt.secret` antes de subir cambios.
