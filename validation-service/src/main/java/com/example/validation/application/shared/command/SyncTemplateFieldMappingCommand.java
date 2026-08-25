package com.example.validation.application.shared.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 同步範本欄位對應的 Command (事件驅動)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncTemplateFieldMappingCommand {
    private String templateCode;
    private String templateType;
    private String contentDefinition;
}
