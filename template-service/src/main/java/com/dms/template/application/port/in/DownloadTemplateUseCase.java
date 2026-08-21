package com.dms.template.application.port.in;

import com.dms.template.application.dto.DocumentGeneratedResult;
import com.dms.template.application.query.DownloadTemplateQuery;

/**
 * 下載範本的業務案例介面 (Inbound Port)
 */
public interface DownloadTemplateUseCase {
    DocumentGeneratedResult downloadTemplate(DownloadTemplateQuery query);
}
