package com.universidad.tutorias.application.service.reportes;

import com.universidad.tutorias.application.dto.ReporteAlumnoDTO;
import com.universidad.tutorias.application.service.reportes.dto.ReporteContexto;
import com.universidad.tutorias.domain.entity.Alumno;
import com.universidad.tutorias.domain.entity.Asignacion;
import com.universidad.tutorias.domain.entity.Tutor;
import com.universidad.tutorias.domain.repository.AlumnoRepository;
import com.universidad.tutorias.domain.repository.AsignacionRepository;
import com.universidad.tutorias.domain.repository.TutorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteConsultaService {

    private final AsignacionRepository asignacionRepository;
    private final TutorRepository tutorRepository;
    private final AlumnoRepository alumnoRepository;

    public ReporteContexto prepararContextoTutor(Long tutorId, String periodo) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new EntityNotFoundException("Tutor no encontrado con ID: " + tutorId));

        List<Asignacion> asignaciones = obtenerAsignacionesPorTutor(tutorId, periodo);
        List<ReporteAlumnoDTO> alumnos = mapearAsignaciones(asignaciones);
        String periodoFinal = resolverPeriodo(periodo, asignaciones);

        return ReporteContexto.builder()
                .tipo(ReporteTipo.TUTOR)
                .tutorId(tutorId)
                .nombreTutor(tutor.getNombre())
                .periodo(periodoFinal)
                .alumnos(alumnos)
                .titulo("Alumnos asignados - " + tutor.getNombre())
                .subtitulo("Periodo: " + periodoFinal)
                .nombreArchivoBase(String.format("ALUMNOS_TUTOR_%s_%s",
                        sanitizarParaArchivo(tutor.getNombre()), sanitizarParaArchivo(periodoFinal)))
                .build();
    }

    public ReporteContexto prepararContextoCarrera(String codigoCarrera, String periodo) {
        String carreraNormalizada = normalizarCarrera(codigoCarrera);

        if (!alumnoRepository.existsByCarreraIgnoreCase(carreraNormalizada)) {
            throw new EntityNotFoundException("Carrera no encontrada: " + carreraNormalizada);
        }

        List<Asignacion> asignaciones = obtenerAsignacionesPorCarrera(carreraNormalizada, periodo);
        List<ReporteAlumnoDTO> alumnos = mapearAsignaciones(asignaciones);
        String periodoFinal = resolverPeriodo(periodo, asignaciones);

        return ReporteContexto.builder()
                .tipo(ReporteTipo.CARRERA)
                .carrera(carreraNormalizada)
                .periodo(periodoFinal)
                .alumnos(alumnos)
                .titulo("Lista de alumnos - " + carreraNormalizada)
                .subtitulo("Periodo: " + periodoFinal)
                .nombreArchivoBase(String.format("LISTA_%s_%s",
                        sanitizarParaArchivo(carreraNormalizada), sanitizarParaArchivo(periodoFinal)))
                .build();
    }

    public List<String> obtenerCarrerasDisponibles(String periodo) {
        if (StringUtils.hasText(periodo)) {
            return asignacionRepository.findCarrerasDisponibles(periodo);
        }
        return alumnoRepository.findDistinctCarreras();
    }

    private List<Asignacion> obtenerAsignacionesPorTutor(Long tutorId, String periodo) {
        if (StringUtils.hasText(periodo)) {
            return asignacionRepository.findByTutorAndSemestreString(tutorId, periodo);
        }
        return asignacionRepository.findByTutorIdWithDetalles(tutorId);
    }

    private List<Asignacion> obtenerAsignacionesPorCarrera(String carrera, String periodo) {
        if (StringUtils.hasText(periodo)) {
            return asignacionRepository.findByCarreraAndSemestreString(carrera, periodo);
        }
        return asignacionRepository.findByCarrera(carrera);
    }

    private List<ReporteAlumnoDTO> mapearAsignaciones(List<Asignacion> asignaciones) {
        return asignaciones.stream()
                .sorted(Comparator.comparing(a -> a.getAlumno().getMatricula()))
                .map(this::mapearAsignacion)
                .collect(Collectors.toList());
    }

    private ReporteAlumnoDTO mapearAsignacion(Asignacion asignacion) {
        Alumno alumno = asignacion.getAlumno();
        Tutor tutor = asignacion.getTutor();

        return ReporteAlumnoDTO.builder()
                .matricula(alumno.getMatricula())
                .nombreAlumno(alumno.getNombre())
                .carrera(alumno.getCarrera() != null ? alumno.getCarrera().toUpperCase(Locale.ROOT) : null)
                .semestre(alumno.getSemestre())
                .periodo(asignacion.getSemestreAcademico())
                .tutor(tutor != null ? tutor.getNombre() : null)
                .areaAtencion(tutor != null ? tutor.getAreaAtencion() : null)
                .edificio(tutor != null ? tutor.getLetraEdificio() : null)
                .build();
    }

    private String resolverPeriodo(String periodoSolicitado, List<Asignacion> asignaciones) {
        if (StringUtils.hasText(periodoSolicitado)) {
            return periodoSolicitado;
        }

        return asignaciones.stream()
                .map(Asignacion::getSemestreAcademico)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse("SIN_PERIODO");
    }

    private String normalizarCarrera(String codigoCarrera) {
        if (!StringUtils.hasText(codigoCarrera)) {
            throw new IllegalArgumentException("El código de carrera es obligatorio");
        }
        return codigoCarrera.trim().toUpperCase(Locale.ROOT);
    }

    private String sanitizarParaArchivo(String valor) {
        String texto = StringUtils.hasText(valor) ? valor : "SIN_PERIODO";
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalizado.replaceAll("[^A-Za-z0-9-_]", "_");
    }
}
