package com.dms.template.presentation.resource.in;

import com.dms.template.domain.template.aggregate.vo.TemplateVariable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 儲存範本草稿的請求載體 (Inbound Resource)
 */
@Schema(description = "儲存範本草稿的請求物件")
public record SaveTemplateDraftResource(
    @Schema(description = "範本設計內容的 JSON 定義 (Draft Json)", example = "{\"columns\": []}")
    String contentDefinition,
    @Schema(description = "範本變數列表")
    List<TemplateVariable> variables
) {}
