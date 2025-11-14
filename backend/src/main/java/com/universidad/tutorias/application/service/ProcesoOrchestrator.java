package com.universidad.tutorias.application.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

public interface ProcesoOrchestrator {
    /**
     * Coordina todo el proceso de asignación de forma asíncrona
     * @param archivo archivo Excel
     * @param semestreId ID del semestre (no string)
     * @param usuario usuario que ejecuta
     * @return ID del proceso iniciado
     */
    @Async
    CompletableFuture<Long> ejecutarProcesoCompleto(MultipartFile archivo,
                                                    Long semestreId,
                                                    String usuario);
}