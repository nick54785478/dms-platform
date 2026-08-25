package com.example.validation.application.assembler;

import com.example.validation.application.shared.dto.ValidationPolicyGottenResult;
import com.example.validation.application.shared.dto.ValidationPolicySearchedResult;
import com.example.validation.domain.policy.aggregate.root.ValidationPolicy;
import org.springframework.stereotype.Component;

@Component
public class ValidationPolicyDtoAssembler {

    public ValidationPolicyGottenResult toGottenResult(ValidationPolicy policy) {
        return new ValidationPolicyGottenResult(
                policy.getId(),
                policy.getCode(),
                policy.getTemplateName(),
                policy.getTemplateSheetName(),
                policy.getMappingFieldName(),
                policy.getType(),
                policy.getRule(),
                policy.getExpression(),
                policy.getErrorMessage(),
                policy.getPriorityNo(),
                policy.getActiveFlag()
        );
    }

    public ValidationPolicySearchedResult toSearchedResult(ValidationPolicy policy) {
        return new ValidationPolicySearchedResult(
                policy.getId(),
                policy.getCode(),
                policy.getTemplateName(),
                policy.getTemplateSheetName(),
                policy.getMappingFieldName(),
                policy.getType(),
                policy.getRule(),
                policy.getExpression(),
                policy.getErrorMessage(),
                policy.getPriorityNo(),
                policy.getActiveFlag()
        );
    }
}
