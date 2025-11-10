// ============================================
// REPORTE CONTROLLER
// ============================================

package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.application.dto.ReporteArchivoDTO;
import com.universidad.tutorias.application.dto.ReporteCarreraDTO;
import com.universidad.tutorias.application.enums.FormatoReporte;
import com.universidad.tutorias.application.service.ReporteExportService;
import com.universidad.tutorias.application.service.ReporteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/reportes")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReporteController {

    private final ReporteService reporteService;
    private final ReporteExportService reporteExportService;

    @GetMapping("/por-carrera")
    public ResponseEntity<ReporteCarreraDTO> generarReportePorCarrera(
            @RequestParam String semestreAcademico,
            @RequestParam(required = false) String carrera) {

        log.info("Generando reporte por carrera. Semestre: {}, Carrera: {}", semestreAcademico, carrera);

        ReporteCarreraDTO reporte = reporteService.generarReportePorCarrera(semestreAcademico, carrera);

        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/tutores/{tutorId}/alumnos/exportar")
    @PreAuthorize("hasAnyRole('COORDINADOR_TUTORIAS','SECRETARIO_ACADEMICO')")
    public ResponseEntity<byte[]> exportarAlumnosPorTutor(@PathVariable Long tutorId,
                                                          @RequestParam(value = "periodo", required = false) String periodo) {

        log.info("Exportando alumnos del tutor {} para el periodo {}", tutorId, periodo);

        ReporteArchivoDTO archivo = reporteExportService.generarExcelAlumnosPorTutor(tutorId, periodo);

        return construirRespuestaArchivo(archivo);
    }

    @GetMapping("/carreras/{codigo}/exportar")
    @PreAuthorize("hasAnyRole('COORDINADOR_TUTORIAS','SECRETARIO_ACADEMICO')")
    public ResponseEntity<byte[]> exportarCarrera(@PathVariable String codigo,
                                                  @RequestParam(value = "formato", required = false) String formato,
                                                  @RequestParam(value = "periodo", required = false) String periodo) {

        FormatoReporte formatoReporte = FormatoReporte.from(formato);

        log.info("Exportando carrera {} en formato {} para periodo {}", codigo, formatoReporte, periodo);

        ReporteArchivoDTO archivo = reporteExportService.generarReporteCarrera(codigo, periodo, formatoReporte);

        return construirRespuestaArchivo(archivo);
    }

    @GetMapping("/carreras/exportar-todos")
    @PreAuthorize("hasRole('COORDINADOR_TUTORIAS')")
    public ResponseEntity<byte[]> exportarTodasLasCarreras(@RequestParam(value = "formato", required = false) String formato,
                                                           @RequestParam(value = "periodo", required = false) String periodo) {

        FormatoReporte formatoReporte = FormatoReporte.from(formato);
        List<ReporteArchivoDTO> archivos = reporteExportService.generarReportesTodasLasCarreras(periodo, formatoReporte);

        byte[] contenidoZip = crearArchivoZip(archivos);

        String periodoParaArchivo = StringUtils.hasText(periodo) ? periodo : "SIN_PERIODO";
        String nombreArchivo = String.format("REPORTES_CARRERAS_%s.zip", sanitizarParaArchivo(periodoParaArchivo));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(nombreArchivo, StandardCharsets.UTF_8)
                .build());
        headers.setContentLength(contenidoZip.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(contenidoZip);
    }

    private ResponseEntity<byte[]> construirRespuestaArchivo(ReporteArchivoDTO archivo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(archivo.getMediaType());
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(archivo.getFileName(), StandardCharsets.UTF_8)
                .build());
        headers.setContentLength(archivo.getContenido().length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(archivo.getContenido());
    }

    private byte[] crearArchivoZip(List<ReporteArchivoDTO> archivos) {
        try (var baos = new java.io.ByteArrayOutputStream();
             var zos = new ZipOutputStream(baos)) {

            for (ReporteArchivoDTO archivo : archivos) {
                ZipEntry entry = new ZipEntry(archivo.getFileName());
                zos.putNextEntry(entry);
                zos.write(archivo.getContenido());
                zos.closeEntry();
            }

            zos.finish();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generando archivo ZIP de reportes", e);
            throw new IllegalStateException("No fue posible generar el archivo comprimido", e);
        }
    }

    private String sanitizarParaArchivo(String valor) {
        String texto = StringUtils.hasText(valor) ? valor : "SIN_PERIODO";
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalizado.replaceAll("[^A-Za-z0-9-_]", "_");
    }
}