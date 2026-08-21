package com.dms.template.application.dto;

public record TemplateVersionGottenResult(
    String version,
    String status,
    String contentDefinition
) {}
