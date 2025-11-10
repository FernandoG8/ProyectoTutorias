package com.universidad.tutorias.application.service.reportes.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.MediaType;

@Getter
@Builder
public class ReporteArchivoDTO {

    private final String fileName;
    private final MediaType mediaType;
    private final byte[] contenido;
}
