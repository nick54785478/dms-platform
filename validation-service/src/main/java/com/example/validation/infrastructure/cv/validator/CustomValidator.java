package com.example.validation.infrastructure.cv.validator;


import com.example.validation.domain.policy.aggregate.root.ValidationPolicy;
import com.example.validation.infrastructure.cv.parser.ExcelAddressParser;
import com.example.validation.infrastructure.cv.shared.TemplateLine;
import com.example.validation.infrastructure.cv.shared.ValidateErrorProperty;
import com.example.validation.infrastructure.cv.shared.context.ContextRoot;
import com.example.validation.infrastructure.util.ValidationUtil;
import com.example.validation.infrastructure.util.VariableUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 客製驗證器 (CustomValidator)
 * <p>
 * 負責執行依據範本欄位動態設定的 {@link ValidationPolicy}，透過 Spring Expression Language (SpEL)
 * 提供高度靈活的欄位資料驗證能力。
 * </p>
 * <p>
 * 具備兩種處理層級：
 * <ul>
 *     <li><b>ROW 級別：</b>針對單一資料列進行逐行驗證，將該列的各欄位轉為 SpEL 變數進行邏輯判斷。</li>
 *     <li><b>SHEET 級別：</b>針對整張資料表進行跨行邏輯檢查，如查找重複值、建立全域變數清單 (VARIABLE) 等。</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
public class CustomValidator {

    private static final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 主驗證方法，遍歷 Excel 資料並依據聚合根 {@link ValidationPolicy} 執行 SpEL 動態驗證。
     *
     * @param contextRoot 封裝當前檔案解析的 Context 資料 (包含所有 Sheet 的原始資料)
     * @param policyList  該範本關聯的所有驗證原則 (ValidationPolicy 聚合根)
     * @return 包含所有驗證失敗的錯誤訊息清單 (ValidateErrorProperty)
     */
    public List<ValidateErrorProperty> validateExcelData(ContextRoot contextRoot, List<ValidationPolicy> policyList) {

        List<ValidateErrorProperty> vepList = new ArrayList<>();

        // 紀錄 Template Line
        Map<String, List<Map<String, String>>> sheetMap = contextRoot.getSheetMap();

        // 根據 SheetName 進行 policy 分群
        Map<String, List<ValidationPolicy>> policyMap = policyList.stream()
                .collect(Collectors.groupingBy(ValidationPolicy::getTemplateSheetName));

        // 遍歷 SheetMap
        sheetMap.forEach((sheetName, dataSheet) -> {
            // 取得所有 MappingFieldName List
            Set<String> mappingFieldNames = dataSheet.stream().flatMap(map -> map.keySet().stream()) // 提取所有 Key
                    .collect(Collectors.toCollection(LinkedHashSet::new)); // 使用 Set 避免重複

            // 生成 TemplateMap Map<mappingFieldName, TemplateLine>
            Map<String, TemplateLine> templateMap = this.generateTemplateMap(sheetName, mappingFieldNames);

            // 設置當前的 Sheet
            contextRoot.setSheet(dataSheet);
            // 初始化 SpEL 上下文，準備變數與方法。
            StandardEvaluationContext context = initContext(contextRoot);
            if (policyMap.containsKey(sheetName)) {
                List<ValidationPolicy> policies = policyMap.get(sheetName);

                // 根據 Policy 去驗證資料
                policies.stream().forEach(policy -> {
                    // 處理 ROW 資料
                    if (StringUtils.equalsIgnoreCase(policy.getType(), "ROW")) {
                        this.processRowPolicy(dataSheet, policy, contextRoot, context, templateMap, vepList);
                        // 處理 SHEET 資料
                    } else if (StringUtils.equalsIgnoreCase(policy.getType(), "SHEET")) {
                        this.processSheetPolicy(sheetName, policy, context, contextRoot, templateMap, vepList);
                    }
                });
            }
        });
        return vepList;
    }

