package com.example.dms.presentation.resource.in;

import lombok.Value;

/**
 * 建立文件的請求資料載體 (Presentation Layer)
 */
@Value
public class CreateDocumentResource {
    String title;
    String description;
    String fileId;
}
