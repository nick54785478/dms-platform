package com.dms.template.presentation.resource.out;

/**
 * 建立範本的回應載體 (Outbound Resource)
 */
public record TemplateCreatedResource(
    String id,
    String templateType,
    String templateCode,
    String name,
    String description
) {}