    /**
     * 處理 ROW 種類的客製驗證。
     * 針對傳入的 dataSheet，逐列執行 SpEL 驗證條件，並在發生錯誤時將錯誤訊息加入 vepList。
     *
     * @param dataSheet   當前的 Sheet 原始資料列集合
     * @param policy      欲執行的驗證政策 (ValidationPolicy)
     * @param contextRoot 當前執行的 ContextRoot，用於追蹤當前列
     * @param context     SpEL 執行的 StandardEvaluationContext 上下文
     * @param templateMap 欄位名稱對應至 {@link TemplateLine} 的映射，用於定位錯誤發生之儲存格
     * @param vepList     用於收集錯誤結果的清單
     */
    public void processRowPolicy(List<Map<String, String>> dataSheet, ValidationPolicy policy, ContextRoot contextRoot,
                                 StandardEvaluationContext context, Map<String, TemplateLine> templateMap,
                                 List<ValidateErrorProperty> vepList) {
        // 遍歷當前的 Sheet 資料
        for (int i = 0; i < dataSheet.size(); i++) {
            contextRoot.setCurrentRow(dataSheet.get(i)); // 設置當前 row
            String mappingFieldName = StringUtils.trimToEmpty(policy.getMappingFieldName());

            // 若非強制執行驗證，且該欄位值為空，則跳過驗證 (允許選填欄位為空)
            if (!StringUtils.equalsIgnoreCase(policy.getRule(), "ENFORCE_ROW_VALIDATION")
                    && isRowFieldBlank(contextRoot, mappingFieldName)) {
                continue;
            }
            // 設置當前處理的欄位值，以便 SpEL 取用 `#checkedValue`
            this.setSingleMappingForRowCellValue(contextRoot, mappingFieldName);
            // 取得預處理表達式，可擴展替換特殊標記
            String expression = preProcessExpression(policy.getExpression());
            log.debug("[validateExcelData] ROW after preprocessor: {}, {}", mappingFieldName, expression);

            // 執行 Expression
            Boolean expressionValue = evaluateExpression(context, expression, contextRoot);
            if (!Boolean.TRUE.equals(expressionValue)) {
                // 格式化驗證錯誤資訊，由於遍歷是從 0 開始，所以此處要輸入 i+1 換算為實際列數
                ValidateErrorProperty vep = formatRowValidateError(context, templateMap, policy, i + 1);
                vepList.add(vep);
            }
        }
    }

    /**
     * 處理 SHEET 種類的客製驗證。
     * 可執行跨越多列的彙整邏輯 (如判斷欄位值是否在特定清單中)，或宣告供後續 ROW 驗證使用的全域變數 (VARIABLE)。
     *
     * @param sheetName   當前的 Sheet 頁籤名稱
     * @param policy      欲執行的驗證政策 (ValidationPolicy)
     * @param context     SpEL 執行的 StandardEvaluationContext 上下文
     * @param contextRoot 當前執行的 ContextRoot
     * @param templateMap 欄位名稱對應至 {@link TemplateLine} 的映射，用於定位錯誤發生之儲存格
     * @param vepList     用於收集錯誤結果的清單
     */
    public void processSheetPolicy(String sheetName, ValidationPolicy policy, StandardEvaluationContext context,
                                   ContextRoot contextRoot, Map<String, TemplateLine> templateMap, List<ValidateErrorProperty> vepList) {
        if (StringUtils.equalsIgnoreCase("VARIABLE", policy.getRule())) {
            // 如果是 VARIABLE => 解析 SpEL 後，將結果設置為全域變數供後續使用
            Object obj = parser.parseExpression(policy.getExpression()).getValue(context);
            contextRoot.getParams().put(policy.getMappingFieldName(), obj);
            context.setVariable(policy.getMappingFieldName(), obj);
        } else {
            // 處理一般 SHEET 級別的驗證規則
            // 取得預處理表達式，可擴展替換特殊標記
            String expression = preProcessExpression(policy.getExpression());
            // 執行 Expression，並回傳 Map< rowIndex, errorMessage >
            /*
             * 說明： SpEL 的 Expression#getValue(EvaluationContext) 回傳型別為 Object，
             * 編譯期無法得知實際泛型型別，因此轉型為 Map<Integer, String> 時，會產生 unchecked cast 警告。
             *
             * 此處 expression 的設計即約定回傳型別必為 Map<Integer, String>， 且 expression 與
             * EvaluationContext 皆由系統內部控制， 非來自外部不可信輸入，風險可控。
             *
             * 因此在此行局部使用 @SuppressWarnings("unchecked") 抑制警告， 避免污染方法或類別層級，同時保持程式碼可讀性與型別語意清楚。
             */
            @SuppressWarnings("unchecked")
            Map<Integer, String> expressionValue = (Map<Integer, String>) parser.parseExpression(expression)
                    .getValue(context);

            if (expressionValue != null) {
                TemplateLine templateLine = getTemplateLine(templateMap, policy.getMappingFieldName());
                expressionValue.forEach((dataRowIndex, errorMessage) -> {
                    // 取得 ExcelAddress，這邊需 +1 (要含標題的 row)，因為是 1-based
                    String excelAddress = ExcelAddressParser.convertNumToAddress(dataRowIndex + 1,
                            templateLine.getDataColumnNum());
                    log.debug("dataRowIndex:{}, errorMessage:{}", dataRowIndex, errorMessage);
                    ValidateErrorProperty vep = new ValidateErrorProperty();
                    vep.setMessage("SheetName : " + sheetName + ", " + excelAddress + " 資料檢核發生錯誤，" + errorMessage);
                    vepList.add(vep);
                });
            }
            log.debug("expressionValue : {}", expressionValue);
        }

    }

