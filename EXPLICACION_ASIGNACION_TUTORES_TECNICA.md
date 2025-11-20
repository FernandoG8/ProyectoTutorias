# 🔧 EXPLICACIÓN TÉCNICA: FLUJO COMPLETO DE ASIGNACIÓN DE TUTORES
## Para programadores y personas con conocimiento técnico

**Documento:** Descripción exhaustiva del flujo de asignación desde entrada hasta persistencia
**Versión:** 1.0
**Última actualización:** 2024-11-19

---

## 📑 TABLA DE CONTENIDOS

1. [Entrada del Sistema](#1-entrada-del-sistema)
2. [Flujo de Alumno en el Sistema](#2-flujo-de-alumno-en-el-sistema)
3. [Orquestación del Proceso](#3-orquestación-del-proceso)
4. [Algoritmo de Asignación](#4-algoritmo-de-asignación)
5. [Persistencia y Transacciones](#5-persistencia-y-transacciones)
6. [Manejo de Concurrencia](#6-manejo-de-concurrencia)
7. [Diagrama de Flujo Detallado](#7-diagrama-de-flujo-detallado)

---

## 1. ENTRADA DEL SISTEMA

### 1.1 Endpoint REST

**URL:** `POST /api/asignaciones/iniciar`
**Clase:** `AsignacionController` (línea 49)
**Decoradores:** `@PostMapping`, `@RequestParam`, `@RequestBody`

```java
@PostMapping("/iniciar")
public ResponseEntity<IniciarProcesoResponse> iniciarProceso(
    @RequestParam("archivo") MultipartFile archivo,
    @RequestParam("semestreId") Long semestreId,
    @RequestParam("usuario") String usuario
) {
    IniciarProcesoResponse response = asignacionService.iniciarProcesoAsignacion(
        archivo, semestreId, usuario
    );
    return ResponseEntity.ok(response);
}
```

**Request DTO:** `IniciarProcesoRequest`
```java
{
  "archivo": <MultipartFile>,           // Archivo Excel (multipart/form-data)
  "semestreId": 1,                       // ID del semestre destino
  "usuario": "admin@universidad.edu"     // Usuario que inicia proceso
}
```

**Response DTO:** `IniciarProcesoResponse`
```java
{
  "exitoso": true,
  "procesoId": 12345,                    // ID único para consultar estado
  "mensaje": "Proceso iniciado exitosamente",
  "estadoActual": "INICIADO"
}
```

---

### 1.2 Invocación de Servicio Principal

**Clase:** `ProcesoOrchestratorImpl` (línea 47)
**Método:** `ejecutarProcesoCompleto()`
**Decorador:** `@Async("asignacionExecutor")` - Ejecución asíncrona en thread pool

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcesoOrchestratorImpl implements ProcesoOrchestrator {

    private final ExcelReaderService excelReaderService;
    private final AlumnoValidadorService validadorService;
    private final ComparadorAlumnosService comparadorService;
    private final InactivacionService inactivacionService;
    private final ReingresoService reingresoService;
    private final AsignacionService asignacionService;
    // ... otros servicios

    @Async("asignacionExecutor")  // Thread pool executor
    @Transactional
    public void ejecutarProcesoCompleto(
        MultipartFile archivo,
        Long semestreId,
        String usuario,
        Long procesoId
    ) {
        try {
            // 5 fases secuenciales
        } catch (Exception e) {
            // Manejo de error global
        }
    }
}
```

**Executor Configuration:**
```java
@Configuration
public class AsyncConfiguration {
    @Bean(name = "asignacionExecutor")
    public Executor asignacionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("asignacion-");
        executor.initialize();
        return executor;
    }
}
```

---

## 2. FLUJO DE ALUMNO EN EL SISTEMA

### 2.1 Objeto de Transferencia: AlumnoExcelDTO

**Clase:** `AlumnoExcelDTO`
**Ubicación:** `application/dto/`

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlumnoExcelDTO {
    private Integer fila;              // Número de fila en Excel (para referencia)
    private String matricula;           // 5-20 caracteres alfanuméricos
    private String nombre;              // Nombre completo
    private String carrera;             // ICA, IE, IMECA, IME, ISC, ITS
    private Integer semestre;           // 1-12
}
```

**Validaciones aplicadas:**

| Campo | Restricción | Patrón/Rango |
|-------|------------|--------------|
| `matricula` | Obligatorio, único en archivo | `^[A-Z0-9]{5,20}$` |
| `nombre` | Obligatorio | No vacío |
| `carrera` | Obligatorio, en lista válida | {ICA, IE, IMECA, IME, ISC, ITS} |
| `semestre` | Obligatorio, número entero | 1-12 |

---

### 2.2 Lectura del Excel

**Clase:** `ExcelReaderServiceImpl`
**Método:** `leerArchivo()` (línea 25)

```java
@Service
@RequiredArgsConstructor
public class ExcelReaderServiceImpl implements ExcelReaderService {

    private final AlumnoValidadorService validador;

    @Override
    public List<AlumnoExcelDTO> leerArchivo(MultipartFile archivo) throws IOException {
        // 1. Validar encabezados
        validarEncabezados(workbook);  // Línea 68

        // 2. Parsear filas
        List<AlumnoExcelDTO> alumnos = new ArrayList<>();
        try (InputStream is = archivo.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;  // Skip header
                AlumnoExcelDTO alumno = parsearFila(row);  // Línea 104
                alumnos.add(alumno);
            }
        }

        return alumnos;
    }

    private AlumnoExcelDTO parsearFila(Row row) {
        return new AlumnoExcelDTO(
            row.getRowNum(),
            row.getCell(0).getStringCellValue().trim(),      // matricula
            row.getCell(1).getStringCellValue().trim(),      // nombre
            row.getCell(2).getStringCellValue().trim(),      // carrera
            (int) row.getCell(3).getNumericCellValue()       // semestre
        );
    }
}
```

**Formato esperado del Excel:**
```
┌─────────────────────────────────────────────────────┐
│ Matrícula | Nombre     | Carrera | Semestre       │
├─────────────────────────────────────────────────────┤
│ A20230145 | Juan Pérez | ICA     | 5              │
│ A20230146 | Maria Gómez| IE      | 3              │
│ A20230147 | Carlos L   | IMECA   | 7              │
└─────────────────────────────────────────────────────┘
```

---

### 2.3 Validación de Alumnos

**Clase:** `AlumnoValidadorServiceImpl`
**Método:** `validarAlumnos()` (línea 52)

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class AlumnoValidadorServiceImpl implements AlumnoValidadorService {

    private final ErrorValidacionRepository errorRepository;
    private final AlumnoRepository alumnoRepository;
    private final CarreraRepository carreraRepository;

    private static final String PATRON_MATRICULA = "^[A-Z0-9]{5,20}$";
    private static final List<String> CARRERAS_VALIDAS =
        List.of("ICA", "IE", "IMECA", "IME", "ISC", "ITS");

    @Override
    @Transactional
    public ResultadoValidacion validarAlumnos(
        List<AlumnoExcelDTO> alumnos,
        Long procesoId
    ) {
        List<AlumnoExcelDTO> validos = new ArrayList<>();
        List<ErrorAsignacionDTO> errores = new ArrayList<>();
        Set<String> matriculasVistas = new HashSet<>();

        for (AlumnoExcelDTO alumno : alumnos) {

            // 1. Validar campos vacíos (Línea 56)
            if (alumno.getMatricula() == null || alumno.getMatricula().isEmpty()) {
                errores.add(crearError(alumno, TipoErrorAsignacion.CAMPO_VACIO,
                    "Matrícula vacía"));
                continue;
            }

            // 2. Validar formato matrícula (Línea 59)
            if (!alumno.getMatricula().matches(PATRON_MATRICULA)) {
                errores.add(crearError(alumno, TipoErrorAsignacion.MATRICULA_INVALIDA,
                    "Formato: 5-20 caracteres alfanuméricos"));
                continue;
            }

            // 3. Validar carrera (Línea 62)
            if (!CARRERAS_VALIDAS.contains(alumno.getCarrera())) {
                errores.add(crearError(alumno, TipoErrorAsignacion.CARRERA_INVALIDA,
                    "Carrera no reconocida"));
                continue;
            }

            // 4. Validar semestre (Línea 65)
            if (alumno.getSemestre() < 1 || alumno.getSemestre() > 12) {
                errores.add(crearError(alumno, TipoErrorAsignacion.SEMESTRE_INVALIDO,
                    "Semestre debe estar entre 1 y 12"));
                continue;
            }

            // 5. Validar duplicación en archivo (Línea 68)
            if (matriculasVistas.contains(alumno.getMatricula())) {
                errores.add(crearError(alumno, TipoErrorAsignacion.ALUMNO_DUPLICADO,
                    "Matrícula repetida en archivo"));
                continue;
            }

            matriculasVistas.add(alumno.getMatricula());
            validos.add(alumno);
        }

        // Guardar errores en BD
        for (ErrorAsignacionDTO error : errores) {
            ErrorValidacion ev = new ErrorValidacion();
            ev.setProcesoAsignacion(procesoRepository.findById(procesoId).get());
            ev.setMatricula(error.getMatricula());
            ev.setMensajeError(error.getMensajeError());
            errorRepository.save(ev);
        }

        return new ResultadoValidacion(validos, errores);
    }
}
```

**Resultado:** `ResultadoValidacion`
```java
@Data
public class ResultadoValidacion {
    private List<AlumnoExcelDTO> alumnosValidos;      // Continúan al proceso
    private List<ErrorAsignacionDTO> errores;          // Guardados en BD

    public int getTotalValidos() {
        return alumnosValidos.size();
    }

    public int getTotalErrores() {
        return errores.size();
    }
}
```

---

## 3. ORQUESTACIÓN DEL PROCESO

### 3.1 Cinco Fases Secuenciales

**Ubicación:** `ProcesoOrchestratorImpl` (línea 47-162)

```java
@Async("asignacionExecutor")
@Transactional
public void ejecutarProcesoCompleto(
    MultipartFile archivo,
    Long semestreId,
    String usuario,
    Long procesoId
) {
    ProcesoAsignacion proceso = procesoRepository.findById(procesoId).get();
    Semestre semestre = semestreRepository.findById(semestreId).get();

    try {
        // ═════════════════════════════════════════════════════════
        // FASE 1: LECTURA Y VALIDACION DEL EXCEL
        // ═════════════════════════════════════════════════════════
        proceso.setEstado(EstadoProceso.INICIADO);
        procesoRepository.save(proceso);

        // 1.1: Leer Excel
        List<AlumnoExcelDTO> alumnosExcel = excelReaderService.leerArchivo(archivo);
        log.info("Lectura completada: {} alumnos en Excel", alumnosExcel.size());

        // 1.2: Validar alumnos
        ResultadoValidacion validacion = validadorService.validarAlumnos(
            alumnosExcel, procesoId
        );
        log.info("Validación: {} válidos, {} errores",
            validacion.getTotalValidos(), validacion.getTotalErrores());

        List<AlumnoExcelDTO> alumnosValidos = validacion.getAlumnosValidos();

        // ═════════════════════════════════════════════════════════
        // FASE 2: COMPARACION CON BASE DE DATOS
        // ═════════════════════════════════════════════════════════
        proceso.setEstado(EstadoProceso.COMPARANDO);
        procesoRepository.save(proceso);

        // 2.1: Identificar inactivos (alumnos que desaparecen)
        List<Alumno> alumnosAInactivar = comparadorService.identificarInactivos(
            alumnosValidos, semestreId
        );
        log.info("Alumnos a inactivar: {}", alumnosAInactivar.size());

        // ═════════════════════════════════════════════════════════
        // FASE 3: MARCAR INACTIVOS Y LIBERAR CUPOS
        // ═════════════════════════════════════════════════════════
        // 3.1: Marcar como inactivos
        inactivacionService.marcarInactivos(
            alumnosAInactivar, procesoId, semestreId
        );

        // 3.2: Liberar cupos de tutores
        inactivacionService.liberarCupos(procesoId);

        // ═════════════════════════════════════════════════════════
        // FASE 4: PROCESAR REINGRESOS
        // ═════════════════════════════════════════════════════════
        proceso.setEstado(EstadoProceso.PROCESANDO);
        procesoRepository.save(proceso);

        List<Alumno> reingresosPendientes = reingresoService.procesarReingresos(
            alumnosValidos, procesoId
        );
        log.info("Reingresos procesados: {}", reingresosPendientes.size());

        // ═════════════════════════════════════════════════════════
        // FASE 5: ASIGNACION MASIVA
        // ═════════════════════════════════════════════════════════
        ResultadoAsignacion resultado = asignacionService.asignarAlumnos(
            alumnosValidos, procesoId, semestreId
        );

        // ═════════════════════════════════════════════════════════
        // FINALIZACION
        // ═════════════════════════════════════════════════════════
        proceso.setEstado(EstadoProceso.COMPLETADO);
        proceso.setTotalAlumnosProcesados(alumnosValidos.size());
        proceso.setTotalAlumnosAsignados(resultado.getTotalAsignados());
        proceso.setTotalErrores(resultado.getTotalErrores());
        proceso.setFechaFinalizacion(LocalDateTime.now());
        procesoRepository.save(proceso);

        log.info("Proceso completado: {} asignados, {} errores",
            resultado.getTotalAsignados(), resultado.getTotalErrores());

    } catch (Exception e) {
        log.error("Error en proceso de asignación", e);
        proceso.setEstado(EstadoProceso.FALLIDO);
        proceso.setMensajeError(e.getMessage());
        procesoRepository.save(proceso);
    }
}
```

---

### 3.2 Estados del Proceso

**Enum:** `EstadoProceso`
```java
public enum EstadoProceso {
    INICIADO,              // Proceso comienza
    COMPARANDO,            // Fase 2: Comparación con BD
    LIBERANDO_CUPOS,       // Fase 3: Liberación de cupos
    PROCESANDO,            // Fase 4: Reingresos
    ASIGNANDO,             // Fase 5: Asignación principal
    COMPLETADO,            // Éxito
    FALLIDO                // Error
}
```

---

## 4. ALGORITMO DE ASIGNACIÓN

### 4.1 Punto de Entrada: `asignarAlumnos()`

**Clase:** `AsignacionServiceImpl` (línea 67)
**Tipo de transacción:** `@Transactional`

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class AsignacionServiceImpl implements AsignacionService {

    private final AsignacionRepository asignacionRepository;
    private final AlumnoRepository alumnoRepository;
    private final TutorRepository tutorRepository;
    private final TutorSincronizacionService tutorSincronizacionService;
    private final MatrizAfinidadCarreraRepository matrizRepository;
    private final AuditoriaService auditoriaService;

    private static final int BATCH_SIZE = 100;  // Procesamiento en lotes

    @Override
    @Transactional
    public ResultadoAsignacion asignarAlumnos(
        List<AlumnoExcelDTO> alumnosValidos,
        Long procesoId,
        Long semestreId
    ) {
        // PASO 1: INICIALIZACION (Línea 67-92)
        ProcesoAsignacion proceso = procesoRepository.findById(procesoId).get();
        Semestre semestre = semestreRepository.findById(semestreId).get();
        ResultadoAsignacion resultado = new ResultadoAsignacion();

        // 1.1: Recalcular carga de TODOS los tutores
        log.info("Sincronizando carga de tutores...");
        tutorSincronizacionService.recalcularCargaTodosLosTutores();

        // 1.2: Obtener y ordenar alumnos
        List<AlumnoExcelDTO> alumnosOrdenados = alumnosValidos.stream()
            .sorted(Comparator
                .comparing(AlumnoExcelDTO::getCarrera)
                .thenComparing(AlumnoExcelDTO::getMatricula))
            .collect(Collectors.toList());

        log.info("Procesando {} alumnos ordenados por carrera/matrícula",
            alumnosOrdenados.size());

        // 1.3: Agrupar tutores por carrera
        Map<String, List<Tutor>> tutoresPorCarrera = agruparTutoresPorCarrera();
        log.info("Tutores agrupados por carrera: {} carreras",
            tutoresPorCarrera.size());

        // ═════════════════════════════════════════════════════════
        // PASO 2: PROCESAMIENTO POR ALUMNO (Línea 97-152)
        // ═════════════════════════════════════════════════════════
        int contador = 0;
        for (AlumnoExcelDTO alumnoDTO : alumnosOrdenados) {
            try {
                // Transacción SEPARADA para cada alumno (aislamiento)
                ResultadoAsignacionIndividual resultIndividual =
                    asignarAlumnoTransaccional(
                        alumnoDTO,
                        tutoresPorCarrera,
                        proceso,
                        semestre
                    );

                // Agregar resultado
                resultado.agregarAsignacion(resultIndividual.getAsignacion());
                resultado.incrementarAsignados();

                contador++;
                if (contador % 100 == 0) {
                    log.info("Progreso: {}/{}", contador, alumnosOrdenados.size());
                }

            } catch (DuplicadoException e) {
                log.warn("Alumno {} ya asignado", alumnoDTO.getMatricula());
                resultado.agregarError(crearErrorDTO(
                    alumnoDTO, TipoErrorAsignacion.ALUMNO_DUPLICADO, e.getMessage()
                ));

            } catch (CapacidadExcedidaException e) {
                log.warn("Capacidad excedida para alumno {}", alumnoDTO.getMatricula());
                resultado.agregarError(crearErrorDTO(
                    alumnoDTO, TipoErrorAsignacion.CAPACIDAD_EXCEDIDA, e.getMessage()
                ));

            } catch (SinTutorDisponibleException e) {
                log.error("Sin tutor disponible para {}", alumnoDTO.getMatricula());
                resultado.agregarError(crearErrorDTO(
                    alumnoDTO, TipoErrorAsignacion.SIN_TUTOR_DISPONIBLE, e.getMessage()
                ));

            } catch (Exception e) {
                log.error("Error inesperado en alumno {}", alumnoDTO.getMatricula(), e);
                resultado.agregarError(crearErrorDTO(
                    alumnoDTO, TipoErrorAsignacion.ERROR_DESCONOCIDO, e.getMessage()
                ));
            }
        }

        log.info("Asignación completada: {} asignados, {} errores",
            resultado.getTotalAsignados(), resultado.getTotalErrores());

        return resultado;
    }

    private Map<String, List<Tutor>> agruparTutoresPorCarrera() {
        return tutorRepository.findAll().stream()
            .collect(Collectors.groupingBy(Tutor::getCarrera));
    }
}
```

---

### 4.2 Lógica Individual: `asignarAlumnoTransaccional()`

**Ubicación:** `AsignacionServiceImpl` (línea 176-349)
**Isolamento:** `@Transactional(propagation = Propagation.REQUIRES_NEW)`

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
private ResultadoAsignacionIndividual asignarAlumnoTransaccional(
    AlumnoExcelDTO alumnoDTO,
    Map<String, List<Tutor>> tutoresPorCarrera,
    ProcesoAsignacion proceso,
    Semestre semestre
) {

    // PASO A: SELECCIONAR TUTOR (Línea 183)
    // ═════════════════════════════════════════════════════════
    SeleccionTutor seleccionTutor = seleccionarTutorDisponible(
        tutoresPorCarrera,
        alumnoDTO,
        proceso
    );

    if (seleccionTutor == null || seleccionTutor.getTutor() == null) {
        throw new SinTutorDisponibleException(
            "No hay tutor disponible para carrera " + alumnoDTO.getCarrera()
        );
    }

    Tutor tutor = seleccionTutor.getTutor();
    String nivelSeleccion = seleccionTutor.getNivel();  // NIVEL_1, 2 o 3

    log.info("Seleccionado tutor {} ({}) para alumno {}",
        tutor.getNombre(), nivelSeleccion, alumnoDTO.getMatricula());


    // PASO B: DETERMINAR TIPO DE ASIGNACION (Línea 206-283)
    // ═════════════════════════════════════════════════════════
    Alumno alumno = null;
    TipoAsignacion tipoAsignacion = TipoAsignacion.INICIAL;
    boolean mantuvoPrevio = false;

    Optional<Alumno> alumnoExistente = alumnoRepository
        .findByMatricula(alumnoDTO.getMatricula());

    if (alumnoExistente.isPresent()) {
        // CASO: Alumno EXISTENTE
        alumno = alumnoExistente.get();
        Tutor tutorAnterior = alumno.getTutorActual();

        // ═══════════════════════════════════════════════
        // SUBCASO 1: Tutor anterior disponible (Línea 227-247)
        // ═══════════════════════════════════════════════
        if (tutorAnterior != null &&
            tutorAnterior.getActivo() &&
            tutorAnterior.tieneCapacidadDisponible()) {

            // MANTENER tutor anterior
            log.info("Alumno {} reingresa con tutor {} anterior",
                alumnoDTO.getMatricula(), tutorAnterior.getNombre());

            // Sincronizar tutor anterior (verificar carga real)
            Tutor tutorAnteriorSincronizado =
                tutorSincronizacionService.sincronizarYBloquearTutor(
                    tutorAnterior.getId()
                );

            if (!tutorAnteriorSincronizado.tieneCapacidadDisponible()) {
                // Capacidad cambió desde la sincronización
                // Proceder a reasignación
                tipoAsignacion = TipoAsignacion.REASIGNACION;
                mantuvoPrevio = false;
                log.warn("Tutor anterior sin capacidad: reasignación necesaria");

                // Crear alerta de REASIGNACION_FORZADA
                AlertaProceso alerta = new AlertaProceso();
                alerta.setProcesoAsignacion(proceso);
                alerta.setTipoAlerta(TipoAlerta.REASIGNACION_FORZADA);
                alerta.setMatricula(alumnoDTO.getMatricula());
                alerta.setMensaje("Tutor anterior sin capacidad disponible");
                alerta.setSeveridad(SeveridadAlerta.WARNING);
                alertaRepository.save(alerta);

            } else {
                // ✅ Tutor anterior tiene capacidad
                tutor = tutorAnteriorSincronizado;
                tipoAsignacion = TipoAsignacion.REINGRESO;
                mantuvoPrevio = true;
                log.info("Alumno {} reasignado exitosamente a {}",
                    alumnoDTO.getMatricula(), tutor.getNombre());
            }
        } else {
            // ═══════════════════════════════════════════════
            // SUBCASO 2: Tutor anterior no disponible (Línea 248-275)
            // ═══════════════════════════════════════════════
            tipoAsignacion = TipoAsignacion.REASIGNACION;

            log.warn("Tutor anterior de {} no disponible: reasignación requerida",
                alumnoDTO.getMatricula());

            // Crear alerta
            AlertaProceso alerta = new AlertaProceso();
            alerta.setProcesoAsignacion(proceso);
            alerta.setTipoAlerta(TipoAlerta.REASIGNACION_FORZADA);
            alerta.setMatricula(alumnoDTO.getMatricula());
            alerta.setMensaje("Tutor anterior no disponible");
            alerta.setSeveridad(SeveridadAlerta.WARNING);
            alerta.setDetallesTecnicos(String.format(
                "Tutor anterior: %s (activo=%s, capacidad=%d/%d)",
                tutorAnterior != null ? tutorAnterior.getNombre() : "NULL",
                tutorAnterior != null ? tutorAnterior.getActivo() : "N/A",
                tutorAnterior != null ? tutorAnterior.getCargaActual() : 0,
                tutorAnterior != null ? tutorAnterior.getCapacidadMax() : 0
            ));
            alertaRepository.save(alerta);
        }

    } else {
        // CASO: Alumno NUEVO (no existe en BD)
        alumno = new Alumno();
        tipoAsignacion = TipoAsignacion.INICIAL;
        log.info("Alumno nuevo: {}", alumnoDTO.getMatricula());
    }


    // PASO C: ACTUALIZAR DATOS DEL ALUMNO (Línea 289-296)
    // ═════════════════════════════════════════════════════════
    alumno.setMatricula(alumnoDTO.getMatricula());
    alumno.setNombre(alumnoDTO.getNombre());

    // Convertir carrera a mayúsculas para consistencia
    String carreraAlumno = alumnoDTO.getCarrera().toUpperCase();
    alumno.setCarrera(carreraAlumno);

    alumno.setSemestre(alumnoDTO.getSemestre());
    alumno.setEstado(EstadoAlumno.ACTIVO);
    alumno.setTutorActual(tutor);

    alumno = alumnoRepository.save(alumno);
    log.debug("Alumno actualizado: {} (ID: {})", alumno.getMatricula(), alumno.getId());


    // PASO D: CREAR REGISTRO DE ASIGNACION (Línea 302-308)
    // ═════════════════════════════════════════════════════════

    // VALIDACION: Evitar duplicación en semestre
    boolean yaAsignado = asignacionRepository.existsByAlumnoAndTutorAndSemestreId(
        alumno.getId(), tutor.getId(), semestre.getId()
    );

    if (yaAsignado && !mantuvoPrevio) {
        throw new DuplicadoException(
            String.format("Alumno %s ya asignado a %s en semestre %s",
                alumno.getMatricula(), tutor.getNombre(), semestre.getCodigo())
        );
    }

    // VALIDACION: Verificar capacidad
    if (!tutor.tieneCapacidadDisponible() && !mantuvoPrevio) {
        throw new CapacidadExcedidaException(
            String.format("Tutor %s alcanzó capacidad máxima (%d)",
                tutor.getNombre(), tutor.getCapacidadMax())
        );
    }

    Asignacion asignacion = new Asignacion();
    asignacion.setAlumno(alumno);
    asignacion.setTutor(tutor);
    asignacion.setSemestre(semestre);
    asignacion.setTipoAsignacion(tipoAsignacion);
    asignacion.setFechaAsignacion(LocalDateTime.now());

    asignacion = asignacionRepository.save(asignacion);
    log.info("Asignación creada: {} → {} (tipo: {})",
        alumno.getMatricula(), tutor.getNombre(), tipoAsignacion);


    // PASO E: ACTUALIZAR CARGA DEL TUTOR (Línea 314-326)
    // ═════════════════════════════════════════════════════════
    if (!mantuvoPrevio) {
        // Obtener tutor con lock PESSIMISTIC_WRITE
        Tutor tutorActualizado = tutorSincronizacionService
            .sincronizarYBloquearTutor(tutor.getId());

        tutorActualizado.incrementarCarga();
        tutorRepository.save(tutorActualizado);

        log.info("Carga actualizada para tutor {}: {} → {}",
            tutorActualizado.getNombre(),
            tutorActualizado.getCargaActual() - 1,
            tutorActualizado.getCargaActual());
    }


    // PASO F: AUDITAR CAMBIO (Línea 328-339)
    // ═════════════════════════════════════════════════════════
    auditoriaService.registrarLog(
        null,  // ID auditoría
        TipoAccion.ASIGNACION_ALUMNO,
        "ASIGNACION",
        asignacion.getId(),
        String.format("Alumno %s asignado a %s (tipo: %s)",
            alumno.getMatricula(), tutor.getNombre(), tipoAsignacion),
        String.format("{\"alumno_id\":%d,\"tipo_anterior\":null}",
            alumno.getId()),
        String.format("{\"alumno_id\":%d,\"tutor_id\":%d,\"tipo\":\"%s\"}",
            alumno.getId(), tutor.getId(), tipoAsignacion),
        "SISTEMA"
    );

    return new ResultadoAsignacionIndividual(
        asignacion,
        nivelSeleccion,
        tipoAsignacion
    );
}
```

---

### 4.3 Algoritmo de Selección de Tutor

**Método:** `seleccionarTutorDisponible()` (línea 351-403)
**Estrategia:** 3 niveles de búsqueda

```java
private SeleccionTutor seleccionarTutorDisponible(
    Map<String, List<Tutor>> tutoresPorCarrera,
    AlumnoExcelDTO alumnoDTO,
    ProcesoAsignacion proceso
) {
    String carreraAlumno = alumnoDTO.getCarrera().toUpperCase();

    // ═════════════════════════════════════════════════════════
    // NIVEL 1️⃣: TUTOR DE LA MISMA CARRERA (Prioridad ALTA)
    // ═════════════════════════════════════════════════════════
    Optional<Tutor> tutorMismaCarrera = buscarTutorDisponible(
        tutoresPorCarrera.getOrDefault(carreraAlumno, new ArrayList<>())
    );

    if (tutorMismaCarrera.isPresent()) {
        log.info("Tutor de carrera seleccionado para {}", alumnoDTO.getMatricula());
        return new SeleccionTutor(tutorMismaCarrera.get(), "NIVEL_1");
    }

    // ═════════════════════════════════════════════════════════
    // NIVEL 2️⃣: TUTOR DE CARRERA COMPATIBLE (Prioridad MEDIA)
    // ═════════════════════════════════════════════════════════
    List<String> carrerasCompatibles = matrizRepository
        .findCarrerasCompatibles(carreraAlumno);

    for (String carreraCompatible : carrerasCompatibles) {
        Optional<Tutor> tutorCompatible = buscarTutorDisponible(
            tutoresPorCarrera.getOrDefault(carreraCompatible, new ArrayList<>())
        );

        if (tutorCompatible.isPresent()) {
            log.info("Tutor compatible ({}) seleccionado para {}",
                carreraCompatible, alumnoDTO.getMatricula());

            // Crear alerta de asignación cruzada
            AlertaProceso alerta = new AlertaProceso();
            alerta.setProcesoAsignacion(proceso);
            alerta.setTipoAlerta(TipoAlerta.ASIGNACION_CRUZADA);
            alerta.setMatricula(alumnoDTO.getMatricula());
            alerta.setMensaje(String.format(
                "Asignado a tutor de carrera compatible: %s → %s",
                carreraAlumno, carreraCompatible));
            alerta.setSeveridad(SeveridadAlerta.WARNING);
            alertaRepository.save(alerta);

            return new SeleccionTutor(tutorCompatible.get(), "NIVEL_2");
        }
    }

    // ═════════════════════════════════════════════════════════
    // NIVEL 3️⃣: TUTOR CON MENOR CARGA GLOBAL (Prioridad BAJA)
    // ═════════════════════════════════════════════════════════
    Optional<Tutor> tutorMenorCarga = buscarTutorMenorCarga(tutoresPorCarrera);

    if (tutorMenorCarga.isPresent()) {
        log.warn("Tutor de menor carga global seleccionado para {}",
            alumnoDTO.getMatricula());

        // Crear alerta
        AlertaProceso alerta = new AlertaProceso();
        alerta.setProcesoAsignacion(proceso);
        alerta.setTipoAlerta(TipoAlerta.ASIGNACION_CRUZADA);
        alerta.setMatricula(alumnoDTO.getMatricula());
        alerta.setMensaje(String.format(
            "Asignado a tutor con menor carga (sin compatibilidad): %s → %s",
            carreraAlumno, tutorMenorCarga.get().getCarrera()));
        alerta.setSeveridad(SeveridadAlerta.ERROR);
        alertaRepository.save(alerta);

        return new SeleccionTutor(tutorMenorCarga.get(), "NIVEL_3");
    }

    // ═════════════════════════════════════════════════════════
    // ERROR: SIN TUTOR DISPONIBLE
    // ═════════════════════════════════════════════════════════
    log.error("SIN TUTOR DISPONIBLE para alumno {}", alumnoDTO.getMatricula());
    throw new SinTutorDisponibleException(
        "No hay tutores disponibles en ningún nivel de búsqueda"
    );
}
```

---

### 4.4 Búsqueda de Tutor Disponible

**Método:** `buscarTutorDisponible()` (línea 440-463)

```java
private Optional<Tutor> buscarTutorDisponible(List<Tutor> tutoresCarrera) {

    // FILTRO: Solo tutores activos con capacidad disponible
    List<Tutor> disponibles = tutoresCarrera.stream()
        .filter(Tutor::getActivo)
        .filter(Tutor::tieneCapacidadDisponible)
        .collect(Collectors.toList());

    if (disponibles.isEmpty()) {
        return Optional.empty();
    }

    // ORDENAMIENTO: Por carga ascendente (menor carga primero)
    disponibles.sort(
        Comparator.comparingInt(Tutor::getCargaActual)
            .thenComparingInt(t -> -t.getId().hashCode())  // Desempate aleatorio
    );

    log.debug("Tutores disponibles: {}",
        disponibles.stream()
            .map(t -> t.getNombre() + " (carga: " + t.getCargaActual() + ")")
            .collect(Collectors.joining(", ")));

    // SELECCION: El primero (menor carga)
    return Optional.of(disponibles.get(0));
}
```

---

### 4.5 Búsqueda de Tutor con Menor Carga

**Método:** `buscarTutorMenorCarga()` (línea 465-470)

```java
private Optional<Tutor> buscarTutorMenorCarga(Map<String, List<Tutor>> tutoresPorCarrera) {

    return tutoresPorCarrera.values().stream()
        .flatMap(List::stream)
        .filter(Tutor::getActivo)
        .filter(Tutor::tieneCapacidadDisponible)
        .min(Comparator.comparingInt(Tutor::getCargaActual));
}
```

---

## 5. PERSISTENCIA Y TRANSACCIONES

### 5.1 Transacciones Anidadas

**Configuración:**
```java
@Transactional                                          // Transacción padre
public void ejecutarProcesoCompleto(...) {

    tutorSincronizacionService.recalcularCargaTodosLosTutores();

    for (AlumnoExcelDTO alumno : alumnos) {
        asignarAlumnoTransaccional(alumno);  // Transacción separada
    }
}

@Transactional(propagation = Propagation.REQUIRES_NEW)  // Transacción hija
private void asignarAlumnoTransaccional(AlumnoExcelDTO alumno) {
    // Cada alumno se procesa en transacción AISLADA
    // Si falla UN alumno, otros no se ven afectados
}
```

**Ventajas:**
- ✅ Aislamiento entre alumnos
- ✅ Si un alumno falla, continúan los demás
- ✅ Rollback selectivo por error

---

### 5.2 Actualización de Carga de Tutor

**Clase:** `TutorSincronizacionServiceImpl`

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class TutorSincronizacionServiceImpl implements TutorSincronizacionService {

    private final TutorRepository tutorRepository;
    private final AsignacionRepository asignacionRepository;

    /**
     * Recalcula la carga REAL de un tutor desde asignaciones en BD
     */
    @Transactional
    public void recalcularCargaTutor(Long tutorId) {
        // COUNT real desde tabla asignaciones
        int cargaReal = asignacionRepository.countByTutorId(tutorId);

        Tutor tutor = tutorRepository.findById(tutorId)
            .orElseThrow(() -> new EntityNotFoundException("Tutor no encontrado"));

        if (tutor.getCargaActual() != cargaReal) {
            log.warn("Sincronización necesaria para tutor {}: {} → {}",
                tutor.getNombre(), tutor.getCargaActual(), cargaReal);

            tutor.sincronizarCarga(cargaReal);
            tutorRepository.save(tutor);
        }
    }

    /**
     * Recalcula TODOS los tutores y aplica lock pesimista
     */
    @Transactional
    public void recalcularCargaTodosLosTutores() {
        List<Tutor> todosLosTutores = tutorRepository.findAll();

        for (Tutor tutor : todosLosTutores) {
            int cargaReal = asignacionRepository.countByTutorId(tutor.getId());
            tutor.sincronizarCarga(cargaReal);
            tutorRepository.save(tutor);
        }
    }

    /**
     * Sincroniza Y BLOQUEA pesimisticamente un tutor para modificación
     * LOCK: PESSIMISTIC_WRITE - Nadie más puede modificar hasta commit
     */
    @Transactional
    public Tutor sincronizarYBloquearTutor(Long tutorId) {
        // Sincronizar primero
        recalcularCargaTutor(tutorId);

        // Luego obtener con lock para modificación
        Optional<Tutor> tutorLocked = tutorRepository.findByIdForUpdate(tutorId);
        return tutorLocked.orElseThrow(
            () -> new EntityNotFoundException("Tutor no encontrado: " + tutorId)
        );
    }
}
```

**Query con Lock:**
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT t FROM Tutor t WHERE t.id = :id")
Optional<Tutor> findByIdForUpdate(@Param("id") Long id);
```

---

### 5.3 Entidades Persistidas

#### Alumno
```java
@Entity
@Table(name = "alumnos", indexes = {
    @Index(name = "idx_matricula", columnList = "matricula", unique = true),
    @Index(name = "idx_estado", columnList = "estado"),
    @Index(name = "idx_carrera", columnList = "carrera")
})
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "matricula", unique = true, nullable = false, length = 20)
    private String matricula;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "carrera", nullable = false, length = 50)
    private String carrera;

    @Column(name = "semestre", nullable = false)
    private Integer semestre;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoAlumno estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tutor_actual")
    private Tutor tutorActual;

    @Column(name = "contador_cambios_tutor", nullable = false)
    private Integer contadorCambiosTutor = 0;  // Máximo 2

    public boolean puedeReasignarse() {
        return contadorCambiosTutor < 2;
    }
}
```

#### Asignacion
```java
@Entity
@Table(name = "asignaciones",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"id_alumno", "id_tutor", "id_semestre"},
            name = "uk_asignacion_alumno_tutor_semestre"
        )
    },
    indexes = {
        @Index(name = "idx_tutor_id", columnList = "id_tutor"),
        @Index(name = "idx_alumno_id", columnList = "id_alumno")
    }
)
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_alumno")
    private Alumno alumno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tutor")
    private Tutor tutor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_semestre")
    private Semestre semestre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_asignacion", nullable = false)
    private TipoAsignacion tipoAsignacion;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;
}
```

**TipoAsignacion:**
```java
public enum TipoAsignacion {
    INICIAL,          // Alumno nuevo
    REASIGNACION,     // Cambio forzado de tutor
    REINGRESO         // Alumno regresa con tutor anterior
}
```

#### Tutor
```java
@Entity
@Table(name = "tutores")
public class Tutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "carrera", nullable = false, length = 50)
    private String carrera;

    @Column(name = "capacidad_max", nullable = false)
    private Integer capacidadMax;

    @Column(name = "carga_actual", nullable = false)
    private Integer cargaActual = 0;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    public boolean tieneCapacidadDisponible() {
        return activo && cargaActual < capacidadMax;
    }

    public void incrementarCarga() {
        this.cargaActual++;
    }

    public void decrementarCarga() {
        if (cargaActual > 0) {
            this.cargaActual--;
        }
    }

    public void sincronizarCarga(int cargaReal) {
        this.cargaActual = cargaReal;
    }
}
```

---

## 6. MANEJO DE CONCURRENCIA

### 6.1 Locks Pesimistas

```java
// En TutorRepository
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT t FROM Tutor t WHERE t.id = :id")
Optional<Tutor> findByIdForUpdate(@Param("id") Long id);

