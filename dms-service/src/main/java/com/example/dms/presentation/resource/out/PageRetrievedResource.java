package com.example.dms.presentation.resource.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分頁資料結果載體")
public class PageRetrievedResource<T> {
    
    @Schema(description = "資料列表")
    private List<T> content;
    
    @Schema(description = "目前頁碼 (0-indexed)")
    private int pageNumber;
    
    @Schema(description = "每頁筆數")
    private int pageSize;
    
    @Schema(description = "總資料筆數")
    private long totalElements;
    
    @Schema(description = "總頁數")
    private int totalPages;
}
