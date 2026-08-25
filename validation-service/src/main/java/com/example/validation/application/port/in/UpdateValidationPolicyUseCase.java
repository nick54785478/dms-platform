package com.example.validation.application.port.in;

import com.example.validation.application.shared.command.UpdateValidationPolicyCommand;

/**
 * 更新驗證規則 UseCase (Inbound Port)
 * <p>
 * 負責處理更新現有 {@link com.example.validation.domain.policy.aggregate.root.ValidationPolicy} 的應用邏輯。
 * </p>
 */
public interface UpdateValidationPolicyUseCase {
    
    /**
     * 執行更新驗證規則
     *
     * @param command 包含更新所需資料的 Command 物件
     */
    void update(UpdateValidationPolicyCommand command);
}
