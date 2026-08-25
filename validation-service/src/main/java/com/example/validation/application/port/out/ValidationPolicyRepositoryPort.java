package com.example.validation.application.port.out;

import com.example.validation.domain.policy.aggregate.root.ValidationPolicy;
import java.util.List;

import java.util.Optional;

public interface ValidationPolicyRepositoryPort {
    List<ValidationPolicy> findByCode(String code);
    
    ValidationPolicy save(ValidationPolicy policy);
    
    Optional<ValidationPolicy> findById(Long id);
    
    List<ValidationPolicy> findAll();
    
    void deleteById(Long id);
}
