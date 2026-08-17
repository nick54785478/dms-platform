package com.example.dms.infrastructure.persistence.document.repository;

import com.example.dms.infrastructure.persistence.document.entity.DocumentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentJpaRepository extends JpaRepository<DocumentJpaEntity, String>, JpaSpecificationExecutor<DocumentJpaEntity> {
}
