package com.example.validation.infrastructure.adapter;

import com.example.validation.application.port.out.ValidationPolicyRepositoryPort;
import com.example.validation.domain.policy.aggregate.root.ValidationPolicy;
import com.example.validation.infrastructure.persistence.policy.entity.ValidationPolicyJpaEntity;
import com.example.validation.infrastructure.persistence.policy.repository.ValidationPolicyJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
class ValidationPolicyRepositoryAdapter implements ValidationPolicyRepositoryPort {

    private final ValidationPolicyJpaRepository validationPolicyJpaRepository;

    @Override
    public List<ValidationPolicy> findByCode(String code) {
        List<ValidationPolicyJpaEntity> entities = validationPolicyJpaRepository.findByCode(code);
        return entities.stream().map(ValidationPolicyJpaEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public ValidationPolicy save(ValidationPolicy policy) {
        ValidationPolicyJpaEntity entity = ValidationPolicyJpaEntity.fromDomain(policy);
        ValidationPolicyJpaEntity savedEntity = validationPolicyJpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public java.util.Optional<ValidationPolicy> findById(Long id) {
        return validationPolicyJpaRepository.findById(id).map(ValidationPolicyJpaEntity::toDomain);
    }

    @Override
    public List<ValidationPolicy> findAll() {
        return validationPolicyJpaRepository.findAll().stream()
                .map(ValidationPolicyJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        validationPolicyJpaRepository.deleteById(id);
    }
}
