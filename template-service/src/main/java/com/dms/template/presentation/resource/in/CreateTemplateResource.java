package com.dms.template.presentation.resource.in;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 建立範本的請求載體 (Inbound Resource)
 */
@Schema(description = "建立範本的請求物件")
public record CreateTemplateResource(
    @Schema(description = "範本類型 (例如: EXCEL, PDF)", example = "EXCEL")
    String templateType,
    @Schema(description = "範本代碼 (獨一無二)", example = "TPL-001")
    String templateCode,
    @Schema(description = "範本名稱", example = "採購單範本")
    String name,
    @Schema(description = "範本說明", example = "用於一般採購申請使用")
    String description
) {}
