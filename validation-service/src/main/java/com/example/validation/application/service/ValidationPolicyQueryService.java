package com.example.validation.application.service;

import com.example.validation.application.assembler.ValidationPolicyDtoAssembler;
import com.example.validation.application.port.in.GetValidationPolicyUseCase;
import com.example.validation.application.port.in.ListValidationPolicyUseCase;
import com.example.validation.application.port.out.ValidationPolicyRepositoryPort;
import com.example.validation.application.shared.dto.ValidationPolicyGottenResult;
import com.example.validation.application.shared.dto.ValidationPolicySearchedResult;
import com.example.validation.application.shared.query.GetValidationPolicyQuery;
import com.example.validation.application.shared.query.ListValidationPolicyQuery;
import com.example.validation.domain.policy.aggregate.exception.ValidationPolicyNotFoundException;
import com.example.validation.domain.policy.aggregate.root.ValidationPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class ValidationPolicyQueryService implements GetValidationPolicyUseCase, ListValidationPolicyUseCase {

    private final ValidationPolicyRepositoryPort repositoryPort;
    private final ValidationPolicyDtoAssembler dtoAssembler;

    @Override
    public ValidationPolicyGottenResult get(GetValidationPolicyQuery query) {
        ValidationPolicy policy = repositoryPort.findById(query.getId())
                .orElseThrow(() -> new ValidationPolicyNotFoundException(query.getId()));
        return dtoAssembler.toGottenResult(policy);
    }

    @Override
    public List<ValidationPolicySearchedResult> list(ListValidationPolicyQuery query) {
        // If code is provided, filter by it. Otherwise return all.
        List<ValidationPolicy> policies;
        if (query.getCode() != null && !query.getCode().trim().isEmpty()) {
            policies = repositoryPort.findByCode(query.getCode());
        } else {
            policies = repositoryPort.findAll();
        }
        
        return policies.stream()
                .map(dtoAssembler::toSearchedResult)
                .collect(Collectors.toList());
    }
}
