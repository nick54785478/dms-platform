package com.dms.template.presentation.resource.in;

/**
 * 建立範本的請求載體 (Inbound Resource)
 */
public record CreateTemplateResource(
    String templateType,
    String templateCode,
    String name,
    String description
) {}
