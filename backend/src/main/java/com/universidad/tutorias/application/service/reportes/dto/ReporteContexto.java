package com.universidad.tutorias.application.service.reportes.dto;

import com.universidad.tutorias.application.dto.ReporteAlumnoDTO;
import com.universidad.tutorias.application.service.reportes.ReporteTipo;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReporteContexto {

    private final ReporteTipo tipo;
    private final Long tutorId;
    private final String nombreTutor;
    private final String carrera;
    private final String periodo;
    private final List<ReporteAlumnoDTO> alumnos;
    private final String titulo;
    private final String subtitulo;
    private final String nombreArchivoBase;

    public boolean esTutor() {
        return ReporteTipo.TUTOR.equals(tipo);
    }

    public boolean esCarrera() {
        return ReporteTipo.CARRERA.equals(tipo);
    }

    public String construirNombreArchivo(String extension) {
        return nombreArchivoBase + extension;
    }
}
