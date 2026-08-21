package com.dms.template.presentation.controller;

import com.dms.template.application.command.CreateTemplateCommand;
import com.dms.template.application.command.PublishTemplateCommand;
import com.dms.template.application.dto.PagedResult;
import com.dms.template.application.dto.TemplateGottenResult;
import com.dms.template.application.dto.TemplateSearchedResult;
import com.dms.template.application.port.in.CreateTemplateUseCase;
import com.dms.template.application.port.in.DownloadTemplateUseCase;
import com.dms.template.application.port.in.GetTemplateUseCase;
import com.dms.template.application.port.in.ListTemplateVersionsUseCase;
import com.dms.template.application.port.in.PublishTemplateUseCase;
import com.dms.template.application.port.in.SaveTemplateDraftUseCase;
import com.dms.template.application.port.in.SearchTemplateUseCase;
import com.dms.template.application.query.DownloadTemplateQuery;
import com.dms.template.application.query.GetTemplateQuery;
import com.dms.template.application.query.SearchTemplateQuery;
import com.dms.template.presentation.assembler.TemplateResourceAssembler;
import com.dms.template.presentation.resource.in.CreateTemplateResource;
import com.dms.template.presentation.resource.out.TemplateCreatedResource;
import com.dms.template.presentation.resource.out.TemplateRetrievedResource;
import com.dms.template.presentation.resource.out.TemplateSearchedResource;
import com.dms.template.presentation.resource.out.TemplateVersionRetrievedResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 範本控制器 (Inbound Adapter)
 */
@Tag(name = "Template Management", description = "範本管理 API")
@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final CreateTemplateUseCase createTemplateUseCase;
    private final SaveTemplateDraftUseCase saveTemplateDraftUseCase;
    private final DownloadTemplateUseCase downloadTemplateUseCase;
    private final PublishTemplateUseCase publishTemplateUseCase;
    private final SearchTemplateUseCase searchTemplateUseCase;
    private final GetTemplateUseCase getTemplateUseCase;
    private final ListTemplateVersionsUseCase listTemplateVersionsUseCase;
    private final TemplateResourceAssembler assembler;

    @Operation(summary = "建立新範本", description = "建立一個全新的範本紀錄")
    @PostMapping
    public ResponseEntity<TemplateCreatedResource> createTemplate(@RequestBody CreateTemplateResource resource) {
        CreateTemplateCommand command = assembler.toCommand(resource);
        TemplateGottenResult result = createTemplateUseCase.createTemplate(command);
        return ResponseEntity.ok(assembler.toResource(result));
    }

    @Operation(summary = "下載範本", description = "根據範本草稿的設定與填入資料匯出 Excel 或 PDF")
    @PostMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadTemplate(
            @Parameter(description = "範本 ID") @PathVariable String id,
            @RequestBody(required = false) java.util.Map<String, Object> data) {

        DownloadTemplateQuery query = new DownloadTemplateQuery(id, data);
        com.dms.template.application.dto.DocumentGeneratedResult result = downloadTemplateUseCase.downloadTemplate(query);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.fileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, result.contentType())
                .body(result.content());
    }

    @Operation(summary = "儲存範本草稿", description = "更新範本設計器的 JSON 定義並儲存為 DRAFT")
    @PutMapping("/{id}/draft")
    public ResponseEntity<Void> saveTemplateDraft(
            @Parameter(description = "範本 ID") @PathVariable String id,
            @RequestBody com.dms.template.presentation.resource.in.SaveTemplateDraftResource resource) {
        com.dms.template.application.command.SaveTemplateDraftCommand command = assembler.toCommand(id, resource);
        saveTemplateDraftUseCase.saveTemplateDraft(command);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "發佈範本版本", description = "將特定草稿版本定版並發佈為正式版本")
    @PutMapping("/{id}/versions/{version}/publish")
    public ResponseEntity<Void> publishTemplate(
            @Parameter(description = "範本 ID") @PathVariable String id,
            @Parameter(description = "欲發佈之版本號 (例: V1.0-DRAFT)") @PathVariable String version) {
        PublishTemplateCommand command = new PublishTemplateCommand(id, version);
        publishTemplateUseCase.publishTemplate(command);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "查詢範本列表", description = "支援根據類型、代碼、名稱進行分頁查詢")
    @GetMapping
    public ResponseEntity<PagedResult<TemplateSearchedResource>> searchTemplates(
            @Parameter(description = "範本類型 (EXCEL / PDF)") @RequestParam(required = false) String templateType,
            @Parameter(description = "範本代碼") @RequestParam(required = false) String templateCode,
            @Parameter(description = "範本名稱關鍵字") @RequestParam(required = false) String name,
            @Parameter(description = "頁碼 (預設 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每頁筆數 (預設 10)") @RequestParam(defaultValue = "10") int size) {

        SearchTemplateQuery query = new SearchTemplateQuery(templateType, templateCode, name, page, size);
        PagedResult<TemplateSearchedResult> results = searchTemplateUseCase.searchTemplates(query);
        return ResponseEntity.ok(results.map(assembler::toSearchedResource));
    }

    @Operation(summary = "取得單一範本詳細資料", description = "根據 ID 查詢範本，包含目前 DRAFT 的設計內容")
    @GetMapping("/{id}")
    public ResponseEntity<TemplateRetrievedResource> getTemplate(
            @Parameter(description = "範本 ID") @PathVariable String id) {
        GetTemplateQuery query = new GetTemplateQuery(id);
        TemplateGottenResult result = getTemplateUseCase.getTemplate(query);
        return ResponseEntity.ok(assembler.toRetrievedResource(result));
    }

    @Operation(summary = "查詢範本版本紀錄", description = "依新到舊分頁列出此範本的所有歷史版號與設計內容")
    @GetMapping("/{id}/versions")
    public ResponseEntity<PagedResult<TemplateVersionRetrievedResource>> listTemplateVersions(
            @Parameter(description = "範本 ID") @PathVariable String id,
            @Parameter(description = "頁碼 (預設 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每頁筆數 (預設 10)") @RequestParam(defaultValue = "10") int size) {
        com.dms.template.application.query.ListTemplateVersionsQuery query = new com.dms.template.application.query.ListTemplateVersionsQuery(id, page, size);
        PagedResult<com.dms.template.application.dto.TemplateVersionGottenResult> results = listTemplateVersionsUseCase.listTemplateVersions(query);
        return ResponseEntity.ok(results.map(assembler::toVersionRetrievedResource));
    }
}
