package com.dms.template.presentation.controller;

import com.dms.template.application.command.CreateTemplateCommand;
import com.dms.template.application.command.PublishTemplateCommand;
import com.dms.template.application.dto.TemplateGottenResult;
import com.dms.template.application.port.in.CreateTemplateUseCase;
import com.dms.template.application.query.GetTemplateQuery;
import com.dms.template.presentation.assembler.TemplateResourceAssembler;
import com.dms.template.presentation.resource.in.CreateTemplateResource;
import com.dms.template.presentation.resource.out.TemplateCreatedResource;
import lombok.RequiredArgsConstructor;
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
@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final CreateTemplateUseCase createTemplateUseCase;
    private final com.dms.template.application.port.in.SaveTemplateDraftUseCase saveTemplateDraftUseCase;
    private final com.dms.template.application.port.in.DownloadTemplateUseCase downloadTemplateUseCase;
    private final com.dms.template.application.port.in.PublishTemplateUseCase publishTemplateUseCase;
    private final com.dms.template.application.port.in.SearchTemplateUseCase searchTemplateUseCase;
    private final com.dms.template.application.port.in.GetTemplateUseCase getTemplateUseCase;
    private final TemplateResourceAssembler assembler;

    @PostMapping
    public ResponseEntity<TemplateCreatedResource> createTemplate(@RequestBody CreateTemplateResource resource) {
        // 1. 透過 Assembler 轉為 Command
        CreateTemplateCommand command = assembler.toCommand(resource);
        
        // 2. 呼叫 UseCase
        TemplateGottenResult result = createTemplateUseCase.createTemplate(command);
        
        // 3. 透過 Assembler 轉為 Resource 回傳
        return ResponseEntity.ok(assembler.toResource(result));
    }
    
    @PostMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadTemplate(
            @PathVariable String id,
            @RequestBody(required = false) java.util.Map<String, Object> data) {
        
        com.dms.template.application.query.DownloadTemplateQuery query = new com.dms.template.application.query.DownloadTemplateQuery(id, data);
        byte[] excelBytes = downloadTemplateUseCase.downloadTemplate(query);
        
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"template_" + id + ".xlsx\"")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excelBytes);
    }
    
    @PutMapping("/{id}/draft")
    public ResponseEntity<Void> saveTemplateDraft(
            @PathVariable String id,
            @RequestBody com.dms.template.presentation.resource.in.SaveTemplateDraftResource resource) {
        // 1. 轉為 Command
        com.dms.template.application.command.SaveTemplateDraftCommand command = assembler.toCommand(id, resource);
        
        // 2. 呼叫 UseCase
        saveTemplateDraftUseCase.saveTemplateDraft(command);
        
        return ResponseEntity.ok().build();
    }
    @PutMapping("/{id}/versions/{version}/publish")
    public ResponseEntity<Void> publishTemplate(
            @PathVariable String id,
            @PathVariable String version) {
        
        // 1. 轉為 Command
        PublishTemplateCommand command = new PublishTemplateCommand(id, version);
        
        // 2. 呼叫 UseCase
        publishTemplateUseCase.publishTemplate(command);
        
        return ResponseEntity.ok().build();
    }
    
    @GetMapping
    public ResponseEntity<com.dms.template.application.dto.PagedResult<com.dms.template.presentation.resource.out.TemplateSearchedResource>> searchTemplates(
            @RequestParam(required = false) String templateType,
            @RequestParam(required = false) String templateCode,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        com.dms.template.application.query.SearchTemplateQuery query = new com.dms.template.application.query.SearchTemplateQuery(templateType, templateCode, name, page, size);
        com.dms.template.application.dto.PagedResult<com.dms.template.application.dto.TemplateSearchedResult> results = searchTemplateUseCase.searchTemplates(query);
        return ResponseEntity.ok(results.map(assembler::toSearchedResource));
    }

    @GetMapping("/{id}")
    public ResponseEntity<com.dms.template.presentation.resource.out.TemplateRetrievedResource> getTemplate(
            @PathVariable String id) {
        GetTemplateQuery query = new com.dms.template.application.query.GetTemplateQuery(id);
        com.dms.template.application.dto.TemplateGottenResult result = getTemplateUseCase.getTemplate(query);
        return ResponseEntity.ok(assembler.toRetrievedResource(result));
    }
}
