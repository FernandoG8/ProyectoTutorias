package com.universidad.tutorias.application.service;

import com.universidad.tutorias.domain.entity.Tutor;

public interface TutorSincronizacionService {
    void recalcularCargaTutor(Long tutorId);
    void recalcularCargaTodosLosTutores();

    /**
     * Recalcula la carga real desde BD y devuelve el tutor bloqueado con el valor actualizado.
     * Útil antes de validar capacidad o modificar carga en operaciones concurrentes.
     */
    Tutor sincronizarYBloquearTutor(Long tutorId);
}
