package com.dms.template.presentation.resource.out;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "取得單一範本的回應物件")
public record TemplateRetrievedResource(
    @Schema(description = "範本 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    String id,
    @Schema(description = "範本類型 (例如: EXCEL, PDF)", example = "EXCEL")
    String templateType,
    @Schema(description = "範本代碼", example = "TPL-001")
    String templateCode,
    @Schema(description = "範本名稱", example = "採購單範本")
    String name,
    @Schema(description = "範本說明", example = "用於一般採購申請使用")
    String description,
    @Schema(description = "範本設計內容 (Draft Json)", example = "{\"columns\": []}")
    String draftJson,
    @Schema(description = "最新正式版號", example = "V1.0")
    String latestVersion
) {}
