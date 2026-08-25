package com.example.validation.application.port.out;

import com.example.validation.domain.policy.aggregate.root.ValidationPolicy;
import java.util.List;
import java.util.Optional;

/**
 * 驗證規則儲存庫介面 (Outbound Port)
 * <p>
 * 定義 Application 層對於 {@link ValidationPolicy} 領域模型的持久化操作需求，
 * 供 Infrastructure 層的 Adapter (如 Spring Data JPA Repository Adapter) 進行實作。
 * 藉由依賴反轉，確保領域與應用層不相依於底層資料庫技術。
 * </p>
 */
public interface ValidationPolicyRepositoryPort {
    
    /**
     * 依據範本代碼查詢所有關聯的驗證規則
     *
     * @param code 範本代碼 (Template Code)
     * @return 該範本代碼下所有的 {@link ValidationPolicy} 領域物件清單
     */
    List<ValidationPolicy> findByCode(String code);
    
    /**
     * 儲存或更新驗證規則
     *
     * @param policy 欲儲存的 {@link ValidationPolicy} 領域物件
     * @return 儲存成功後的 {@link ValidationPolicy} 領域物件
     */
    ValidationPolicy save(ValidationPolicy policy);
    
    /**
     * 依據 ID 查詢驗證規則
     *
     * @param id 驗證規則 ID
     * @return 封裝查詢結果的 Optional 物件
     */
    Optional<ValidationPolicy> findById(Long id);
    
    /**
     * 查詢系統中所有的驗證規則
     *
     * @return 所有的 {@link ValidationPolicy} 領域物件清單
     */
    List<ValidationPolicy> findAll();
    
    /**
     * 依據 ID 刪除驗證規則
     *
     * @param id 欲刪除的驗證規則 ID
     */
    void deleteById(Long id);
}
