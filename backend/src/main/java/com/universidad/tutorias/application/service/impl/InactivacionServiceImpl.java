// ============================================
// INACTIVACION SERVICE IMPLEMENTATION
// ============================================

package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.ResolucionMotivoResultDTO;
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
    public void marcarInactivos(List<Alumno> alumnos, Long procesoId, Long semestreId) {
        log.info("Marcando {} alumnos como inactivos para semestre ID: {}", alumnos.size(), semestreId);

        int contador = 0;
        for (Alumno alumno : alumnos) {
            try {
                String datosAntes = String.format(
                        "{\"estado\":\"%s\",\"tutor_id\":%s,\"semestre_id\":%d}",
                        alumno.getEstado(),
                        alumno.getTutorActual() != null ? alumno.getTutorActual().getId() : "null",
                        semestreId
                );

                // Cambiar estado
                alumno.setEstado(EstadoAlumno.INACTIVO);
                alumnoRepository.save(alumno);

                // Crear registro de inactivo con referencia al semestre
                AlumnoInactivo inactivo = AlumnoInactivo.builder()
                        .alumno(alumno)
                        .motivoInactividad(MotivoInactividad.SIN_DEFINIR)
                        .tutorPreservado(alumno.getTutorActual())
                        .cupoLiberado(true)
                        .build();
                alumnoInactivoRepository.save(inactivo);

                String datosDespues = String.format(
                        "{\"estado\":\"%s\",\"motivo\":\"%s\",\"tutor_preservado_id\":%s,\"semestre_id\":%d}",
                        EstadoAlumno.INACTIVO,
                        MotivoInactividad.SIN_DEFINIR,
                        alumno.getTutorActual() != null ? alumno.getTutorActual().getId() : "null",
                        semestreId
                );

                // Auditoría
                auditoriaService.registrarLog(
                        procesoId,
                        TipoAccion.MARCADO_INACTIVOS,
                        "ALUMNO",
                        alumno.getId(),
                        String.format("Alumno %s marcado como inactivo para semestre %d", alumno.getMatricula(), semestreId),
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

    @Override
    @Transactional
    public ResolucionMotivoResultDTO cambiarMotivoInactividad(Long alumnoInactivoId, MotivoInactividad motivoInactividad) {
        log.info("Cambiando motivo de inactividad del alumno inactivo ID: {} a: {}", alumnoInactivoId, motivoInactividad);

        // 1. Obtener el registro de alumno inactivo
        AlumnoInactivo alumnoInactivo = alumnoInactivoRepository.findById(alumnoInactivoId)
                .orElseThrow(() -> new EntityNotFoundException("Alumno inactivo no encontrado: " + alumnoInactivoId));

        Alumno alumno = alumnoInactivo.getAlumno();
        Tutor tutorPreservado = alumnoInactivo.getTutorPreservado();

        // 2. Inicializar el resultado
        ResolucionMotivoResultDTO resultado = ResolucionMotivoResultDTO.builder()
                .alumnoInactivoId(alumnoInactivoId)
                .alumnoMatricula(alumno.getMatricula())
                .alumnoNombre(alumno.getNombre())
                .motivoInactividad(motivoInactividad)
                .tutorPreservadoId(tutorPreservado != null ? tutorPreservado.getId() : null)
                .tutorNombre(tutorPreservado != null ? tutorPreservado.getNombre() : "N/A")
                .build();

        // 3. Analizar si el tutor preservado tiene capacidad
        if (tutorPreservado == null) {
            resultado.setExitoso(false);
            resultado.setMensaje("No hay tutor preservado para este alumno inactivo");
            resultado.setSugerencia("El alumno no tenía tutor asignado cuando se marcó como inactivo");
            return resultado;
        }

        // 4. Sincronizar carga del tutor para obtener datos precisos
        tutorSincronizacionService.recalcularCargaTutor(tutorPreservado.getId());
        tutorPreservado = tutorRepository.findByIdForUpdate(tutorPreservado.getId())
                .orElseThrow(() -> new EntityNotFoundException("Tutor preservado no encontrado"));

        int cargaActual = tutorPreservado.getCargaActual();
        int capacidadMax = tutorPreservado.getCapacidadMax();
        int cuposDisponibles = capacidadMax - cargaActual;
        boolean tutorActivo = tutorPreservado.getActivo();
        boolean tutorTieneCapacidad = tutorActivo && cuposDisponibles > 0;

        // 5. Establecer información del tutor en el resultado
        resultado.setTutorCargaActual(cargaActual);
        resultado.setTutorCapacidadMax(capacidadMax);
        resultado.setTutorCuposDisponibles(cuposDisponibles);
        resultado.setTutorActivo(tutorActivo);
        resultado.setTutorTieneCapacidad(tutorTieneCapacidad);

        // 6. Actualizar el motivo de inactividad
        MotivoInactividad motivoAnterior = alumnoInactivo.getMotivoInactividad();
        alumnoInactivo.setMotivoInactividad(motivoInactividad);

        // 7. Determinar si debe liberar o preservar el cupo según el motivo
        // Si el motivo debe preservar tutor, mantener el alumno con su tutor
        // Si debe liberar, entonces cupoLiberado = true
        boolean debePreservar = motivoInactividad.debePreservarTutor();
        alumnoInactivo.setCupoLiberado(!debePreservar);

        // 8. Guardar cambios
        alumnoInactivoRepository.save(alumnoInactivo);

        // 9. Preparar respuesta según la situación
        if (!tutorActivo) {
            resultado.setExitoso(true);
            resultado.setMensaje(String.format("Motivo actualizado a %s", motivoInactividad.toString()));
            resultado.setSugerencia(String.format("⚠️ AVISO: El tutor %s (ID: %d) NO ESTÁ ACTIVO. El alumno podría necesitar reasignación.",
                    tutorPreservado.getNombre(), tutorPreservado.getId()));
            log.warn("Tutor preservado inactivo: {} (ID: {})", tutorPreservado.getNombre(), tutorPreservado.getId());

        } else if (debePreservar && !tutorTieneCapacidad) {
            resultado.setExitoso(true);
            resultado.setMensaje(String.format("Motivo actualizado a %s (requiere preservar tutor)", motivoInactividad.toString()));
            resultado.setSugerencia(String.format(
                    "⚠️ AVISO: El tutor %s está a CAPACIDAD MÁXIMA (%d/%d). " +
                    "Para mantener al alumno con este tutor, debe incrementar la capacidad a %d o más.",
                    tutorPreservado.getNombre(), cargaActual, capacidadMax, cargaActual + 1));
            log.warn("Tutor sin capacidad disponible: {} (ID: {}), Carga: {}/{}",
                    tutorPreservado.getNombre(), tutorPreservado.getId(), cargaActual, capacidadMax);

        } else if (debePreservar && tutorTieneCapacidad) {
            resultado.setExitoso(true);
            resultado.setMensaje(String.format("Motivo actualizado a %s - Tutor preservado tiene capacidad", motivoInactividad.toString()));
            resultado.setSugerencia(String.format("✓ El tutor %s tiene %d cupo(s) disponible(s). El alumno puede reintegrarse cuando sea necesario.",
                    tutorPreservado.getNombre(), cuposDisponibles));
            log.info("Alumno preservado con tutor que tiene capacidad: {} ", alumno.getMatricula());

        } else {
            // No debe preservar, se liberará el cupo en la próxima ejecución de liberarCupos()
            resultado.setExitoso(true);
            resultado.setMensaje(String.format("Motivo actualizado a %s - Cupo será liberado en próximo proceso", motivoInactividad.toString()));
            resultado.setSugerencia(String.format("El cupo será liberado del tutor %s en la próxima ejecución del proceso de liberación.",
                    tutorPreservado.getNombre()));
            log.info("Cupo será liberado para alumno: {} del tutor: {}", alumno.getMatricula(), tutorPreservado.getNombre());
        }

        // 10. Auditoría
        auditoriaService.registrarLog(
                null, // No hay procesoId en este contexto
                TipoAccion.RESOLUCION_MOTIVO,
                "ALUMNO_INACTIVO",
                alumnoInactivo.getId(),
                String.format("Motivo de inactividad actualizado de %s a %s", motivoAnterior, motivoInactividad),
                String.format("{\"motivo\":\"%s\",\"cupoLiberado\":%s}", motivoAnterior, !debePreservar),
                String.format("{\"motivo\":\"%s\",\"cupoLiberado\":%s}", motivoInactividad, alumnoInactivo.getCupoLiberado()),
                "USUARIO"
        );

        return resultado;
    }

}
