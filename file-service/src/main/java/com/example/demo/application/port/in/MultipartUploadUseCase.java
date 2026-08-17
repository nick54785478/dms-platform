package com.example.demo.application.port.in;

import com.example.demo.application.shared.command.MultipartUploadCommand;
import com.example.demo.application.shared.dto.MultipartUploadInitiatedResult;
import com.example.demo.domain.file.aggregate.root.FileMetadata;

/**
 * 分段上傳的 Inbound Port (UseCase)。
 * <p>
 * 定義了應用層處理大檔案分段上傳流程的業務功能介面。
 * 包含初始化、取得分段上傳網址、完成與放棄分段上傳。
 * </p>
 */
public interface MultipartUploadUseCase {

    /**
     * 初始化分段上傳。
     * 
     * @param command 初始化分段上傳命令
     * @return 包含 FileId 與 UploadId 的結果
     * @throws Exception 處理過程中可能發生的例外
     */
    MultipartUploadInitiatedResult initiateMultipartUpload(MultipartUploadCommand.InitiateCommand command) throws Exception;

    /**
     * 取得特定分段的預先簽名上傳網址。
     * 
     * @param fileId 檔案 ID
     * @param uploadId 分段上傳 ID
     * @param partNumber 分段編號
     * @param expiryMinutes 網址有效時長 (分鐘)
     * @return 該分段的預先簽名上傳網址
     * @throws Exception 處理過程中可能發生的例外
     */
    String getPresignedPartUrl(String fileId, String uploadId, int partNumber, int expiryMinutes) throws Exception;

    /**
     * 完成分段上傳，合併所有分段。
     * 
     * @param command 完成分段上傳命令
     * @return 完成後建立的檔案中繼資料 (領域實體)
     * @throws Exception 處理過程中可能發生的例外
     */
    FileMetadata completeMultipartUpload(MultipartUploadCommand.CompleteCommand command) throws Exception;

    /**
     * 放棄分段上傳，清理已上傳的分段。
     * 
     * @param command 放棄分段上傳命令
     * @throws Exception 處理過程中可能發生的例外
     */
    void abortMultipartUpload(MultipartUploadCommand.AbortCommand command) throws Exception;
}
