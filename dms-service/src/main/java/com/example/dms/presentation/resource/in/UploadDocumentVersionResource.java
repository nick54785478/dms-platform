package com.example.dms.presentation.resource.in;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 接收上傳新版本文件的 Request Body 載體
 */
@Schema(description = "上傳新版本文件的請求資料")
public class UploadDocumentVersionResource {

    @Schema(description = "新的實體檔案 ID", required = true)
    private String fileId;

    @Schema(description = "是否為主版本更新", required = true, defaultValue = "false")
    @com.fasterxml.jackson.annotation.JsonProperty("isMajorVersion")
    private boolean isMajorVersion;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public boolean isMajorVersion() {
        return isMajorVersion;
    }

    public void setMajorVersion(boolean majorVersion) {
        isMajorVersion = majorVersion;
    }
}
