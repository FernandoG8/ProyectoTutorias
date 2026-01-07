package com.universidad.tutorias.domain.exception;

import lombok.Getter;

@Getter
public class GoogleLinkException extends RuntimeException {

    private final String code;

    public GoogleLinkException(String code, String message) {
        super(message);
        this.code = code;
    }
}
