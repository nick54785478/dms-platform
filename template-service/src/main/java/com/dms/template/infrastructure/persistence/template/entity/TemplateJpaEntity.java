package com.dms.template.infrastructure.persistence.template.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 範本聚合根 (Template Aggregate Root) 的 JPA 持久化實體.
 *
 * <p>
 * 屬於基礎設施層 (Infrastructure Layer) 的一部分，專責與關聯式資料庫 (RDBMS) 的 ORM 映射。
 * 嚴禁將此類別或任何 JPA 標註 (Annotations) 洩漏至領域層 (Domain Layer) 或應用層 (Application Layer)。
 * 在基礎設施的 Adapter 中，應透過對應的 Mapper/Assembler 將其與純領域模型進行互相轉換。
 * </p>
 */
@Entity
@Table(name = "template")
@Getter
@Setter
public class TemplateJpaEntity {
    @Id
    private String id;
    private String templateType;
    private String templateCode;
    private String name;
    private String description;
    
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<TemplateVersionJpaEntity> versions = new java.util.ArrayList<>();
}
