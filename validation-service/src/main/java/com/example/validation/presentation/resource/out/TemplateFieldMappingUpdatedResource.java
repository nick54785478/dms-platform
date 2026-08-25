package com.example.validation.presentation.resource.out;

public class TemplateFieldMappingUpdatedResource {
    private boolean success;

    public TemplateFieldMappingUpdatedResource() {}

    public TemplateFieldMappingUpdatedResource(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
