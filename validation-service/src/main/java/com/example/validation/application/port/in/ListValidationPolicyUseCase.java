package com.example.validation.application.port.in;

import com.example.validation.application.shared.dto.ValidationPolicySearchedResult;
import com.example.validation.application.shared.query.ListValidationPolicyQuery;

import java.util.List;

/**
 * 查詢驗證規則清單 UseCase (Inbound Port)
 * <p>
 * 負責處理查詢多筆 {@link com.example.validation.domain.policy.aggregate.root.ValidationPolicy} 的應用邏輯。
 * </p>
 */
public interface ListValidationPolicyUseCase {
    
    /**
     * 執行查詢
     *
     * @param query 包含查詢條件的 Query 物件
     * @return 查詢結果的 DTO 清單
     */
    List<ValidationPolicySearchedResult> list(ListValidationPolicyQuery query);
}
