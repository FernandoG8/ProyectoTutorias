package com.universidad.tutorias.application.service.reportes.strategy;

import com.universidad.tutorias.application.service.reportes.dto.ReporteArchivoDTO;
import com.universidad.tutorias.application.service.reportes.dto.ReporteContexto;

public interface ReporteStrategy {

    ReporteArchivoDTO generar(ReporteContexto contexto);
}
