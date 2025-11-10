package com.universidad.tutorias.application.service.reportes.strategy;

import com.universidad.tutorias.application.dto.ReporteAlumnoDTO;
import com.universidad.tutorias.application.service.reportes.dto.ReporteArchivoDTO;
import com.universidad.tutorias.application.service.reportes.dto.ReporteContexto;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class ExcelReporteStrategy implements ReporteStrategy {

    private static final MediaType EXCEL_MEDIA_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    @Override
    public ReporteArchivoDTO generar(ReporteContexto contexto) {
        String[] headers = contexto.esTutor()
                ? new String[]{"No", "MATRICULA", "NOMBRE DEL ALUMNO", "CARRERA", "SEMESTRE", "PERIODO", "TUTOR", "AREA DE ATENCION", "EDIFICIO"}
                : new String[]{"No", "MATRICULA", "NOMBRE DEL ALUMNO", "PERIODO SEMESTRAL", "LICENCIATURA", "TUTOR", "AREA DE ATENCION", "EDIFICIO"};

        String sheetName = WorkbookUtil.createSafeSheetName(contexto.esTutor()
                ? obtenerValorSeguro(contexto.getNombreTutor(), "Tutor")
                : obtenerValorSeguro(contexto.getCarrera(), "Carrera"));

        byte[] contenido = crearExcel(sheetName, headers, contexto);

        return ReporteArchivoDTO.builder()
                .fileName(contexto.construirNombreArchivo(".xlsx"))
                .mediaType(EXCEL_MEDIA_TYPE)
                .contenido(contenido)
                .build();
    }

    private byte[] crearExcel(String sheetName, String[] headers, ReporteContexto contexto) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);

            CellStyle headerStyle = construirEstiloEncabezado(workbook);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            List<ReporteAlumnoDTO> alumnos = contexto.getAlumnos();
            boolean reporteTutor = contexto.esTutor();
            int rowIndex = 1;
            int contador = 1;
            for (ReporteAlumnoDTO alumno : alumnos) {
                Row row = sheet.createRow(rowIndex++);
                int columnIndex = 0;

                crearCelda(row, columnIndex++, String.valueOf(contador++));
                crearCelda(row, columnIndex++, alumno.getMatricula());
                crearCelda(row, columnIndex++, alumno.getNombreAlumno());

                if (reporteTutor) {
                    crearCelda(row, columnIndex++, alumno.getCarrera());
                    crearCelda(row, columnIndex++, alumno.getSemestre() != null ? alumno.getSemestre().toString() : "");
                    crearCelda(row, columnIndex++, alumno.getPeriodo() != null ? alumno.getPeriodo() : contexto.getPeriodo());
                } else {
                    crearCelda(row, columnIndex++, alumno.getPeriodo() != null ? alumno.getPeriodo() : contexto.getPeriodo());
                    crearCelda(row, columnIndex++, alumno.getCarrera());
                }

                crearCelda(row, columnIndex++, alumno.getTutor());
                crearCelda(row, columnIndex++, alumno.getAreaAtencion());
                crearCelda(row, columnIndex, alumno.getEdificio());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error generando archivo Excel", e);
            throw new IllegalStateException("No fue posible generar el archivo Excel", e);
        }
    }

    private CellStyle construirEstiloEncabezado(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private void crearCelda(Row row, int index, String valor) {
        Cell cell = row.createCell(index);
        cell.setCellValue(valor != null ? valor : "");
    }

    private String obtenerValorSeguro(String valor, String defecto) {
        return (valor != null && !valor.isBlank()) ? valor.trim() : defecto;
    }
}
