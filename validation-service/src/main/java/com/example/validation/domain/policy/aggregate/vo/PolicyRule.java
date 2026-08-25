package com.example.validation.domain.policy.aggregate.vo;

import java.util.Objects;

/**
 * 驗證規則內容 (Value Object)
 * <p>
 * 領域實質物件 (Value Object)，負責封裝具體的驗證邏輯與錯誤訊息。
 * 將驗證的「類型」、「規則設定」、「表達式 (SpEL)」與「錯誤訊息」內聚在一起。
 * 由於是 Value Object，其為不可變 (Immutable) 物件。
 */
public class PolicyRule {

    /**
     * 驗證類型 (例如: ROW 代表單行驗證，SHEET 代表整表驗證)
     */
    private final String type;

    /**
     * 規則類型 (例如: ENFORCE_ROW_VALIDATION，表示即使欄位為空仍要強制執行驗證)
     */
    private final String rule;

    /**
     * 具體執行的 SpEL Expression (Spring 表達式)，用於動態執行邏輯判斷
     */
    private final String expression;

    /**
     * 驗證失敗時的錯誤訊息範本，可支援 SpEL 動態渲染上下文參數
     */
    private final String errorMessage;

    /**
     * 私有建構子，確保必須透過工廠方法建立。
     *
     * @param type         驗證類型 (ROW, SHEET)
     * @param rule         規則類型
     * @param expression   SpEL 驗證表達式
     * @param errorMessage 錯誤訊息
     */
    private PolicyRule(String type, String rule, String expression, String errorMessage) {
        this.type = type;
        this.rule = rule;
        this.expression = expression;
        this.errorMessage = errorMessage;
    }

    /**
     * 建立 PolicyRule 的靜態工廠方法。
     *
     * @param type         驗證類型 (ROW, SHEET)
     * @param rule         規則類型
     * @param expression   SpEL 驗證表達式
     * @param errorMessage 錯誤訊息
     * @return 封裝好的 PolicyRule (Value Object) 實體
     */
    public static PolicyRule of(String type, String rule, String expression, String errorMessage) {
        return new PolicyRule(type, rule, expression, errorMessage);
    }

    /**
     * 取得驗證類型
     *
     * @return 驗證類型
     */
    public String getType() {
        return type;
    }

    /**
     * 取得規則類型
     *
     * @return 規則類型
     */
    public String getRule() {
        return rule;
    }

    /**
     * 取得 SpEL 驗證表達式
     *
     * @return SpEL 表達式
     */
    public String getExpression() {
        return expression;
    }

    /**
     * 取得驗證失敗時的錯誤訊息範本
     *
     * @return 錯誤訊息
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PolicyRule that = (PolicyRule) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(rule, that.rule) &&
                Objects.equals(expression, that.expression) &&
                Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, rule, expression, errorMessage);
    }
}
