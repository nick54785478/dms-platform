package com.example.validation.presentation.resource.out;

import io.swagger.v3.oas.annotations.media.Schema;

public record ExcelValidatedResource(
        @Schema(description = "回應代碼", example = "200")
        String code,
        
        @Schema(description = "回應訊息", example = "驗證成功")
        String message
) {
}
