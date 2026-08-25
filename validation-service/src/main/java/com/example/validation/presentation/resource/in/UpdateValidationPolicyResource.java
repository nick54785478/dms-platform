package com.example.validation.presentation.resource.in;

import com.example.validation.domain.shared.vo.YesNo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "修改驗證規則的請求載體")
public class UpdateValidationPolicyResource {
    
    @Schema(description = "對應到 Template Service 的範本唯一代碼", example = "USER_PROFILE")
    private String code;
    
    @Schema(description = "範本名稱", example = "UserImportTemplate")
    private String templateName;
    
    @Schema(description = "範本內的頁籤名稱", example = "Users")
    private String templateSheetName;
    
    @Schema(description = "對應的欄位名稱", example = "email")
    private String mappingFieldName;
    
    @Schema(description = "驗證類型", example = "ROW")
    private String type;
    
    @Schema(description = "規則類型", example = "ENFORCE_ROW_VALIDATION")
    private String rule;
    
    @Schema(description = "具體執行的 SpEL Expression")
    private String expression;
    
    @Schema(description = "驗證失敗時的錯誤訊息範本")
    private String errorMessage;
    
    @Schema(description = "規則執行優先級序號", example = "1")
    private Integer priorityNo;
    
    @Schema(description = "是否啟用此規則", example = "Y")
    private YesNo activeFlag;

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getTemplateSheetName() { return templateSheetName; }
    public void setTemplateSheetName(String templateSheetName) { this.templateSheetName = templateSheetName; }
    public String getMappingFieldName() { return mappingFieldName; }
    public void setMappingFieldName(String mappingFieldName) { this.mappingFieldName = mappingFieldName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getRule() { return rule; }
    public void setRule(String rule) { this.rule = rule; }
    public String getExpression() { return expression; }
    public void setExpression(String expression) { this.expression = expression; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Integer getPriorityNo() { return priorityNo; }
    public void setPriorityNo(Integer priorityNo) { this.priorityNo = priorityNo; }
    public YesNo getActiveFlag() { return activeFlag; }
    public void setActiveFlag(YesNo activeFlag) { this.activeFlag = activeFlag; }
}
