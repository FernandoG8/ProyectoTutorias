package com.universidad.tutorias.infrastructure.service.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.DriveList;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriveStorageService {

    private final GoogleTokenService googleTokenService;

    @Value("${app.drive.shared-drive-name:Tutolink Reportes}")
    private String sharedDriveName;

    public String resolveSharedDriveId(UsuarioGoogleContext context) {
        Drive drive = buildDriveClient(context.accessToken());
        try {
            DriveList drives = drive.drives().list()
                    .setPageSize(10)
                    .setQ("name = '" + sharedDriveName + "'")
                    .setUseDomainAdminAccess(false)
                    .execute();
            Optional<String> driveId = drives.getDrives().stream()
                    .filter(d -> sharedDriveName.equalsIgnoreCase(d.getName()))
                    .map(com.google.api.services.drive.model.Drive::getId)
                    .findFirst();
            return driveId.orElseThrow(() -> new IllegalStateException("NO_SHARED_DRIVE_ACCESS"));
        } catch (IOException e) {
            throw new IllegalStateException("NO_SHARED_DRIVE_ACCESS", e);
        }
    }

    public DriveFileResult uploadFile(UsuarioGoogleContext context, String parentId, String fileName, String contentType, byte[] content) {
        Drive drive = buildDriveClient(context.accessToken());
        try {
            String fileId = findFileId(drive, context.driveId(), parentId, fileName);
            ByteArrayContent mediaContent = new ByteArrayContent(contentType, content);

            File uploaded;
            if (fileId != null) {
                log.info("Archivo existente encontrado, actualizando: {}", fileName);
                File metadata = new File().setName(fileName); // No parents en update
                uploaded = drive.files().update(fileId, metadata, mediaContent)
                        .setSupportsAllDrives(true)
                        .setFields("id, webViewLink")
                        .execute();
                log.info("Archivo actualizado: {} (ID: {})", fileName, uploaded.getId());
            } else {
                log.info("Archivo no existe, creando nuevo: {}", fileName);
                File metadata = new File().setName(fileName);
                if (parentId != null) {
                    metadata.setParents(List.of(parentId));
                }
                uploaded = drive.files().create(metadata, mediaContent)
                        .setSupportsAllDrives(true)
                        .setFields("id, webViewLink")
                        .execute();
                log.info("Archivo creado: {} (ID: {})", fileName, uploaded.getId());
            }

            return new DriveFileResult(uploaded.getId(), uploaded.getWebViewLink(), parentId, buildFolderLink(parentId));
        } catch (GoogleJsonResponseException e) {
            log.error("Error de Google Drive API: {} - {}", e.getStatusCode(), e.getDetails() != null ? e.getDetails().getMessage() : e.getMessage());
            throw new IllegalStateException("Error subiendo archivo a Drive: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado subiendo archivo: {}", e.getMessage(), e);
            throw new IllegalStateException("Error subiendo archivo a Drive", e);
        }
    }

    public String getOrCreateFolder(UsuarioGoogleContext context, String parentId, String name) {
        Drive drive = buildDriveClient(context.accessToken());
        try {
            String folderId = findFolder(drive, context.driveId(), parentId, name);
            if (folderId != null) {
                return folderId;
            }

            File folder = new File()
                    .setName(name)
                    .setMimeType("application/vnd.google-apps.folder")
                    .setParents(List.of(parentId));

            File created = drive.files().create(folder)
                    .setSupportsAllDrives(true)
                    .setFields("id")
                    .execute();
            return created.getId();
        } catch (IOException e) {
            throw new IllegalStateException("Error creando carpeta en Drive", e);
        }
    }

    private String findFolder(Drive drive, String driveId, String parentId, String name) throws IOException {
        String query = "mimeType = 'application/vnd.google-apps.folder' and name = '" + name + "' and '" + parentId + "' in parents and trashed = false";
        FileList files = drive.files().list()
                .setCorpora("drive")
                .setDriveId(driveId)
                .setIncludeItemsFromAllDrives(true)
                .setSupportsAllDrives(true)
                .setQ(query)
                .setFields("files(id, name)")
                .setPageSize(5)
                .execute();

        return files.getFiles().stream()
                .findFirst()
                .map(File::getId)
                .orElse(null);
    }

    private String findFileId(Drive drive, String driveId, String parentId, String name) throws IOException {
        String safeName = name.replace("'", "\\'");
        StringBuilder query = new StringBuilder("name = '").append(safeName).append("' and trashed = false");
        if (parentId != null) {
            query.append(" and '").append(parentId).append("' in parents");
        }

        FileList files = drive.files().list()
                .setCorpora("drive")
                .setDriveId(driveId)
                .setSpaces("drive")
                .setIncludeItemsFromAllDrives(true)
                .setSupportsAllDrives(true)
                .setQ(query.toString())
                .setFields("files(id)")
                .setPageSize(1)
                .execute();

        if (files.getFiles() != null && !files.getFiles().isEmpty()) {
            String foundId = files.getFiles().get(0).getId();
            log.info("Archivo encontrado: {} (ID: {})", name, foundId);
            return foundId;
        }

        log.info("Archivo no encontrado: {}", name);
        return null;
    }

    private Drive buildDriveClient(String accessToken) {
        try {
            GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
            return new Drive.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
            ).setApplicationName("Tutolink").build();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo construir el cliente de Drive", e);
        }
    }

    public record DriveFileResult(String fileId, String webViewLink, String folderId, String folderLink) {}

    public record UsuarioGoogleContext(String driveId, String accessToken) {}

    private String buildFolderLink(String folderId) {
        return folderId != null ? "https://drive.google.com/drive/folders/" + folderId : null;
    }
}