// En AlumnoRepository
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT a FROM Alumno a WHERE a.id = :id")
Optional<Alumno> findByIdForUpdate(@Param("id") Long id);
```

**Comportamiento:**
- Cuando se obtiene un tutor con `findByIdForUpdate()`, se adquiere un lock exclusivo
- Otras transacciones NO PUEDEN modificar ese tutor
- El lock se libera al commit de la transacción

**Escenario:**
```
Transacción 1: Lock tutor García
  ├─ incrementar carga (20→21)
  └─ COMMIT → Libera lock

Transacción 2: Espera lock de tutor García
  ├─ Cuando obtiene lock, lee carga=21
  └─ incrementar carga (21→22)
```

---

### 6.2 Constraint UNIQUE

**En tabla `asignaciones`:**
```sql
UNIQUE KEY `uk_asignacion_alumno_tutor_semestre`
    (id_alumno, id_tutor, id_semestre)
```

Previene:
```
❌ Juan asignado a García 2 veces en 2025-1
✅ Juan asignado a García en 2024-2 y López en 2025-1
```

---

## 7. DIAGRAMA DE FLUJO DETALLADO

```
╔══════════════════════════════════════════════════════════════════════════╗
║                 FLUJO COMPLETO DE ASIGNACIÓN DE TUTORES                 ║
╚══════════════════════════════════════════════════════════════════════════╝

