package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.AlumnoSearchResultDTO;
import com.universidad.tutorias.application.enums.SearchSortOption;
import com.universidad.tutorias.domain.enums.EstadoAlumno;
import com.universidad.tutorias.domain.repository.AlumnoSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlumnoSearchServiceImplTest {

    @Mock
    private AlumnoSearchRepository alumnoSearchRepository;

    @InjectMocks
    private AlumnoSearchServiceImpl alumnoSearchService;

    private Page<Object[]> samplePage;

    @BeforeEach
    void setUp() {
        // Object array structure: [id, matricula, nombre, carrera, semestre, estado, ranking, tutor_id, tutor_nombre, tutor_carrera]
        Object[] row = new Object[]{1L, "202101234", "Juan Pérez", "INGENIERIA", 5, "ACTIVO", 800, 10L, "Dr. García", "INGENIERIA"};
        List<Object[]> rowList = new ArrayList<>();
        rowList.add(row);
        samplePage = new PageImpl<>(rowList, PageRequest.of(0, 20), 1);
    }

    @Test
    void searchShouldThrowWhenQueryTooShort() {
        assertThrows(IllegalArgumentException.class, () ->
                alumnoSearchService.search(" a", null, null, null, 1, 20, SearchSortOption.RELEVANCE));
    }

    @Test
    void searchShouldTrimQueryBeforeDelegating() {
        when(alumnoSearchRepository.searchWithRanking(anyString(), anyString(), anyString(),
                any(), any(), any(), any(Pageable.class), any())).thenReturn(samplePage);

        Page<AlumnoSearchResultDTO> page = alumnoSearchService.search(
                "   202101234   ",
                EstadoAlumno.ACTIVO,
                "INGENIERIA",
                5,
                1,
                20,
                SearchSortOption.RELEVANCE
        );

        assertThat(page.getContent()).hasSize(1);

        ArgumentCaptor<String> normalizedCaptor = ArgumentCaptor.forClass(String.class);
        verify(alumnoSearchRepository).searchWithRanking(
                normalizedCaptor.capture(),
                anyString(),
                anyString(),
                any(),
                any(),
                any(),
                any(Pageable.class),
                any()
        );

        assertEquals("202101234", normalizedCaptor.getValue());
    }

    @Test
    void searchShouldConvertPageToZeroBased() {
        when(alumnoSearchRepository.searchWithRanking(anyString(), anyString(), anyString(),
                any(), any(), any(), any(Pageable.class), any())).thenReturn(samplePage);

        alumnoSearchService.search("Ana", null, null, null, 2, 25, SearchSortOption.MATRICULA);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(alumnoSearchRepository).searchWithRanking(
                anyString(),
                anyString(),
                anyString(),
                any(),
                any(),
                any(),
                pageableCaptor.capture(),
                any()
        );

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(25, pageable.getPageSize());
    }

    @Test
    void autocompleteShouldLimitResultsToTen() {
        when(alumnoSearchRepository.searchWithRanking(anyString(), anyString(), anyString(),
                any(), any(), any(), any(Pageable.class), any())).thenReturn(samplePage);

        alumnoSearchService.autocomplete("Juan", null, null, null);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(alumnoSearchRepository).searchWithRanking(
                anyString(),
                anyString(),
                anyString(),
                any(),
                any(),
                any(),
                pageableCaptor.capture(),
                any()
        );

        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(10, pageableCaptor.getValue().getPageSize());
    }
}
