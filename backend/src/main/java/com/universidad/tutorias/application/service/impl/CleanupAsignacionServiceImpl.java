package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.service.CleanupAsignacionService;
import com.universidad.tutorias.application.service.AuditoriaService;
import com.universidad.tutorias.application.service.TutorSincronizacionService;
import com.universidad.tutorias.domain.entity.Asignacion;
import com.universidad.tutorias.domain.entity.Semestre;
import com.universidad.tutorias.domain.entity.Tutor;
import com.universidad.tutorias.domain.enums.TipoAccion;
import com.universidad.tutorias.domain.repository.AsignacionRepository;
import com.universidad.tutorias.domain.repository.SemestreRepository;
import com.universidad.tutorias.domain.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CleanupAsignacionServiceImpl implements CleanupAsignacionService {

    private final AsignacionRepository asignacionRepository;
    private final SemestreRepository semestreRepository;
    private final TutorRepository tutorRepository;
    private final AuditoriaService auditoriaService;
    private final TutorSincronizacionService tutorSincronizacionService;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> detectarAsignacionesCorruptas(Long semestreId) {
        log.info("Detectando asignaciones corruptas para semestre: {}", semestreId);

        Map<String, Object> resultado = new HashMap<>();

        // 1. Detectar duplicadas
        List<Asignacion> duplicadas = encontrarDuplicadasPorSemestre(semestreId);
        resultado.put("asignaciones_duplicadas", duplicadas.size());
        resultado.put("detalle_duplicadas", duplicadas.stream()
                .map(a -> Map.of(
                    "asignacion_id", a.getId(),
                    "alumno_matricula", a.getAlumno().getMatricula(),
                    "alumno_nombre", a.getAlumno().getNombre(),
                    "tutor_nombre", a.getTutor().getNombre(),
                    "fecha_asignacion", a.getFechaAsignacion().toString()
                ))
                .collect(Collectors.toList()));

        // 2. Detectar sin tutor
        List<Asignacion> sinTutor = encontrarAsignacionesSinTutor();
        resultado.put("asignaciones_sin_tutor", sinTutor.size());

        // 3. Detectar sin alumno
        List<Asignacion> sinAlumno = encontrarAsignacionesSinAlumno();
        resultado.put("asignaciones_sin_alumno", sinAlumno.size());

        // 4. Detectar sin semestre
        List<Asignacion> sinSemestre = encontrarAsignacionesSinSemestre();
        resultado.put("asignaciones_sin_semestre", sinSemestre.size());

        // 5. Total de corrupción
        int totalCorrupcion = duplicadas.size() + sinTutor.size() + sinAlumno.size() + sinSemestre.size();
        resultado.put("total_corrupcion", totalCorrupcion);
        resultado.put("estado", totalCorrupcion == 0 ? "LIMPIO" : "CORRUPTO");

        log.info("Detección completada. Total corrupción: {}", totalCorrupcion);
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> detectarAsignacionesCorruptasGlobal() {
        log.info("Detectando asignaciones corruptas GLOBALES");

        List<Semestre> semestres = semestreRepository.findAll();
        Map<String, Object> resultadoGlobal = new HashMap<>();

        int totalDuplicadas = 0;
        int totalSinTutor = encontrarAsignacionesSinTutor().size();
        int totalSinAlumno = encontrarAsignacionesSinAlumno().size();
        int totalSinSemestre = encontrarAsignacionesSinSemestre().size();

        Map<Long, Map<String, Object>> detallesPorSemestre = new HashMap<>();

        for (Semestre semestre : semestres) {
            List<Asignacion> duplicadas = encontrarDuplicadasPorSemestre(semestre.getId());
            totalDuplicadas += duplicadas.size();

            if (!duplicadas.isEmpty()) {
                detallesPorSemestre.put(semestre.getId(), Map.of(
                    "codigo_semestre", semestre.getCodigo(),
                    "duplicadas", duplicadas.size()
                ));
            }
        }

        resultadoGlobal.put("total_duplicadas", totalDuplicadas);
        resultadoGlobal.put("total_sin_tutor", totalSinTutor);
        resultadoGlobal.put("total_sin_alumno", totalSinAlumno);
        resultadoGlobal.put("total_sin_semestre", totalSinSemestre);
        resultadoGlobal.put("total_corrupcion", totalDuplicadas + totalSinTutor + totalSinAlumno + totalSinSemestre);
        resultadoGlobal.put("detalles_por_semestre", detallesPorSemestre);

        return resultadoGlobal;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asignacion> encontrarDuplicadasPorSemestre(Long semestreId) {
        log.debug("Buscando asignaciones duplicadas para semestre: {}", semestreId);

        List<Asignacion> todasAsignaciones = asignacionRepository.findBySemestreId(semestreId);

        // Agrupar por (alumno, semestre) y encontrar grupos con más de 1 asignación
        Map<String, List<Asignacion>> gruposAlumnoSemestre = todasAsignaciones.stream()
                .collect(Collectors.groupingBy(a ->
                    a.getAlumno().getId() + "_" + a.getSemestre().getId()));

        List<Asignacion> duplicadas = new ArrayList<>();
        for (List<Asignacion> grupo : gruposAlumnoSemestre.values()) {
            if (grupo.size() > 1) {
                // Las duplicadas son todas excepto la más reciente
                grupo.sort(Comparator.comparing(Asignacion::getFechaAsignacion).reversed());
                duplicadas.addAll(grupo.subList(1, grupo.size()));
            }
        }

        log.debug("Se encontraron {} asignaciones duplicadas en semestre {}", duplicadas.size(), semestreId);
        return duplicadas;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asignacion> encontrarAsignacionesSinTutor() {
        log.debug("Buscando asignaciones sin tutor válido");

        List<Asignacion> todas = asignacionRepository.findAll();
        List<Asignacion> sinTutor = todas.stream()
                .filter(a -> a.getTutor() == null)
                .collect(Collectors.toList());

        log.debug("Se encontraron {} asignaciones sin tutor", sinTutor.size());
        return sinTutor;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asignacion> encontrarAsignacionesSinAlumno() {
        log.debug("Buscando asignaciones sin alumno válido");

        List<Asignacion> todas = asignacionRepository.findAll();
        List<Asignacion> sinAlumno = todas.stream()
                .filter(a -> a.getAlumno() == null)
                .collect(Collectors.toList());

        log.debug("Se encontraron {} asignaciones sin alumno", sinAlumno.size());
        return sinAlumno;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Asignacion> encontrarAsignacionesSinSemestre() {
        log.debug("Buscando asignaciones sin semestre válido");

        List<Asignacion> todas = asignacionRepository.findAll();
        List<Asignacion> sinSemestre = todas.stream()
                .filter(a -> a.getSemestre() == null)
                .collect(Collectors.toList());

        log.debug("Se encontraron {} asignaciones sin semestre", sinSemestre.size());
        return sinSemestre;
    }

    @Override
    @Transactional
    public Map<String, Object> limpiarAsignacionesPorSemestre(Long semestreId) {
        log.warn("INICIANDO LIMPIEZA DE ASIGNACIONES PARA SEMESTRE: {}", semestreId);

        Map<String, Object> resultado = new HashMap<>();

        try {
            int eliminadas = 0;

            // 1. Eliminar asignaciones duplicadas (mantener la más reciente)
            List<Asignacion> duplicadas = encontrarDuplicadasPorSemestre(semestreId);
            for (Asignacion asignacion : duplicadas) {
                log.info("Eliminando asignación duplicada ID: {} (alumno: {}, tutor: {})",
                        asignacion.getId(),
                        asignacion.getAlumno().getMatricula(),
                        asignacion.getTutor().getNombre());

                asignacionRepository.deleteById(asignacion.getId());
                eliminadas++;

                // Registrar auditoría
                auditoriaService.registrarLog(
                        null,
                        TipoAccion.ELIMINACION_ASIGNACION,
                        "ASIGNACION",
                        asignacion.getId(),
                        String.format("Asignación duplicada eliminada (limpieza): alumno %s, tutor %s, semestre %s",
                                asignacion.getAlumno().getMatricula(),
                                asignacion.getTutor().getNombre(),
                                asignacion.getSemestre().getCodigo()),
                        null,
                        null,
                        "CLEANUP_SYSTEM"
                );
            }

            // 2. Eliminar asignaciones sin tutor
            List<Asignacion> sinTutor = encontrarAsignacionesSinTutor().stream()
                    .filter(a -> a.getSemestre() != null && a.getSemestre().getId().equals(semestreId))
                    .collect(Collectors.toList());
            for (Asignacion asignacion : sinTutor) {
                log.warn("Eliminando asignación sin tutor ID: {}", asignacion.getId());
                asignacionRepository.deleteById(asignacion.getId());
                eliminadas++;
            }

            // 3. Eliminar asignaciones sin alumno
            List<Asignacion> sinAlumno = encontrarAsignacionesSinAlumno().stream()
                    .filter(a -> a.getSemestre() != null && a.getSemestre().getId().equals(semestreId))
                    .collect(Collectors.toList());
            for (Asignacion asignacion : sinAlumno) {
                log.warn("Eliminando asignación sin alumno ID: {}", asignacion.getId());
                asignacionRepository.deleteById(asignacion.getId());
                eliminadas++;
            }

            resultado.put("estado", "EXITOSO");
            resultado.put("asignaciones_eliminadas", eliminadas);
            resultado.put("fecha_limpieza", LocalDateTime.now().toString());
            resultado.put("semestre_id", semestreId);

            log.warn("LIMPIEZA COMPLETADA: {} asignaciones eliminadas del semestre {}", eliminadas, semestreId);

        } catch (Exception e) {
            log.error("ERROR DURANTE LIMPIEZA: {}", e.getMessage(), e);
            resultado.put("estado", "ERROR");
            resultado.put("error", e.getMessage());
        }

        return resultado;
    }

    @Override
    @Transactional
    public Map<String, Object> limpiarAsignacionesGlobal() {
        log.warn("INICIANDO LIMPIEZA GLOBAL DE ASIGNACIONES");

        Map<String, Object> resultado = new HashMap<>();
        int totalEliminadas = 0;
        Map<Long, Integer> eliminadasPorSemestre = new HashMap<>();

        try {
            List<Semestre> semestres = semestreRepository.findAll();

            for (Semestre semestre : semestres) {
                log.info("Limpiando semestre: {} ({})", semestre.getId(), semestre.getCodigo());

                Map<String, Object> resultadoSemestre = limpiarAsignacionesPorSemestre(semestre.getId());

                if ("EXITOSO".equals(resultadoSemestre.get("estado"))) {
                    int eliminadas = (Integer) resultadoSemestre.get("asignaciones_eliminadas");
                    eliminadasPorSemestre.put(semestre.getId(), eliminadas);
                    totalEliminadas += eliminadas;
                }
            }

            // Eliminar referencias globales sin semestre
            List<Asignacion> sinSemestre = encontrarAsignacionesSinSemestre();
            for (Asignacion asignacion : sinSemestre) {
                log.warn("Eliminando asignación sin semestre ID: {}", asignacion.getId());
                asignacionRepository.deleteById(asignacion.getId());
                totalEliminadas++;
            }

            resultado.put("estado", "EXITOSO");
            resultado.put("total_asignaciones_eliminadas", totalEliminadas);
            resultado.put("eliminadas_por_semestre", eliminadasPorSemestre);
            resultado.put("fecha_limpieza_global", LocalDateTime.now().toString());

            log.warn("LIMPIEZA GLOBAL COMPLETADA: {} asignaciones eliminadas totales", totalEliminadas);

        } catch (Exception e) {
            log.error("ERROR DURANTE LIMPIEZA GLOBAL: {}", e.getMessage(), e);
            resultado.put("estado", "ERROR");
            resultado.put("error", e.getMessage());
        }

        return resultado;
    }

    @Override
    @Transactional
    public Map<String, Object> recalcularCargaDespuesLimpieza(Long semestreId) {
        log.info("Recalculando carga de tutores después de limpieza");

        Map<String, Object> resultado = new HashMap<>();
        int tutoresActualizados = 0;

        try {
            List<Tutor> tutores;

            if (semestreId != null) {
                // Recalcular solo tutores del semestre especificado
                List<Asignacion> asignaciones = asignacionRepository.findBySemestreId(semestreId);
                tutores = asignaciones.stream()
                        .map(Asignacion::getTutor)
                        .distinct()
                        .collect(Collectors.toList());

                log.info("Recalculando {} tutores para semestre {}", tutores.size(), semestreId);
            } else {
                // Recalcular todos los tutores
                tutores = tutorRepository.findAll();
                log.info("Recalculando {} tutores GLOBALMENTE", tutores.size());
            }

            for (Tutor tutor : tutores) {
                tutorSincronizacionService.recalcularCargaTutor(tutor.getId());
                tutoresActualizados++;
            }

            resultado.put("estado", "EXITOSO");
            resultado.put("tutores_actualizados", tutoresActualizados);
            resultado.put("fecha_recalculo", LocalDateTime.now().toString());

            log.info("Recalculo completado: {} tutores actualizados", tutoresActualizados);

        } catch (Exception e) {
            log.error("ERROR DURANTE RECALCULO: {}", e.getMessage(), e);
            resultado.put("estado", "ERROR");
            resultado.put("error", e.getMessage());
        }

        return resultado;
    }
}
