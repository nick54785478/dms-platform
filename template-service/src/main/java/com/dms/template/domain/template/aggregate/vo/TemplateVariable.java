package com.dms.template.domain.template.aggregate.vo;

import lombok.Value;

/**
 * 範本變數 (Value Object)
 * 定義範本內的變數名稱、預設值以及資料格式設定
 */
@Value
public class TemplateVariable {
    String name;
    String defaultValue;
    String dataType; // e.g. "String", "Number", "Date", "Boolean"
    String format;   // e.g. "yyyy-MM-dd"
    Boolean required;

    public static TemplateVariable of(String name, String defaultValue, String dataType, String format, Boolean required) {
        return new TemplateVariable(name, defaultValue, dataType, format, required);
    }

    // 為了相容原本的 of 方法
    public static TemplateVariable of(String name, String defaultValue) {
        return new TemplateVariable(name, defaultValue, "String", null, false);
    }
}
