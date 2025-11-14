package com.universidad.tutorias.domain.exception;

public class SemestreNotFoundException extends SemestreException {

    public SemestreNotFoundException(Long id) {
        super(String.format("Semestre con ID %d no encontrado", id));
    }

    public SemestreNotFoundException(String codigo) {
        super(String.format("Semestre con código '%s' no encontrado", codigo));
    }
}
