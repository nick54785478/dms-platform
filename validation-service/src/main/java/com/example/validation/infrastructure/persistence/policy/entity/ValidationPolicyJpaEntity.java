package com.example.validation.infrastructure.persistence.policy.entity;

import com.example.validation.domain.policy.aggregate.root.ValidationPolicy;
import com.example.validation.domain.policy.aggregate.vo.PolicyRule;
import com.example.validation.domain.policy.aggregate.vo.PolicyTarget;
import com.example.validation.domain.shared.vo.YesNo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor	
@Table(name = "validation_policy")
public class ValidationPolicyJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "code")
	private String code; // 範本唯一代碼

	private String type; // 驗證類型，ROW、SHEET

	@Column(name = "template_name")
	private String templateName; // 範本名稱

	@Column(name = "template_sheet_name")
	private String templateSheetName; // Sheet Name

	@Column(name = "mapping_field_name")
	private String mappingFieldName; // Mapping Field Name

	private String rule; // 規則，如: ENFORCE_ROW_VALIDATION -> 表示即使欄位為空，仍要執行驗證邏輯

	private String expression; // SpEL Expression
	
	@Column(name = "error_message")
	private String errorMessage;

	@Column(name = "priority_no")
	private Integer priorityNo;

	@Enumerated(EnumType.STRING)
	@Column(name = "active_flag")
	private YesNo activeFlag;

	public ValidationPolicyJpaEntity(String code, String type, String templateSheetName, String rule, String mappingFieldName, String expression, String errorMessage) {
		this.code = code;
		this.type = type;
		this.templateSheetName = templateSheetName;
		this.mappingFieldName = mappingFieldName;
		this.rule = rule;
		this.expression = expression;
		this.errorMessage = errorMessage;
	}

    public ValidationPolicy toDomain() {
        PolicyTarget target = PolicyTarget.of(templateName, templateSheetName, mappingFieldName);
        PolicyRule policyRule = PolicyRule.of(type, rule, expression, errorMessage);
        
        return ValidationPolicy.reconstitute(id, code, target, policyRule, priorityNo, activeFlag);
    }

    public static ValidationPolicyJpaEntity fromDomain(ValidationPolicy domain) {
        return new ValidationPolicyJpaEntity(
            domain.getId(),
            domain.getCode(),
            domain.getType(),
            domain.getTemplateName(),
            domain.getTemplateSheetName(),
            domain.getMappingFieldName(),
            domain.getRule(),
            domain.getExpression(),
            domain.getErrorMessage(),
            domain.getPriorityNo(),
            domain.getActiveFlag()
        );
    }
}
