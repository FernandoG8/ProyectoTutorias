package com.universidad.tutorias.application.service.impl;

import java.util.Locale;

final class SearchQuerySanitizer {

    private static final int MIN_QUERY_LENGTH = 2;
    private static final String DEFAULT_ERROR =
            "El parámetro q debe contener al menos 2 caracteres";
    private static final String MATRICULA_ERROR =
            "La matrícula debe contener al menos 2 caracteres";
    private static final String REGEX_SPECIALS = "\\.^$|?*+()[]{}";

    private SearchQuerySanitizer() {
    }

    static SearchTokens sanitize(String query) {
        return sanitize(query, DEFAULT_ERROR);
    }

    static SearchTokens sanitizeMatricula(String value) {
        return sanitize(value, MATRICULA_ERROR);
    }

    static SearchTokens sanitize(String query, String errorMessage) {
        if (query == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        String trimmed = query.trim();
        if (trimmed.length() < MIN_QUERY_LENGTH) {
            throw new IllegalArgumentException(errorMessage);
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        return new SearchTokens(normalized, escapeLike(normalized), escapeRegex(normalized));
    }

    static String normalizeFilterValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase(Locale.ROOT);
    }

    private static String escapeLike(String input) {
        String escaped = input.replace("\\", "\\\\");
        escaped = escaped.replace("%", "\\%");
        escaped = escaped.replace("_", "\\_");
        return escaped;
    }

    private static String escapeRegex(String input) {
        StringBuilder builder = new StringBuilder(input.length());
        for (char c : input.toCharArray()) {
            if (REGEX_SPECIALS.indexOf(c) >= 0) {
                builder.append('\\');
            }
            builder.append(c);
        }
        return builder.toString();
    }

    static record SearchTokens(String normalizedQuery, String likeTerm, String regexTerm) {
    }
}