HTTP POST /api/asignaciones/iniciar
├─ archivo.xlsx (MultipartFile)
├─ semestreId (Long)
└─ usuario (String)
    │
    ↓ AsignacionController.iniciarProceso()
    │
    ├─ Crear registro ProcesoAsignacion(estado=INICIADO)
    │
    ├─ Iniciar ProcesoOrchestratorImpl.ejecutarProcesoCompleto() [@Async]
    │
    ├────────────────────────────────────────────────────────────
    │ FASE 1: LECTURA Y VALIDACION
    ├────────────────────────────────────────────────────────────
    │
    ├─ ExcelReaderServiceImpl.leerArchivo()
    │  ├─ Valida encabezados
    │  ├─ Parsea cada fila
    │  └─ Retorna List<AlumnoExcelDTO>
    │
    ├─ AlumnoValidadorServiceImpl.validarAlumnos()
    │  ├─ FOR EACH alumno:
    │  │  ├─ ✓ Campos no vacíos?
    │  │  ├─ ✓ Matrícula válida?
    │  │  ├─ ✓ Carrera válida?
    │  │  ├─ ✓ Semestre 1-12?
    │  │  └─ ✓ No duplicada en archivo?
    │  │
    │  └─ Retorna ResultadoValidacion(válidos, errores)
    │
    ├────────────────────────────────────────────────────────────
    │ FASE 2: COMPARACION CON BD
    ├────────────────────────────────────────────────────────────
    │
    ├─ ComparadorAlumnosServiceImpl.identificarInactivos()
    │  ├─ SELECT alumnos activos de BD
    │  ├─ Identificar que NO están en Excel
    │  └─ Retorna List<Alumno> aInactivar
    │
    ├────────────────────────────────────────────────────────────
    │ FASE 3: MARCAR INACTIVOS Y LIBERAR CUPOS
    ├────────────────────────────────────────────────────────────
    │
    ├─ InactivacionServiceImpl.marcarInactivos()
    │  └─ UPDATE alumnos SET estado='INACTIVO'
    │
    ├─ InactivacionServiceImpl.liberarCupos()
    │  └─ FOR EACH tutor:
    │     └─ UPDATE carga_actual = COUNT(asignaciones activas)
    │
    ├────────────────────────────────────────────────────────────
    │ FASE 4: PROCESAR REINGRESOS
    ├────────────────────────────────────────────────────────────
    │
    ├─ ReingresoServiceImpl.procesarReingresos()
    │  └─ FOR EACH alumno que reaparece:
    │     ├─ ¿Estaba inactivo?
    │     ├─ ¿Tutor anterior activo?
    │     ├─ ¿Tutor anterior tiene capacidad?
    │     │
    │     ├─ SÍ a todo: Asignar a tutor anterior
    │     │  └─ Retorna reingresosPendientes
    │     │
    │     └─ NO: Crear alerta de reasignación
    │
    ├────────────────────────────────────────────────────────────
    │ FASE 5: ASIGNACION MASIVA
    ├────────────────────────────────────────────────────────────
    │
    ├─ AsignacionServiceImpl.asignarAlumnos()
    │  │
    │  ├─ 1. TutorSincronizacionServiceImpl.recalcularCargaTodosLosTutores()
    │  │   └─ FOR EACH tutor: carga = COUNT(asignaciones activas)
    │  │
    │  ├─ 2. Agrupar tutores por carrera
    │  │   └─ Map<Carrera, List<Tutor>>
    │  │
    │  ├─ 3. Ordenar alumnos (carrera, matrícula)
    │  │
    │  └─ 4. FOR EACH alumno VALIDADO:
    │     │
    │     └─ asignarAlumnoTransaccional() [@REQUIRES_NEW]
    │        │
    │        ├─ A. seleccionarTutorDisponible()
    │        │  │
    │        │  ├─ NIVEL 1: Tutor MISMA CARRERA
    │        │  │  ├─ Filtrar: activo + capacidad disponible
    │        │  │  └─ Ordenar: por cargaActual ASC
    │        │  │  └─ Elegir: primero
    │        │  │
    │        │  ├─ NIVEL 2: Tutor CARRERA COMPATIBLE
    │        │  │  ├─ Consultar MatrizAfinidadCarrera
    │        │  │  ├─ FOR EACH carrera compatible (por prioridad):
    │        │  │  │  └─ Buscar tutor disponible
    │        │  │  │
    │        │  │  └─ Generar alerta: ASIGNACION_CRUZADA
    │        │  │
    │        │  ├─ NIVEL 3: MENOR CARGA GLOBAL
    │        │  │  ├─ Buscar entre TODOS los tutores
    │        │  │  └─ Generar alerta: ASIGNACION_CRUZADA (crítica)
    │        │  │
    │        │  └─ ERROR: SinTutorDisponibleException
    │        │
    │        ├─ B. Determinar tipo de asignación
    │        │  │
    │        │  ├─ ¿Alumno NUEVO?
    │        │  │  └─ tipoAsignacion = INICIAL
    │        │  │
    │        │  ├─ ¿EXISTENTE + tutor anterior DISPONIBLE?
    │        │  │  └─ tipoAsignacion = REINGRESO
    │        │  │  └─ NO incrementar carga
    │        │  │
    │        │  └─ ¿EXISTENTE + tutor anterior NO DISPONIBLE?
    │        │     └─ tipoAsignacion = REASIGNACION
    │        │     └─ Generar alerta: REASIGNACION_FORZADA
    │        │
    │        ├─ C. Actualizar campos del Alumno
    │        │  ├─ matricula
    │        │  ├─ nombre
    │        │  ├─ carrera
    │        │  ├─ semestre
    │        │  ├─ estado = ACTIVO
    │        │  └─ tutorActual = tutor seleccionado
    │        │
    │        ├─ D. Crear registro Asignacion
    │        │  ├─ VALIDAR: (alumno, tutor, semestre) UNIQUE
    │        │  ├─ VALIDAR: tutor tiene capacidad
    │        │  └─ INSERT Asignacion
    │        │
    │        ├─ E. Actualizar carga del Tutor
    │        │  └─ IF NOT mantuvoPrevio:
    │        │     ├─ Lock tutor con findByIdForUpdate()
    │        │     ├─ tutor.incrementarCarga()
    │        │     └─ UPDATE tutor
    │        │
    │        ├─ F. Registrar en auditoría
    │        │  └─ INSERT LogAuditoria(acción, detalles)
    │        │
    │        └─ Retornar ResultadoAsignacionIndividual
    │           ├─ exitoso: boolean
    │           ├─ asignacion: Asignacion
    │           └─ tipoAsignacion: TipoAsignacion
    │
    ├─ Compilar ResultadoAsignacion
    │  ├─ totalAsignados
    │  ├─ totalErrores
    │  ├─ asignaciones (List)
    │  ├─ errores (List)
    │  └─ alertas (List)
    │
    ├────────────────────────────────────────────────────────────
    │ FINALIZACION
    ├────────────────────────────────────────────────────────────
    │
    ├─ UPDATE ProcesoAsignacion
    │  ├─ estado = COMPLETADO (o FALLIDO)
    │  ├─ totalAlumnosProcesados
    │  ├─ totalAlumnosAsignados
    │  ├─ totalErrores
    │  └─ fechaFinalizacion = NOW
    │
    └─ Responder cliente
       └─ IniciarProcesoResponse(exitoso, procesoId, estadoActual)


