package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.semestre.ActualizarSemestreDTO;
import com.universidad.tutorias.application.dto.semestre.CrearSemestreDTO;
import com.universidad.tutorias.application.dto.semestre.EstadisticasSemestreDTO;
import com.universidad.tutorias.application.service.SemestreService;
import com.universidad.tutorias.domain.entity.Asignacion;
import com.universidad.tutorias.domain.entity.Semestre;
import com.universidad.tutorias.domain.entity.Tutor;
import com.universidad.tutorias.domain.exception.SemestreNotFoundException;
import com.universidad.tutorias.domain.exception.SemestreValidationException;
import com.universidad.tutorias.domain.repository.AlumnoRepository;
import com.universidad.tutorias.domain.repository.AsignacionRepository;
import com.universidad.tutorias.domain.repository.SemestreRepository;
import com.universidad.tutorias.domain.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SemestreServiceImpl implements SemestreService {

    private final SemestreRepository semestreRepository;
    private final AsignacionRepository asignacionRepository;
    private final TutorRepository tutorRepository;
    private final AlumnoRepository alumnoRepository;

    private static final Pattern PATTERN_CODIGO = Pattern.compile("^\\d{4}-\\d{4}-F[12]$");

    // ============================================
    // CRUD BÁSICO
    // ============================================

    @Override
    public Semestre crearSemestre(CrearSemestreDTO dto) {
        log.info("Creando semestre: {}", dto.getCodigo());

        // Validar formato de código
        validarFormatoCodigo(dto.getCodigo());

        // Validar que no exista
        if (existeCodigo(dto.getCodigo())) {
            throw new SemestreValidationException(
                    String.format("Ya existe un semestre con código '%s'", dto.getCodigo())
            );
        }

        // Validar fechas
        if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new SemestreValidationException(
                    "La fecha de fin debe ser posterior a la fecha de inicio"
            );
        }

        // Crear entidad
        Semestre semestre = Semestre.builder()
                .codigo(dto.getCodigo().toUpperCase())
                .nombre(dto.getNombre().trim())
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .activo(false) // Siempre inicia inactivo
                .build();

        Semestre guardado = semestreRepository.save(semestre);

        log.info("Semestre creado exitosamente: {} (ID: {})",
                 guardado.getCodigo(), guardado.getId());

        return guardado;
    }

    @Override
    public Semestre actualizarSemestre(Long id, ActualizarSemestreDTO dto) {
        log.info("Actualizando semestre ID: {}", id);

        Semestre semestre = obtenerPorId(id);

        // Validar fechas
        if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new SemestreValidationException(
                    "La fecha de fin debe ser posterior a la fecha de inicio"
            );
        }

        // Actualizar campos
        semestre.setNombre(dto.getNombre().trim());
        semestre.setFechaInicio(dto.getFechaInicio());
        semestre.setFechaFin(dto.getFechaFin());

        Semestre actualizado = semestreRepository.save(semestre);

        log.info("Semestre {} actualizado exitosamente", actualizado.getCodigo());

        return actualizado;
    }

    @Override
    public void eliminarSemestre(Long id) {
        log.warn("Eliminando semestre ID: {} (OPERACIÓN IRREVERSIBLE)", id);

        Semestre semestre = obtenerPorId(id);

        // Validar que se puede eliminar
        validarSemestreParaEliminacion(id);

        // Obtener total de asignaciones que se eliminarán
        Long totalAsignaciones = asignacionRepository.countBySemestreId(id);

        log.info("Semestre {} tiene {} asignaciones que serán eliminadas en cascada",
                 semestre.getCodigo(), totalAsignaciones);

        // Liberar cupos de tutores ANTES de eliminar
        liberarCuposTutores(semestre);

        // Eliminar (cascade eliminará asignaciones automáticamente)
        semestreRepository.delete(semestre);

        log.info("Semestre {} eliminado exitosamente ({} asignaciones eliminadas)",
                 semestre.getCodigo(), totalAsignaciones);
    }

    // ============================================
    // CONSULTAS
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public Semestre obtenerPorId(Long id) {
        return semestreRepository.findById(id)
                .orElseThrow(() -> new SemestreNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Semestre obtenerPorCodigo(String codigo) {
        return semestreRepository.findByCodigo(codigo.toUpperCase())
                .orElseThrow(() -> new SemestreNotFoundException(codigo));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Semestre> obtenerSemestreActivo() {
        return semestreRepository.findByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Semestre> listarTodos() {
        return semestreRepository.findAllByOrderByFechaInicioDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Semestre> listarUltimos(int cantidad) {
        return semestreRepository.findTop5ByOrderByFechaInicioDesc()
                .stream()
                .limit(cantidad)
                .collect(Collectors.toList());
    }

    // ============================================
    // ACTIVACIÓN
    // ============================================

    @Override
    public void activarSemestre(Long id) {
        log.info("Activando semestre ID: {}", id);

        Semestre semestre = obtenerPorId(id);

        if (semestre.estaActivo()) {
            log.warn("El semestre {} ya está activo", semestre.getCodigo());
            return;
        }

        // Desactivar todos los demás
        semestreRepository.desactivarTodos();

        // Activar el solicitado
        semestre.setActivo(true);
        semestreRepository.save(semestre);

        log.info("Semestre {} activado exitosamente", semestre.getCodigo());
    }

    @Override
    public void desactivarSemestre(Long id) {
        log.info("Desactivando semestre ID: {}", id);

        Semestre semestre = obtenerPorId(id);
        semestre.setActivo(false);
        semestreRepository.save(semestre);

        log.info("Semestre {} desactivado", semestre.getCodigo());
    }

    // ============================================
    // VALIDACIONES
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public boolean existeCodigo(String codigo) {
        return semestreRepository.existsByCodigo(codigo.toUpperCase());
    }

    @Override
    public void validarFormatoCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new SemestreValidationException(
                    "El código del semestre no puede estar vacío"
            );
        }

        String codigoUpper = codigo.toUpperCase();

        if (!PATTERN_CODIGO.matcher(codigoUpper).matches()) {
            throw new SemestreValidationException(
                    String.format(
                            "Formato de código inválido: '%s'. Use formato YYYY-YYYY-FN (ejemplo: 2025-2026-F1)",
                            codigo
                    )
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validarSemestreParaEliminacion(Long id) {
        Semestre semestre = obtenerPorId(id);

        // No permitir eliminar semestre activo
        if (semestre.estaActivo()) {
            throw new SemestreValidationException(
                    String.format(
                            "No se puede eliminar el semestre '%s' porque está activo. " +
                            "Active otro semestre primero.",
                            semestre.getCodigo()
                    )
            );
        }

        log.info("Validación de eliminación exitosa para semestre {}", semestre.getCodigo());
    }

    // ============================================
    // ESTADÍSTICAS
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public EstadisticasSemestreDTO obtenerEstadisticas(Long id) {
        log.info("Calculando estadísticas del semestre ID: {}", id);

        Semestre semestre = obtenerPorId(id);

        // Estadísticas de asignaciones
        Long totalAsignaciones = asignacionRepository.countBySemestreId(id);
        Long alumnosActivos = asignacionRepository.countActivosBySemestreId(id);
        Long alumnosInactivos = totalAsignaciones - alumnosActivos;

        // Estadísticas de tutores
        List<Tutor> tutoresActivos = tutorRepository.findAllActivos();
        int totalTutoresActivos = tutoresActivos.size();

        // Contar tutores con asignaciones en este semestre
        Set<Long> tutoresConAsignaciones = asignacionRepository.findBySemestreId(id)
                .stream()
                .map(a -> a.getTutor().getId())
                .collect(Collectors.toSet());

        int tutoresConAsignacionesCount = tutoresConAsignaciones.size();

        // Contar tutores con capacidad llena
        int tutoresLlenos = (int) tutoresActivos.stream()
                .filter(t -> t.getCargaActual() >= t.getCapacidadMax())
                .count();

        int tutoresDisponibles = totalTutoresActivos - tutoresLlenos;

        // Promedio de alumnos por tutor (solo tutores con asignaciones)
        double promedio = tutoresConAsignacionesCount > 0
                ? (double) alumnosActivos / tutoresConAsignacionesCount
                : 0.0;

        // Distribución por carrera
        Map<String, Integer> distribucionCarrera = asignacionRepository.findBySemestreId(id)
                .stream()
                .map(a -> a.getAlumno().getCarrera())
                .collect(Collectors.groupingBy(
                        carrera -> carrera,
                        Collectors.summingInt(c -> 1)
                ));

        return EstadisticasSemestreDTO.builder()
                .semestreId(semestre.getId())
                .codigo(semestre.getCodigo())
                .nombre(semestre.getNombre())
                .fechaInicio(semestre.getFechaInicio())
                .fechaFin(semestre.getFechaFin())
                .activo(semestre.getActivo())
                .estaVigente(semestre.estaVigente())
                .totalAsignaciones(totalAsignaciones)
                .alumnosActivos(alumnosActivos)
                .alumnosInactivos(alumnosInactivos)
                .totalTutoresActivos(totalTutoresActivos)
                .tutoresConAsignaciones(tutoresConAsignacionesCount)
                .tutoresConCapacidadLlena(tutoresLlenos)
                .tutoresDisponibles(tutoresDisponibles)
                .promedioAlumnosPorTutor(Math.round(promedio * 100.0) / 100.0)
                .distribucionPorCarrera(distribucionCarrera)
                .build();
    }

    // ============================================
    // CONVERSIÓN LEGACY
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public Long convertirCodigoAId(String codigoLegacy) {
        if (codigoLegacy == null || codigoLegacy.isBlank()) {
            throw new SemestreValidationException("Código de semestre vacío");
        }

        return semestreRepository.findByCodigo(codigoLegacy.toUpperCase())
                .map(Semestre::getId)
                .orElseThrow(() -> new SemestreNotFoundException(codigoLegacy));
    }

    @Override
    @Transactional(readOnly = true)
    public String convertirIdACodigo(Long id) {
        return obtenerPorId(id).getCodigo();
    }

    // ============================================
    // MÉTODOS PRIVADOS AUXILIARES
    // ============================================

    private void liberarCuposTutores(Semestre semestre) {
        log.info("Liberando cupos de tutores del semestre {}", semestre.getCodigo());

        List<Asignacion> asignaciones = asignacionRepository.findBySemestreId(semestre.getId());

        // Agrupar por tutor y contar asignaciones
        Map<Long, Integer> cargaPorTutor = asignaciones.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getTutor().getId(),
                        Collectors.summingInt(a -> 1)
                ));

        // Decrementar carga de cada tutor
        cargaPorTutor.forEach((tutorId, cantidad) -> {
            tutorRepository.findById(tutorId).ifPresent(tutor -> {
                int nuevaCarga = Math.max(0, tutor.getCargaActual() - cantidad);
                tutor.setCargaActual(nuevaCarga);
                tutorRepository.save(tutor);

                log.debug("Tutor {} liberó {} cupos (nueva carga: {})",
                         tutor.getNombre(), cantidad, nuevaCarga);
            });
        });

        log.info("Cupos liberados exitosamente para {} tutores", cargaPorTutor.size());
    }
}
