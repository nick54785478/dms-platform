package com.dms.template.application.command;

/**
 * 建立範本的命令 (Command)
 */
public record CreateTemplateCommand(
    String templateType,
    String templateCode,
    String name,
    String description
) {}
