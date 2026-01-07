
package com.universidad.tutorias.application.service;

import com.universidad.tutorias.application.dto.AlumnoExcelDTO;
import com.universidad.tutorias.infrastructure.exception.ExcelFormatoException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ExcelReaderService {
    /**
     * Lee un archivo Excel y convierte las filas a DTOs
     * @param archivo archivo Excel multipart
     * @return lista de AlumnoExcelDTO
     * @throws ExcelFormatoException si el formato es inválido
     */
    List<AlumnoExcelDTO> leerArchivo(MultipartFile archivo) throws ExcelFormatoException;
}