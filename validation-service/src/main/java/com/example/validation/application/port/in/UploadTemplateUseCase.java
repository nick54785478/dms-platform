package com.example.validation.application.port.in;

import com.example.validation.application.shared.command.UploadTemplateCommand;
import java.io.IOException;

/**
 * 上傳範本 UseCase (Inbound Port)
 * <p>
 * 負責處理上傳實體 Excel 範本檔案時的應用邏輯，通常伴隨著呼叫客製驗證。
 * </p>
 */
public interface UploadTemplateUseCase {
    
    /**
     * 執行上傳範本作業
     *
     * @param command 包含檔案名稱、代碼與檔案內容的 Command 物件
     * @throws IOException 當檔案讀取或寫入發生錯誤時拋出
     */
    void upload(UploadTemplateCommand command) throws IOException;
}
