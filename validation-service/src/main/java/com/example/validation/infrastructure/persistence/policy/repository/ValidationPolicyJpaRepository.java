package com.example.validation.infrastructure.persistence.policy.repository;

import com.example.validation.infrastructure.persistence.policy.entity.ValidationPolicyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValidationPolicyJpaRepository extends JpaRepository<ValidationPolicyJpaEntity, Long> {

	List<ValidationPolicyJpaEntity> findByCode(String code);
}