    /**
     * 建立欄位名稱至 TemplateLine 的對應關係 (TemplateMap)
     *
     * @param sheetName         當前的 SheetName
     * @param mappingFieldNames 該 Sheet 下的所有欄位名稱清單
     * @return 映射集合 Map<mappingFieldName, TemplateLine>
     */
    private Map<String, TemplateLine> generateTemplateMap(String sheetName, Set<String> mappingFieldNames) {
        Map<String, TemplateLine> templateMap = new HashMap<>();
        int columnIndex = 1;
        for (String mappingFieldName : mappingFieldNames) {
            templateMap.put(mappingFieldName, new TemplateLine(sheetName, mappingFieldName, columnIndex));
            columnIndex++;
        }
        return templateMap;
    }

    /**
     * 執行 ROW 級別的 SpEL 表達式，並返回布林值作為驗證結果。
     * 會將當前的欄位值放入 context 的 `checkedValue` 變數中，供表達式呼叫。
     *
     * @param context     StandardEvaluationContext
     * @param expression  欲執行的 SpEL 表達式字串
     * @param contextRoot ContextRoot
     * @return 驗證結果，符合規則則回傳 true，否則回傳 false
     */
    private Boolean evaluateExpression(StandardEvaluationContext context, String expression, ContextRoot contextRoot) {
        String cellValue = contextRoot.getCurrentCellValue();
        context.setVariable("checkedValue", cellValue == null ? "" : cellValue);
        return parser.parseExpression(expression).getValue(context, Boolean.class);
    }

    /**
     * 使用 SpEL 解析動態錯誤訊息 (允許在錯誤訊息中嵌入表達式及變數)。
     *
     * @param context      StandardEvaluationContext
     * @param express      包含 SpEL 變數之錯誤訊息模板 (例："'錯誤位址：' + #excelAddress")
     * @param excelAddress Excel 格式的欄位地址 (如 "B1")
     * @param sheetName    Sheet 頁籤名稱
     * @return 解析與組合完成的最終錯誤訊息
     */
    private static String evaluateErrorMsg(StandardEvaluationContext context, String express, String excelAddress,
                                           String sheetName) {
        context.setVariable("excelAddress", excelAddress); // 設定變數
        context.setVariable("sheetName", sheetName); // SheetName
        return parser.parseExpression(express).getValue(context, String.class);
    }

    /**
     * 判斷某欄位在當前 Row 的值是否為空字串或 null。
     *
     * @param contextRoot ContextRoot
     * @param fieldName   欲檢查的欄位名稱
     * @return 若為空白或 null 則回傳 true，否則回傳 false
     */
    private boolean isRowFieldBlank(ContextRoot contextRoot, String fieldName) {
        String value = contextRoot.getFieldValue(fieldName);
        return StringUtils.isBlank(value);
    }

    /**
     * 將當前處理的欄位值寫入 ContextRoot，供後續 evaluateExpression 取用。
     *
     * @param contextRoot ContextRoot
     * @param fieldName   當前檢查的欄位名稱
     */
    private void setSingleMappingForRowCellValue(ContextRoot contextRoot, String fieldName) {
        contextRoot.setCurrentCellValue(contextRoot.getFieldValue(fieldName));
    }

    /**
     * 根據 MappingFieldName 取得對應的 TemplateLine，用以反推 Excel 實際儲存格位址。
     *
     * @param templateLineMap TemplateLine 對應映射表
     * @param mappingFieldName 欄位名稱
     * @return 封裝欄位位址與索引資訊的 {@link TemplateLine} 實體
     */
    public static TemplateLine getTemplateLine(Map<String, TemplateLine> templateLineMap, String mappingFieldName) {
        return templateLineMap.get(mappingFieldName);
    }

    /**
     * 預處理 SpEL 表達式，保留做未來擴展替換特殊自定義標記用。
     *
     * @param expression 原始表達式字串
     * @return 預處理完成之表達式字串
     */
    private static String preProcessExpression(String expression) {
        // 可加強處理，例如: 替換特殊標記
        return expression;
    }

