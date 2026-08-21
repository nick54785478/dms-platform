package com.dms.template.infrastructure.persistence.template.repository;

import com.dms.template.infrastructure.persistence.template.entity.TemplateJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateJpaRepository extends JpaRepository<TemplateJpaEntity, String> {
}
