package com.universidad.tutorias.application.service.reportes;

import com.universidad.tutorias.application.enums.FormatoReporte;
import com.universidad.tutorias.application.service.reportes.dto.ReporteArchivoDTO;
import com.universidad.tutorias.domain.entity.Usuario;
import com.universidad.tutorias.domain.entity.UsuarioGoogleLink;
import com.universidad.tutorias.domain.exception.DriveNotConnectedException;
import com.universidad.tutorias.domain.exception.GoogleLinkException;
import com.universidad.tutorias.domain.repository.UsuarioGoogleLinkRepository;
import com.universidad.tutorias.infrastructure.service.google.DriveStorageService;
import com.universidad.tutorias.infrastructure.service.google.GoogleTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportDriveExportService {

    private final ReporteExportService reporteExportService;
    private final UsuarioGoogleLinkRepository usuarioGoogleLinkRepository;
    private final GoogleTokenService googleTokenService;
    private final DriveStorageService driveStorageService;

    public DriveResult exportCarrera(Usuario usuario, String codigo, String periodo, FormatoReporte formato) {
        var context = buildContext(usuario);

        ReporteArchivoDTO archivo = reporteExportService.generarReporteCarrera(codigo, periodo, formato);
        String semestreFolder = StringUtils.hasText(periodo) ? periodo : "SIN_PERIODO";
        String baseFolder = ensureFolders(context, semestreFolder, "Carreras", sanitizar(codigo));

        DriveFileItem item = uploadWithItem(context, baseFolder, archivo, "carrera:" + codigo);
        return DriveResult.single(item);
    }

    public DriveResult exportTutor(Usuario usuario, Long tutorId, String periodo, FormatoReporte formato) {
        var context = buildContext(usuario);

        ReporteArchivoDTO archivo = reporteExportService.generarReporteTutor(tutorId, periodo, formato);
        String semestreFolder = StringUtils.hasText(periodo) ? periodo : "SIN_PERIODO";
        String baseFolder = ensureFolders(context, semestreFolder, "Tutores", sanitizar(nombreBase(archivo.getFileName())));

        DriveFileItem item = uploadWithItem(context, baseFolder, archivo, "tutor:" + tutorId);
        return DriveResult.single(item);
    }

    public DriveResult exportMasivo(Usuario usuario, String periodo, FormatoReporte formato) {
        var context = buildContext(usuario);

        String semestreFolder = StringUtils.hasText(periodo) ? periodo : "SIN_PERIODO";
        List<DriveFileItem> items = new ArrayList<>();

        // Carreras
        for (ReporteArchivoDTO archivo : reporteExportService.generarReportesTodasLasCarreras(periodo, formato)) {
            String folder = ensureFolders(context, semestreFolder, "Carreras", sanitizar(nombreBase(archivo.getFileName())));
            items.add(uploadWithItem(context, folder, archivo, "carrera:" + nombreBase(archivo.getFileName())));
        }

        // Tutores
        for (ReporteArchivoDTO archivo : reporteExportService.generarReportesTodosLosTutores(periodo, formato)) {
            String folder = ensureFolders(context, semestreFolder, "Tutores", sanitizar(nombreBase(archivo.getFileName())));
            items.add(uploadWithItem(context, folder, archivo, "tutor:" + nombreBase(archivo.getFileName())));
        }

        return DriveResult.fromItems(items);
    }

    private String resolveDriveId(GoogleTokenService.GoogleAccessToken access) {
        var ctx = new DriveStorageService.UsuarioGoogleContext(null, access.accessToken());
        return driveStorageService.resolveSharedDriveId(ctx);
    }

    public DriveStorageService.UsuarioGoogleContext buildContext(Usuario usuario) {
        UsuarioGoogleLink link = requireLinkedUser(usuario);
        var access = googleTokenService.refreshAccessToken(link);
        String driveId = resolveDriveId(access);
        return new DriveStorageService.UsuarioGoogleContext(driveId, access.accessToken());
    }

    private UsuarioGoogleLink requireLinkedUser(Usuario usuario) {
        UsuarioGoogleLink link = usuarioGoogleLinkRepository.findByUsuarioId(usuario.getId())
                .filter(l -> StringUtils.hasText(l.getGoogleSub()))
                .orElse(null);
        if (link == null) {
            throw new GoogleLinkException("GOOGLE_NOT_LINKED", "Usuario no vinculado a Google.");
        }
        if (!StringUtils.hasText(link.getGoogleRefreshTokenEnc()) || !StringUtils.hasText(link.getGoogleRefreshTokenIv())) {
            throw new DriveNotConnectedException("Usuario no tiene Drive conectado.");
        }
        return link;
    }

    private String ensureFolders(DriveStorageService.UsuarioGoogleContext context, String semestre, String categoria, String entityFolder) {
        String rootDriveId = context.driveId();
        String semestreId = driveStorageService.getOrCreateFolder(context, rootDriveId, semestre);
        String categoriaId = driveStorageService.getOrCreateFolder(context, semestreId, categoria);
        if (entityFolder == null) {
            return categoriaId;
        }
        return driveStorageService.getOrCreateFolder(context, categoriaId, entityFolder);
    }

    private String sanitizar(String valor) {
        if (!StringUtils.hasText(valor)) return "SIN_NOMBRE";
        return valor.replaceAll("[^A-Za-z0-9_\\-\\.]", "_");
    }

    private String nombreBase(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx > 0 ? fileName.substring(0, idx) : fileName;
    }

    private String mediaTypeToString(MediaType mediaType) {
        return mediaType != null ? mediaType.toString() : "application/octet-stream";
    }

    private DriveFileItem uploadWithItem(DriveStorageService.UsuarioGoogleContext context, String folder, ReporteArchivoDTO archivo, String entity) {
        try {
            DriveStorageService.DriveFileResult res = driveStorageService.uploadFile(
                    context,
                    folder,
                    archivo.getFileName(),
                    mediaTypeToString(archivo.getMediaType()),
                    archivo.getContenido()
            );
            return new DriveFileItem(entity, "OK", res.webViewLink(), null);
        } catch (Exception e) {
            log.error("Error subiendo {} a Drive", entity, e);
            return new DriveFileItem(entity, "FAIL", null, e.getMessage());
        }
    }

    public record DriveFileItem(String entity, String status, String link, String errorCode) {}

    public record DriveResult(int total, int okCount, int failCount, List<DriveFileItem> items) {
        public static DriveResult single(DriveFileItem item) {
            int ok = "OK".equals(item.status()) ? 1 : 0;
            int fail = "OK".equals(item.status()) ? 0 : 1;
            return new DriveResult(1, ok, fail, List.of(item));
        }

        public static DriveResult fromItems(List<DriveFileItem> items) {
            int ok = (int) items.stream().filter(i -> "OK".equals(i.status())).count();
            int fail = items.size() - ok;
            return new DriveResult(items.size(), ok, fail, items);
        }
    }
}
