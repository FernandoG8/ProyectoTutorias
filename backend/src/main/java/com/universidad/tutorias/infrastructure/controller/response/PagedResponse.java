package com.universidad.tutorias.infrastructure.controller.response;

import java.util.List;

public record PagedResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
