package com.example.validation.infrastructure.persistence.mapping.repository;

import com.example.validation.infrastructure.persistence.mapping.entity.TemplateFieldMappingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateFieldMappingJpaRepository extends JpaRepository<TemplateFieldMappingJpaEntity, Long> {
    List<TemplateFieldMappingJpaEntity> findByTemplateCode(String templateCode);
    void deleteByTemplateCode(String templateCode);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT m.templateSheetName FROM TemplateFieldMappingJpaEntity m WHERE m.templateCode = :templateCode")
    List<String> findDistinctTemplateSheetNameByTemplateCode(@org.springframework.data.repository.query.Param("templateCode") String templateCode);

    List<TemplateFieldMappingJpaEntity> findByTemplateCodeAndTemplateSheetName(String templateCode, String templateSheetName);
}
