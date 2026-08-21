package com.dms.template.application.query;

/**
 * 查詢範本列表條件
 */
public record SearchTemplateQuery(
    String templateType,
    String templateCode,
    String name,
    int page,
    int size
) {}
