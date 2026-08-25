package com.example.validation.application.port.in;

import com.example.validation.application.shared.command.DeleteValidationPolicyCommand;

/**
 * 刪除驗證規則 UseCase (Inbound Port)
 * <p>
 * 負責處理刪除指定的 {@link com.example.validation.domain.policy.aggregate.root.ValidationPolicy} 的應用邏輯。
 * </p>
 */
public interface DeleteValidationPolicyUseCase {
    
    /**
     * 執行刪除驗證規則
     *
     * @param command 包含刪除所需條件的 Command 物件
     */
    void delete(DeleteValidationPolicyCommand command);
}
