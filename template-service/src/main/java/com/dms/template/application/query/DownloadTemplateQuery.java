package com.dms.template.application.query;

import java.util.Map;

/**
 * 下載範本測試檔的查詢 (Query)
 */
public record DownloadTemplateQuery(
    String templateId,
    Map<String, Object> data
) {}