┌──────────────────────────────────────────────────────────────┐
│            CONSULTAR ESTADO DEL PROCESO                      │
└──────────────────────────────────────────────────────────────┘

GET /api/asignaciones/proceso/{procesoId}
    │
    ├─ AsignacionController.obtenerEstadoProceso()
    │
    └─ Retorna EstadoProcesoResponse
       ├─ estadoActual (INICIADO, PROCESANDO, COMPLETADO, etc)
       ├─ porcentajeAvance
       ├─ totalAlumnosProcesados
       ├─ totalAlumnosAsignados
       ├─ totalErrores
       ├─ alertas[]
       └─ asignaciones[] (para descargar)
```

---

## 📊 ESTADÍSTICAS Y CONTADORES

```java
public class ResultadoAsignacion {

    private int totalAsignados = 0;
    private int totalErrores = 0;
    private List<Asignacion> asignaciones = new ArrayList<>();
    private List<ErrorAsignacionDTO> errores = new ArrayList<>();
    private List<AlertaProceso> alertas = new ArrayList<>();

    public void incrementarAsignados() {
        this.totalAsignados++;
    }

    public void agregarError(ErrorAsignacionDTO error) {
        this.totalErrores++;
        this.errores.add(error);
    }

    public int getTotalProcesados() {
        return totalAsignados + totalErrores;
    }

    public double getPorcentajeExito() {
        if (getTotalProcesados() == 0) return 0;
        return (totalAsignados * 100.0) / getTotalProcesados();
    }
}
```

---

## 🎯 RESUMEN TÉCNICO

| Aspecto | Detalle |
|---------|---------|
| **Orquestación** | 5 fases secuenciales en ProcesoOrchestratorImpl |
| **Ejecución** | Asíncrona con @Async("asignacionExecutor") |
| **Transacciones** | REQUIRES_NEW por alumno para aislamiento |
| **Locks** | PESSIMISTIC_WRITE en Tutor y Alumno |
| **Selección de Tutor** | 3 niveles: Misma carrera → Compatible → Menor carga |
| **Validaciones** | Matrícula única, capacidad, constraint UNIQUE |
| **Alertas** | Asignación cruzada, reasignación forzada |
| **Auditoría** | Completa con LogAuditoria |
| **Concurrencia** | Thread pool de 2-5 threads para procesos |

---

**Documento técnico completo para arquitectura y debugging de flujo de asignación.**
