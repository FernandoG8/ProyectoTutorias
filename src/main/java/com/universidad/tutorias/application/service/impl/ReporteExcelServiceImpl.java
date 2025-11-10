package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.ReporteAlumnoDTO;
import com.universidad.tutorias.application.service.ReporteExcelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ReporteExcelServiceImpl implements ReporteExcelService {

    @Override
    public byte[] generarReporteAlumnosTutor(String nombreTutor, String periodo, List<ReporteAlumnoDTO> alumnos) {
        String sheetName = sanitizeSheetName(nombreTutor, "Tutor");
        String[] headers = new String[]{
                "No",
                "MATRICULA",
                "NOMBRE DEL ALUMNO",
                "CARRERA",
                "SEMESTRE",
                "PERIODO",
                "TUTOR",
                "AREA DE ATENCION",
                "EDIFICIO"
        };

        return crearExcel(sheetName, headers, alumnos, periodo);
    }

    @Override
    public byte[] generarReporteCarrera(String codigoCarrera, String periodo, List<ReporteAlumnoDTO> alumnos) {
        String sheetName = sanitizeSheetName(codigoCarrera, "Carrera");
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

        return crearExcel(sheetName, headers, alumnos, periodo);
    }

    private byte[] crearExcel(String sheetName, String[] headers, List<ReporteAlumnoDTO> alumnos, String periodo) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);

            CellStyle headerStyle = construirEstiloEncabezado(workbook);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            boolean reporteTutor = headers.length == 9;
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
                    crearCelda(row, columnIndex++, alumno.getPeriodo() != null ? alumno.getPeriodo() : periodo);
                } else {
                    crearCelda(row, columnIndex++, alumno.getPeriodo() != null ? alumno.getPeriodo() : periodo);
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

    private String sanitizeSheetName(String value, String defaultName) {
        String baseName = (value != null && !value.isBlank()) ? value.trim() : defaultName;
        return WorkbookUtil.createSafeSheetName(baseName);
    }
}
