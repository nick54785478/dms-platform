package com.example.validation.application.port.in;

import com.example.validation.application.shared.command.CreateValidationPolicyCommand;

public interface CreateValidationPolicyUseCase {
    Long create(CreateValidationPolicyCommand command);
}
