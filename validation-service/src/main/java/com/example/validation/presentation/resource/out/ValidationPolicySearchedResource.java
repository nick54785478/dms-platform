package com.example.validation.presentation.resource.out;

import com.example.validation.domain.shared.vo.YesNo;

public class ValidationPolicySearchedResource {
    private Long id;
    private String code;
    private String templateName;
    private String templateSheetName;
    private String mappingFieldName;
    private String type;
    private String rule;
    private String expression;
    private String errorMessage;
    private Integer priorityNo;
    private YesNo activeFlag;

    public ValidationPolicySearchedResource(Long id, String code, String templateName, String templateSheetName, String mappingFieldName,
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
