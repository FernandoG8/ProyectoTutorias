package com.universidad.tutorias.application.service.reportes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.universidad.tutorias.application.dto.drive.ExportJobFileDTO;
import com.universidad.tutorias.application.dto.drive.ExportJobResultDTO;
import com.universidad.tutorias.application.enums.FormatoReporte;
import com.universidad.tutorias.application.service.reportes.dto.ReporteArchivoDTO;
import com.universidad.tutorias.domain.entity.ExportJob;
import com.universidad.tutorias.domain.entity.Usuario;
import com.universidad.tutorias.domain.enums.ExportJobStatus;
import com.universidad.tutorias.infrastructure.service.google.DriveStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class DriveExportJobRunner {

    private static final String JOB_TYPE = "TUTORES_MASIVO";

    private final ReporteExportService reporteExportService;
    private final ReportDriveExportService reportDriveExportService;
    private final ExportJobService exportJobService;
    private final DriveStorageService driveStorageService;
    @Qualifier("driveExportExecutor")
    private final ThreadPoolTaskExecutor driveExportExecutor;
    private final ObjectMapper objectMapper;

    public String startJob(Usuario usuario, String semestre, FormatoReporte formato, String idempotencyKey) {
        ExportJob job = exportJobService.getOrCreateQueued(usuario.getUsername(), JOB_TYPE, idempotencyKey);
        // Si ya existe, no encolamos de nuevo
        if (job.getStatus() == ExportJobStatus.QUEUED || job.getStatus() == ExportJobStatus.RUNNING) {
            enqueueIfNeeded(job, usuario, semestre, formato);
            return job.getId();
        }
        // si ya terminó, reutilizamos jobId sin re-ejecutar
        return job.getId();
    }

    private void enqueueIfNeeded(ExportJob job, Usuario usuario, String semestre, FormatoReporte formato) {
        if (job.getStatus() == ExportJobStatus.RUNNING) {
            return;
        }
        driveExportExecutor.submit(() -> runJob(job.getId(), usuario, semestre, formato));
    }

    @org.springframework.transaction.annotation.Transactional
    public void runJob(String jobId, Usuario usuario, String semestre, FormatoReporte formato) {
        ExportJob job = exportJobService.findById(jobId);
        try {
            job.markRunning();
            job.setMessage("Preparando exportación...");
            exportJobService.save(job);

            var context = reportDriveExportService.buildContext(usuario);

            List<String> carreras = reporteExportService.obtenerCarrerasDisponibles(semestre);
            List<Long> tutores = reporteExportService.obtenerTutoresDisponibles(semestre);
            int total = carreras.size() + tutores.size();
            job.setTotal(total);
            job.setProcessed(0);
            job.setSuccessCount(0);
            job.setFailCount(0);
            exportJobService.save(job);

            String semestreFolder = StringUtils.hasText(semestre) ? semestre : "SIN_PERIODO";
            String semestreFolderId = driveStorageService.getOrCreateFolder(context, context.driveId(), semestreFolder);
            String folderLink = buildFolderLink(semestreFolderId);

            ExportJobResultDTO result = ExportJobResultDTO.builder()
                    .folderId(semestreFolderId)
                    .folderLink(folderLink)
                    .files(new ArrayList<>())
                    .build();

            AtomicInteger processed = new AtomicInteger(0);
            processCarreras(job, context, semestreFolderId, carreras, semestre, formato, result, processed);
            processTutores(job, context, semestreFolderId, tutores, semestre, formato, result, processed);

            ExportJobStatus finalStatus = job.getFailCount() != null && job.getFailCount() > 0
                    ? ExportJobStatus.PARTIAL_SUCCESS
                    : ExportJobStatus.SUCCESS;
            job.markFinished(finalStatus);
            job.setMessage(finalStatus == ExportJobStatus.SUCCESS ? "Exportación finalizada" : "Exportación finalizada con errores");
            job.setResultJson(toJson(result));
            exportJobService.save(job);
        } catch (Exception e) {
            log.error("Error ejecutando job de exportación {}: {}", jobId, e.getMessage(), e);
            job.setStatus(ExportJobStatus.FAILED);
            job.setMessage("Error en exportación: " + e.getMessage());
            job.setErrorJson(e.getMessage());
            job.setFinishedAt(java.time.LocalDateTime.now());
            exportJobService.save(job);
        }
    }

    private void processCarreras(ExportJob job,
                                 DriveStorageService.UsuarioGoogleContext context,
                                 String semestreFolderId,
                                 List<String> carreras,
                                 String periodo,
                                 FormatoReporte formato,
                                 ExportJobResultDTO result,
                                 AtomicInteger processed) {
        String categoriaFolder = driveStorageService.getOrCreateFolder(context, semestreFolderId, "Carreras");
        for (String carrera : carreras) {
            String entityFolder = sanitizar(carrera);
            String targetFolder = driveStorageService.getOrCreateFolder(context, categoriaFolder, entityFolder);
            handleSingle(job, result, processed, () -> {
                ReporteArchivoDTO archivo = reporteExportService.generarReporteCarrera(carrera, periodo, formato);
                return upload(context, targetFolder, archivo, "carrera:" + carrera, null);
            });
        }
    }

    private void processTutores(ExportJob job,
                                DriveStorageService.UsuarioGoogleContext context,
                                String semestreFolderId,
                                List<Long> tutores,
                                String periodo,
                                FormatoReporte formato,
                                ExportJobResultDTO result,
                                AtomicInteger processed) {
        String categoriaFolder = driveStorageService.getOrCreateFolder(context, semestreFolderId, "Tutores");
        for (Long tutorId : tutores) {
            handleSingle(job, result, processed, () -> {
                ReporteArchivoDTO archivo = reporteExportService.generarReporteTutor(tutorId, periodo, formato);
                String entityFolder = sanitizar(nombreBase(archivo.getFileName()));
                String targetFolder = driveStorageService.getOrCreateFolder(context, categoriaFolder, entityFolder);
                return upload(context, targetFolder, archivo, "tutor:" + tutorId, tutorId);
            });
        }
    }

    private void handleSingle(ExportJob job,
                              ExportJobResultDTO result,
                              AtomicInteger processed,
                              FileSupplier supplier) {
        try {
            ExportJobFileDTO file = supplier.get();
            result.getFiles().add(file);
            incrementSuccess(job);
        } catch (Exception e) {
            log.error("Error procesando archivo del job {}: {}", job.getId(), e.getMessage(), e);
            ExportJobFileDTO failed = ExportJobFileDTO.builder()
                    .status("FAIL")
                    .error(e.getMessage())
                    .build();
            result.getFiles().add(failed);
            incrementFail(job);
        } finally {
            int current = processed.incrementAndGet();
            job.setProcessed(current);
            job.setProgressPct(calculateProgress(job));
            exportJobService.save(job);
        }
    }

    private ExportJobFileDTO upload(DriveStorageService.UsuarioGoogleContext context,
                                    String folderId,
                                    ReporteArchivoDTO archivo,
                                    String entity,
                                    Long tutorId) {
        var res = driveStorageService.uploadFile(
                context,
                folderId,
                archivo.getFileName(),
                mediaTypeToString(archivo.getMediaType()),
                archivo.getContenido()
        );
        return ExportJobFileDTO.builder()
                .entity(entity)
                .tutorId(tutorId)
                .fileName(archivo.getFileName())
                .fileId(res.fileId())
                .webViewLink(res.webViewLink())
                .folderId(res.folderId())
                .folderLink(res.folderLink())
                .status("SUCCESS")
                .build();
    }

    private void incrementSuccess(ExportJob job) {
        job.setSuccessCount(Optional.ofNullable(job.getSuccessCount()).orElse(0) + 1);
    }

    private void incrementFail(ExportJob job) {
        job.setFailCount(Optional.ofNullable(job.getFailCount()).orElse(0) + 1);
    }

    private int calculateProgress(ExportJob job) {
        if (job.getTotal() == null || job.getTotal() == 0) return 0;
        return (int) ((job.getProcessed() * 100.0) / job.getTotal());
    }

    private String mediaTypeToString(org.springframework.http.MediaType mediaType) {
        return mediaType != null ? mediaType.toString() : "application/octet-stream";
    }

    private String nombreBase(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx > 0 ? fileName.substring(0, idx) : fileName;
    }

    private String buildFolderLink(String folderId) {
        return folderId != null ? "https://drive.google.com/drive/folders/" + folderId : null;
    }

    private String sanitizar(String valor) {
        if (!StringUtils.hasText(valor)) return "SIN_NOMBRE";
        return valor.replaceAll("[^A-Za-z0-9_\\-\\.]", "_");
    }

    private String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    @FunctionalInterface
    interface FileSupplier {
        ExportJobFileDTO get();
    }
}
