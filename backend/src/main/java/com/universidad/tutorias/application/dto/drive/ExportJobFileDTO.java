package com.universidad.tutorias.application.dto.drive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportJobFileDTO {
    private String entity;
    private Long tutorId;
    private String fileName;
    private String fileId;
    private String webViewLink;
    private String folderId;
    private String folderLink;
    private String status;
    private String error;
}
