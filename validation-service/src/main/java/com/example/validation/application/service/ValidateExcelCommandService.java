package com.example.validation.application.service;

import com.example.validation.application.port.in.ValidateExcelUseCase;
import com.example.validation.application.port.out.TemplateFieldMappingRepositoryPort;
import com.example.validation.application.port.out.ValidationPolicyRepositoryPort;
import com.example.validation.application.shared.command.ValidateExcelCommand;
import com.example.validation.domain.mapping.aggregate.root.TemplateFieldMapping;
import com.example.validation.domain.policy.aggregate.root.ValidationPolicy;
import com.example.validation.domain.shared.vo.YesNo;
import com.example.validation.infrastructure.cv.shared.ValidateErrorProperty;
import com.example.validation.infrastructure.cv.shared.context.ContextRoot;
import com.example.validation.infrastructure.cv.validator.CustomValidator;
import com.example.validation.infrastructure.exception.ExcelValidationException;
import com.example.validation.infrastructure.util.ExcelUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
class ValidateExcelCommandService implements ValidateExcelUseCase {

    private CustomValidator customValidator;
    private ValidationPolicyRepositoryPort validationPolicyRepositoryPort;
    private TemplateFieldMappingRepositoryPort templateFieldMappingRepositoryPort;

    @Override
    public void validate(ValidateExcelCommand command) {
        log.debug("Validating excel for code: {}", command.code());

        // 取出驗證規則，並過濾出啟用的規則 (YesNo.Y)
        List<ValidationPolicy> policyList = validationPolicyRepositoryPort.findByCode(command.code())
                .stream()
                .filter(policy -> policy.getActiveFlag() == YesNo.Y)
                .toList();
        List<String> sheetNameList = policyList.stream().map(ValidationPolicy::getTemplateSheetName).toList();

        // 讀取多張表資料
        try (ByteArrayInputStream is = new ByteArrayInputStream(command.fileContent())) {
            Map<String, List<Map<String, String>>> excelData = ExcelUtil.readExcelData(is, sheetNameList);
            List<TemplateFieldMapping> mappings = templateFieldMappingRepositoryPort.findByTemplateCode(command.code());

            // 轉換 excelData 的 Key (HeaderName -> MappingFieldName)
            Map<String, List<Map<String, String>>> convertedExcelData = new LinkedHashMap<>();
            excelData.forEach((sheet, rowList) -> {
                List<TemplateFieldMapping> sheetMappings = mappings.stream()
                        .filter(m -> m.getTemplateSheetName().equals(sheet))
                        .toList();

                Map<String, String> headerToFieldMap = new HashMap<>();
                for (TemplateFieldMapping mapping : sheetMappings) {
                    headerToFieldMap.put(mapping.getHeaderName(), mapping.getMappingFieldName());
                }

                List<Map<String, String>> convertedRowList = new ArrayList<>();
                for (Map<String, String> row : rowList) {
                    Map<String, String> convertedRow = new LinkedHashMap<>();
                    row.forEach((headerName, value) -> {
                        String fieldName = headerToFieldMap.getOrDefault(headerName, headerName);
                        convertedRow.put(fieldName, value);
                    });
                    convertedRowList.add(convertedRow);
                }
                convertedExcelData.put(sheet, convertedRowList);
            });

            // 建立 ContextRoot
            ContextRoot contextRoot = ContextRoot.builder().params(new LinkedHashMap<>()).sheetMap(convertedExcelData).build();

            List<ValidateErrorProperty> vepList = customValidator.validateExcelData(contextRoot, policyList);
            if (!vepList.isEmpty()) {
                throw new ExcelValidationException("VALIDATED_FAILED", vepList);
            }
        } catch (IOException e) {
            log.error("Read Excel failed", e);
            throw new ExcelValidationException("READ_FAILED", null);
        }
    }
}
