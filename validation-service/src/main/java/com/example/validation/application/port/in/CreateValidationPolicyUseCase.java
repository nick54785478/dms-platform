package com.example.validation.application.port.in;

import com.example.validation.application.shared.command.CreateValidationPolicyCommand;

/**
 * 建立驗證規則 UseCase (Inbound Port)
 * <p>
 * 負責處理建立新的 {@link com.example.validation.domain.policy.aggregate.root.ValidationPolicy} 的應用邏輯。
 * 接收包含建立資訊的 Command，並回傳建立完成的驗證規則 ID。
 * </p>
 */
public interface CreateValidationPolicyUseCase {
    
    /**
     * 執行建立驗證規則
     *
     * @param command 包含建立所需資料的 Command 物件
     * @return 建立成功後產生的驗證規則 ID
     */
    Long create(CreateValidationPolicyCommand command);
}
