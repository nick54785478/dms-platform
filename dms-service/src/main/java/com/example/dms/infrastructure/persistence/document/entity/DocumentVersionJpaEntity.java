package com.example.dms.infrastructure.persistence.document.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_versions")
@Getter
@Setter
public class DocumentVersionJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 32)
    private String id;

    // Optional mapping back to Document if bidirectional is needed,
    // but typically unidirectional @OneToMany from DocumentJpaEntity is enough if configured right.
    // To keep it simple and performant, we map the document_id column via @JoinColumn in DocumentJpaEntity.

    @Column(name = "major_version", nullable = false)
    private Integer majorVersion;

    @Column(name = "minor_version", nullable = false)
    private Integer minorVersion;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "file_id", length = 100)
    private String fileId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
