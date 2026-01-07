package com.universidad.tutorias.application.dto.drive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportJobResultDTO {
    private String folderId;
    private String folderLink;
    @Builder.Default
    private List<ExportJobFileDTO> files = new ArrayList<>();
}
