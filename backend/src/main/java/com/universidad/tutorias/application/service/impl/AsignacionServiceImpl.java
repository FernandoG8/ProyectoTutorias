// ============================================
// ASIGNACION SERVICE IMPLEMENTATION
// ============================================

package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.AlumnoExcelDTO;
import com.universidad.tutorias.application.dto.ResultadoAsignacion;
import com.universidad.tutorias.application.service.AsignacionService;
import com.universidad.tutorias.application.service.AuditoriaService;
import com.universidad.tutorias.domain.entity.*;
import com.universidad.tutorias.domain.enums.*;
import com.universidad.tutorias.domain.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    @PersistenceContext
    private EntityManager entityManager;

    private static final int BATCH_SIZE = 100;
    private final Random random = new Random();

    // Mapeo de carreras
    private static final Map<String, String> MAPEO_CARRERAS = Map.of(
            "Ingeniería Civil y Administración", "ICA",
            "Ingeniería en Energía", "IE",
            "Ingeniería en Mecatrónica", "IMECA",
            "Ingeniería en Mecánica Eléctrica", "IME",
            "Ingeniería en Sistemas Computacionales", "ISC",
            "Ingeniería en Tecnologías de Software", "ITS"
    );

    @Override
    @Transactional
    public ResultadoAsignacion asignarAlumnos(List<AlumnoExcelDTO> alumnosValidos,
                                              Long procesoId,
                                              String semestreAcademico) {

        log.info("Iniciando asignación de {} alumnos para semestre {}", alumnosValidos.size(), semestreAcademico);

        ProcesoAsignacion proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new EntityNotFoundException("Proceso no encontrado: " + procesoId));

        // Ordenar alumnos por carrera y matrícula
        List<AlumnoExcelDTO> alumnosOrdenados = alumnosValidos.stream()
                .sorted(Comparator.comparing(AlumnoExcelDTO::getCarrera)
                        .thenComparing(AlumnoExcelDTO::getMatricula))
                .collect(Collectors.toList());

        // Obtener tutores disponibles agrupados por carrera
        Map<String, List<Tutor>> tutoresPorCarrera = agruparTutoresPorCarrera();

        log.info("Tutores disponibles por carrera: {}",
                tutoresPorCarrera.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().size()
                        ))
        );

        // Resultado de asignaciones
        List<Asignacion> asignaciones = new ArrayList<>();
        List<AlertaProceso> alertas = new ArrayList<>();

        int procesados = 0;
        int asignados = 0;
        int errores = 0;

        for (AlumnoExcelDTO alumnoDTO : alumnosOrdenados) {
            try {
                // Verificar si ya existe en BD (caso de reingreso)
                Optional<Alumno> alumnoExistente = alumnoRepository.findByMatricula(
                        alumnoDTO.getMatricula().toUpperCase().trim()
                );

                if (alumnoExistente.isPresent() &&
                        alumnoExistente.get().getEstado() == EstadoAlumno.ACTIVO &&
                        alumnoExistente.get().getTutorActual() != null) {
                    // Ya tiene tutor asignado, no hacer nada
                    log.debug("Alumno {} ya tiene tutor asignado, se omite", alumnoDTO.getMatricula());
                    procesados++;
                    asignados++;
                    continue;
                }

                // Intentar asignación
                ResultadoAsignacionIndividual resultado = asignarAlumnoIndividual(
                        alumnoDTO,
                        tutoresPorCarrera,
                        proceso,
                        semestreAcademico
                );

                if (resultado.isExitosa()) {
                    asignaciones.add(resultado.getAsignacion());
                    asignados++;

                    // Actualizar mapa de tutores en memoria
                    actualizarTutorEnMapa(tutoresPorCarrera, resultado.getAsignacion().getTutor());
                } else {
                    if (resultado.getAlertas() != null) {
                        alertas.addAll(resultado.getAlertas());
                    }
                    errores++;
                }

                procesados++;

                // Commit por lotes
                if (procesados % BATCH_SIZE == 0) {
                    entityManager.flush();
                    entityManager.clear();

                    proceso.setTotalAlumnosProcesados(procesados);
                    proceso.setTotalAlumnosAsignados(asignados);
                    proceso.setTotalErrores(errores);
                    procesoRepository.save(proceso);

                    log.info("Progreso: {}/{} alumnos procesados ({} asignados, {} errores)",
                            procesados, alumnosOrdenados.size(), asignados, errores);
                }

            } catch (Exception e) {
                log.error("Error al asignar alumno {}: {}", alumnoDTO.getMatricula(), e.getMessage(), e);
                errores++;
            }
        }

        // Flush final
        entityManager.flush();

        // Guardar alertas
        if (!alertas.isEmpty()) {
            alertaRepository.saveAll(alertas);
            log.info("Guardadas {} alertas", alertas.size());
        }

        // Actualizar proceso final
        proceso.setTotalAlumnosProcesados(procesados);
        proceso.setTotalAlumnosAsignados(asignados);
        proceso.setTotalErrores(errores);
        proceso.setTotalWarnings(
                (int) alertas.stream()
                        .filter(a -> a.getSeveridad() == SeveridadAlerta.WARNING)
                        .count()
        );
        procesoRepository.save(proceso);

        log.info("Asignación completada. Procesados: {}, Asignados: {}, Errores: {}",
                procesados, asignados, errores);

        return ResultadoAsignacion.builder()
                .totalProcesados(procesados)
                .totalAsignados(asignados)
                .totalErrores(errores)
                .alertas(alertas)
                .asignaciones(asignaciones)
                .build();
    }

    private Map<String, List<Tutor>> agruparTutoresPorCarrera() {
        List<Tutor> todosLosTutores = tutorRepository.findDisponibles();
        return todosLosTutores.stream()
                .collect(Collectors.groupingBy(Tutor::getCarrera));
    }

    private void actualizarTutorEnMapa(Map<String, List<Tutor>> mapa, Tutor tutor) {
        List<Tutor> tutoresCarrera = mapa.get(tutor.getCarrera());
        if (tutoresCarrera != null) {
            // Actualizar el tutor en la lista
            for (int i = 0; i < tutoresCarrera.size(); i++) {
                if (tutoresCarrera.get(i).getId().equals(tutor.getId())) {
                    tutoresCarrera.set(i, tutor);
                    break;
                }
            }
        }
    }

    private ResultadoAsignacionIndividual asignarAlumnoIndividual(AlumnoExcelDTO alumnoDTO,
                                                                  Map<String, List<Tutor>> tutoresPorCarrera,
                                                                  ProcesoAsignacion proceso,
                                                                  String semestreAcademico) {

        String carreraAlumno = normalizarCarrera(alumnoDTO.getCarrera());

        // ESTRATEGIA 1: Buscar tutor de la misma carrera
        Optional<Tutor> tutorMismaCarrera = buscarTutorDisponible(tutoresPorCarrera.get(carreraAlumno));
        if (tutorMismaCarrera.isPresent()) {
            log.debug("Asignando alumno {} a tutor de su misma carrera {}",
                    alumnoDTO.getMatricula(), carreraAlumno);
            return crearAsignacion(alumnoDTO, tutorMismaCarrera.get(), proceso,
                    semestreAcademico, TipoAsignacion.INICIAL, null);
        }

        // ESTRATEGIA 2: Buscar tutor de carrera compatible
        List<String> carrerasCompatibles = matrizAfinidadRepository.findCarrerasCompatibles(carreraAlumno);
        for (String carreraCompatible : carrerasCompatibles) {
            Optional<Tutor> tutorCompatible = buscarTutorDisponible(tutoresPorCarrera.get(carreraCompatible));
            if (tutorCompatible.isPresent()) {
                log.debug("Asignando alumno {} a tutor de carrera compatible {} (alumno de {})",
                        alumnoDTO.getMatricula(), carreraCompatible, carreraAlumno);

                AlertaProceso alerta = crearAlerta(
                        proceso,
                        TipoAlerta.ASIGNACION_CRUZADA,
                        SeveridadAlerta.WARNING,
                        String.format("Alumno %s de carrera %s asignado a tutor de carrera %s por disponibilidad",
                                alumnoDTO.getMatricula(), carreraAlumno, carreraCompatible),
                        alumnoDTO,
                        tutorCompatible.get()
                );
                return crearAsignacion(alumnoDTO, tutorCompatible.get(), proceso,
                        semestreAcademico, TipoAsignacion.INICIAL, alerta);
            }
        }

        // ESTRATEGIA 3: Buscar tutor con menor carga (cualquier carrera)
        Optional<Tutor> tutorMenorCarga = buscarTutorMenorCarga(tutoresPorCarrera);
        if (tutorMenorCarga.isPresent()) {
            log.debug("Asignando alumno {} a tutor por menor carga (carrera {})",
                    alumnoDTO.getMatricula(), tutorMenorCarga.get().getCarrera());

            AlertaProceso alerta = crearAlerta(
                    proceso,
                    TipoAlerta.ASIGNACION_CRUZADA,
                    SeveridadAlerta.WARNING,
                    String.format("Alumno %s de carrera %s asignado por menor carga a tutor de carrera %s (sin compatibilidad)",
                            alumnoDTO.getMatricula(), carreraAlumno, tutorMenorCarga.get().getCarrera()),
                    alumnoDTO,
                    tutorMenorCarga.get()
            );
            return crearAsignacion(alumnoDTO, tutorMenorCarga.get(), proceso,
                    semestreAcademico, TipoAsignacion.INICIAL, alerta);
        }

        // NO SE PUDO ASIGNAR
        log.error("No hay tutores disponibles para alumno {} de carrera {}",
                alumnoDTO.getMatricula(), carreraAlumno);

        AlertaProceso alerta = crearAlerta(
                proceso,
                TipoAlerta.SIN_TUTOR_DISPONIBLE,
                SeveridadAlerta.CRITICO,
                String.format("No hay tutores disponibles para el alumno %s de carrera %s",
                        alumnoDTO.getMatricula(), carreraAlumno),
                alumnoDTO,
                null
        );

        // Crear alumno sin tutor y marcarlo como inactivo
        Alumno alumnoSinTutor = crearAlumnoSinTutor(alumnoDTO);
        alumnoRepository.save(alumnoSinTutor);

        return ResultadoAsignacionIndividual.builder()
                .exitosa(false)
                .alertas(Collections.singletonList(alerta))
                .build();
    }

    private Optional<Tutor> buscarTutorDisponible(List<Tutor> tutores) {
        if (tutores == null || tutores.isEmpty()) {
            return Optional.empty();
        }

        // Filtrar tutores con capacidad disponible
        List<Tutor> disponibles = tutores.stream()
                .filter(Tutor::tieneCapacidadDisponible)
                .collect(Collectors.toList());

        if (disponibles.isEmpty()) {
            return Optional.empty();
        }

        // Ordenar por carga actual (menor primero)
        disponibles.sort(Comparator.comparingInt(Tutor::getCargaActual));

        // Si hay empate en menor carga, seleccionar aleatoriamente
        int menorCarga = disponibles.get(0).getCargaActual();
        List<Tutor> conMenorCarga = disponibles.stream()
                .filter(t -> t.getCargaActual() == menorCarga)
                .collect(Collectors.toList());

        if (conMenorCarga.size() == 1) {
            return Optional.of(conMenorCarga.get(0));
        } else {
            // Selección aleatoria en caso de empate
            return Optional.of(conMenorCarga.get(random.nextInt(conMenorCarga.size())));
        }
    }

    private Optional<Tutor> buscarTutorMenorCarga(Map<String, List<Tutor>> tutoresPorCarrera) {
        return tutoresPorCarrera.values().stream()
                .flatMap(List::stream)
                .filter(Tutor::tieneCapacidadDisponible)
                .min(Comparator.comparingInt(Tutor::getCargaActual));
    }

    private ResultadoAsignacionIndividual crearAsignacion(AlumnoExcelDTO alumnoDTO,
                                                          Tutor tutor,
                                                          ProcesoAsignacion proceso,
                                                          String semestreAcademico,
                                                          TipoAsignacion tipo,
                                                          AlertaProceso alerta) {

        // Crear o actualizar alumno
        Alumno alumno = alumnoRepository.findByMatricula(alumnoDTO.getMatricula().toUpperCase().trim())
                .orElse(new Alumno());

        alumno.setMatricula(alumnoDTO.getMatricula().toUpperCase().trim());
        alumno.setNombre(alumnoDTO.getNombre());
        alumno.setCarrera(normalizarCarrera(alumnoDTO.getCarrera()));
        alumno.setSemestre(alumnoDTO.getSemestre());
        alumno.setEstado(EstadoAlumno.ACTIVO);
        alumno.setTutorActual(tutor);

        alumno = alumnoRepository.save(alumno);

        // Crear asignación
        Asignacion asignacion = new Asignacion();
        asignacion.setAlumno(alumno);
        asignacion.setTutor(tutor);
        asignacion.setTipoAsignacion(tipo);
        asignacion.setSemestreAcademico(semestreAcademico);
        asignacion = asignacionRepository.save(asignacion);

        // Actualizar carga del tutor
        tutor.incrementarCarga();
        tutorRepository.save(tutor);

        // Auditoría
        auditoriaService.registrarLog(
                proceso.getId(),
                TipoAccion.ASIGNACION,
                "ALUMNO",
                alumno.getId(),
                String.format("Alumno %s asignado a tutor %s", alumno.getMatricula(), tutor.getNombre()),
                null,
                String.format("{\"id_tutor\":%d,\"tipo\":\"%s\",\"semestre\":\"%s\"}",
                        tutor.getId(), tipo, semestreAcademico),
                "SISTEMA"
        );

        List<AlertaProceso> alertas = alerta != null ? Collections.singletonList(alerta) : Collections.emptyList();

        return ResultadoAsignacionIndividual.builder()
                .exitosa(true)
                .asignacion(asignacion)
                .alertas(alertas)
                .build();
    }

    private Alumno crearAlumnoSinTutor(AlumnoExcelDTO alumnoDTO) {
        Alumno alumno = new Alumno();
        alumno.setMatricula(alumnoDTO.getMatricula().toUpperCase().trim());
        alumno.setNombre(alumnoDTO.getNombre());
        alumno.setCarrera(normalizarCarrera(alumnoDTO.getCarrera()));
        alumno.setSemestre(alumnoDTO.getSemestre());
        alumno.setEstado(EstadoAlumno.INACTIVO);
        alumno.setTutorActual(null);
        return alumno;
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

    private String normalizarCarrera(String carrera) {
        if (carrera == null) return null;

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
}