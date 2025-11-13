package com.universidad.tutorias.application.dto;

public record TutorSearchResultDTO(
        Long id,
        String nombre,
        String carrera,
        Integer score
) {
}
