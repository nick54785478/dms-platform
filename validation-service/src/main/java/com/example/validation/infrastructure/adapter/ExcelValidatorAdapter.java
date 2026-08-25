package com.example.validation.infrastructure.adapter;

import com.example.validation.application.port.out.ExcelValidatorPort;
import com.example.validation.application.port.out.ValidationPolicyRepositoryPort;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
class ExcelValidatorAdapter implements ExcelValidatorPort {

    private CustomValidator customValidator;
    private ValidationPolicyRepositoryPort validationPolicyRepositoryPort;

    @Override
    public void validateExcelData(String code, byte[] fileContent) {
        // 取出驗證規則，過濾出啟用的規則
        List<ValidationPolicy> policyList = validationPolicyRepositoryPort.findByCode(code)
                .stream()
                .filter(policy -> policy.getActiveFlag() == YesNo.Y)
                .toList();
        List<String> sheetNameList = policyList.stream().map(ValidationPolicy::getTemplateSheetName).toList();

        // 讀取多張表資料
        try (ByteArrayInputStream is = new ByteArrayInputStream(fileContent)) {
            Map<String, List<Map<String, String>>> excelData = ExcelUtil.readExcelData(is, sheetNameList);
            // 建立 ContextRoot
            ContextRoot contextRoot = ContextRoot.builder().params(new LinkedHashMap<>()).sheetMap(excelData).build();
            // 執行客製驗證
            List<ValidateErrorProperty> vepList = customValidator.validateExcelData(contextRoot, policyList);

            // 拋出例外，將驗證失敗的資料拋出
            if (!vepList.isEmpty()) {
                throw new ExcelValidationException("VALIDATED_FAILED", vepList);
            }

        } catch (IOException e) {
            log.error("讀取檔案發生錯誤", e);
        }
    }
}
