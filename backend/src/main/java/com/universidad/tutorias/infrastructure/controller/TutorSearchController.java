package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.application.dto.TutorSearchResultDTO;
import com.universidad.tutorias.application.enums.SearchSortOption;
import com.universidad.tutorias.application.service.TutorSearchService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tutores")
@RequiredArgsConstructor
@Slf4j
@Validated
@CrossOrigin(origins = "*")
@Tag(name = "Tutores - Búsqueda", description = "Búsqueda de tutores con ranking por nombre")
public class TutorSearchController {

    private static final int AUTOCOMPLETE_LIMIT = 10;

    private final TutorSearchService tutorSearchService;

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('COORDINADOR_TUTORIAS','SECRETARIO_ACADEMICO')")
    @Operation(
            summary = "Buscar tutores con ranking",
            description = "Permite buscar tutores aplicando ranking por nombre y filtros",
            security = {@SecurityRequirement(name = "cookieAuth")}
    )
    public ResponseEntity<ApiResponse<PagedResponse<TutorSearchResultDTO>>> searchTutores(
            @Parameter(description = "Término a buscar (>= 2 caracteres)", example = "María")
            @RequestParam("q") @NotBlank @Size(min = 2, message = "Debe contener al menos 2 caracteres") String query,
            @Parameter(description = "Página solicitada (1-based)", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @Parameter(description = "Tamaño de página (máx. 100)", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Ordenamiento: relevance | matricula | nombre", example = "relevance",
                    schema = @Schema(allowableValues = {"relevance", "matricula", "nombre"}))
            @RequestParam(defaultValue = "relevance") String sort,
            @Parameter(description = "Filtrar por carrera", example = "INGENIERIA_SISTEMAS")
            @RequestParam(required = false) String carrera
    ) {
        log.info("Buscando tutores q='{}', page={}, size={}, sort={}, carrera={}",
                query, page, size, sort, carrera);

        SearchSortOption sortOption = normalizeSort(SearchSortOption.from(sort));
        Page<TutorSearchResultDTO> resultados = tutorSearchService.search(
                query,
                carrera,
                page,
                size,
                sortOption
        );

        PagedResponse<TutorSearchResultDTO> response = new PagedResponse<>(
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
            summary = "Autocomplete de tutores",
            description = "Devuelve hasta 10 coincidencias de tutores por nombre",
            security = {@SecurityRequirement(name = "cookieAuth")}
    )
    public ResponseEntity<ApiResponse<List<TutorSearchResultDTO>>> autocomplete(
            @Parameter(description = "Término parcial a buscar", example = "Mar")
            @RequestParam("q") @NotBlank @Size(min = 2, message = "Debe contener al menos 2 caracteres") String query,
            @Parameter(description = "Filtrar por carrera", example = "INGENIERIA_SISTEMAS")
            @RequestParam(required = false) String carrera
    ) {
        log.info("Autocomplete de tutores q='{}', carrera={}", query, carrera);
        List<TutorSearchResultDTO> suggestions = tutorSearchService.autocomplete(query, carrera);
        return ResponseEntity.ok(ApiResponse.success(limitSuggestions(suggestions)));
    }

    private SearchSortOption normalizeSort(SearchSortOption option) {
        if (option == null) {
            return SearchSortOption.RELEVANCE;
        }
        return option == SearchSortOption.MATRICULA ? SearchSortOption.NOMBRE : option;
    }

    private <T> List<T> limitSuggestions(List<T> suggestions) {
        if (suggestions.size() <= AUTOCOMPLETE_LIMIT) {
            return suggestions;
        }
        return List.copyOf(suggestions.subList(0, AUTOCOMPLETE_LIMIT));
    }
}
