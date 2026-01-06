package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.CambioTutorRequestDTO;
import com.universidad.tutorias.application.dto.CambioTutorResponseDTO;
import com.universidad.tutorias.application.service.AuditoriaService;
import com.universidad.tutorias.application.service.SemestreService;
import com.universidad.tutorias.application.service.TutorCambioAuditoriaService;
import com.universidad.tutorias.application.service.TutorReasignacionService;
import com.universidad.tutorias.application.service.TutorSincronizacionService;
import com.universidad.tutorias.domain.entity.Alumno;
import com.universidad.tutorias.domain.entity.Asignacion;
import com.universidad.tutorias.domain.entity.Semestre;
import com.universidad.tutorias.domain.entity.Tutor;
import com.universidad.tutorias.domain.enums.TipoAccion;
import com.universidad.tutorias.domain.enums.TipoAsignacion;
import com.universidad.tutorias.domain.exception.ReasignacionTutorException;
import com.universidad.tutorias.domain.repository.AlumnoRepository;
import com.universidad.tutorias.domain.repository.AsignacionRepository;
import com.universidad.tutorias.domain.repository.TutorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class TutorReasignacionServiceImpl implements TutorReasignacionService {

    private final AlumnoRepository alumnoRepository;
    private final TutorRepository tutorRepository;
    private final AsignacionRepository asignacionRepository;
    private final AuditoriaService auditoriaService;
    private final SemestreService semestreService;
    private final TutorSincronizacionService tutorSincronizacionService;
    private final TutorCambioAuditoriaService tutorCambioAuditoriaService;

    @Override
    @Transactional
    public CambioTutorResponseDTO reasignarTutor(CambioTutorRequestDTO request) {
        Alumno alumno = alumnoRepository.findByIdForUpdate(request.getAlumnoId())
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado con ID: " + request.getAlumnoId()));

        Tutor tutorOrigen = tutorSincronizacionService.sincronizarYBloquearTutor(request.getTutorOrigenId());
        Tutor tutorDestino = tutorSincronizacionService.sincronizarYBloquearTutor(request.getTutorDestinoId());

        if (alumno.getTutorActual() == null || !alumno.getTutorActual().getId().equals(tutorOrigen.getId())) {
            throw new ReasignacionTutorException("El alumno no está asignado al tutor de origen indicado");
        }

        if (tutorOrigen.getId().equals(tutorDestino.getId())) {
            throw new ReasignacionTutorException("El tutor destino debe ser diferente al tutor origen");
        }

        if (!tutorDestino.tieneCapacidadDisponible()) {
            throw new ReasignacionTutorException("El tutor destino no tiene capacidad disponible");
        }

        if (!alumno.puedeReasignarse()) {
            throw new ReasignacionTutorException("El alumno ha alcanzado el límite de cambios de tutor permitidos");
        }

        String semestreNormalizado = request.getSemestreAcademico() != null ? request.getSemestreAcademico().trim() : "";

        Semestre semestre;
        if (!StringUtils.hasText(semestreNormalizado)) {
            semestre = semestreService.obtenerSemestreActivo()
                    .orElseThrow(() -> new ReasignacionTutorException(
                            "No hay semestre activo configurado. Envíe semestreAcademico en la solicitud o configure un semestre activo"));
            semestreNormalizado = semestre.getCodigo();
            log.info("Semestre académico no enviado, usando semestre activo {}", semestreNormalizado);
        } else {
            // Obtener la entidad Semestre por código
            semestre = semestreService.obtenerPorCodigo(semestreNormalizado);
            if (semestre == null) {
                throw new ReasignacionTutorException("Semestre no encontrado con código: " + semestreNormalizado);
            }
        }

        int cargaOrigenAntes = tutorOrigen.getCargaActual();
        int cargaDestinoAntes = tutorDestino.getCargaActual();

        tutorOrigen.decrementarCarga();
        tutorDestino.incrementarCarga();

        alumno.setTutorActual(tutorDestino);
        alumno.incrementarCambiosTutor();

        alumnoRepository.save(alumno);
        tutorRepository.save(tutorOrigen);
        tutorRepository.save(tutorDestino);

        LocalDateTime fechaCambio = LocalDateTime.now();

        // Buscar si existe asignación previa para este alumno en el semestre
        Asignacion asignacion = asignacionRepository.findByAlumnoAndSemestreAcademico(
                alumno.getId(), semestreNormalizado).orElse(null);

        if (asignacion != null) {
            // Actualizar asignación existente
            asignacion.setTutor(tutorDestino);
            asignacion.setTipoAsignacion(TipoAsignacion.NUEVO_INGRESO);
            asignacion.setFechaAsignacion(fechaCambio);
            log.info("Actualizando asignación existente {} para alumno {} en semestre {}",
                    asignacion.getId(), alumno.getId(), semestreNormalizado);
        } else {
            // Crear nueva asignación si no existe
            asignacion = new Asignacion();
            asignacion.setAlumno(alumno);
            asignacion.setTutor(tutorDestino);
            asignacion.setSemestre(semestre);
            asignacion.setTipoAsignacion(TipoAsignacion.NUEVO_INGRESO);
            asignacion.setSemestreAcademico(semestreNormalizado);
            asignacion.setFechaAsignacion(fechaCambio);
            log.info("Creando nueva asignación para alumno {} en semestre {}",
                    alumno.getId(), semestreNormalizado);
        }

        asignacionRepository.save(asignacion);

        registrarAuditoria(alumno, tutorOrigen, tutorDestino, request, cargaOrigenAntes, cargaDestinoAntes, semestreNormalizado);

        log.info("Alumno {} reasignado de tutor {} a tutor {} para el semestre {}", alumno.getId(), tutorOrigen.getId(), tutorDestino.getId(), semestreNormalizado);

        return CambioTutorResponseDTO.builder()
                .alumnoId(alumno.getId())
                .tutorAnteriorId(tutorOrigen.getId())
                .tutorNuevoId(tutorDestino.getId())
                .tutorAnteriorNombre(tutorOrigen.getNombre())
                .tutorNuevoNombre(tutorDestino.getNombre())
                .fechaCambio(fechaCambio)
                .motivo(request.getMotivo())
                .semestreAcademico(semestreNormalizado)
                .build();
    }

    private void registrarAuditoria(Alumno alumno,
                                    Tutor tutorOrigen,
                                    Tutor tutorDestino,
                                    CambioTutorRequestDTO request,
                                    int cargaOrigenAntes,
                                    int cargaDestinoAntes,
                                    String semestreNormalizado) {

        String datosAntes = String.format(
                "{\"tutor_actual\":%d,\"carga_tutor_origen\":%d,\"carga_tutor_destino\":%d,\"semestre\":\"%s\"}",
                tutorOrigen.getId(),
                cargaOrigenAntes,
                cargaDestinoAntes,
                semestreNormalizado
        );

        String datosDespues = String.format(
                "{\"tutor_actual\":%d,\"carga_tutor_origen\":%d,\"carga_tutor_destino\":%d,\"semestre\":\"%s\"}",
                tutorDestino.getId(),
                tutorOrigen.getCargaActual(),
                tutorDestino.getCargaActual(),
                semestreNormalizado
        );

        auditoriaService.registrarLog(
                null,
                TipoAccion.CAMBIO_TUTOR,
                "ALUMNO",
                alumno.getId(),
                String.format("Reasignación manual por motivo: %s", request.getMotivo()),
                datosAntes,
                datosDespues,
                request.getUsuarioResponsable()
        );
    }
}
