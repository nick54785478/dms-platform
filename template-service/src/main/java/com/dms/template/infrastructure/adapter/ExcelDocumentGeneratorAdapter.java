package com.dms.template.infrastructure.adapter;

import com.dms.template.application.dto.DocumentGeneratedResult;
import com.dms.template.application.port.out.DocumentGeneratorPort;
import com.dms.template.domain.template.aggregate.root.Template;
import com.dms.template.domain.template.aggregate.vo.TemplateType;
import com.dms.template.infrastructure.util.ExcelUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 負責產生 Excel 文件的基礎設施層適配器 (Outbound Adapter).
 * 實作了 {@link DocumentGeneratorPort} 介面，使用 Apache POI (透過 ExcelUtil)
 * 進行 Excel 檔案的產生與資料綁定。
 *
 * 宣告為 package-private 以避免其他層直接存取實作類別，符合六角形架構規範。
 */
@Component
class ExcelDocumentGeneratorAdapter implements DocumentGeneratorPort {

    @Override
    public boolean supports(TemplateType type) {
        return type == TemplateType.EXCEL;
    }

    @Override
    public DocumentGeneratedResult generate(Template template, Map<String, Object> data) {
        String content = template.getLatestVersion()
                .map(com.dms.template.domain.template.aggregate.entity.TemplateVersion::getContentDefinition)
                .orElse("{}");

        List<ExcelUtil.SheetExportData> sheetsData = new ArrayList<>();
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(content);
            
            // 如果 JSON 中存在 "sheets" (新版格式)
            if (rootNode.has("sheets") && rootNode.get("sheets").isArray()) {
                JsonNode sheetsNode = rootNode.get("sheets");
                for (JsonNode sheetNode : sheetsNode) {
                    String sheetName = sheetNode.path("sheetName").asText("Sheet");
                    JsonNode columnsNode = sheetNode.path("columns");
                    sheetsData.add(buildSheetExportData(sheetName, columnsNode, data));
                }
            } else {
                // 相容舊版 "columns" 格式
                JsonNode columnsNode = rootNode.path("columns");
                String sheetName = template.getName() != null ? template.getName() : "Template";
                sheetsData.add(buildSheetExportData(sheetName, columnsNode, data));
            }
            
            if (sheetsData.isEmpty()) {
                 sheetsData.add(new ExcelUtil.SheetExportData("Template", List.of("No Template Defined"), List.of()));
            }
        } catch (Exception e) {
            sheetsData.add(new ExcelUtil.SheetExportData("Error", List.of("Invalid Template JSON"), List.of()));
        }

        byte[] bytes = ExcelUtil.exportMultiSheetDataAsByteArray(sheetsData);
        String fileName = "template_" + template.getId().value() + ".xlsx";
        String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        return new DocumentGeneratedResult(bytes, fileName, contentType);
    }
    
    @SuppressWarnings("unchecked")
    private ExcelUtil.SheetExportData buildSheetExportData(String sheetName, JsonNode columnsNode, Map<String, Object> data) {
        List<String> headers = new ArrayList<>();
        List<String> fieldNames = new ArrayList<>();
        
        if (columnsNode != null && columnsNode.isArray() && !columnsNode.isEmpty()) {
            for (JsonNode colNode : columnsNode) {
                headers.add(colNode.path("header").asText());
                fieldNames.add(colNode.path("field").asText());
            }
        } else {
            headers.add("No Columns Defined");
        }

        List<Object[]> dataset = new ArrayList<>();
        
        if (data != null && data.containsKey(sheetName)) {
            Object sheetDataObj = data.get(sheetName);
            if (sheetDataObj instanceof List) {
                List<Map<String, Object>> rows = (List<Map<String, Object>>) sheetDataObj;
                for (Map<String, Object> rowMap : rows) {
                    Object[] rowData = new Object[fieldNames.size()];
                    for (int i = 0; i < fieldNames.size(); i++) {
                        String field = fieldNames.get(i);
                        rowData[i] = rowMap.containsKey(field) ? rowMap.get(field) : "";
                    }
                    dataset.add(rowData);
                }
            }
        } else if (data != null && !data.isEmpty() && !fieldNames.isEmpty()) {
            // Fallback for flat object (old payload from frontend before this PR)
            Object[] rowData = new Object[fieldNames.size()];
            for (int i = 0; i < fieldNames.size(); i++) {
                String field = fieldNames.get(i);
                rowData[i] = data.containsKey(field) ? data.get(field) : "";
            }
            dataset.add(rowData);
        }

        return new ExcelUtil.SheetExportData(sheetName, headers, dataset);
    }
}
