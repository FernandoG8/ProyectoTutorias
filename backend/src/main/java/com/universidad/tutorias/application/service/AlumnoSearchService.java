package com.universidad.tutorias.application.service;

import com.universidad.tutorias.application.dto.AlumnoSearchResultDTO;
import com.universidad.tutorias.application.enums.SearchSortOption;
import com.universidad.tutorias.domain.enums.EstadoAlumno;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AlumnoSearchService {

    Page<AlumnoSearchResultDTO> search(
            String query,
            EstadoAlumno estado,
            String carrera,
            Integer semestre,
            int page,
            int size,
            SearchSortOption sortOption
    );

    List<AlumnoSearchResultDTO> autocomplete(
            String query,
            EstadoAlumno estado,
            String carrera,
            Integer semestre
    );

    AlumnoSearchResultDTO findByMatricula(String matricula);
}
