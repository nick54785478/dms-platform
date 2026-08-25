package com.example.validation.domain.policy.aggregate.vo;

import java.util.Objects;

/**
 * 驗證目標 (Value Object)
 * <p>
 * 領域實質物件 (Value Object)，負責標識該驗證規則所套用的具體位置。
 * 封裝了「範本名稱」、「頁籤名稱」以及「對應欄位」這三個維度的資訊，
 * 透過這三個屬性可以精確定位該規則作用的範圍。由於是 Value Object，
 * 其為不可變 (Immutable) 物件，且透過屬性值來判定相等性。
 */
public class PolicyTarget {

    /**
     * 範本名稱 (例如: "UserImportTemplate")
     */
    private final String templateName;

    /**
     * 範本內的 Sheet Name (頁籤名稱，例如: "Sheet1" 或 "Users")
     */
    private final String templateSheetName;

    /**
     * Mapping Field Name (對應的欄位名稱，例如: "username" 或 "email")
     */
    private final String mappingFieldName;

    /**
     * 私有建構子，確保必須透過工廠方法建立。
     *
     * @param templateName      範本名稱
     * @param templateSheetName 範本內的頁籤名稱
     * @param mappingFieldName  對應的欄位名稱
     */
    private PolicyTarget(String templateName, String templateSheetName, String mappingFieldName) {
        this.templateName = templateName;
        this.templateSheetName = templateSheetName;
        this.mappingFieldName = mappingFieldName;
    }

    /**
     * 建立 PolicyTarget 的靜態工廠方法。
     *
     * @param templateName      範本名稱
     * @param templateSheetName 範本內的頁籤名稱
     * @param mappingFieldName  對應的欄位名稱
     * @return 封裝好的 PolicyTarget (Value Object) 實體
     */
    public static PolicyTarget of(String templateName, String templateSheetName, String mappingFieldName) {
        return new PolicyTarget(templateName, templateSheetName, mappingFieldName);
    }

    /**
     * 取得範本名稱
     *
     * @return 範本名稱
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * 取得範本內的頁籤名稱
     *
     * @return 頁籤名稱
     */
    public String getTemplateSheetName() {
        return templateSheetName;
    }

    /**
     * 取得對應的欄位名稱
     *
     * @return 欄位名稱
     */
    public String getMappingFieldName() {
        return mappingFieldName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PolicyTarget that = (PolicyTarget) o;
        return Objects.equals(templateName, that.templateName) &&
                Objects.equals(templateSheetName, that.templateSheetName) &&
                Objects.equals(mappingFieldName, that.mappingFieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(templateName, templateSheetName, mappingFieldName);
    }
}
