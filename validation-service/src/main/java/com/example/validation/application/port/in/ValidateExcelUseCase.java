package com.example.validation.application.port.in;

import com.example.validation.application.shared.command.ValidateExcelCommand;

public interface ValidateExcelUseCase {
    void validate(ValidateExcelCommand command);
}
