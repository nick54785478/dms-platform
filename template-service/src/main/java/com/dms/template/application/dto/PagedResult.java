package com.dms.template.application.dto;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分頁查詢結果的純資料載體 (DTO).
 * 嚴禁依賴任何基礎設施分頁物件 (如 Spring Data 的 Page).
 */
public record PagedResult<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages
) {
    public <U> PagedResult<U> map(Function<? super T, ? extends U> converter) {
        List<U> convertedContent = this.content.stream().map(converter).collect(Collectors.toList());
        return new PagedResult<>(convertedContent, this.pageNumber, this.pageSize, this.totalElements, this.totalPages);
    }
}
