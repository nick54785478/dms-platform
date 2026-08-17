package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.infrastructure.persistence.entity.FileMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataFileMetadataRepository extends JpaRepository<FileMetadataEntity, String> {
    
    Optional<FileMetadataEntity> findByChecksum(String checksum);
}
