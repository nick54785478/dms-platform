package com.dms.template.domain.template.exception;

/**
 * 範本找不到例外 (Domain Exception)
 */
public class TemplateNotFoundException extends RuntimeException {
    public TemplateNotFoundException(String id) {
        super("Template not found with ID: " + id);
    }
}
