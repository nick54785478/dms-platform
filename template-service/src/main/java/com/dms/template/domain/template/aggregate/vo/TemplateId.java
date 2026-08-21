package com.dms.template.domain.template.aggregate.vo;

import lombok.Value;

import java.util.UUID;

/**
 * 範本的唯一識別碼 (Value Object)
 */
@Value
public class TemplateId {
    String value;

    public static TemplateId generate() {
        return new TemplateId(UUID.randomUUID().toString());
    }

    public static TemplateId of(String value) {
        return new TemplateId(value);
    }
}
