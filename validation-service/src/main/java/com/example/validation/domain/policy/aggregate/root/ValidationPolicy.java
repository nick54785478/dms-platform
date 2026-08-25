package com.example.validation.domain.policy.aggregate.root;

import com.example.validation.domain.policy.aggregate.vo.PolicyRule;
import com.example.validation.domain.policy.aggregate.vo.PolicyTarget;
import com.example.validation.domain.shared.vo.YesNo;

/**
 * 範本驗證規則 (Aggregate Root)
 * <p>
 * 封裝範本上傳時的動態驗證邏輯與屬性。
 * 作為聚合根，它負責確保內部的 {@link PolicyTarget} (套用目標) 
 * 與 {@link PolicyRule} (規則內容) 狀態的一致性，
 * 並提供 Facade 方法供外層 (如 Validator) 取得具體的設定參數。
 */
public class ValidationPolicy {

    /**
     * 聚合根的唯一識別碼 (資料庫 Primary Key)
     */
    private Long id;

    /**
     * 對應到 Template Service 的範本唯一代碼
     */
    private String code;

    /**
     * 驗證規則所套用的目標 (Value Object)
     * 定義了此規則應作用於哪個範本、哪個頁籤以及哪個欄位。
     */
    private PolicyTarget target;

    /**
     * 驗證規則的具體內容 (Value Object)
     * 封裝了驗證類型、SpEL 邏輯表達式以及錯誤訊息。
     */
    private PolicyRule rule;

    /**
     * 規則執行優先級序號，數字越小越優先執行
     */
    private Integer priorityNo;

    /**
     * 是否啟用此規則 (Y=啟用, N=停用)
     */
    private YesNo activeFlag;

    /**
     * 私有建構子，由靜態工廠方法呼叫。
     *
     * @param id         聚合根 ID
     * @param code       範本唯一代碼
     * @param target     驗證目標 (VO)
     * @param rule       規則內容 (VO)
     * @param priorityNo 優先級序號
     * @param activeFlag 啟用狀態
     */
    private ValidationPolicy(Long id, String code, PolicyTarget target, PolicyRule rule, Integer priorityNo, YesNo activeFlag) {
        this.id = id;
        this.code = code;
        this.target = target;
        this.rule = rule;
        this.priorityNo = priorityNo;
        this.activeFlag = activeFlag;
    }

    /**
     * 從資料庫或持久層重新建構 Aggregate 時使用的工廠方法。
     * 用於將既有的資料還原為 Domain Object，不會觸發「建立」的業務邏輯。
     *
     * @param id         實體 ID
     * @param code       範本唯一代碼
     * @param target     封裝的目標 VO
     * @param rule       封裝的規則 VO
     * @param priorityNo 優先級序號
     * @param activeFlag 啟用狀態
     * @return 重新建構完成的 ValidationPolicy
     */
    public static ValidationPolicy reconstitute(Long id, String code, PolicyTarget target, PolicyRule rule, Integer priorityNo, YesNo activeFlag) {
        return new ValidationPolicy(id, code, target, rule, priorityNo, activeFlag);
    }

    /**
     * 建立全新 ValidationPolicy (業務行為) 時使用的工廠方法。
     * 會賦予預設值 (例如：預設啟用 `YesNo.Y`)。
     *
     * @param code       範本唯一代碼
     * @param target     封裝的目標 VO
     * @param rule       封裝的規則 VO
     * @param priorityNo 優先級序號
     * @return 全新的 ValidationPolicy 實體 (尚未持久化，無 ID)
     */
    public static ValidationPolicy create(String code, PolicyTarget target, PolicyRule rule, Integer priorityNo) {
        // 新建立的政策預設為啟用
        return new ValidationPolicy(null, code, target, rule, priorityNo, YesNo.Y);
    }

    /**
     * 更新 ValidationPolicy 屬性 (業務行為)
     *
     * @param code       更新後的範本唯一代碼
     * @param target     更新後的目標 VO
     * @param rule       更新後的規則 VO
     * @param priorityNo 更新後的優先級序號
     * @param activeFlag 更新後的啟用狀態
     */
    public void update(String code, PolicyTarget target, PolicyRule rule, Integer priorityNo, YesNo activeFlag) {
        this.code = code;
        this.target = target;
        this.rule = rule;
        this.priorityNo = priorityNo;
        this.activeFlag = activeFlag;
    }

    /**
     * 取得聚合根 ID
     * @return 識別碼 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 取得範本唯一代碼
     * @return code
     */
    public String getCode() {
        return code;
    }

    /**
     * 取得驗證目標 VO
     * @return PolicyTarget
     */
    public PolicyTarget getTarget() {
        return target;
    }

    /**
     * 取得驗證規則 VO
     * @return PolicyRule
     */
    public PolicyRule getPolicyRule() {
        return rule;
    }

    /**
     * 取得執行優先級序號
     * @return 優先順序
     */
    public Integer getPriorityNo() {
        return priorityNo;
    }

    /**
     * 取得啟用狀態
     * @return YesNo.Y 或是 YesNo.N
     */
    public YesNo getActiveFlag() {
        return activeFlag;
    }

    // ========================================================================
    // Facade Methods (迪米特法則 Law of Demeter)
    // 將內部 VO 的屬性平坦化拋出，避免外部 (如 Validator) 過度耦合內部 VO 的結構
    // ========================================================================

    /**
     * @return 驗證類型 (e.g., ROW, SHEET)
     */
    public String getType() {
        return rule.getType();
    }

    /**
     * @return 範本名稱
     */
    public String getTemplateName() {
        return target.getTemplateName();
    }

    /**
     * @return 範本內的頁籤名稱
     */
    public String getTemplateSheetName() {
        return target.getTemplateSheetName();
    }

    /**
     * @return 對應的欄位名稱
     */
    public String getMappingFieldName() {
        return target.getMappingFieldName();
    }

    /**
     * @return 規則類型 (e.g., ENFORCE_ROW_VALIDATION)
     */
    public String getRule() {
        return rule.getRule();
    }

    /**
     * @return 具體執行的 SpEL Expression
     */
    public String getExpression() {
        return rule.getExpression();
    }

    /**
     * @return 驗證失敗時的錯誤訊息範本
     */
    public String getErrorMessage() {
        return rule.getErrorMessage();
    }
}
