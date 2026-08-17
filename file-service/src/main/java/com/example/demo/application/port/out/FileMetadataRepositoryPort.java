package com.example.demo.application.port.out;

import com.example.demo.domain.file.aggregate.root.FileMetadata;

import java.util.Optional;

public interface FileMetadataRepositoryPort {

    /**
     * 儲存或更新檔案中介資料
     */
    FileMetadata save(FileMetadata metadata);

    /**
     * 透過 ID 尋找檔案資料
     */
    Optional<FileMetadata> findById(String id);

    /**
     * 透過 Checksum (MD5/SHA) 尋找已存在的檔案資料 (用於秒傳/去重)
     */
    Optional<FileMetadata> findByChecksum(String checksum);
}
