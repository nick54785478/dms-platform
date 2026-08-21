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

        String sheetName = template.getName() != null ? template.getName() : "Template";
        List<String> headers = new ArrayList<>();
        List<Object[]> dataset = new ArrayList<>();
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(content);
            JsonNode columnsNode = rootNode.path("columns");
            
            if (columnsNode.isArray() && !columnsNode.isEmpty()) {
                Object[] rowData = new Object[columnsNode.size()];
                for (int i = 0; i < columnsNode.size(); i++) {
                    JsonNode colNode = columnsNode.get(i);
                    headers.add(colNode.path("header").asText());
                    
                    String field = colNode.path("field").asText();
                    if (data != null && data.containsKey(field)) {
                        rowData[i] = data.get(field);
                    } else {
                        rowData[i] = "";
                    }
                }
                dataset.add(rowData);
            } else {
                headers.add("No Columns Defined");
            }
        } catch (Exception e) {
            headers.add("Invalid Template JSON");
        }

        byte[] bytes = ExcelUtil.exportDataAsByteArrayFromArrays(sheetName, headers, dataset);
        String fileName = "template_" + template.getId().value() + ".xlsx";
        String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        return new DocumentGeneratedResult(bytes, fileName, contentType);
    }
}
