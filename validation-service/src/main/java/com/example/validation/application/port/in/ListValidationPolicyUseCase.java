package com.example.validation.application.port.in;

import com.example.validation.application.shared.dto.ValidationPolicySearchedResult;
import com.example.validation.application.shared.query.ListValidationPolicyQuery;

import java.util.List;

public interface ListValidationPolicyUseCase {
    List<ValidationPolicySearchedResult> list(ListValidationPolicyQuery query);
}
