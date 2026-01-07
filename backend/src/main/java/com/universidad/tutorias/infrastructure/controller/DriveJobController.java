package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.application.dto.drive.ExportJobStatusResponse;
import com.universidad.tutorias.application.dto.drive.ExportMasivoJobRequest;
import com.universidad.tutorias.application.service.reportes.DriveExportJobRunner;
import com.universidad.tutorias.application.service.reportes.ExportJobService;
import com.universidad.tutorias.domain.entity.ExportJob;
import com.universidad.tutorias.domain.entity.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes/drive")
@RequiredArgsConstructor
public class DriveJobController {

    private final DriveExportJobRunner jobRunner;
    private final ExportJobService exportJobService;

    @PostMapping("/export-masivo")
    @PreAuthorize("hasAnyRole('COORDINADOR_TUTORIAS','SECRETARIO_ACADEMICO')")
    public ResponseEntity<?> iniciarExportMasivo(
            Authentication authentication,
            @Valid @RequestBody ExportMasivoJobRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        String jobId = jobRunner.startJob(usuario, request.getSemestre(), request.getFormato(), idempotencyKey);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(java.util.Map.of("jobId", jobId));
    }

    @GetMapping("/export-masivo/{jobId}/status")
    @PreAuthorize("hasAnyRole('COORDINADOR_TUTORIAS','SECRETARIO_ACADEMICO')")
    public ResponseEntity<ExportJobStatusResponse> status(
            Authentication authentication,
            @PathVariable String jobId
    ) {
        // optional: verify ownership
        ExportJob job = exportJobService.findById(jobId);
        ExportJobStatusResponse response = exportJobService.buildStatusResponse(job);
        return ResponseEntity.ok(response);
    }
}
