package com.example.validation.application.shared.dto;

import com.example.validation.domain.shared.vo.YesNo;

/**
 * 查詢結果載體 DTO
 */
public class ValidationPolicyGottenResult {
    private final Long id;
    private final String code;
    private final String templateName;
    private final String templateSheetName;
    private final String mappingFieldName;
    private final String type;
    private final String rule;
    private final String expression;
    private final String errorMessage;
    private final Integer priorityNo;
    private final YesNo activeFlag;

    public ValidationPolicyGottenResult(Long id, String code, String templateName, String templateSheetName, String mappingFieldName,
                                        String type, String rule, String expression, String errorMessage, Integer priorityNo, YesNo activeFlag) {
        this.id = id;
        this.code = code;
        this.templateName = templateName;
        this.templateSheetName = templateSheetName;
        this.mappingFieldName = mappingFieldName;
        this.type = type;
        this.rule = rule;
        this.expression = expression;
        this.errorMessage = errorMessage;
        this.priorityNo = priorityNo;
        this.activeFlag = activeFlag;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getTemplateName() { return templateName; }
    public String getTemplateSheetName() { return templateSheetName; }
    public String getMappingFieldName() { return mappingFieldName; }
    public String getType() { return type; }
    public String getRule() { return rule; }
    public String getExpression() { return expression; }
    public String getErrorMessage() { return errorMessage; }
    public Integer getPriorityNo() { return priorityNo; }
    public YesNo getActiveFlag() { return activeFlag; }
}
