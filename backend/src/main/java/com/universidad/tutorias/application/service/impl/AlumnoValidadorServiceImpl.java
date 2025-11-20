// ============================================
// ALUMNO VALIDADOR SERVICE IMPLEMENTATION
// ============================================

package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.AlumnoExcelDTO;
import com.universidad.tutorias.application.dto.ResultadoValidacion;
import com.universidad.tutorias.application.service.AlumnoValidadorService;
import com.universidad.tutorias.domain.entity.ErrorValidacion;
import com.universidad.tutorias.domain.entity.ProcesoAsignacion;
import com.universidad.tutorias.domain.enums.TipoError;
import com.universidad.tutorias.domain.repository.ErrorValidacionRepository;
import com.universidad.tutorias.domain.repository.ProcesoAsignacionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlumnoValidadorServiceImpl implements AlumnoValidadorService {

    private static final Pattern MATRICULA_PATTERN = Pattern.compile("^[A-Z0-9]{5,20}$");
    private static final List<String> CARRERAS_VALIDAS = Arrays.asList(
            "ICA", "Ingeniería Civil y Administración",
            "IE", "Ingeniería en Energía",
            "IMECA", "Ingeniería en Mecatrónica",
            "IME", "Ingeniería en Mecánica Eléctrica",
            "ISC", "Ingeniería en Sistemas Computacionales",
            "ITS", "Ingeniería en Tecnologías de Software"
    );

    private final ErrorValidacionRepository errorRepository;
    private final ProcesoAsignacionRepository procesoRepository;

    @Override
    @Transactional
    public ResultadoValidacion validarAlumnos(List<AlumnoExcelDTO> alumnos, Long procesoId) {
        List<AlumnoExcelDTO> validos = new ArrayList<>();
        List<ErrorValidacion> errores = new ArrayList<>();
        Set<String> matriculasEnArchivo = new HashSet<>();

        // Proceso es opcional: solo se requiere si procesoId es válido (> 0)
        // Si es null o <= 0, validamos sin proceso (para endpoint /validar-excel)
        ProcesoAsignacion proceso = null;
        if (procesoId != null && procesoId > 0) {
            proceso = procesoRepository.findById(procesoId)
                    .orElseThrow(() -> new EntityNotFoundException("Proceso no encontrado: " + procesoId));
        }

        for (AlumnoExcelDTO alumno : alumnos) {
            List<ErrorValidacion> erroresAlumno = new ArrayList<>();

            // Validación 1: Campos vacíos
            validarCamposVacios(alumno, erroresAlumno, proceso);

            // Validación 2: Formato de matrícula
            validarFormatoMatricula(alumno, erroresAlumno, proceso);

            // Validación 3: Carrera válida
            validarCarrera(alumno, erroresAlumno, proceso);

            // Validación 4: Semestre válido
            validarSemestre(alumno, erroresAlumno, proceso);

            // Validación 5: Matrícula duplicada en archivo
            validarMatriculaDuplicada(alumno, matriculasEnArchivo, erroresAlumno, proceso);

            if (erroresAlumno.isEmpty()) {
                validos.add(alumno);
                if (alumno.getMatricula() != null) {
                    matriculasEnArchivo.add(alumno.getMatricula().toUpperCase().trim());
                }
            } else {
                errores.addAll(erroresAlumno);
            }
        }

        // Guardar errores en BD solo si hay un proceso válido
        // Si proceso es null, no guardamos en BD (validación previa sin ejecutar)
        // Los errores se incluyen en el resultado pero no se persisten
        if (!errores.isEmpty() && proceso != null) {
            errorRepository.saveAll(errores);
            log.warn("Se encontraron {} errores de validación (guardados en BD)", errores.size());
        } else if (!errores.isEmpty()) {
            log.warn("Se encontraron {} errores de validación (no guardados en BD - validación previa sin proceso)", errores.size());
        }

        log.info("Validación completada. Válidos: {}, Errores: {}", validos.size(), errores.size());

        return ResultadoValidacion.builder()
                .alumnosValidos(validos)
                .errores(errores)
                .tieneErroresCriticos(false)
                .build();
    }

    private void validarCamposVacios(AlumnoExcelDTO alumno, List<ErrorValidacion> errores, ProcesoAsignacion proceso) {
        if (esVacio(alumno.getMatricula())) {
            errores.add(crearError(proceso, alumno, TipoError.CAMPO_VACIO,
                    "La matrícula está vacía", alumno.getMatricula()));
        }
        if (esVacio(alumno.getNombre())) {
            errores.add(crearError(proceso, alumno, TipoError.CAMPO_VACIO,
                    "El nombre está vacío", alumno.getNombre()));
        }
        if (esVacio(alumno.getCarrera())) {
            errores.add(crearError(proceso, alumno, TipoError.CAMPO_VACIO,
                    "La carrera está vacía", alumno.getCarrera()));
        }
        if (alumno.getSemestre() == null) {
            errores.add(crearError(proceso, alumno, TipoError.CAMPO_VACIO,
                    "El semestre está vacío", null));
        }
    }

    private void validarFormatoMatricula(AlumnoExcelDTO alumno, List<ErrorValidacion> errores, ProcesoAsignacion proceso) {
        if (esVacio(alumno.getMatricula())) return;

        String matricula = alumno.getMatricula().toUpperCase().trim();
        if (!MATRICULA_PATTERN.matcher(matricula).matches()) {
            errores.add(crearError(proceso, alumno, TipoError.MATRICULA_INVALIDA,
                    "Formato de matrícula inválido. Debe tener entre 5-20 caracteres alfanuméricos",
                    alumno.getMatricula()));
        }
    }

    private void validarCarrera(AlumnoExcelDTO alumno, List<ErrorValidacion> errores, ProcesoAsignacion proceso) {
        if (esVacio(alumno.getCarrera())) return;

        String carrera = alumno.getCarrera().trim();
        boolean esValida = CARRERAS_VALIDAS.stream()
                .anyMatch(c -> c.equalsIgnoreCase(carrera));

        if (!esValida) {
            errores.add(crearError(proceso, alumno, TipoError.CARRERA_INVALIDA,
                    "Carrera no reconocida: " + carrera + ". Carreras válidas: " + String.join(", ", CARRERAS_VALIDAS),
                    alumno.getCarrera()));
        }
    }

    private void validarSemestre(AlumnoExcelDTO alumno, List<ErrorValidacion> errores, ProcesoAsignacion proceso) {
        if (alumno.getSemestre() == null) return;

        if (alumno.getSemestre() < 1 || alumno.getSemestre() > 12) {
            errores.add(crearError(proceso, alumno, TipoError.SEMESTRE_INVALIDO,
                    "El semestre debe estar entre 1 y 12, valor recibido: " + alumno.getSemestre(),
                    String.valueOf(alumno.getSemestre())));
        }
    }

    private void validarMatriculaDuplicada(AlumnoExcelDTO alumno, Set<String> matriculasVistas,
                                           List<ErrorValidacion> errores, ProcesoAsignacion proceso) {
        if (esVacio(alumno.getMatricula())) return;

        String matricula = alumno.getMatricula().toUpperCase().trim();
        if (matriculasVistas.contains(matricula)) {
            errores.add(crearError(proceso, alumno, TipoError.MATRICULA_DUPLICADA,
                    "La matrícula " + matricula + " aparece duplicada en el archivo",
                    alumno.getMatricula()));
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private ErrorValidacion crearError(ProcesoAsignacion proceso, AlumnoExcelDTO alumno,
                                       TipoError tipo, String descripcion, String datoErroneo) {
        ErrorValidacion.ErrorValidacionBuilder builder = ErrorValidacion.builder()
                .filaExcel(alumno.getFila())
                .matricula(alumno.getMatricula())
                .tipoError(tipo)
                .descripcion(descripcion)
                .datoErroneo(datoErroneo);
        
        // Solo asociar proceso si existe (para validación previa, proceso puede ser null)
        if (proceso != null) {
            builder.proceso(proceso);
        }
        
        return builder.build();
    }
}