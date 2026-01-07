package com.universidad.tutorias.application.dto.drive;

import com.universidad.tutorias.domain.enums.ExportJobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportJobStatusResponse {
    private String jobId;
    private ExportJobStatus status;
    private Integer total;
    private Integer processed;
    private Integer successCount;
    private Integer failCount;
    private Integer progressPct;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String folderId;
    private String folderLink;
    private List<ExportJobFileDTO> files;
    private String errorJson;
}
