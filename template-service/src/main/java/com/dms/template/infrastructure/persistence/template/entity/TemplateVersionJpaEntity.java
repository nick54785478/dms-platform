package com.dms.template.infrastructure.persistence.template.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 範本版本 (Template Version) 的 JPA 持久化實體.
 *
 * <p>
 * 對應於領域模型中的 {@link com.dms.template.domain.template.aggregate.entity.TemplateVersion}。
 * 作為範本聚合根底下的內部實體，與 {@link TemplateJpaEntity} 維持一對多的生命週期綁定關係。
 * 嚴禁將此類別洩漏至領域層 (Domain Layer) 或應用層 (Application Layer)。
 * </p>
 */
@Entity
@Table(name = "template_version")
@Getter
@Setter
public class TemplateVersionJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private TemplateJpaEntity template;

    private String version;

    @Column(columnDefinition = "TEXT")
    private String contentDefinition;

    private String status;

    // For simplicity, store variables as JSON text right now
    @Column(columnDefinition = "TEXT")
    private String variablesJson;
}
