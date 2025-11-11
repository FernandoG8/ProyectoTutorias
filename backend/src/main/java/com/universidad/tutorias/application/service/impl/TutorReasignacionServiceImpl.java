package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.CambioTutorRequestDTO;
import com.universidad.tutorias.application.dto.CambioTutorResponseDTO;
import com.universidad.tutorias.application.service.AuditoriaService;
import com.universidad.tutorias.application.service.TutorReasignacionService;
import com.universidad.tutorias.domain.entity.Alumno;
import com.universidad.tutorias.domain.entity.Asignacion;
import com.universidad.tutorias.domain.entity.Tutor;
import com.universidad.tutorias.domain.enums.TipoAccion;
import com.universidad.tutorias.domain.enums.TipoAsignacion;
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

    @Override
    @Transactional
    public CambioTutorResponseDTO reasignarTutor(CambioTutorRequestDTO request) {
        Alumno alumno = alumnoRepository.findByIdForUpdate(request.getAlumnoId())
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado con ID: " + request.getAlumnoId()));

        Tutor tutorOrigen = tutorRepository.findByIdForUpdate(request.getTutorOrigenId())
                .orElseThrow(() -> new EntityNotFoundException("Tutor origen no encontrado con ID: " + request.getTutorOrigenId()));

        Tutor tutorDestino = tutorRepository.findByIdForUpdate(request.getTutorDestinoId())
                .orElseThrow(() -> new EntityNotFoundException("Tutor destino no encontrado con ID: " + request.getTutorDestinoId()));

        if (alumno.getTutorActual() == null || !alumno.getTutorActual().getId().equals(tutorOrigen.getId())) {
            throw new IllegalArgumentException("El alumno no está asignado al tutor de origen indicado");
        }

        if (tutorOrigen.getId().equals(tutorDestino.getId())) {
            throw new IllegalArgumentException("El tutor destino debe ser diferente al tutor origen");
        }

        if (!tutorDestino.tieneCapacidadDisponible()) {
            throw new IllegalArgumentException("El tutor destino no tiene capacidad disponible");
        }

        if (!alumno.puedeReasignarse()) {
            throw new IllegalStateException("El alumno ha alcanzado el límite de cambios de tutor permitidos");
        }

        String semestreNormalizado = request.getSemestreAcademico() != null ? request.getSemestreAcademico().trim() : "";
        if (!StringUtils.hasText(semestreNormalizado)) {
            throw new IllegalArgumentException("El semestre académico es obligatorio para registrar el cambio de tutor");
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

        Asignacion asignacion = new Asignacion();
        asignacion.setAlumno(alumno);
        asignacion.setTutor(tutorDestino);
        asignacion.setTipoAsignacion(TipoAsignacion.REASIGNACION);
        asignacion.setSemestreAcademico(semestreNormalizado);
        asignacion.setFechaAsignacion(fechaCambio);
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
                request.getUsuario()
        );
    }
}
