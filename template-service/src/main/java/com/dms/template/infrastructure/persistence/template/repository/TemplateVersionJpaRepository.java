package com.dms.template.infrastructure.persistence.template.repository;

import com.dms.template.infrastructure.persistence.template.entity.TemplateVersionJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 範本版本 (Template Version) JPA 儲存庫.
 */
@Repository
public interface TemplateVersionJpaRepository extends JpaRepository<TemplateVersionJpaEntity, Long> {
    
    /**
     * 依據 Template ID 查詢版本歷史紀錄，並支援分頁 (依 ID 降冪排序代表最新版在前).
     */
    Page<TemplateVersionJpaEntity> findByTemplateIdOrderByIdDesc(String templateId, Pageable pageable);
}
