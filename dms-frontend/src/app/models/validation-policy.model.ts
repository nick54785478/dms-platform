export interface ValidationPolicy {
    id: number;
    code: string;
    templateName: string;
    templateSheetName: string;
    mappingFieldName: string;
    type: string;
    rule: string;
    expression: string;
    errorMessage: string;
    priorityNo: number;
    activeFlag: 'Y' | 'N';
}

export interface CreateValidationPolicyResource {
    code: string;
    templateName: string;
    templateSheetName: string;
    mappingFieldName: string;
    type: string;
    rule: string;
    expression: string;
    errorMessage: string;
    priorityNo: number;
}

export interface UpdateValidationPolicyResource {
    code: string;
    templateName: string;
    templateSheetName: string;
    mappingFieldName: string;
    type: string;
    rule: string;
    expression: string;
    errorMessage: string;
    priorityNo: number;
    activeFlag: 'Y' | 'N';
}
