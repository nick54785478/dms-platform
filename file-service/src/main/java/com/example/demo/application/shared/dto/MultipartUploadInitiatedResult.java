package com.example.demo.application.shared.dto;

/**
 * 初始化分段上傳結果 DTO (Data Transfer Object)。
 * <p>
 * 嚴格作為純資料載體，不包含任何業務轉換邏輯。
 * 負責將初始化的檔案 ID 與分段上傳專用的 UploadId 回傳給表現層。
 * </p>
 */
public record MultipartUploadInitiatedResult(String fileId, String uploadId) {
}
