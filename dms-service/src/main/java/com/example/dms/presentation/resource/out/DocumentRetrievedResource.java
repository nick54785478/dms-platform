package com.example.dms.presentation.resource.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Schema(description = "文件詳細資訊與歷史紀錄載體")
public class DocumentRetrievedResource {
    @Schema(description = "文件 ID")
    private String id;
    @Schema(description = "標題")
    private String title;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "當前最新檔案 ID")
    private String fileId;
    @Schema(description = "狀態")
    private String status;
    @Schema(description = "建立時間")
    private LocalDateTime createdAt;
    @Schema(description = "更新時間")
    private LocalDateTime updatedAt;
    @Schema(description = "語意化版本號")
    private String semanticVersion;
    @Schema(description = "歷史版本列表")
    private List<DocumentVersionResource> history;
}
