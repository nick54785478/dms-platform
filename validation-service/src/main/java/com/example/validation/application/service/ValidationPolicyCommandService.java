package com.example.validation.application.service;

import com.example.validation.application.port.in.CreateValidationPolicyUseCase;
import com.example.validation.application.port.in.DeleteValidationPolicyUseCase;
import com.example.validation.application.port.in.UpdateValidationPolicyUseCase;
import com.example.validation.application.port.out.ValidationPolicyRepositoryPort;
import com.example.validation.application.shared.command.CreateValidationPolicyCommand;
import com.example.validation.application.shared.command.DeleteValidationPolicyCommand;
import com.example.validation.application.shared.command.UpdateValidationPolicyCommand;
import com.example.validation.domain.policy.aggregate.exception.ValidationPolicyNotFoundException;
import com.example.validation.domain.policy.aggregate.root.ValidationPolicy;
import com.example.validation.domain.policy.aggregate.vo.PolicyRule;
import com.example.validation.domain.policy.aggregate.vo.PolicyTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
class ValidationPolicyCommandService implements CreateValidationPolicyUseCase, UpdateValidationPolicyUseCase, DeleteValidationPolicyUseCase {

    private final ValidationPolicyRepositoryPort repositoryPort;

    @Override
    public Long create(CreateValidationPolicyCommand command) {
        PolicyTarget target = PolicyTarget.of(command.getTemplateName(), command.getTemplateSheetName(), command.getMappingFieldName());
        PolicyRule rule = PolicyRule.of(command.getType(), command.getRule(), command.getExpression(), command.getErrorMessage());
        
        ValidationPolicy policy = ValidationPolicy.create(command.getCode(), target, rule, command.getPriorityNo());
        ValidationPolicy savedPolicy = repositoryPort.save(policy);
        
        return savedPolicy.getId();
    }

    @Override
    public void update(UpdateValidationPolicyCommand command) {
        ValidationPolicy policy = repositoryPort.findById(command.getId())
                .orElseThrow(() -> new ValidationPolicyNotFoundException(command.getId()));

        PolicyTarget target = PolicyTarget.of(command.getTemplateName(), command.getTemplateSheetName(), command.getMappingFieldName());
        PolicyRule rule = PolicyRule.of(command.getType(), command.getRule(), command.getExpression(), command.getErrorMessage());
        
        policy.update(command.getCode(), target, rule, command.getPriorityNo(), command.getActiveFlag());
        repositoryPort.save(policy);
    }

    @Override
    public void delete(DeleteValidationPolicyCommand command) {
        ValidationPolicy policy = repositoryPort.findById(command.getId())
                .orElseThrow(() -> new ValidationPolicyNotFoundException(command.getId()));
        repositoryPort.deleteById(policy.getId());
    }
}
