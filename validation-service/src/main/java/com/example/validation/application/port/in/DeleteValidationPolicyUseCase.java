package com.example.validation.application.port.in;

import com.example.validation.application.shared.command.DeleteValidationPolicyCommand;

public interface DeleteValidationPolicyUseCase {
    void delete(DeleteValidationPolicyCommand command);
}
