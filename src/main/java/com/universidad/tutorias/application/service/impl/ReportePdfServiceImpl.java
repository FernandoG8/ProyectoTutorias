package com.universidad.tutorias.application.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.universidad.tutorias.application.dto.ReporteAlumnoDTO;
import com.universidad.tutorias.application.service.ReportePdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@Slf4j
public class ReportePdfServiceImpl implements ReportePdfService {

    @Override
    public byte[] generarReporteCarrera(String codigoCarrera, String periodo, List<ReporteAlumnoDTO> alumnos) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.LETTER.rotate());
            PdfWriter.getInstance(document, out);

            document.open();

            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font subtituloFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            Paragraph titulo = new Paragraph("Lista de alumnos - " + codigoCarrera, tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            String periodoTexto = periodo != null ? periodo : "";
            Paragraph subtitulo = new Paragraph("Periodo: " + periodoTexto, subtituloFont);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(15f);
            document.add(subtitulo);

            PdfPTable tabla = new PdfPTable(8);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(5f);
            tabla.setSpacingAfter(10f);
            tabla.setWidths(new float[]{1f, 3f, 5f, 3f, 4f, 4f, 4f, 2f});

            String[] headers = new String[]{
                    "No",
                    "MATRICULA",
                    "NOMBRE DEL ALUMNO",
                    "PERIODO SEMESTRAL",
                    "LICENCIATURA",
                    "TUTOR",
                    "AREA DE ATENCION",
                    "EDIFICIO"
            };

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBackgroundColor(new Color(45, 68, 134));
                cell.setPadding(6f);
                tabla.addCell(cell);
            }

            int contador = 1;
            for (ReporteAlumnoDTO alumno : alumnos) {
                agregarCelda(tabla, String.valueOf(contador++), cellFont);
                agregarCelda(tabla, alumno.getMatricula(), cellFont);
                agregarCelda(tabla, alumno.getNombreAlumno(), cellFont);
                agregarCelda(tabla, alumno.getPeriodo() != null ? alumno.getPeriodo() : periodoTexto, cellFont);
                agregarCelda(tabla, alumno.getCarrera(), cellFont);
                agregarCelda(tabla, alumno.getTutor(), cellFont);
                agregarCelda(tabla, alumno.getAreaAtencion(), cellFont);
                agregarCelda(tabla, alumno.getEdificio(), cellFont);
            }

            if (alumnos.isEmpty()) {
                for (int i = 0; i < headers.length; i++) {
                    agregarCelda(tabla, "", cellFont);
                }
            }

            document.add(tabla);
            document.close();

            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generando reporte PDF", e);
            throw new IllegalStateException("No fue posible generar el reporte PDF", e);
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
