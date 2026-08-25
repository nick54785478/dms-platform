package com.example.validation.presentation.resource.in;

public class CreateTemplateFieldMappingResource {
    private String templateCode;
    private String templateSheetName;
    private String headerName;
    private String mappingFieldName;

    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getTemplateSheetName() { return templateSheetName; }
    public void setTemplateSheetName(String templateSheetName) { this.templateSheetName = templateSheetName; }
    public String getHeaderName() { return headerName; }
    public void setHeaderName(String headerName) { this.headerName = headerName; }
    public String getMappingFieldName() { return mappingFieldName; }
    public void setMappingFieldName(String mappingFieldName) { this.mappingFieldName = mappingFieldName; }
}
