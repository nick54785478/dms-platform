package com.example.demo.application.port.out;

import com.example.demo.application.shared.command.CloneFilePairCommand;
import com.example.demo.application.shared.dto.FileListSearchedResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 檔案儲存管理器 Outbound Port。
 * <p>
 * 定義應用層操作底層 Blob Storage (如 MinIO、AWS S3 等) 的介面。
 * 負責處理實體的檔案上傳、下載、刪除、複製以及分段上傳等基礎設施操作。
 * </p>
 */
public interface BlobStorageManagerPort {

    /**
     * 檢查指定的 Bucket 是否存在。
     *
     * @param bucket Bucket 名稱
     * @return 如果存在則回傳 true，否則回傳 false
     * @throws Exception 操作過程中可能發生的例外
     */
    Boolean checkBucketExists(String bucket) throws Exception;

    /**
     * 上傳檔案 (MultipartFile)。
     *
     * @param bucket Bucket 名稱
     * @param file   上傳的檔案
     * @return 檔案的 ETag 或儲存的識別碼
     * @throws Exception 操作過程中可能發生的例外
     */
    String uploadFile(String bucket, MultipartFile file) throws Exception;

    /**
     * 上傳檔案到指定路徑。
     *
     * @param bucket   Bucket 名稱
     * @param file     上傳的檔案
     * @param filePath 儲存的路徑
     * @param fileName 儲存的檔案名稱
     * @return 檔案的 ETag 或儲存的識別碼
     * @throws Exception 操作過程中可能發生的例外
     */
    String uploadFile(String bucket, MultipartFile file, String filePath, String fileName) throws Exception;

    /**
     * 下載檔案。
     *
     * @param bucket   Bucket 名稱
     * @param filePath 檔案儲存的路徑
     * @param fileName 檔案名稱
     * @return 檔案的 InputStream，呼叫端需負責關閉
     * @throws Exception 操作過程中可能發生的例外
     */
    InputStream downloadFile(String bucket, String filePath, String fileName) throws Exception;

    /**
     * 刪除檔案。
     *
     * @param bucket   Bucket 名稱
     * @param filePath 檔案儲存的路徑
     * @param fileName 檔案名稱
     * @throws Exception 操作過程中可能發生的例外
     */
    void deleteFile(String bucket, String filePath, String fileName) throws Exception;

    /**
     * 列出該 bucket 下，以 prefix 為開頭的所有物件 key (非遞迴)。
     *
     * @param bucket Bucket 名稱
     * @param prefix 檔案前綴
     * @return 物件 key 列表
     */
    List<String> listFiles(String bucket, String prefix);

    /**
     * 分頁列出該 prefix 下的物件 key (非遞迴)。
     *
     * @param bucket     Bucket 名稱
     * @param prefix     檔案前綴
     * @param startAfter 分頁起始標記 (在此之後的項目)
     * @param maxKeys    最大回傳數量
     * @return 包含分頁結果的 DTO
     */
    FileListSearchedResult listPagedFiles(String bucket, String prefix, String startAfter, int maxKeys);

    /**
     * 複製檔案 (在同一個 bucket 內)。
     *
     * @param bucket       Bucket 名稱
     * @param sourceObject 來源物件 key
     * @param targetObject 目標物件 key
     */
    void cloneFile(String bucket, String sourceObject, String targetObject);

    /**
     * 跨 bucket 複製檔案。
     *
     * @param sourceBucket 來源 Bucket 名稱
     * @param sourceObject 來源物件 key
     * @param targetBucket 目標 Bucket 名稱
     * @param targetObject 目標物件 key
     */
    void cloneFile(String sourceBucket, String sourceObject, String targetBucket, String targetObject);

    /**
     * 批次檔案複製 (可使用非同步或批次操作進行多檔複製)。
     *
     * @param bucket Bucket 名稱
     * @param pairs  來源與目標物件對應列表
     */
    void cloneFiles(String bucket, List<CloneFilePairCommand> pairs);

    /**
     * 刪除指定目錄下的所有檔案 (依 prefix 刪除)。
     *
     * @param bucket Bucket 名稱
     * @param prefix 檔案前綴
     */
    void deleteFilesByPrefix(String bucket, String prefix);

    // ==========================================
    // 新增：Pre-signed URL 支援
    // ==========================================

    /**
     * 取得預先簽名的上傳網址 (適用於單檔上傳)。
     *
     * @param bucket        Bucket 名稱
     * @param objectName    完整物件路徑與名稱
     * @param expiryMinutes 網址有效時長 (分鐘)
     * @return 預先簽名的上傳 URL
     * @throws Exception 操作過程中可能發生的例外
     */
    String getPresignedUploadUrl(String bucket, String objectName, int expiryMinutes) throws Exception;

    /**
     * 取得預先簽名的下載網址。
     *
     * @param bucket        Bucket 名稱
     * @param objectName    完整物件路徑與名稱
     * @param expiryMinutes 網址有效時長 (分鐘)
     * @return 預先簽名的下載 URL
     * @throws Exception 操作過程中可能發生的例外
     */
    String getPresignedDownloadUrl(String bucket, String objectName, String originalFileName, boolean isDownload, int expiryMinutes) throws Exception;

    // ==========================================
    // 新增：分段上傳 (Multipart Upload) 支援
    // ==========================================

    /**
     * 初始化分段上傳並取得 UploadId。
     *
     * @param bucket     Bucket 名稱
     * @param objectName 完整物件路徑與名稱
     * @return UploadId (後續分段操作的必要 ID)
     * @throws Exception 操作過程中可能發生的例外
     */
    String initiateMultipartUpload(String bucket, String objectName) throws Exception;

    /**
     * 取得特定分段 (Part) 的預先簽名上傳 URL。
     *
     * @param bucket        Bucket 名稱
     * @param objectName    完整物件路徑與名稱
     * @param uploadId      初始化時取得的 UploadId
     * @param partNumber    分段序號 (由 1 開始)
     * @param expiryMinutes 網址有效時長 (分鐘)
     * @return 該分段專屬的上傳 URL
     * @throws Exception 操作過程中可能發生的例外
     */
    String getPresignedUploadPartUrl(String bucket, String objectName, String uploadId, int partNumber, int expiryMinutes) throws Exception;

    /**
     * 完成分段上傳，合併所有分段。
     *
     * @param bucket     Bucket 名稱
     * @param objectName 完整物件路徑與名稱
     * @param uploadId   UploadId
     * @param partETags  每個分段的 ETag 對應表 (Map<PartNumber, ETag>)
     * @throws Exception 操作過程中可能發生的例外
     */
    void completeMultipartUpload(String bucket, String objectName, String uploadId, Map<Integer, String> partETags) throws Exception;

    /**
     * 放棄 (中斷) 分段上傳，清除儲存體上已上傳的分段空間。
     *
     * @param bucket     Bucket 名稱
     * @param objectName 完整物件路徑與名稱
     * @param uploadId   UploadId
     * @throws Exception 操作過程中可能發生的例外
     */
    void abortMultipartUpload(String bucket, String objectName, String uploadId) throws Exception;

}
