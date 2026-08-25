package com.example.validation.application.shared.query;

public class ListTemplateSheetNameQuery {
    private final String templateCode;

    public ListTemplateSheetNameQuery(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getTemplateCode() {
        return templateCode;
    }
}
