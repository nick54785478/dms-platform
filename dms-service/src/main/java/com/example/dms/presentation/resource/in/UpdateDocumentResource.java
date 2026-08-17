package com.example.dms.presentation.resource.in;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新文件請求載體")
public class UpdateDocumentResource {
    
    @Schema(description = "文件標題", example = "新版合約文件")
    private String title;
    
    @Schema(description = "文件描述", example = "這是更新後的合約內容")
    private String description;
}
