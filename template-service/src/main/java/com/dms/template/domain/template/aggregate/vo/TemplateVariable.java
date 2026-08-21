package com.dms.template.domain.template.aggregate.vo;

import lombok.Value;

/**
 * 範本變數 (Value Object)
 * 定義範本內的變數名稱與預設值
 */
@Value
public class TemplateVariable {
    String name;
    String defaultValue;

    public static TemplateVariable of(String name, String defaultValue) {
        return new TemplateVariable(name, defaultValue);
    }
}
