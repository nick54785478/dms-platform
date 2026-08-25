package com.example.validation.application.shared.command;

public class CreateTemplateFieldMappingCommand {
    private final String templateCode;
    private final String templateSheetName;
    private final String headerName;
    private final String mappingFieldName;

    public CreateTemplateFieldMappingCommand(String templateCode, String templateSheetName, String headerName, String mappingFieldName) {
        this.templateCode = templateCode;
        this.templateSheetName = templateSheetName;
        this.headerName = headerName;
        this.mappingFieldName = mappingFieldName;
    }

    public String getTemplateCode() { return templateCode; }
    public String getTemplateSheetName() { return templateSheetName; }
    public String getHeaderName() { return headerName; }
    public String getMappingFieldName() { return mappingFieldName; }
}
