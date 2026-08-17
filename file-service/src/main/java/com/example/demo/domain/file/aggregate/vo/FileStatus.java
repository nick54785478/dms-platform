package com.example.demo.domain.file.aggregate.vo;

public enum FileStatus {
    /**
     * 檔案尚在上傳中 (如分片上傳)
     */
    UPLOADING,

    /**
     * 檔案已完整上傳，存在於 Temp Bucket，等待業務認領
     */
    UNBOUND,

    /**
     * 檔案已被業務端認領綁定，已搬移至 Permanent Bucket
     */
    BOUND,

    /**
     * 檔案已被標記為刪除 (準備物理刪除或保留作為稽核)
     */
    DELETED
}
