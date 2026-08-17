package com.example.dms.presentation.resource.out;

import lombok.Value;

import java.time.LocalDateTime;

/**
 * 建立文件後的回傳資料載體 (Presentation Layer)
 */
@Value
public class DocumentCreatedResource {
    String id;
    String title;
    String description;
    String fileId;
    String status;
    LocalDateTime createdAt;
}
