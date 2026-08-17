package com.example.demo.infrastructure.adapter.persistence;

import com.example.demo.application.port.out.FileMetadataRepositoryPort;
import com.example.demo.domain.file.aggregate.root.FileMetadata;
import com.example.demo.infrastructure.persistence.entity.FileMetadataEntity;
import com.example.demo.infrastructure.persistence.repository.SpringDataFileMetadataRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaFileMetadataRepositoryAdapter implements FileMetadataRepositoryPort {

    private final SpringDataFileMetadataRepository repository;

    public JpaFileMetadataRepositoryAdapter(SpringDataFileMetadataRepository repository) {
        this.repository = repository;
    }

    @Override
    public FileMetadata save(FileMetadata metadata) {
        FileMetadataEntity entity = FileMetadataEntity.create(metadata);
        FileMetadataEntity savedEntity = repository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<FileMetadata> findById(String id) {
        return repository.findById(id)
                .map(FileMetadataEntity::toDomain);
    }

    @Override
    public Optional<FileMetadata> findByChecksum(String checksum) {
        return repository.findByChecksum(checksum)
                .map(FileMetadataEntity::toDomain);
    }
}
