package com.example.dms.application.shared.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record PageGottenResult<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
) {
}
