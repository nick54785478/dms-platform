package com.dms.template.domain.template.event;

public class TemplatePublishedEvent {
    private String templateCode;
    private String templateType;
    private String contentDefinition;

    public TemplatePublishedEvent() {}

    public TemplatePublishedEvent(String templateCode, String templateType, String contentDefinition) {
        this.templateCode = templateCode;
        this.templateType = templateType;
        this.contentDefinition = contentDefinition;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getContentDefinition() {
        return contentDefinition;
    }

    public void setContentDefinition(String contentDefinition) {
        this.contentDefinition = contentDefinition;
    }
}
