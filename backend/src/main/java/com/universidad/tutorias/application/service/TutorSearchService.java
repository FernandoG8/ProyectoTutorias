package com.universidad.tutorias.application.service;

import com.universidad.tutorias.application.dto.TutorSearchResultDTO;
import com.universidad.tutorias.application.enums.SearchSortOption;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TutorSearchService {

    Page<TutorSearchResultDTO> search(
            String query,
            String carrera,
            int page,
            int size,
            SearchSortOption sortOption
    );

    List<TutorSearchResultDTO> autocomplete(String query, String carrera);
}
