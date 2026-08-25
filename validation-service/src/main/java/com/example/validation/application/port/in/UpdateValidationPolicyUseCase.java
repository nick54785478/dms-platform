package com.example.validation.application.port.in;

import com.example.validation.application.shared.command.UpdateValidationPolicyCommand;

public interface UpdateValidationPolicyUseCase {
    void update(UpdateValidationPolicyCommand command);
}
