package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.ReporteAlumnoDTO;
import com.universidad.tutorias.application.dto.ReporteArchivoDTO;
import com.universidad.tutorias.application.enums.FormatoReporte;
import com.universidad.tutorias.application.service.ReporteExcelService;
import com.universidad.tutorias.application.service.ReporteExportService;
import com.universidad.tutorias.application.service.ReportePdfService;
import com.universidad.tutorias.domain.entity.Asignacion;
import com.universidad.tutorias.domain.entity.Alumno;
import com.universidad.tutorias.domain.entity.Tutor;
import com.universidad.tutorias.domain.repository.AlumnoRepository;
import com.universidad.tutorias.domain.repository.AsignacionRepository;
import com.universidad.tutorias.domain.repository.TutorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteExportServiceImpl implements ReporteExportService {

    private static final MediaType EXCEL_MEDIA_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final AsignacionRepository asignacionRepository;
    private final TutorRepository tutorRepository;
    private final AlumnoRepository alumnoRepository;
    private final ReporteExcelService reporteExcelService;
    private final ReportePdfService reportePdfService;

    @Override
    @Transactional(readOnly = true)
    public ReporteArchivoDTO generarExcelAlumnosPorTutor(Long tutorId, String periodo) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new EntityNotFoundException("Tutor no encontrado con ID: " + tutorId));

        List<Asignacion> asignaciones = obtenerAsignacionesPorTutor(tutorId, periodo);
        List<ReporteAlumnoDTO> alumnos = mapearAsignaciones(asignaciones);
        String periodoFinal = resolverPeriodo(periodo, asignaciones);

        byte[] contenido = reporteExcelService.generarReporteAlumnosTutor(tutor.getNombre(), periodoFinal, alumnos);

        String nombreArchivo = String.format("ALUMNOS_TUTOR_%s_%s.xlsx",
                sanitizarParaArchivo(tutor.getNombre()), sanitizarParaArchivo(periodoFinal));

        log.info("Reporte Excel generado para tutor {} con {} alumnos", tutor.getNombre(), alumnos.size());

        return ReporteArchivoDTO.builder()
                .fileName(nombreArchivo)
                .mediaType(EXCEL_MEDIA_TYPE)
                .contenido(contenido)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteArchivoDTO generarReporteCarrera(String codigoCarrera, String periodo, FormatoReporte formato) {
        String carreraNormalizada = normalizarCarrera(codigoCarrera);

        if (!alumnoRepository.existsByCarreraIgnoreCase(carreraNormalizada)) {
            throw new EntityNotFoundException("Carrera no encontrada: " + carreraNormalizada);
        }

        List<Asignacion> asignaciones = obtenerAsignacionesPorCarrera(carreraNormalizada, periodo);
        List<ReporteAlumnoDTO> alumnos = mapearAsignaciones(asignaciones);
        String periodoFinal = resolverPeriodo(periodo, asignaciones);

        byte[] contenido;
        MediaType mediaType;
        String extension;

        if (FormatoReporte.PDF.equals(formato)) {
            contenido = reportePdfService.generarReporteCarrera(carreraNormalizada, periodoFinal, alumnos);
            mediaType = MediaType.APPLICATION_PDF;
            extension = ".pdf";
        } else {
            contenido = reporteExcelService.generarReporteCarrera(carreraNormalizada, periodoFinal, alumnos);
            mediaType = EXCEL_MEDIA_TYPE;
            extension = ".xlsx";
        }

        String nombreArchivo = String.format("LISTA_%s_%s%s",
                sanitizarParaArchivo(carreraNormalizada), sanitizarParaArchivo(periodoFinal), extension);

        log.info("Reporte {} generado para carrera {} con {} alumnos", formato, carreraNormalizada, alumnos.size());

        return ReporteArchivoDTO.builder()
                .fileName(nombreArchivo)
                .mediaType(mediaType)
                .contenido(contenido)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteArchivoDTO> generarReportesTodasLasCarreras(String periodo, FormatoReporte formato) {
        List<String> carreras;

        if (StringUtils.hasText(periodo)) {
            carreras = asignacionRepository.findCarrerasDisponibles(periodo);
        } else {
            carreras = alumnoRepository.findDistinctCarreras();
        }

        if (carreras.isEmpty()) {
            log.warn("No se encontraron carreras para generar reportes");
            return List.of();
        }

        return carreras.stream()
                .sorted()
                .map(carrera -> generarReporteCarrera(carrera, periodo, formato))
                .collect(Collectors.toList());
    }

    private List<Asignacion> obtenerAsignacionesPorTutor(Long tutorId, String periodo) {
        if (StringUtils.hasText(periodo)) {
            return asignacionRepository.findByTutorAndSemestre(tutorId, periodo);
        }
        return asignacionRepository.findByTutorIdWithDetalles(tutorId);
    }

    private List<Asignacion> obtenerAsignacionesPorCarrera(String carrera, String periodo) {
        if (StringUtils.hasText(periodo)) {
            return asignacionRepository.findByCarreraAndSemestre(carrera, periodo);
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
