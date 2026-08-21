package com.dms.template.application.command;

import com.dms.template.domain.template.aggregate.vo.TemplateVariable;
import java.util.List;

/**
 * 儲存範本草稿的命令 (Command)
 */
public record SaveTemplateDraftCommand(
    String templateId,
    String contentDefinition,
    List<TemplateVariable> variables
) {}
