package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.application.enums.FormatoReporte;
import com.universidad.tutorias.application.service.reportes.ReportDriveExportService;
import com.universidad.tutorias.application.service.reportes.ReporteExportService;
import com.universidad.tutorias.infrastructure.service.google.DriveStorageService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DriveController {

    private final ReportDriveExportService reportDriveExportService;
    private final DriveStorageService driveStorageService;

    @PostMapping("/drive/test")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> testDrive(Authentication authentication) {
        var usuario = (com.universidad.tutorias.domain.entity.Usuario) authentication.getPrincipal();
        var context = reportDriveExportService.buildContext(usuario);
        String testFolder = driveStorageService.getOrCreateFolder(context, context.driveId(), "TEST");
        var result = driveStorageService.uploadFile(
                context,
                testFolder,
                "hello.txt",
                "text/plain",
                "hello drive".getBytes()
        );
        Map<String, String> body = new HashMap<>();
        body.put("status", "ok");
        body.put("link", result.webViewLink());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/reportes/carrera/exportar-drive")
    @PreAuthorize("hasAnyRole('COORDINADOR_TUTORIAS','SECRETARIO_ACADEMICO')")
    public ResponseEntity<ReportDriveExportService.DriveResult> exportarCarreraDrive(
            Authentication authentication,
            @RequestParam String codigo,
            @RequestParam(required = false) String periodo,
            @RequestParam String formato
    ) {
        var usuario = (com.universidad.tutorias.domain.entity.Usuario) authentication.getPrincipal();
        FormatoReporte fmt = FormatoReporte.from(formato);
        ReportDriveExportService.DriveResult result = reportDriveExportService.exportCarrera(usuario, codigo, periodo, fmt);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reportes/tutor/exportar-drive")
    @PreAuthorize("hasAnyRole('COORDINADOR_TUTORIAS','SECRETARIO_ACADEMICO')")
    public ResponseEntity<ReportDriveExportService.DriveResult> exportarTutorDrive(
            Authentication authentication,
            @RequestParam Long tutorId,
            @RequestParam(required = false) String periodo,
            @RequestParam String formato
    ) {
        var usuario = (com.universidad.tutorias.domain.entity.Usuario) authentication.getPrincipal();
        FormatoReporte fmt = FormatoReporte.from(formato);
        ReportDriveExportService.DriveResult result = reportDriveExportService.exportTutor(usuario, tutorId, periodo, fmt);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reportes/exportar-drive/masivo")
    @PreAuthorize("hasAnyRole('COORDINADOR_TUTORIAS','SECRETARIO_ACADEMICO')")
    public ResponseEntity<ReportDriveExportService.DriveResult> exportarMasivoDrive(
            Authentication authentication,
            @RequestBody Map<String, String> body
    ) {
        var usuario = (com.universidad.tutorias.domain.entity.Usuario) authentication.getPrincipal();
        String periodo = body.get("semestre");
        FormatoReporte formato = FormatoReporte.from(body.get("formato"));
        ReportDriveExportService.DriveResult result = reportDriveExportService.exportMasivo(usuario, periodo, formato);
        return ResponseEntity.ok(result);
    }
}
