package com.universidad.tutorias.application.dto;

public record AlumnoSearchResultDTO(
        Long id,
        String matricula,
        String nombre,
        String carrera,
        Integer semestre,
        String estado,
        Integer score,
        TutorSimpleDTO tutor
) {
}
