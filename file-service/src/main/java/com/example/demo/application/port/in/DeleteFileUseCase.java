package com.example.demo.application.port.in;

/**
 * 刪除檔案的 Inbound Port (UseCase)。
 * <p>
 * 定義了應用層刪除不再需要的檔案資源的業務功能介面。
 * 刪除操作會清除儲存空間中的實際檔案及資料庫中的中繼資料。
 * </p>
 */
public interface DeleteFileUseCase {

    /**
     * 刪除指定的檔案。
     * 
     * @param fileId 要刪除的檔案 ID
     * @throws Exception 處理過程中可能發生的例外
     */
    void deleteFile(String fileId) throws Exception;
}
