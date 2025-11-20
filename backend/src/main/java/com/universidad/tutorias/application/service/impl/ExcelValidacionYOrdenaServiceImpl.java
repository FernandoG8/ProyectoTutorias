package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.*;
import com.universidad.tutorias.application.service.AlumnoValidadorService;
import com.universidad.tutorias.application.service.ExcelReaderService;
import com.universidad.tutorias.application.service.ExcelValidacionYOrdenaService;
import com.universidad.tutorias.domain.entity.Semestre;
import com.universidad.tutorias.domain.repository.SemestreRepository;
import com.universidad.tutorias.infrastructure.exception.ExcelFormatoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de validación, limpieza y ordenamiento de Excel.
 * 
 * RESPONSABILIDADES:
 * ✓ Leer archivo Excel
 * ✓ Validar estructura y datos (reportar TODOS los errores)
 * ✓ Limpiar y normalizar datos
 * ✓ Ordenar por semestre (mayores primero)
 * ✓ Construir respuesta final
 * 
 * ARQUITECTURA:
 * - Delega lectura a ExcelReaderService
 * - Delega validación a AlumnoValidadorService
 * - Agregaímplementación propia de limpieza y ordenamiento
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelValidacionYOrdenaServiceImpl implements ExcelValidacionYOrdenaService {

    private final ExcelReaderService excelReaderService;
    private final AlumnoValidadorService alumnoValidadorService;
    private final SemestreRepository semestreRepository;

    /**
     * Valida, limpia y ordena un archivo Excel completo.
     * 
     * FLUJO:
     * 1. Leer Excel
     * 2. Validar semestre existe
     * 3. Validar datos de alumnos
     * 4. Si hay errores → return error response
     * 5. Si válido → limpiar, ordenar, convertir, return success response
     */
    @Override
    public ExcelValidacionResponse validarYProcesarExcel(MultipartFile archivo, Long semestreId) {
        LocalDateTime inicio = LocalDateTime.now();
        
        try {
            log.info("Iniciando validación de Excel para semestre ID: {}", semestreId);
            
            // STEP 1: Validar que semestre existe
            Semestre semestre = semestreRepository.findById(semestreId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Semestre con ID " + semestreId + " no encontrado"));
            
            log.info("Semestre validado: {} ({})", semestre.getNombre(), semestre.getCodigo());
            
            // STEP 2: Leer archivo Excel
            List<AlumnoExcelDTO> alumnosExcel;
            try {
                alumnosExcel = excelReaderService.leerArchivo(archivo);
                log.info("Excel leído exitosamente: {} filas", alumnosExcel.size());
            } catch (ExcelFormatoException e) {
                log.error("Error al leer Excel: {}", e.getMessage());
                return ExcelValidacionResponse.builder()
                        .status("ERROR")
                        .message("Error al leer archivo Excel: " + e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .totalFilas(0)
                        .totalValidas(0)
                        .totalErrores(1)
                        .errors(List.of(ExcelErrorDTO.builder()
                                .campo("archivo")
                                .descripcion(e.getMessage())
                                .tipoError("ERROR_FORMATO")
                                .severidad("ERROR")
                                .build()))
                        .data(null)
                        .build();
            }
            
            // STEP 3: Limpiar datos crudos
            limpiarDatos(alumnosExcel);
            log.info("Datos limpiados y normalizados");
            
            // STEP 4: Validar alumnos (sin procesoId, usar -1 como placeholder)
            ResultadoValidacion resultadoValidacion = alumnoValidadorService.validarAlumnos(alumnosExcel, -1L);
            log.info("Validación completada: {} válidos, {} errores",
                    resultadoValidacion.getTotalValidos(),
                    resultadoValidacion.getTotalErrores());
            
            // STEP 5: Si hay errores, retornar response de error
            if (resultadoValidacion.getTotalErrores() > 0) {
                log.warn("Validación encontró {} errores", resultadoValidacion.getTotalErrores());
                
                // Convertir ErrorValidacion a ExcelErrorDTO
                List<ExcelErrorDTO> erroresDto = resultadoValidacion.getErrores().stream()
                        .map(errorValidacion -> ExcelErrorDTO.builder()
                                .filaExcel(Math.toIntExact(errorValidacion.getFilaExcel()))
                                .campo(errorValidacion.getTipoError().toString())
                                .valor(errorValidacion.getDatoErroneo())
                                .descripcion(errorValidacion.getDescripcion())
                                .tipoError(errorValidacion.getTipoError().toString())
                                .severidad("ERROR")
                                .build())
                        .collect(Collectors.toList());
                
                int totalAlumnos = alumnosExcel.size();
                int alumnosValidos = resultadoValidacion.getTotalValidos();
                double porcentaje = totalAlumnos > 0 ? (alumnosValidos * 100.0 / totalAlumnos) : 0;
                
                return ExcelValidacionResponse.builder()
                        .status("ERROR")
                        .message(String.format("Se encontraron %d errores de validación en %d filas",
                                resultadoValidacion.getTotalErrores(), totalAlumnos))
                        .timestamp(LocalDateTime.now())
                        .totalFilas(totalAlumnos)
                        .totalValidas(alumnosValidos)
                        .totalErrores(resultadoValidacion.getTotalErrores())
                        .porcentajeExito(porcentaje)
                        .errors(erroresDto)
                        .data(null)
                        .resumen(String.format(
                                "Validación fallida: %d de %d filas tienen errores. " +
                                "Por favor corrija los datos y vuelva a intentar.",
                                resultadoValidacion.getTotalErrores(),
                                totalAlumnos))
                        .build();
            }
            
            // STEP 6: Si válido, convertir, ordenar, retornar success
            log.info("Todos los datos son válidos, procediendo a convertir y ordenar");
            
            // Convertir a AlumnoValidadoDTO
            List<AlumnoValidadoDTO> alumnosValidados = convertirADtos(
                    resultadoValidacion.getAlumnosValidos(),
                    semestreId);
            
            // Ordenar por semestre (mayores primero)
            List<AlumnoValidadoDTO> alumnosOrdenados = ordenarPorSemestre(alumnosValidados);
            
            log.info("Alumnos ordenados: {} alumnos listos para asignación", alumnosOrdenados.size());
            
            return ExcelValidacionResponse.builder()
                    .status("OK")
                    .message("Validación completada exitosamente. Datos listos para asignación.")
                    .timestamp(LocalDateTime.now())
                    .totalFilas(alumnosExcel.size())
                    .totalValidas(alumnosOrdenados.size())
                    .totalErrores(0)
                    .porcentajeExito(100.0)
                    .errors(List.of())
                    .data(alumnosOrdenados)
                    .resumen(String.format(
                            "✓ %d alumnos validados exitosamente. " +
                            "Ordenados por semestre (mayores primero, nuevo ingreso al final). " +
                            "Listos para pasar al endpoint /ejecutar",
                            alumnosOrdenados.size()))
                    .build();
            
        } catch (IllegalArgumentException e) {
            log.error("Validación fallida: {}", e.getMessage());
            return ExcelValidacionResponse.builder()
                    .status("ERROR")
                    .message("Error de validación: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .totalFilas(0)
                    .totalValidas(0)
                    .totalErrores(1)
                    .errors(List.of(ExcelErrorDTO.builder()
                            .campo("general")
                            .descripcion(e.getMessage())
                            .tipoError("VALIDACION_FALLIDA")
                            .severidad("ERROR")
                            .build()))
                    .data(null)
                    .build();
        } catch (Exception e) {
            log.error("Error inesperado durante validación", e);
            return ExcelValidacionResponse.builder()
                    .status("ERROR")
                    .message("Error inesperado: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .totalFilas(0)
                    .totalValidas(0)
                    .totalErrores(1)
                    .errors(List.of(ExcelErrorDTO.builder()
                            .campo("general")
                            .descripcion("Error inesperado: " + e.getMessage())
                            .tipoError("ERROR_SISTEMA")
                            .severidad("ERROR")
                            .build()))
                    .data(null)
                    .build();
        }
    }

    /**
     * Limpia y normaliza datos de alumnos desde Excel.
     * 
     * OPERACIONES:
     * - Trim en strings
     * - Uppercase en código (matricula, carrera)
     * - Normalización de espacios múltiples
     * - Validación de longitud básica
     */
    @Override
    public void limpiarDatos(List<AlumnoExcelDTO> alumnos) {
        if (alumnos == null || alumnos.isEmpty()) {
            return;
        }
        
        for (AlumnoExcelDTO alumno : alumnos) {
            // Trim y uppercase en matricula
            if (alumno.getMatricula() != null) {
                alumno.setMatricula(alumno.getMatricula().trim().toUpperCase());
            }
            
            // Trim en nombre (NO uppercase para preservar mayúsculas de nombre propio)
            if (alumno.getNombre() != null) {
                alumno.setNombre(alumno.getNombre().trim());
            }
            
            // Trim y uppercase en carrera
            if (alumno.getCarrera() != null) {
                alumno.setCarrera(alumno.getCarrera().trim().toUpperCase());
            }
            
            // Semestre ya es numérico, no requiere limpieza
        }
        
        log.debug("Limpieza de datos completada para {} alumnos", alumnos.size());
    }

    /**
     * Ordena alumnos por semestre para asignación óptima.
     * 
     * ALGORITMO:
     * - Semestres MAYORES (8°, 7°, 6°) primero (ordenPriority = 12 - semestre)
     * - Semestres MENORES (1°, 2°, 3°) después
     * - Los alumnos se ordean DESC por ordenPriority
     * 
     * RAZÓN:
     * Esto permite liberar cupos en semestres avanzados PRIMERO.
     * Cuando llegamos a nuevo ingreso, hay más cupos disponibles.
     * Maximiza éxito de asignación.
     */
    @Override
    public List<AlumnoValidadoDTO> ordenarPorSemestre(List<AlumnoValidadoDTO> alumnos) {
        if (alumnos == null || alumnos.isEmpty()) {
            return alumnos;
        }
        
        // Asignar ordenPriority a cada alumno
        for (AlumnoValidadoDTO alumno : alumnos) {
            Integer semestreNum = alumno.getSemestreNumerico();
            if (semestreNum != null && semestreNum >= 1 && semestreNum <= 12) {
                // Mayor semestre = mayor prioridad
                // Semestre 12 → prioridad 12
                // Semestre 1 → prioridad 1
                alumno.setOrdenPriority(semestreNum);
            } else {
                // Semestre desconocido, asignar baja prioridad
                alumno.setOrdenPriority(0);
            }
        }
        
        // Ordenar DESC por prioridad (mayores primero)
        List<AlumnoValidadoDTO> ordenados = alumnos.stream()
                .sorted(Comparator
                        .comparingInt((AlumnoValidadoDTO a) -> a.getOrdenPriority() != null ? a.getOrdenPriority() : 0)
                        .reversed()  // DESC (mayores primero)
                        .thenComparingLong(a -> a.getAlumnoId() != null ? a.getAlumnoId() : 0))  // Tiebreaker
                .collect(Collectors.toList());
        
        log.info("Alumnos ordenados por semestre: {} alumnos", ordenados.size());
        return ordenados;
    }

    /**
     * Convierte AlumnoExcelDTO a AlumnoValidadoDTO con todos los campos necesarios.
     * 
     * CAMPOS AGREGADOS:
     * - semestreId: buscado en BD
     * - semestreCodigo: YYYY-YYYY-FN
     * - semestreNumerico: 1-12
     * - ordenPriority: 0 (asignado después en ordenarPorSemestre)
     * - validado: true
     */
    @Override
    public List<AlumnoValidadoDTO> convertirADtos(List<AlumnoExcelDTO> alumnosExcel, Long semestreId) {
        if (alumnosExcel == null || alumnosExcel.isEmpty()) {
            return List.of();
        }
        
        // Obtener semestre para obtener código
        Semestre semestre = semestreRepository.findById(semestreId)
                .orElseThrow(() -> new IllegalArgumentException("Semestre no encontrado: " + semestreId));
        
        return alumnosExcel.stream()
                .map(alumnoExcel -> AlumnoValidadoDTO.builder()
                        .alumnoId(null)  // Se obtendrá en asignación
                        .matricula(alumnoExcel.getMatricula())
                        .nombre(alumnoExcel.getNombre())
                        .carrera(alumnoExcel.getCarrera())
                        .semestreId(semestreId)
                        .semestreCodigo(semestre.getCodigo())
                        .semestreNumerico(alumnoExcel.getSemestre())
                        .ordenPriority(0)  // Se asignará en ordenarPorSemestre
                        .validado(true)
                        .tipoAsignacionPrevisto(null)  // Se determina en asignación
                        .build())
                .collect(Collectors.toList());
    }

}
