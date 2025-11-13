package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.TutorSearchResultDTO;
import com.universidad.tutorias.application.enums.SearchSortOption;
import com.universidad.tutorias.application.service.TutorSearchService;
import com.universidad.tutorias.domain.repository.TutorSearchRepository;
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
public class TutorSearchServiceImpl implements TutorSearchService {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final int AUTOCOMPLETE_LIMIT = 10;

    private final TutorSearchRepository tutorSearchRepository;

    @Override
    public Page<TutorSearchResultDTO> search(String query,
                                             String carrera,
                                             int page,
                                             int size,
                                             SearchSortOption sortOption) {

        SearchTokens tokens = SearchQuerySanitizer.sanitize(query);
        Pageable pageable = buildPageable(page, size);
        SearchSortOption resolvedSort = resolveSort(sortOption);
        String carreraFilter = SearchQuerySanitizer.normalizeFilterValue(carrera);

        Page<Object[]> result = tutorSearchRepository.searchWithRanking(
                tokens.normalizedQuery(),
                tokens.likeTerm(),
                tokens.regexTerm(),
                carreraFilter,
                pageable,
                resolvedSort
        );

        List<TutorSearchResultDTO> mapped = result.getContent()
                .stream()
                .map(this::mapTutorResult)
                .toList();

        return new PageImpl<>(mapped, pageable, result.getTotalElements());
    }

    @Override
    public List<TutorSearchResultDTO> autocomplete(String query, String carrera) {
        SearchTokens tokens = SearchQuerySanitizer.sanitize(query);
        String carreraFilter = SearchQuerySanitizer.normalizeFilterValue(carrera);

        Page<Object[]> page = tutorSearchRepository.searchWithRanking(
                tokens.normalizedQuery(),
                tokens.likeTerm(),
                tokens.regexTerm(),
                carreraFilter,
                PageRequest.of(0, AUTOCOMPLETE_LIMIT),
                SearchSortOption.RELEVANCE
        );

        return page.getContent()
                .stream()
                .map(this::mapTutorResult)
                .toList();
    }

    private Pageable buildPageable(int page, int size) {
        int safePage = Math.max(page, 1) - 1;
        int requestedSize = size <= 0 ? DEFAULT_SIZE : size;
        int safeSize = Math.min(requestedSize, MAX_SIZE);
        return PageRequest.of(safePage, safeSize);
    }

    private SearchSortOption resolveSort(SearchSortOption option) {
        if (option == null) {
            return SearchSortOption.RELEVANCE;
        }
        return option == SearchSortOption.MATRICULA ? SearchSortOption.NOMBRE : option;
    }

    private TutorSearchResultDTO mapTutorResult(Object[] row) {
        return new TutorSearchResultDTO(
                toLong(row[0]),
                (String) row[1],
                (String) row[2],
                row[3] == null ? 0 : ((Number) row[3]).intValue()
        );
    }

    private Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }
}
