// ============================================
// ASIGNACION SERVICE IMPLEMENTATION
// ============================================

package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.AlumnoExcelDTO;
import com.universidad.tutorias.application.dto.ErrorAsignacionDTO;
import com.universidad.tutorias.application.dto.ResultadoAsignacion;
import com.universidad.tutorias.application.dto.TipoErrorAsignacion;
import com.universidad.tutorias.application.service.AsignacionService;
import com.universidad.tutorias.application.service.AuditoriaService;
import com.universidad.tutorias.application.service.SemestreService;
import com.universidad.tutorias.application.service.TutorSincronizacionService;
import com.universidad.tutorias.domain.entity.*;
import com.universidad.tutorias.domain.enums.*;
import com.universidad.tutorias.domain.exception.CapacidadExcedidaException;
import com.universidad.tutorias.domain.exception.DuplicadoException;
import com.universidad.tutorias.domain.exception.SinTutorDisponibleException;
import com.universidad.tutorias.domain.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsignacionServiceImpl implements AsignacionService {

    private final AlumnoRepository alumnoRepository;
    private final TutorRepository tutorRepository;
    private final AsignacionRepository asignacionRepository;
    private final MatrizAfinidadCarreraRepository matrizAfinidadRepository;
    private final AlertaProcesoRepository alertaRepository;
    private final ProcesoAsignacionRepository procesoRepository;
    private final AuditoriaService auditoriaService;
    private final ErrorValidacionRepository errorValidacionRepository;
    private final TutorSincronizacionService tutorSincronizacionService;
    private final SemestreService semestreService;

    @PersistenceContext
    private EntityManager entityManager;

    private static final int BATCH_SIZE = 100;
    private final Random random = new Random();

    private static final Map<String, String> MAPEO_CARRERAS = Map.of(
            "Ingeniería Civil y Administración", "ICA",
            "Ingeniería en Energía", "IE",
            "Ingeniería en Mecatrónica", "IMECA",
            "Ingeniería en Mecánica Eléctrica", "IME",
            "Ingeniería en Sistemas Computacionales", "ISC",
            "Ingeniería en Tecnologías de Software", "ITS"
    );

    @Override
    public ResultadoAsignacion asignarAlumnos(List<AlumnoExcelDTO> alumnosValidos,
                                              Long procesoId,
                                              Long semestreId) {

        log.info("Iniciando asignación de {} alumnos para semestre ID: {}", alumnosValidos.size(), semestreId);

        // Obtener y validar el semestre
        Semestre semestre = semestreService.obtenerPorId(semestreId);
        log.info("Semestre encontrado: {} - {}", semestre.getCodigo(), semestre.getNombre());

        ProcesoAsignacion proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new EntityNotFoundException("Proceso no encontrado: " + procesoId));

        tutorSincronizacionService.recalcularCargaTodosLosTutores();

        List<AlumnoExcelDTO> alumnosOrdenados = alumnosValidos.stream()
                .sorted(Comparator.comparing(AlumnoExcelDTO::getCarrera)
                        .thenComparing(AlumnoExcelDTO::getMatricula))
                .collect(Collectors.toList());

        Map<String, List<Tutor>> tutoresPorCarrera = agruparTutoresPorCarrera();

        List<Asignacion> asignaciones = new ArrayList<>();
        List<AlertaProceso> alertas = new ArrayList<>();
        List<ErrorAsignacionDTO> errores = new ArrayList<>();
        Map<String, Integer> estadisticas = new HashMap<>();

        int procesados = 0;
        int exitosos = 0;

        for (int index = 0; index < alumnosOrdenados.size(); index++) {
            AlumnoExcelDTO alumnoDTO = alumnosOrdenados.get(index);
            int filaExcel = index + 2; // +2 por encabezado
            try {
                ResultadoAsignacionIndividual resultado = asignarAlumnoTransaccional(
                        alumnoDTO,
                        tutoresPorCarrera,
                        proceso,
                        semestre
                );

                procesados++;

                if (resultado.isExitosa()) {
                    asignaciones.add(resultado.getAsignacion());
                    exitosos++;
                    if (resultado.getAlertas() != null) {
                        alertas.addAll(resultado.getAlertas());
                    }
                    registrarEstadistica(estadisticas, resultado.getAsignacion().getTutor());
                    actualizarTutorEnMapa(tutoresPorCarrera, resultado.getAsignacion().getTutor());
                } else {
                    if (resultado.getAlertas() != null) {
                        alertas.addAll(resultado.getAlertas());
                    }
                }

                if (procesados % BATCH_SIZE == 0) {
                    entityManager.flush();
                    entityManager.clear();
                    actualizarProceso(proceso, procesados, exitosos, errores.size(), alertas);
                }

            } catch (DuplicadoException e) {
                log.warn("Asignación duplicada para alumno {}: {}", alumnoDTO.getMatricula(), e.getMessage());
                errores.add(crearErrorAsignacion(filaExcel, alumnoDTO, TipoErrorAsignacion.ALUMNO_DUPLICADO, e.getMessage(), null));
                registrarErrorValidacion(alumnoDTO, filaExcel, proceso, TipoError.ASIGNACION_DUPLICADA, e.getMessage());
                procesados++;
            } catch (CapacidadExcedidaException e) {
                log.warn("Capacidad excedida para tutor al asignar alumno {}: {}", alumnoDTO.getMatricula(), e.getMessage());
                errores.add(crearErrorAsignacion(filaExcel, alumnoDTO, TipoErrorAsignacion.CAPACIDAD_EXCEDIDA, e.getMessage(), null));
                registrarErrorValidacion(alumnoDTO, filaExcel, proceso, TipoError.CAPACIDAD_EXCEDIDA, e.getMessage());
                procesados++;
            } catch (SinTutorDisponibleException e) {
                log.error("Sin tutor disponible para alumno {}: {}", alumnoDTO.getMatricula(), e.getMessage());
                errores.add(crearErrorAsignacion(filaExcel, alumnoDTO, TipoErrorAsignacion.SIN_TUTOR_DISPONIBLE, e.getMessage(), null));
                registrarErrorValidacion(alumnoDTO, filaExcel, proceso, TipoError.SIN_TUTOR_DISPONIBLE, e.getMessage());
                procesados++;
            } catch (Exception e) {
                log.error("Error inesperado al asignar alumno {}: {}", alumnoDTO.getMatricula(), e.getMessage(), e);
                errores.add(crearErrorAsignacion(filaExcel, alumnoDTO, TipoErrorAsignacion.ERROR_DESCONOCIDO,
                        "Ocurrió un error inesperado al asignar al alumno", e.getMessage()));
                registrarErrorValidacion(alumnoDTO, filaExcel, proceso, TipoError.ERROR_SISTEMA, e.getMessage());
                procesados++;
            }
        }

        entityManager.flush();
        actualizarProceso(proceso, procesados, exitosos, errores.size(), alertas);

        if (!alertas.isEmpty()) {
            alertaRepository.saveAll(alertas);
            log.info("Guardadas {} alertas", alertas.size());
        }

        log.info("Asignación completada. Procesados: {}, Asignados: {}, Errores: {}",
                procesados, exitosos, errores.size());

        return ResultadoAsignacion.builder()
                .totalProcesados(procesados)
                .totalAsignados(exitosos)
                .totalErrores(errores.size())
                .errores(errores)
                .alertas(alertas)
                .asignaciones(asignaciones)
                .estadisticas(estadisticas)
                .build();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    protected ResultadoAsignacionIndividual asignarAlumnoTransaccional(AlumnoExcelDTO alumnoDTO,
                                                                       Map<String, List<Tutor>> tutoresPorCarrera,
                                                                       ProcesoAsignacion proceso,
                                                                       Semestre semestre) {

        String carreraAlumno = normalizarCarrera(alumnoDTO.getCarrera());
        SeleccionTutor seleccionTutor = seleccionarTutorDisponible(tutoresPorCarrera, alumnoDTO, proceso);

        if (seleccionTutor == null || seleccionTutor.getTutor() == null) {
            throw new SinTutorDisponibleException(String.format(
                    "No hay tutores disponibles para el alumno %s de la carrera %s",
                    alumnoDTO.getMatricula(), carreraAlumno));
        }

        final Tutor tutorBase = seleccionTutor.getTutor();
        tutorSincronizacionService.recalcularCargaTutor(tutorBase.getId());
        Tutor tutor = tutorRepository.findByIdForUpdate(tutorBase.getId())
                .orElseThrow(() -> new EntityNotFoundException("Tutor no encontrado: " + tutorBase.getId()));

        if (!tutor.tieneCapacidadDisponible()) {
            throw new CapacidadExcedidaException(String.format(
                    "Tutor %s ha alcanzado su capacidad máxima (%d/%d)",
                    tutor.getNombre(), tutor.getCargaActual(), tutor.getCapacidadMax()));
        }

        // ============================================
        // BUSCAR O CREAR ALUMNO
        // ============================================

        Alumno alumno = alumnoRepository.findByMatricula(alumnoDTO.getMatricula().toUpperCase().trim())
                .orElse(null);

        boolean esAlumnoNuevo = (alumno == null);
        boolean mantuvoPrevio = false;
        boolean estabaInactivo = false;
        TipoAsignacion tipoAsignacion = TipoAsignacion.NUEVO_INGRESO;

        if (!esAlumnoNuevo) {
            // Capturar estado previo del alumno ANTES de cualquier modificación
            // Esto es crítico para el caso de reingresos desde INACTIVO
            estabaInactivo = (alumno.getEstado() == EstadoAlumno.INACTIVO);

            // ALUMNO EXISTENTE - Validar si ya tiene asignación en este semestre (con cualquier tutor)
            Optional<Asignacion> asignacionEnSemestre = asignacionRepository.findByAlumnoAndSemestreId(
                    alumno.getId(), semestre.getId());

            if (asignacionEnSemestre.isPresent()) {
                Asignacion asignacionExistente = asignacionEnSemestre.get();
                throw new DuplicadoException(String.format(
                        "El alumno %s ya cuenta con una asignación activa con el tutor %s para el semestre %s",
                        alumno.getMatricula(),
                        asignacionExistente.getTutor().getNombre(),
                        semestre.getCodigo()));
            }

            // ============================================
            // LÓGICA DE MANTENER TUTOR ANTERIOR
            // ============================================

            Tutor tutorAnterior = alumno.getTutorActual();

            if (tutorAnterior != null &&
                tutorAnterior.getActivo() &&
                tutorAnterior.tieneCapacidadDisponible()) {

                // ✅ MANTENER tutor anterior
                Tutor tutorAnteriorActualizado = tutorRepository.findByIdForUpdate(tutorAnterior.getId())
                        .orElseThrow(() -> new EntityNotFoundException("Tutor anterior no encontrado: " + tutorAnterior.getId()));

                tutor = tutorAnteriorActualizado;

                seleccionTutor = SeleccionTutor.builder()
                        .tutor(tutor)
                        .build();

                mantuvoPrevio = true;
                tipoAsignacion = TipoAsignacion.REINGRESO;

                log.debug("Alumno {} mantiene tutor anterior: {} (estado previo: {})",
                         alumno.getMatricula(), tutor.getNombre(),
                         estabaInactivo ? "INACTIVO" : "ACTIVO");
            } else {
                // ❌ REASIGNAR a nuevo tutor
                mantuvoPrevio = false;
                tipoAsignacion = TipoAsignacion.NUEVO_INGRESO;

                String motivo = tutorAnterior == null ? "sin tutor previo" :
                               !tutorAnterior.getActivo() ? "tutor inactivo" :
                               "tutor sin capacidad";

                AlertaProceso alerta = crearAlerta(
                    proceso,
                    TipoAlerta.REASIGNACION_FORZADA,
                    SeveridadAlerta.WARNING,
                    String.format("Alumno %s reasignado de %s a %s (motivo: %s)",
                            alumnoDTO.getMatricula(),
                            tutorAnterior != null ? tutorAnterior.getNombre() : "ninguno",
                            tutor.getNombre(),
                            motivo),
                    alumnoDTO,
                    tutor
                );

                log.debug("Alumno {} reasignado de {} a {} (motivo: {})",
                         alumno.getMatricula(),
                         tutorAnterior != null ? tutorAnterior.getNombre() : "ninguno",
                         tutor.getNombre(),
                         motivo);
            }
        } else {
            // ALUMNO NUEVO
            alumno = new Alumno();
            tipoAsignacion = TipoAsignacion.NUEVO_INGRESO;
            mantuvoPrevio = false;

            log.debug("Creando alumno nuevo: {}", alumnoDTO.getMatricula());
        }

        // ============================================
        // ACTUALIZAR DATOS DEL ALUMNO
        // ============================================

        alumno.setMatricula(alumnoDTO.getMatricula().toUpperCase().trim());
        alumno.setNombre(alumnoDTO.getNombre());
        alumno.setCarrera(carreraAlumno);
        alumno.setSemestre(alumnoDTO.getSemestre());
        alumno.setEstado(EstadoAlumno.ACTIVO);
        alumno.setTutorActual(tutor);

        alumno = alumnoRepository.save(alumno);

        // ============================================
        // CREAR ASIGNACIÓN CON SEMESTRE
        // ============================================

        Asignacion asignacion = new Asignacion();
        asignacion.setAlumno(alumno);
        asignacion.setTutor(tutor);
        asignacion.setSemestre(semestre);  // ← Relación nueva
        asignacion.setSemestreAcademico(semestre.getCodigo());  // ← Legacy (para compatibilidad)
        asignacion.setTipoAsignacion(tipoAsignacion);
        asignacion = asignacionRepository.save(asignacion);

        // ============================================
        // ACTUALIZAR CARGA DEL TUTOR
        // ============================================

        // IMPORTANTE: Incrementar carga en estos casos:
        // 1. Nueva asignación (NO mantuvo tutor previo)
        // 2. Reingreso desde INACTIVO (mantuvo tutor previo PERO estaba inactivo)
        //    En este caso, cuando se marcó INACTIVO se liberó el cupo,
        //    por lo tanto al regresar DEBE incrementarse nuevamente
        if (!mantuvoPrevio || estabaInactivo) {
            tutor.incrementarCarga();
            tutorRepository.save(tutor);

            String motivo = estabaInactivo ? "reingreso desde INACTIVO" :
                           mantuvoPrevio ? "reingreso con tutor previo" :
                           "nueva asignación";

            log.debug("Carga del tutor {} incrementada a {}/{} (motivo: {})",
                     tutor.getNombre(),
                     tutor.getCargaActual(),
                     tutor.getCapacidadMax(),
                     motivo);
        } else {
            log.debug("Carga del tutor {} NO incrementada (mantuvo alumno previo que estaba ACTIVO)",
                     tutor.getNombre());
        }

        auditoriaService.registrarLog(
                proceso.getId(),
                TipoAccion.ASIGNACION,
                "ALUMNO",
                alumno.getId(),
                String.format("Alumno %s asignado a tutor %s (tipo: %s)",
                             alumno.getMatricula(), tutor.getNombre(), tipoAsignacion),
                null,
                String.format("{\"id_tutor\":%d,\"id_semestre\":%d,\"tipo\":\"%s\",\"semestre\":\"%s\"}",
                        tutor.getId(), semestre.getId(), tipoAsignacion, semestre.getCodigo()),
                "SISTEMA"
        );

        List<AlertaProceso> alertas = Optional.ofNullable(seleccionTutor.getAlertas())
                .orElseGet(Collections::emptyList);

        return ResultadoAsignacionIndividual.builder()
                .exitosa(true)
                .asignacion(asignacion)
                .alertas(alertas)
                .build();
    }

    private SeleccionTutor seleccionarTutorDisponible(Map<String, List<Tutor>> tutoresPorCarrera,
                                                      AlumnoExcelDTO alumnoDTO,
                                                      ProcesoAsignacion proceso) {
        String carreraAlumno = normalizarCarrera(alumnoDTO.getCarrera());

        Optional<Tutor> tutorMismaCarrera = buscarTutorDisponible(tutoresPorCarrera.get(carreraAlumno));
        if (tutorMismaCarrera.isPresent()) {
            return SeleccionTutor.builder()
                    .tutor(tutorMismaCarrera.get())
                    .build();
        }

        List<String> carrerasCompatibles = matrizAfinidadRepository.findCarrerasCompatibles(carreraAlumno);
        for (String carreraCompatible : carrerasCompatibles) {
            Optional<Tutor> tutorCompatible = buscarTutorDisponible(tutoresPorCarrera.get(carreraCompatible));
            if (tutorCompatible.isPresent()) {
                Tutor tutor = tutorCompatible.get();
                AlertaProceso alerta = crearAlerta(
                        proceso,
                        TipoAlerta.ASIGNACION_CRUZADA,
                        SeveridadAlerta.WARNING,
                        String.format("Alumno %s de carrera %s asignado a tutor de carrera %s por disponibilidad",
                                alumnoDTO.getMatricula(), carreraAlumno, carreraCompatible),
                        alumnoDTO,
                        tutor
                );
                return SeleccionTutor.builder()
                        .tutor(tutor)
                        .alertas(Collections.singletonList(alerta))
                        .build();
            }
        }

        Optional<Tutor> tutorMenorCarga = buscarTutorMenorCarga(tutoresPorCarrera);
        if (tutorMenorCarga.isPresent()) {
            Tutor tutor = tutorMenorCarga.get();
            AlertaProceso alerta = crearAlerta(
                    proceso,
                    TipoAlerta.ASIGNACION_CRUZADA,
                    SeveridadAlerta.WARNING,
                    String.format("Alumno %s de carrera %s asignado por menor carga a tutor de carrera %s (sin compatibilidad)",
                            alumnoDTO.getMatricula(), carreraAlumno, tutor.getCarrera()),
                    alumnoDTO,
                    tutor
            );
            return SeleccionTutor.builder()
                    .tutor(tutor)
                    .alertas(Collections.singletonList(alerta))
                    .build();
        }

        return null;
    }

    private Map<String, List<Tutor>> agruparTutoresPorCarrera() {
        List<Tutor> todosLosTutores = tutorRepository.findDisponibles();
        return todosLosTutores.stream()
                .collect(Collectors.groupingBy(Tutor::getCarrera));
    }

    private void actualizarProceso(ProcesoAsignacion proceso,
                                    int procesados,
                                    int asignados,
                                    int errores,
                                    List<AlertaProceso> alertas) {
        proceso.setTotalAlumnosProcesados(procesados);
        proceso.setTotalAlumnosAsignados(asignados);
        proceso.setTotalErrores(errores);
        proceso.setTotalWarnings((int) alertas.stream()
                .filter(a -> a.getSeveridad() == SeveridadAlerta.WARNING)
                .count());
        procesoRepository.save(proceso);
    }

    private void actualizarTutorEnMapa(Map<String, List<Tutor>> mapa, Tutor tutor) {
        List<Tutor> tutoresCarrera = mapa.computeIfAbsent(tutor.getCarrera(), key -> new ArrayList<>());
        boolean actualizado = false;
        for (int i = 0; i < tutoresCarrera.size(); i++) {
            if (tutoresCarrera.get(i).getId().equals(tutor.getId())) {
                tutoresCarrera.set(i, tutor);
                actualizado = true;
                break;
            }
        }
        if (!actualizado) {
            tutoresCarrera.add(tutor);
        }
    }

    private Optional<Tutor> buscarTutorDisponible(List<Tutor> tutores) {
        if (tutores == null || tutores.isEmpty()) {
            return Optional.empty();
        }

        List<Tutor> disponibles = tutores.stream()
                .filter(Tutor::tieneCapacidadDisponible)
                .sorted(Comparator.comparingInt(Tutor::getCargaActual))
                .collect(Collectors.toList());

        if (disponibles.isEmpty()) {
            return Optional.empty();
        }

        int menorCarga = disponibles.get(0).getCargaActual();
        List<Tutor> conMenorCarga = disponibles.stream()
                .filter(t -> t.getCargaActual() == menorCarga)
                .collect(Collectors.toList());

        if (conMenorCarga.size() == 1) {
            return Optional.of(conMenorCarga.get(0));
        }
        return Optional.of(conMenorCarga.get(random.nextInt(conMenorCarga.size())));
    }

    private Optional<Tutor> buscarTutorMenorCarga(Map<String, List<Tutor>> tutoresPorCarrera) {
        return tutoresPorCarrera.values().stream()
                .flatMap(List::stream)
                .filter(Tutor::tieneCapacidadDisponible)
                .min(Comparator.comparingInt(Tutor::getCargaActual));
    }

    private ErrorAsignacionDTO crearErrorAsignacion(int filaExcel,
                                                    AlumnoExcelDTO alumnoDTO,
                                                    TipoErrorAsignacion tipo,
                                                    String mensaje,
                                                    String detalles) {
        return ErrorAsignacionDTO.builder()
                .filaExcel(filaExcel)
                .matricula(alumnoDTO.getMatricula())
                .nombreAlumno(alumnoDTO.getNombre())
                .carrera(alumnoDTO.getCarrera())
                .tipoError(tipo)
                .mensajeError(mensaje)
                .detallesTecnicos(detalles)
                .build();
    }

    private void registrarErrorValidacion(AlumnoExcelDTO alumnoDTO,
                                          int filaExcel,
                                          ProcesoAsignacion proceso,
                                          TipoError tipoError,
                                          String mensaje) {
        ErrorValidacion error = ErrorValidacion.builder()
                .proceso(proceso)
                .filaExcel(filaExcel)
                .matricula(alumnoDTO.getMatricula())
                .tipoError(tipoError)
                .descripcion(mensaje)
                .datoErroneo(String.format("%s - %s", alumnoDTO.getMatricula(), alumnoDTO.getNombre()))
                .build();
        errorValidacionRepository.save(error);
    }

    private AlertaProceso crearAlerta(ProcesoAsignacion proceso,
                                      TipoAlerta tipo,
                                      SeveridadAlerta severidad,
                                      String descripcion,
                                      AlumnoExcelDTO alumnoDTO,
                                      Tutor tutor) {

        Alumno alumno = null;
        if (alumnoDTO.getMatricula() != null) {
            alumno = alumnoRepository.findByMatricula(alumnoDTO.getMatricula().toUpperCase().trim())
                    .orElse(null);
        }

        return AlertaProceso.builder()
                .proceso(proceso)
                .tipo(tipo)
                .severidad(severidad)
                .descripcion(descripcion)
                .alumno(alumno)
                .tutor(tutor)
                .resuelta(false)
                .build();
    }

    private void registrarEstadistica(Map<String, Integer> estadisticas, Tutor tutor) {
        String clave = String.format("%s|%s", tutor.getCarrera(), tutor.getNombre());
        estadisticas.merge(clave, 1, Integer::sum);
    }

    private String normalizarCarrera(String carrera) {
        if (carrera == null) {
            return null;
        }
        String carreraTrim = carrera.trim();
        return MAPEO_CARRERAS.getOrDefault(carreraTrim, carreraTrim.toUpperCase());
    }

    @Data
    @Builder
    private static class ResultadoAsignacionIndividual {
        private boolean exitosa;
        private Asignacion asignacion;
        private List<AlertaProceso> alertas;
    }

    @Data
    @Builder
    private static class SeleccionTutor {
        private Tutor tutor;
        private List<AlertaProceso> alertas;
    }
}
