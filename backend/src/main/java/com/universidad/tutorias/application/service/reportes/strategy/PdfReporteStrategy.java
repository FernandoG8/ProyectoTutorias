package com.universidad.tutorias.application.service.reportes.strategy;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.universidad.tutorias.application.dto.ReporteAlumnoDTO;
import com.universidad.tutorias.application.service.reportes.dto.ReporteArchivoDTO;
import com.universidad.tutorias.application.service.reportes.dto.ReporteContexto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Slf4j
@Component
public class PdfReporteStrategy implements ReporteStrategy {

    @Override
    public ReporteArchivoDTO generar(ReporteContexto contexto) {
        byte[] contenido = contexto.esTutor() ? generarReporteTutor(contexto) : generarReporteCarrera(contexto);

        return ReporteArchivoDTO.builder()
                .fileName(contexto.construirNombreArchivo(".pdf"))
                .mediaType(MediaType.APPLICATION_PDF)
                .contenido(contenido)
                .build();
    }

    private byte[] generarReporteTutor(ReporteContexto contexto) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.LETTER.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            agregarTitulos(document, contexto);

            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(5f);
            tabla.setSpacingAfter(10f);
            tabla.setWidths(new float[]{2.5f, 5f, 2f, 4f});

            agregarEncabezados(tabla, new String[]{"MATRICULA", "NOMBRE", "SEMESTRE", "CARRERA"});
            agregarFilasTutor(tabla, contexto.getAlumnos());

            document.add(tabla);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generando reporte PDF del tutor", e);
            throw new IllegalStateException("No fue posible generar el reporte PDF", e);
        }
    }

    private byte[] generarReporteCarrera(ReporteContexto contexto) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.LETTER.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            agregarTitulos(document, contexto);

            PdfPTable tabla = new PdfPTable(8);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(5f);
            tabla.setSpacingAfter(10f);
            tabla.setWidths(new float[]{1f, 3f, 5f, 3f, 4f, 4f, 4f, 2f});

            agregarEncabezados(tabla, new String[]{
                    "No",
                    "MATRICULA",
                    "NOMBRE DEL ALUMNO",
                    "PERIODO SEMESTRAL",
                    "LICENCIATURA",
                    "TUTOR",
                    "AREA DE ATENCION",
                    "EDIFICIO"
            });

            agregarFilasCarrera(tabla, contexto.getAlumnos(), contexto.getPeriodo());

            document.add(tabla);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generando reporte PDF de carrera", e);
            throw new IllegalStateException("No fue posible generar el reporte PDF", e);
        }
    }

    private void agregarTitulos(Document document, ReporteContexto contexto) throws DocumentException {
        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font subtituloFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        if (contexto.getTitulo() != null) {
            Paragraph titulo = new Paragraph(contexto.getTitulo(), tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
        }

        if (contexto.getSubtitulo() != null) {
            Paragraph subtitulo = new Paragraph(contexto.getSubtitulo(), subtituloFont);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(15f);
            document.add(subtitulo);
        }
    }

    private void agregarEncabezados(PdfPTable tabla, String[] headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(new Color(49, 87, 98));
            cell.setPadding(6f);
            tabla.addCell(cell);
        }
    }

    private void agregarFilasTutor(PdfPTable tabla, List<ReporteAlumnoDTO> alumnos) {
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        for (ReporteAlumnoDTO alumno : alumnos) {
            agregarCelda(tabla, alumno.getMatricula(), cellFont);
            agregarCelda(tabla, alumno.getNombreAlumno(), cellFont);
            String semestre = alumno.getSemestre() != null ? alumno.getSemestre().toString() : "";
            agregarCelda(tabla, semestre, cellFont);
            agregarCelda(tabla, alumno.getCarrera(), cellFont);
        }

        if (alumnos.isEmpty()) {
            for (int i = 0; i < 4; i++) {
                agregarCelda(tabla, "", cellFont);
            }
        }
    }

    private void agregarFilasCarrera(PdfPTable tabla, List<ReporteAlumnoDTO> alumnos, String periodoContexto) {
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        int contador = 1;
        for (ReporteAlumnoDTO alumno : alumnos) {
            agregarCelda(tabla, String.valueOf(contador++), cellFont);
            agregarCelda(tabla, alumno.getMatricula(), cellFont);
            agregarCelda(tabla, alumno.getNombreAlumno(), cellFont);
            agregarCelda(tabla, alumno.getPeriodo() != null ? String.valueOf(alumno.getSemestre()) : periodoContexto, cellFont);
            agregarCelda(tabla, alumno.getCarrera(), cellFont);
            agregarCelda(tabla, alumno.getTutor(), cellFont);
            agregarCelda(tabla, alumno.getAreaAtencion(), cellFont);
            agregarCelda(tabla, alumno.getEdificio(), cellFont);
        }

        if (alumnos.isEmpty()) {
            for (int i = 0; i < 8; i++) {
                agregarCelda(tabla, "", cellFont);
            }
        }
    }

    private void agregarCelda(PdfPTable tabla, String valor, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(valor != null ? valor : "", font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5f);
        tabla.addCell(cell);
    }
}
