package com.example.demo.application.port.in;

import com.example.demo.application.shared.command.PresignedUrlCommand;
import com.example.demo.application.shared.command.UploadFileCommand;
import com.example.demo.application.shared.dto.PresignedUrlGeneratedResult;
import com.example.demo.domain.file.aggregate.root.FileMetadata;

/**
 * 管理檔案的 Inbound Port (UseCase)。
 * <p>
 * 定義了應用層管理檔案 (如一般上傳、產生預先簽名 URL 等) 的業務功能介面。
 * 接收 Command 並回傳對應的領域物件或 DTO。
 * </p>
 */
public interface ManageFileUseCase {

    /**
     * 直接上傳小檔案。
     * 
     * @param command 上傳檔案命令
     * @return 創建的檔案中繼資料 (領域實體)
     * @throws Exception 處理過程中可能發生的例外
     */
    FileMetadata uploadFile(UploadFileCommand command) throws Exception;

    /**
     * 簽發預先簽名的上傳網址 (供客戶端直接上傳)。
     * 
     * @param command 預先簽名網址命令
     * @return 包含 URL 與產生的 FileId 的結果
     * @throws Exception 處理過程中可能發生的例外
     */
    PresignedUrlGeneratedResult generatePresignedUploadUrl(PresignedUrlCommand command) throws Exception;

    /**
     * 簽發預先簽名的下載網址。
     * 
     * @param fileId 檔案 ID
     * @param expiryMinutes 網址有效時長 (分鐘)
     * @return 下載網址字串
     * @throws Exception 處理過程中可能發生的例外
     */
    String generatePresignedDownloadUrl(String fileId, boolean isDownload, int expiryMinutes) throws Exception;
}
