package com.universidad.tutorias.application.enums;

import java.util.Locale;

public enum SearchSortOption {
    RELEVANCE("relevance"),
    MATRICULA("matricula"),
    NOMBRE("nombre");

    private final String value;

    SearchSortOption(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SearchSortOption from(String value) {
        if (value == null) {
            return RELEVANCE;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (SearchSortOption option : values()) {
            if (option.value.equals(normalized)) {
                return option;
            }
        }
        return RELEVANCE;
    }
}
