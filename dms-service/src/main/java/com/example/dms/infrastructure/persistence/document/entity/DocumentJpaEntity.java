package com.example.dms.infrastructure.persistence.document.entity;

import com.example.dms.domain.document.aggregate.vo.DocumentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "documents")
@Getter
@Setter
public class DocumentJpaEntity {
    
    @Id
    @Column(name = "id", nullable = false, length = 32)
    private String id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "file_id", length = 100)
    private String fileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DocumentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "major_version", nullable = false, columnDefinition = "integer default 1")
    private Integer majorVersion;

    @Column(name = "minor_version", nullable = false, columnDefinition = "integer default 0")
    private Integer minorVersion;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "document_id", nullable = false)
    private List<DocumentVersionJpaEntity> history = new ArrayList<>();
}
