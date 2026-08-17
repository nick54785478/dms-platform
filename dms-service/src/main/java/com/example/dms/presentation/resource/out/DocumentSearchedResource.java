package com.example.dms.presentation.resource.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "文件搜尋結果載體")
public class DocumentSearchedResource {
    @Schema(description = "文件 ID")
    private String id;
    @Schema(description = "標題")
    private String title;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "關聯檔案 ID")
    private String fileId;
    @Schema(description = "狀態")
    private String status;
    @Schema(description = "建立時間")
    private LocalDateTime createdAt;
    @Schema(description = "更新時間")
    private LocalDateTime updatedAt;
    @Schema(description = "語意化版本號")
    private String semanticVersion;
}
