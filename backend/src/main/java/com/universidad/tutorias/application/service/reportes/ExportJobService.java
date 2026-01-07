package com.universidad.tutorias.application.service.reportes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universidad.tutorias.application.dto.drive.ExportJobResultDTO;
import com.universidad.tutorias.application.dto.drive.ExportJobStatusResponse;
import com.universidad.tutorias.domain.entity.ExportJob;
import com.universidad.tutorias.domain.enums.ExportJobStatus;
import com.universidad.tutorias.domain.repository.ExportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportJobService {

    private final ExportJobRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ExportJob getOrCreateQueued(String createdBy, String type, String idempotencyKey) {
        if (idempotencyKey != null) {
            Optional<ExportJob> existing = repository.findByCreatedByAndTypeAndIdempotencyKey(createdBy, type, idempotencyKey);
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        ExportJob job = ExportJob.builder()
                .status(ExportJobStatus.QUEUED)
                .type(type)
                .createdBy(createdBy)
                .idempotencyKey(idempotencyKey)
                .build();
        return repository.save(job);
    }

    @Transactional(readOnly = true)
    public ExportJobStatusResponse buildStatusResponse(ExportJob job) {
        ExportJobResultDTO result = null;
        if (job.getResultJson() != null) {
            try {
                result = objectMapper.readValue(job.getResultJson(), ExportJobResultDTO.class);
            } catch (Exception e) {
                log.warn("No se pudo parsear result_json del job {}", job.getId(), e);
            }
        }
        return ExportJobStatusResponse.builder()
                .jobId(job.getId())
                .status(job.getStatus())
                .total(job.getTotal())
                .processed(job.getProcessed())
                .successCount(job.getSuccessCount())
                .failCount(job.getFailCount())
                .progressPct(job.getProgressPct())
                .message(job.getMessage())
                .createdAt(job.getCreatedAt())
                .startedAt(job.getStartedAt())
                .finishedAt(job.getFinishedAt())
                .folderId(result != null ? result.getFolderId() : null)
                .folderLink(result != null ? result.getFolderLink() : null)
                .files(result != null ? result.getFiles() : null)
                .errorJson(job.getErrorJson())
                .build();
    }

    @Transactional(readOnly = true)
    public ExportJob findById(String id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Job no encontrado: " + id));
    }

    @Transactional
    public ExportJob save(ExportJob job) {
        return repository.save(job);
    }
}
