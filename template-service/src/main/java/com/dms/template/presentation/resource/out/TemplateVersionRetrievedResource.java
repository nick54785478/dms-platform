package com.dms.template.presentation.resource.out;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "取得範本歷史版本的回應物件")
public record TemplateVersionRetrievedResource(
    @Schema(description = "版號", example = "V1.0-DRAFT")
    String version,
    @Schema(description = "狀態 (例如: DRAFT, PUBLISHED)", example = "DRAFT")
    String status,
    @Schema(description = "該版本的設計內容 (Draft Json)", example = "{\"columns\": []}")
    String contentDefinition
) {}
