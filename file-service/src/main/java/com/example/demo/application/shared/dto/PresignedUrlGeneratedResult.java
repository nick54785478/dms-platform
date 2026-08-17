package com.example.demo.application.shared.dto;

/**
 * 預先簽名網址結果 DTO (Data Transfer Object)。
 * <p>
 * 嚴格作為純資料載體，不包含任何業務轉換邏輯。
 * 負責將應用層產生的預先簽名 URL 與對應的檔案 ID 回傳給表現層。
 * </p>
 */
public record PresignedUrlGeneratedResult(String fileId, String url) {
}
