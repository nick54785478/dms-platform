package com.example.validation.application.port.in;

import com.example.validation.application.shared.dto.ValidationPolicyGottenResult;
import com.example.validation.application.shared.query.GetValidationPolicyQuery;

/**
 * 取得單筆驗證規則 UseCase (Inbound Port)
 * <p>
 * 負責處理查詢單筆 {@link com.example.validation.domain.policy.aggregate.root.ValidationPolicy} 的應用邏輯。
 * </p>
 */
public interface GetValidationPolicyUseCase {
    
    /**
     * 執行取得驗證規則
     *
     * @param query 包含查詢條件的 Query 物件
     * @return 包含查詢結果資料的 DTO 物件
     */
    ValidationPolicyGottenResult get(GetValidationPolicyQuery query);
}
