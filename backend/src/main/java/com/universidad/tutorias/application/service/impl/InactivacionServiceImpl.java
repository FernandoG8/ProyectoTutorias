// ============================================
// INACTIVACION SERVICE IMPLEMENTATION
// ============================================

package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.service.AuditoriaService;
import com.universidad.tutorias.application.service.InactivacionService;
import com.universidad.tutorias.application.service.TutorSincronizacionService;
import com.universidad.tutorias.domain.entity.Alumno;
import com.universidad.tutorias.domain.entity.AlumnoInactivo;
import com.universidad.tutorias.domain.entity.Tutor;
import com.universidad.tutorias.domain.enums.EstadoAlumno;
import com.universidad.tutorias.domain.enums.MotivoInactividad;
import com.universidad.tutorias.domain.enums.TipoAccion;
import com.universidad.tutorias.domain.repository.AlumnoInactivoRepository;
import com.universidad.tutorias.domain.repository.AlumnoRepository;
import com.universidad.tutorias.domain.repository.TutorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class InactivacionServiceImpl implements InactivacionService {

    private final AlumnoRepository alumnoRepository;
    private final AlumnoInactivoRepository alumnoInactivoRepository;
    private final TutorRepository tutorRepository;
    private final AuditoriaService auditoriaService;
    private final TutorSincronizacionService tutorSincronizacionService;

    @Override
    @Transactional
    public void marcarInactivos(List<Alumno> alumnos, Long procesoId) {
        log.info("Marcando {} alumnos como inactivos", alumnos.size());

        int contador = 0;
        for (Alumno alumno : alumnos) {
            try {
                String datosAntes = String.format(
                        "{\"estado\":\"%s\",\"tutor_id\":%s}",
                        alumno.getEstado(),
                        alumno.getTutorActual() != null ? alumno.getTutorActual().getId() : "null"
                );

                // Cambiar estado
                alumno.setEstado(EstadoAlumno.INACTIVO);
                alumnoRepository.save(alumno);

                // Crear registro de inactivo
                AlumnoInactivo inactivo = AlumnoInactivo.builder()
                        .alumno(alumno)
                        .motivoInactividad(MotivoInactividad.SIN_DEFINIR)
                        .tutorPreservado(alumno.getTutorActual())
                        .cupoLiberado(true)
                        .build();
                alumnoInactivoRepository.save(inactivo);

                String datosDespues = String.format(
                        "{\"estado\":\"%s\",\"motivo\":\"%s\",\"tutor_preservado_id\":%s}",
                        EstadoAlumno.INACTIVO,
                        MotivoInactividad.SIN_DEFINIR,
                        alumno.getTutorActual() != null ? alumno.getTutorActual().getId() : "null"
                );

                // Auditoría
                auditoriaService.registrarLog(
                        procesoId,
                        TipoAccion.MARCADO_INACTIVOS,
                        "ALUMNO",
                        alumno.getId(),
                        String.format("Alumno %s marcado como inactivo", alumno.getMatricula()),
                        datosAntes,
                        datosDespues,
                        "SISTEMA"
                );

                contador++;

            } catch (Exception e) {
                log.error("Error al marcar inactivo alumno {}: {}", alumno.getMatricula(), e.getMessage());
            }
        }

        log.info("Marcados {} alumnos como inactivos exitosamente", contador);
    }

    @Override
    @Transactional
    public void liberarCupos(Long procesoId) {
        log.info("Iniciando liberación de cupos de tutores");

        List<AlumnoInactivo> inactivos = alumnoInactivoRepository.findConCupoLiberado();

        if (inactivos.isEmpty()) {
            log.info("No hay cupos que liberar");
            return;
        }

        // <- declara antes de usar
        List<AlumnoInactivo> inactivosProcesados = new ArrayList<>();

        // Agrupar por tutor preservado y recolectar los que no tienen tutor
        Map<Long, List<AlumnoInactivo>> cuposPorTutor = new HashMap<>();
        List<AlumnoInactivo> sinTutor = new ArrayList<>();

        for (AlumnoInactivo inactivo : inactivos) {
            if (Boolean.TRUE.equals(inactivo.getCupoLiberado()) && inactivo.getTutorPreservado() != null) {
                cuposPorTutor
                        .computeIfAbsent(inactivo.getTutorPreservado().getId(), k -> new ArrayList<>())
                        .add(inactivo);
            } else {
                // No se puede atribuir a un tutor; marcar como procesado para no reintentar
                sinTutor.add(inactivo);
            }
        }

        int tutoresActualizados = 0;
        int cuposTotalesLiberados = 0;

        // Procesar los que sí tienen tutor preservado
        for (Map.Entry<Long, List<AlumnoInactivo>> entry : cuposPorTutor.entrySet()) {
            try {
                Tutor tutor = tutorRepository.findByIdForUpdate(entry.getKey())
                        .orElseThrow(() -> new EntityNotFoundException("Tutor no encontrado: " + entry.getKey()));

                // Recalcular carga antes de liberar
                tutorSincronizacionService.recalcularCargaTutor(tutor.getId());
                tutor = tutorRepository.findByIdForUpdate(entry.getKey())
                        .orElseThrow(() -> new EntityNotFoundException("Tutor no encontrado: " + entry.getKey()));

                int cuposALiberar = entry.getValue().size();
                int cargaAntes = tutor.getCargaActual();
                if (cuposALiberar > cargaAntes) {
                    log.warn("Se intentó liberar {} cupos del tutor {} (ID: {}), pero solo tiene {} ocupados. Se ajustará al máximo disponible.",
                            cuposALiberar, tutor.getNombre(), tutor.getId(), cargaAntes);
                }

                int cuposEfectivos = Math.min(cuposALiberar, cargaAntes);
                tutor.sincronizarCarga(cargaAntes - cuposEfectivos);
                tutorRepository.save(tutor);

                auditoriaService.registrarLog(
                        procesoId,
                        TipoAccion.LIBERACION_CUPOS,
                        "TUTOR",
                        tutor.getId(),
                        cuposALiberar == cuposEfectivos
                                ? String.format("Liberados %d cupos del tutor %s", cuposEfectivos, tutor.getNombre())
                                : String.format("Liberados %d cupos del tutor %s (solicitados %d)", cuposEfectivos, tutor.getNombre(), cuposALiberar),
                        String.format("{\"carga_actual\":%d}", cargaAntes),
                        String.format("{\"carga_actual\":%d}", tutor.getCargaActual()),
                        "SISTEMA"
                );

                tutoresActualizados++;
                cuposTotalesLiberados += cuposEfectivos;

                // Marcar como procesados estos inactivos
                for (AlumnoInactivo ai : entry.getValue()) {
                    ai.setCupoLiberado(false);
                }
                inactivosProcesados.addAll(entry.getValue());

            } catch (Exception e) {
                log.error("Error al liberar cupos del tutor {}: {}", entry.getKey(), e.getMessage());
            }
        }

        // Marcar como procesados los inactivos sin tutor preservado (para no reintentar indefinidamente)
        for (AlumnoInactivo ai : sinTutor) {
            ai.setCupoLiberado(false);
        }
        inactivosProcesados.addAll(sinTutor);

        if (!inactivosProcesados.isEmpty()) {
            alumnoInactivoRepository.saveAll(inactivosProcesados);
        }

        log.info("Liberados {} cupos de {} tutores", cuposTotalesLiberados, tutoresActualizados);
    }

}
