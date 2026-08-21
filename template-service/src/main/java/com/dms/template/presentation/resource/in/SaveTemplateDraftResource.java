package com.dms.template.presentation.resource.in;

import com.dms.template.domain.template.aggregate.vo.TemplateVariable;
import java.util.List;

/**
 * 儲存範本草稿的請求載體 (Inbound Resource)
 */
public record SaveTemplateDraftResource(
    String contentDefinition,
    List<TemplateVariable> variables
) {}
