package com.example.dms.presentation.resource.out;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "文件歷史版本載體")
public class DocumentVersionResource {
    @Schema(description = "版本紀錄 ID")
    private String versionId;
    @Schema(description = "主版本號")
    private int majorVersion;
    @Schema(description = "次版本號")
    private int minorVersion;
    @Schema(description = "語意化版本號")
    private String semanticVersion;
    @Schema(description = "當時的標題")
    private String title;
    @Schema(description = "當時的描述")
    private String description;
    @Schema(description = "當時關聯的實體檔案 ID")
    private String fileId;
    @Schema(description = "此版本的建立時間")
    private LocalDateTime createdAt;
}
