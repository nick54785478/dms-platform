package com.example.validation.application.shared.query;

public class ListTemplateFieldMappingBySheetQuery {
    private final String templateCode;
    private final String templateSheetName;

    public ListTemplateFieldMappingBySheetQuery(String templateCode, String templateSheetName) {
        this.templateCode = templateCode;
        this.templateSheetName = templateSheetName;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public String getTemplateSheetName() {
        return templateSheetName;
    }
}
