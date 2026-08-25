package com.example.validation.application.port.in;

import com.example.validation.application.shared.command.ValidateExcelCommand;

/**
 * 驗證 Excel 檔案 UseCase (Inbound Port)
 * <p>
 * 提供給 API 介面主動呼叫，對指定的 Excel 檔案內容執行 SpEL 驗證。
 * 將解析檔案並匹配 {@link com.example.validation.domain.policy.aggregate.root.ValidationPolicy}。
 * </p>
 */
public interface ValidateExcelUseCase {
    
    /**
     * 執行 Excel 檔案驗證作業
     *
     * @param command 包含範本代碼與檔案 byte array 的 Command 物件
     */
    void validate(ValidateExcelCommand command);
}
