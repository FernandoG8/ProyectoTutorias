package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.AlumnoSearchResultDTO;
import com.universidad.tutorias.application.enums.SearchSortOption;
import com.universidad.tutorias.application.service.AlumnoSearchService;
import com.universidad.tutorias.domain.enums.EstadoAlumno;
import com.universidad.tutorias.domain.repository.AlumnoSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.universidad.tutorias.application.service.impl.SearchQuerySanitizer.SearchTokens;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlumnoSearchServiceImpl implements AlumnoSearchService {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final int AUTOCOMPLETE_LIMIT = 10;

    private final AlumnoSearchRepository alumnoSearchRepository;

    @Override
    public Page<AlumnoSearchResultDTO> search(String query,
                                              EstadoAlumno estado,
                                              String carrera,
                                              Integer semestre,
                                              int page,
                                              int size,
                                              SearchSortOption sortOption) {

        SearchTokens tokens = SearchQuerySanitizer.sanitize(query);
        Pageable pageable = buildPageable(page, size);
        SearchSortOption resolvedSort = sortOption == null ? SearchSortOption.RELEVANCE : sortOption;
        String carreraFilter = SearchQuerySanitizer.normalizeFilterValue(carrera);

        Page<Object[]> results = alumnoSearchRepository.searchWithRanking(
                tokens.normalizedQuery(),
                tokens.likeTerm(),
                tokens.regexTerm(),
                estado,
                carreraFilter,
                semestre,
                pageable,
                resolvedSort
        );

        List<AlumnoSearchResultDTO> mapped = results.getContent()
                .stream()
                .map(this::mapAlumnoResult)
                .toList();

        return new PageImpl<>(mapped, pageable, results.getTotalElements());
    }

    @Override
    public List<AlumnoSearchResultDTO> autocomplete(String query,
                                                    EstadoAlumno estado,
                                                    String carrera,
                                                    Integer semestre) {

        SearchTokens tokens = SearchQuerySanitizer.sanitize(query);
        String carreraFilter = SearchQuerySanitizer.normalizeFilterValue(carrera);

        Page<Object[]> page = alumnoSearchRepository.searchWithRanking(
                tokens.normalizedQuery(),
                tokens.likeTerm(),
                tokens.regexTerm(),
                estado,
                carreraFilter,
                semestre,
                PageRequest.of(0, AUTOCOMPLETE_LIMIT),
                SearchSortOption.RELEVANCE
        );

        return page.getContent()
                .stream()
                .map(this::mapAlumnoResult)
                .toList();
    }

    @Override
    public AlumnoSearchResultDTO findByMatricula(String matricula) {
        SearchTokens tokens = SearchQuerySanitizer.sanitizeMatricula(matricula);

        return alumnoSearchRepository.findByMatricula(
                        tokens.normalizedQuery(),
                        tokens.likeTerm(),
                        tokens.regexTerm()
                )
                .map(this::mapAlumnoResult)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "No se encontró un alumno con la matrícula proporcionada"));
    }

    private Pageable buildPageable(int page, int size) {
        int safePage = Math.max(page, 1) - 1;
        int requestedSize = size <= 0 ? DEFAULT_SIZE : size;
        int safeSize = Math.min(requestedSize, MAX_SIZE);
        return PageRequest.of(safePage, safeSize);
    }

    private AlumnoSearchResultDTO mapAlumnoResult(Object[] row) {
        // Map tutor if present (rows 7-9: tutor_id, tutor_nombre, tutor_carrera)
        com.universidad.tutorias.application.dto.TutorSimpleDTO tutor = null;
        if (row.length > 7 && row[7] != null) {
            tutor = new com.universidad.tutorias.application.dto.TutorSimpleDTO(
                    ((Number) row[7]).longValue(),
                    (String) row[8],
                    (String) row[9]
            );
        }

        return new AlumnoSearchResultDTO(
                toLong(row[0]),
                (String) row[1],
                (String) row[2],
                (String) row[3],
                row[4] == null ? null : ((Number) row[4]).intValue(),
                (String) row[5],
                row[6] == null ? 0 : ((Number) row[6]).intValue(),
                tutor
        );
    }

    private Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }
}
