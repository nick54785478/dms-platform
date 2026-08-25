package com.example.validation.presentation.assembler;

import com.example.validation.application.shared.command.CreateValidationPolicyCommand;
import com.example.validation.application.shared.command.UpdateValidationPolicyCommand;
import com.example.validation.application.shared.dto.ValidationPolicyGottenResult;
import com.example.validation.application.shared.dto.ValidationPolicySearchedResult;
import com.example.validation.presentation.resource.in.CreateValidationPolicyResource;
import com.example.validation.presentation.resource.in.UpdateValidationPolicyResource;
import com.example.validation.presentation.resource.out.ValidationPolicyRetrievedResource;
import com.example.validation.presentation.resource.out.ValidationPolicySearchedResource;
import org.springframework.stereotype.Component;

@Component
public class ValidationPolicyResourceAssembler {

    public CreateValidationPolicyCommand toCommand(CreateValidationPolicyResource resource) {
        return new CreateValidationPolicyCommand(
                resource.getCode(),
                resource.getTemplateName(),
                resource.getTemplateSheetName(),
                resource.getMappingFieldName(),
                resource.getType(),
                resource.getRule(),
                resource.getExpression(),
                resource.getErrorMessage(),
                resource.getPriorityNo()
        );
    }

    public UpdateValidationPolicyCommand toCommand(Long id, UpdateValidationPolicyResource resource) {
        return new UpdateValidationPolicyCommand(
                id,
                resource.getCode(),
                resource.getTemplateName(),
                resource.getTemplateSheetName(),
                resource.getMappingFieldName(),
                resource.getType(),
                resource.getRule(),
                resource.getExpression(),
                resource.getErrorMessage(),
                resource.getPriorityNo(),
                resource.getActiveFlag()
        );
    }

    public ValidationPolicyRetrievedResource toResource(ValidationPolicyGottenResult result) {
        return new ValidationPolicyRetrievedResource(
                result.getId(),
                result.getCode(),
                result.getTemplateName(),
                result.getTemplateSheetName(),
                result.getMappingFieldName(),
                result.getType(),
                result.getRule(),
                result.getExpression(),
                result.getErrorMessage(),
                result.getPriorityNo(),
                result.getActiveFlag()
        );
    }

    public ValidationPolicySearchedResource toResource(ValidationPolicySearchedResult result) {
        return new ValidationPolicySearchedResource(
                result.getId(),
                result.getCode(),
                result.getTemplateName(),
                result.getTemplateSheetName(),
                result.getMappingFieldName(),
                result.getType(),
                result.getRule(),
                result.getExpression(),
                result.getErrorMessage(),
                result.getPriorityNo(),
                result.getActiveFlag()
        );
    }
}
