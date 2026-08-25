package com.example.validation.application.port.in;

import com.example.validation.application.shared.dto.ValidationPolicyGottenResult;
import com.example.validation.application.shared.query.GetValidationPolicyQuery;

public interface GetValidationPolicyUseCase {
    ValidationPolicyGottenResult get(GetValidationPolicyQuery query);
}
