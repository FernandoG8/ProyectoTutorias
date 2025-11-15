package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.application.dto.AlumnoSearchResultDTO;
import com.universidad.tutorias.application.enums.SearchSortOption;
import com.universidad.tutorias.application.service.AlumnoSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AlumnoSearchController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AlumnoSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlumnoSearchService alumnoSearchService;

    @Test
    void shouldRejectQueryShorterThanTwoCharacters() throws Exception {
        mockMvc.perform(get("/api/alumnos/search")
                        .param("q", "a")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUseRelevanceSortByDefault() throws Exception {
        Page<AlumnoSearchResultDTO> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(alumnoSearchService.search(anyString(), any(), any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/alumnos/search")
                        .param("q", "Ana"))
                .andExpect(status().isOk());

        verify(alumnoSearchService).search(
                eq("Ana"),
                isNull(),
                isNull(),
                isNull(),
                eq(1),
                eq(20),
                eq(SearchSortOption.RELEVANCE)
        );
    }

    @Test
    void autocompleteShouldLimitResponseToTenItems() throws Exception {
        List<AlumnoSearchResultDTO> results = IntStream.range(0, 12)
                .mapToObj(i -> new AlumnoSearchResultDTO(
                        (long) i,
                        "20210" + i,
                        "Alumno " + i,
                        "ING",
                        5,
                        "ACTIVO",
                        400,
                        null
                ))
                .toList();
        when(alumnoSearchService.autocomplete(anyString(), any(), any(), any())).thenReturn(results);

        mockMvc.perform(get("/api/alumnos/autocomplete")
                        .param("q", "Al"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(10)));
    }

    @Test
    void shouldReturnNotFoundWhenMatriculaDoesNotExist() throws Exception {
        when(alumnoSearchService.findByMatricula("999")).thenThrow(new EntityNotFoundException("No encontrado"));

        mockMvc.perform(get("/api/alumnos/by-matricula/999"))
                .andExpect(status().isNotFound());
    }
}
