package com.dms.template.infrastructure.adapter;

import com.dms.template.application.dto.DocumentGeneratedResult;
import com.dms.template.application.port.out.DocumentGeneratorPort;
import com.dms.template.domain.template.aggregate.root.Template;
import com.dms.template.domain.template.aggregate.vo.TemplateType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * 負責產生 PDF 文件的基礎設施層適配器 (Outbound Adapter).
 * 實作了 {@link DocumentGeneratorPort} 介面。
 * 結合了 Thymeleaf 進行 HTML 範本渲染與資料綁定，並透過 openhtmltopdf
 * 將生成的 HTML 轉換為 PDF 二進位檔案。
 *
 * 宣告為 package-private 以避免其他層直接存取實作類別，符合六角形架構規範。
 */
@Component
@RequiredArgsConstructor
class PdfDocumentGeneratorAdapter implements DocumentGeneratorPort {

    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(TemplateType type) {
        return type == TemplateType.PDF;
    }

    @Override
    public DocumentGeneratedResult generate(Template template, Map<String, Object> data) {
        String content = template.getLatestVersion()
                .map(com.dms.template.domain.template.aggregate.entity.TemplateVersion::getContentDefinition)
                .orElse("{}");

        try {
            Map<String, Object> draft = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
            Object blocks = draft.getOrDefault("blocks", Collections.emptyList());

            Context context = new Context();
            context.setVariable("blocks", blocks);
            context.setVariable("data", data != null ? data : Collections.emptyMap());

            // 1. Render HTML via Thymeleaf
            String htmlContent = templateEngine.process("pdf-layout", context);

            // 2. Convert HTML to PDF
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(htmlContent, "classpath:/");

                // Load Chinese Font
                try {
                    ClassPathResource fontResource = new ClassPathResource("fonts/NotoSansTC-Regular.ttf");
                    if (fontResource.exists()) {
                        builder.useFont(() -> {
                            try {
                                return fontResource.getInputStream();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }, "Noto Sans TC");
                    } else {
                        // Fallback to Windows font for local development
                        java.io.File winFont = new java.io.File("C:\\Windows\\Fonts\\msjh.ttc");
                        if (winFont.exists()) {
                            builder.useFont(winFont, "Noto Sans TC");
                        }
                    }
                } catch (Exception e) {
                    // Fallback or log if font missing
                }

                builder.toStream(os);
                builder.run();

                byte[] bytes = os.toByteArray();
                String fileName = "template_" + template.getId().value() + ".pdf";
                String contentType = "application/pdf";

                return new DocumentGeneratedResult(bytes, fileName, contentType);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF document", e);
        }
    }
}
