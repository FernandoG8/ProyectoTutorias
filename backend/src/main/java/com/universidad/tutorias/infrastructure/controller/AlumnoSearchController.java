package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.application.dto.AlumnoSearchResultDTO;
import com.universidad.tutorias.application.enums.SearchSortOption;
import com.universidad.tutorias.application.service.AlumnoSearchService;
import com.universidad.tutorias.domain.enums.EstadoAlumno;
import com.universidad.tutorias.infrastructure.controller.response.ApiResponse;
import com.universidad.tutorias.infrastructure.controller.response.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alumnos")
@RequiredArgsConstructor
@Slf4j
@Validated
@CrossOrigin(origins = "*")
@Tag(name = "Alumnos - Búsqueda", description = "Búsqueda avanzada de alumnos con ranking y filtros")
public class AlumnoSearchController {

    private static final int AUTOCOMPLETE_LIMIT = 10;

    private final AlumnoSearchService alumnoSearchService;

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('COORDINADOR_TUTORIAS','SECRETARIO_ACADEMICO')")
    @Operation(
            summary = "Buscar alumnos con ranking",
            description = "Permite buscar alumnos por matrícula o nombre aplicando ranking y filtros",
            security = {@SecurityRequirement(name = "cookieAuth")}
    )
    public ResponseEntity<ApiResponse<PagedResponse<AlumnoSearchResultDTO>>> searchAlumnos(
            @Parameter(description = "Término a buscar (>= 2 caracteres)", example = "202101234")
            @RequestParam("q") @NotBlank @Size(min = 2, message = "Debe contener al menos 2 caracteres") String query,
            @Parameter(description = "Página solicitada (1-based)", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @Parameter(description = "Tamaño de página (máx. 100)", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Ordenamiento: relevance | matricula | nombre", example = "relevance",
                    schema = @Schema(allowableValues = {"relevance", "matricula", "nombre"}))
            @RequestParam(defaultValue = "relevance") String sort,
            @Parameter(description = "Filtrar por estado", example = "ACTIVO")
            @RequestParam(required = false) EstadoAlumno estado,
            @Parameter(description = "Filtrar por carrera", example = "INGENIERIA_SISTEMAS")
            @RequestParam(required = false) String carrera,
            @Parameter(description = "Filtrar por semestre", example = "5")
            @RequestParam(required = false) Integer semestre
    ) {
        log.info("Buscando alumnos q='{}', page={}, size={}, sort={}, estado={}, carrera={}, semestre={}",
                query, page, size, sort, estado, carrera, semestre);

        SearchSortOption sortOption = SearchSortOption.from(sort);
        Page<AlumnoSearchResultDTO> resultados = alumnoSearchService.search(
                query,
                estado,
                carrera,
                semestre,
                page,
                size,
                sortOption
        );

        PagedResponse<AlumnoSearchResultDTO> response = new PagedResponse<>(
                resultados.getContent(),
                page,
                resultados.getSize(),
                resultados.getTotalElements(),
                resultados.getTotalPages()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/autocomplete")
    @PreAuthorize("hasAnyRole('COORDINADOR_TUTORIAS','SECRETARIO_ACADEMICO')")
    @Operation(
            summary = "Autocomplete de alumnos",
            description = "Devuelve hasta 10 coincidencias para sugerencias rápidas",
            security = {@SecurityRequirement(name = "cookieAuth")}
    )
    public ResponseEntity<ApiResponse<List<AlumnoSearchResultDTO>>> autocomplete(
            @Parameter(description = "Término parcial a buscar", example = "Mar")
            @RequestParam("q") @NotBlank @Size(min = 2, message = "Debe contener al menos 2 caracteres") String query,
            @Parameter(description = "Filtrar por estado", example = "ACTIVO")
            @RequestParam(required = false) EstadoAlumno estado,
            @Parameter(description = "Filtrar por carrera", example = "INGENIERIA_SISTEMAS")
            @RequestParam(required = false) String carrera,
            @Parameter(description = "Filtrar por semestre", example = "5")
            @RequestParam(required = false) Integer semestre
    ) {
        log.info("Autocomplete de alumnos q='{}', estado={}, carrera={}, semestre={}",
                query, estado, carrera, semestre);

        List<AlumnoSearchResultDTO> suggestions = alumnoSearchService.autocomplete(query, estado, carrera, semestre);
        return ResponseEntity.ok(ApiResponse.success(limitSuggestions(suggestions)));
    }

    @GetMapping("/by-matricula/{matricula}")
    @PreAuthorize("hasAnyRole('COORDINADOR_TUTORIAS','SECRETARIO_ACADEMICO')")
    @Operation(
            summary = "Buscar alumno por matrícula exacta",
            description = "Localiza un alumno por matrícula exacta aplicando el mismo ranking",
            security = {@SecurityRequirement(name = "cookieAuth")}
    )
    public ResponseEntity<ApiResponse<AlumnoSearchResultDTO>> findByMatricula(
            @Parameter(description = "Matrícula exacta", example = "202101234")
            @PathVariable("matricula") @NotBlank String matricula
    ) {
        log.info("Buscando alumno por matrícula {}", matricula);
        AlumnoSearchResultDTO alumno = alumnoSearchService.findByMatricula(matricula);
        return ResponseEntity.ok(ApiResponse.success(alumno));
    }

    private <T> List<T> limitSuggestions(List<T> suggestions) {
        if (suggestions.size() <= AUTOCOMPLETE_LIMIT) {
            return suggestions;
        }
        return List.copyOf(suggestions.subList(0, AUTOCOMPLETE_LIMIT));
    }
}
