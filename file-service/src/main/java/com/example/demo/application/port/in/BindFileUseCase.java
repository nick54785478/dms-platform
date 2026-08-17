package com.example.demo.application.port.in;

/**
 * 綁定檔案的 Inbound Port (UseCase)。
 * <p>
 * 定義了應用層將上傳檔案綁定到特定業務領域實體（如 Document）的業務功能介面。
 * 綁定操作通常會觸發將暫存檔案搬移至永久儲存區的處理。
 * </p>
 */
public interface BindFileUseCase {

    /**
     * 綁定指定的檔案。
     * 將檔案從暫存區移動到永久儲存區，並更新其狀態為已綁定。
     * 
     * @param fileId 要綁定的檔案 ID
     * @throws Exception 處理過程中可能發生的例外
     */
    void bindFile(String fileId) throws Exception;
}