    /**
     * 格式化驗證錯誤資訊，將發生錯誤的欄位資訊與 SpEL 解析後的錯誤訊息組合為 {@link ValidateErrorProperty}。
     *
     * @param context         StandardEvaluationContext
     * @param templateLineMap 欄位名稱與位址的映射表
     * @param policy          發生錯誤的驗證政策 {@link ValidationPolicy}
     * @param rowIndex        錯誤發生的真實列索引 (1-based)
     * @return 封裝完整錯誤資訊之屬性物件
     */
    private static ValidateErrorProperty formatRowValidateError(StandardEvaluationContext context,
                                                                Map<String, TemplateLine> templateLineMap, ValidationPolicy policy, int rowIndex) {
        // 取得 MappingFieldName 對應的 TemplateLine (包含 Excel Column Index)
        TemplateLine templateLine = getTemplateLine(templateLineMap,
                StringUtils.trimToEmpty(policy.getMappingFieldName()));
        // 取得 Excel 地址，如 "B1"，此處要 +1 (要含標題的 row)
        String excelAddress = ExcelAddressParser.convertNumToAddress(rowIndex + 1, templateLine.getDataColumnNum());
        log.debug("rowIndex:{}, excelAddress:{}", rowIndex, excelAddress);
        // 使用 SpEL 解析錯誤訊息
        String errorMessage = evaluateErrorMsg(context, policy.getErrorMessage(), excelAddress,
                policy.getTemplateSheetName());
        return new ValidateErrorProperty(policy.getRule(), rowIndex, errorMessage);
    }

    /**
     * 初始化 SpEL 上下文。
     * 設定基礎的 Context 變數 (例如整份 sheet 資料)，並將 {@link ValidationUtil} 及 {@link VariableUtil}
     * 內的方法註冊進 StandardEvaluationContext，使得動態表達式內能直接呼叫這些客製方法。
     *
     * @param contextRoot 初始的 ContextRoot 資料
     * @return 配置完成之 StandardEvaluationContext
     */
    public StandardEvaluationContext initContext(ContextRoot contextRoot) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        // 準備基礎 Param 資料
        Map<String, Object> params = contextRoot.getParams();
        params.put("sheet", contextRoot.getSheet());
        contextRoot.setParams(params);
        this.setContextVariable(contextRoot, context);
        methodRegistration(context);
        return context;
    }

    /**
     * 將 contextRoot 內暫存的參數 (params) 全數設置為 SpEL 的上下文變數，
     * 讓 SpEL 驗證表達式可以直接使用 `#[變數名稱]` 動態讀取值。
     *
     * @param contextRoot 包含 params 的 ContextRoot
     * @param context     欲設定變數之 StandardEvaluationContext
     */
    private void setContextVariable(ContextRoot contextRoot, StandardEvaluationContext context) {
        // 將 contextRoot 內的參數 (params) 設置為 SpEL 上下文變數，讓驗證時可以動態讀取參數值。
        for (Map.Entry<String, Object> p : contextRoot.getParams().entrySet()) {
            // minValue
            context.setVariable(p.getKey(), p.getValue());
        }
        // 用法範例:
        // #minValue < #checkedValue && #checkedValue < #maxValue
    }

    /**
     * 動態方法註冊器。
     * 將 {@link ValidationUtil} 和 {@link VariableUtil} 內的所有靜態方法註冊至 StandardEvaluationContext，
     * 讓 SpEL 表達式可以無縫調用這些預製好的輔助方法 (如: #isNumeric(), #toSet())。
     *
     * @param context 欲註冊方法之 StandardEvaluationContext
     */
    public static void methodRegistration(StandardEvaluationContext context) {
        try {
            log.debug("Start registering context methods");
            for (Method m : ValidationUtil.class.getDeclaredMethods()) {
                log.debug("Context methods:{} registered", m.getName());
                context.registerFunction(m.getName(), m);
            }

            for (Method m : VariableUtil.class.getDeclaredMethods()) {
                log.debug("Context methods:{} registered", m.getName());
                context.registerFunction(m.getName(), m);
            }

//            for (Method m : SpelExpressionUtils.class.getDeclaredMethods()) {
//            	context.registerFunction(m.getName(), m);
//            }
//			context.registerFunction("isNotNumericWithComma", ValidationUtils.class.getDeclaredMethod("isNotNumericWithComma", String.class));
//			context.registerFunction("isNumeric", ValidationUtils.class.getDeclaredMethod("isNumeric", String.class));
        } catch (Exception e) {
            log.error("function reg fail");
        }
    }
}
