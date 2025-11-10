package com.universidad.tutorias.application.service.reportes.factory;

import com.universidad.tutorias.application.enums.FormatoReporte;
import com.universidad.tutorias.application.service.reportes.strategy.ExcelReporteStrategy;
import com.universidad.tutorias.application.service.reportes.strategy.PdfReporteStrategy;
import com.universidad.tutorias.application.service.reportes.strategy.ReporteStrategy;
import org.springframework.stereotype.Component;

@Component
public class ReporteStrategyFactory {

    private final ExcelReporteStrategy excelStrategy;
    private final PdfReporteStrategy pdfStrategy;

    public ReporteStrategyFactory(ExcelReporteStrategy excelStrategy, PdfReporteStrategy pdfStrategy) {
        this.excelStrategy = excelStrategy;
        this.pdfStrategy = pdfStrategy;
    }

    public ReporteStrategy get(FormatoReporte formato) {
        return switch (formato) {
            case EXCEL -> excelStrategy;
            case PDF -> pdfStrategy;
        };
    }
}
