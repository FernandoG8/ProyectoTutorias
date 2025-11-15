package com.universidad.tutorias.application.service;

public interface TutorSincronizacionService {
    void recalcularCargaTutor(Long tutorId);
    void recalcularCargaTodosLosTutores();
}
