package com.example.validation.application.shared.query;

public class ListTemplateFieldMappingQuery {
    private final String templateCode;

    public ListTemplateFieldMappingQuery(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getTemplateCode() {
        return templateCode;
    }
}
