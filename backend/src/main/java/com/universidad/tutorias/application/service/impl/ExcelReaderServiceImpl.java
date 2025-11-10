
package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.AlumnoExcelDTO;
import com.universidad.tutorias.application.service.ExcelReaderService;
import com.universidad.tutorias.infrastructure.exception.ExcelFormatoException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ExcelReaderServiceImpl implements ExcelReaderService {

    private static final String[] COLUMNAS_REQUERIDAS = {"matricula", "nombre", "carrera", "semestre"};

    @Override
    public List<AlumnoExcelDTO> leerArchivo(MultipartFile archivo) throws ExcelFormatoException {
        validarArchivo(archivo);

        List<AlumnoExcelDTO> alumnos = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(archivo.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Validar encabezados
            Row headerRow = sheet.getRow(0);
            validarEncabezados(headerRow);

            // Leer datos (empezar desde fila 1)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (esFilaVacia(row)) {
                    continue;
                }

                AlumnoExcelDTO alumno = parsearFila(row, i);
                alumnos.add(alumno);
            }

            log.info("Archivo Excel leído correctamente. Total filas: {}", alumnos.size());
            return alumnos;

        } catch (IOException e) {
            log.error("Error al leer archivo Excel", e);
            throw new ExcelFormatoException("Error al procesar el archivo Excel: " + e.getMessage(), e);
        }
    }

    private void validarArchivo(MultipartFile archivo) throws ExcelFormatoException {
        if (archivo == null || archivo.isEmpty()) {
            throw new ExcelFormatoException("El archivo está vacío");
        }

        String filename = archivo.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new ExcelFormatoException("El archivo debe ser formato Excel (.xlsx o .xls)");
        }
    }

    private void validarEncabezados(Row headerRow) throws ExcelFormatoException {
        if (headerRow == null) {
            throw new ExcelFormatoException("El archivo no tiene encabezados");
        }

        Map<String, Integer> columnas = new HashMap<>();
        for (Cell cell : headerRow) {
            String valor = obtenerValorCeldaString(cell).toLowerCase().trim();
            columnas.put(valor, cell.getColumnIndex());
        }

        for (String columnaRequerida : COLUMNAS_REQUERIDAS) {
            if (!columnas.containsKey(columnaRequerida)) {
                throw new ExcelFormatoException("Falta la columna requerida: " + columnaRequerida);
            }
        }
    }

    private boolean esFilaVacia(Row row) {
        if (row == null) return true;

        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String valor = obtenerValorCeldaString(cell);
                if (valor != null && !valor.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private AlumnoExcelDTO parsearFila(Row row, int numeroFila) {
        return AlumnoExcelDTO.builder()
                .fila(numeroFila + 1) // +1 porque Excel empieza en 1
                .matricula(obtenerValorCelda(row, 0))
                .nombre(obtenerValorCelda(row, 1))
                .carrera(obtenerValorCelda(row, 2))
                .semestre(obtenerValorNumerico(row, 3))
                .build();
    }

    private String obtenerValorCelda(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        return obtenerValorCeldaString(cell);
    }

    private String obtenerValorCeldaString(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return null;
            default:
                return null;
        }
    }

    private Integer obtenerValorNumerico(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String valor = cell.getStringCellValue().trim();
                if (valor.isEmpty()) return null;
                return Integer.parseInt(valor);
            }
        } catch (Exception e) {
            log.warn("Error al parsear valor numérico en fila {}, columna {}: {}",
                    row.getRowNum(), columnIndex, e.getMessage());
        }
        return null;
    }
}