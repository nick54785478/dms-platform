package com.dms.template.application.command;

/**
 * 發佈範本命令 (Command)
 */
public record PublishTemplateCommand(
    String templateId,
    String version
) {}
