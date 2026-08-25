package com.example.validation.application.shared.command;

public class UpdateTemplateFieldMappingCommand {
    private final Long id;
    private final String templateCode;
    private final String templateSheetName;
    private final String headerName;
    private final String mappingFieldName;

    public UpdateTemplateFieldMappingCommand(Long id, String templateCode, String templateSheetName, String headerName, String mappingFieldName) {
        this.id = id;
        this.templateCode = templateCode;
        this.templateSheetName = templateSheetName;
        this.headerName = headerName;
        this.mappingFieldName = mappingFieldName;
    }

    public Long getId() { return id; }
    public String getTemplateCode() { return templateCode; }
    public String getTemplateSheetName() { return templateSheetName; }
    public String getHeaderName() { return headerName; }
    public String getMappingFieldName() { return mappingFieldName; }
}
