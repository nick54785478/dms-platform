package com.dms.template.domain.template.aggregate.vo;

import java.util.UUID;

/**
 * 範本的唯一識別碼 (Value Object)
 */
public record TemplateId(String value) {

    public static TemplateId generate() {
        return new TemplateId(UUID.randomUUID().toString());
    }

    public static TemplateId of(String value) {
        return new TemplateId(value);
    }
}
