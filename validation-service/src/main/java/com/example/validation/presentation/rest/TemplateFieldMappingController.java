package com.example.validation.presentation.rest;

import com.example.validation.application.port.in.CreateTemplateFieldMappingUseCase;
import com.example.validation.application.port.in.DeleteTemplateFieldMappingUseCase;
import com.example.validation.application.port.in.ListTemplateFieldMappingUseCase;
import com.example.validation.application.port.in.ListTemplateSheetNameUseCase;
import com.example.validation.application.port.in.ListTemplateFieldMappingBySheetUseCase;
import com.example.validation.application.port.in.UpdateTemplateFieldMappingUseCase;
import com.example.validation.application.shared.query.ListTemplateFieldMappingQuery;
import com.example.validation.application.shared.query.ListTemplateSheetNameQuery;
import com.example.validation.application.shared.query.ListTemplateFieldMappingBySheetQuery;
import com.example.validation.presentation.assembler.TemplateFieldMappingResourceAssembler;
import com.example.validation.presentation.resource.in.CreateTemplateFieldMappingResource;
import com.example.validation.presentation.resource.in.UpdateTemplateFieldMappingResource;
import com.example.validation.presentation.resource.out.TemplateFieldMappingCreatedResource;
import com.example.validation.presentation.resource.out.TemplateFieldMappingRetrievedResource;
import com.example.validation.presentation.resource.out.TemplateFieldMappingUpdatedResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Template Field Mapping API", description = "範本欄位對應維護 API")
@RestController
@RequestMapping("/api/template-field-mappings")
public class TemplateFieldMappingController {

    private final CreateTemplateFieldMappingUseCase createUseCase;
    private final UpdateTemplateFieldMappingUseCase updateUseCase;
    private final DeleteTemplateFieldMappingUseCase deleteUseCase;
    private final ListTemplateFieldMappingUseCase listUseCase;
    private final ListTemplateSheetNameUseCase listSheetNameUseCase;
    private final ListTemplateFieldMappingBySheetUseCase listBySheetUseCase;
    private final TemplateFieldMappingResourceAssembler resourceAssembler;

    public TemplateFieldMappingController(
            CreateTemplateFieldMappingUseCase createUseCase,
            UpdateTemplateFieldMappingUseCase updateUseCase,
            DeleteTemplateFieldMappingUseCase deleteUseCase,
            ListTemplateFieldMappingUseCase listUseCase,
            ListTemplateSheetNameUseCase listSheetNameUseCase,
            ListTemplateFieldMappingBySheetUseCase listBySheetUseCase,
            TemplateFieldMappingResourceAssembler resourceAssembler) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.deleteUseCase = deleteUseCase;
        this.listUseCase = listUseCase;
        this.listSheetNameUseCase = listSheetNameUseCase;
        this.listBySheetUseCase = listBySheetUseCase;
        this.resourceAssembler = resourceAssembler;
    }

    @Operation(summary = "新增欄位對應", description = "新增一筆範本與驗證規則欄位的對應關係")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TemplateFieldMappingCreatedResource createMapping(@RequestBody CreateTemplateFieldMappingResource resource) {
        Long id = createUseCase.create(resourceAssembler.toCommand(resource));
        return new TemplateFieldMappingCreatedResource(id);
    }

    @Operation(summary = "更新欄位對應", description = "更新現有的欄位對應關係")
    @PutMapping("/{id}")
    public TemplateFieldMappingUpdatedResource updateMapping(@PathVariable Long id, @RequestBody UpdateTemplateFieldMappingResource resource) {
        updateUseCase.update(resourceAssembler.toCommand(id, resource));
        return new TemplateFieldMappingUpdatedResource(true);
    }

    @Operation(summary = "刪除欄位對應", description = "透過 ID 刪除一筆欄位對應關係")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMapping(@PathVariable Long id) {
        deleteUseCase.delete(id);
    }

    @Operation(summary = "查詢範本的欄位對應", description = "根據範本代碼查詢該範本的所有欄位對應")
    @GetMapping("/template/{templateCode}")
    public List<TemplateFieldMappingRetrievedResource> listMappings(@PathVariable String templateCode) {
        return listUseCase.list(new ListTemplateFieldMappingQuery(templateCode)).stream()
                .map(resourceAssembler::toResource)
                .collect(Collectors.toList());
    }

    @Operation(summary = "查詢範本的 Sheet 選單", description = "根據範本代碼查詢所有不重複的 Sheet 名稱")
    @GetMapping("/template/{templateCode}/sheets")
    public List<String> listSheetNames(@PathVariable String templateCode) {
        return listSheetNameUseCase.list(new ListTemplateSheetNameQuery(templateCode));
    }

    @Operation(summary = "查詢特定 Sheet 的欄位對應", description = "根據範本代碼與 Sheet 名稱查詢對應欄位")
    @GetMapping("/template/{templateCode}/sheets/{sheetName}/fields")
    public List<TemplateFieldMappingRetrievedResource> listMappingsBySheet(@PathVariable String templateCode, @PathVariable String sheetName) {
        return listBySheetUseCase.list(new ListTemplateFieldMappingBySheetQuery(templateCode, sheetName)).stream()
                .map(resourceAssembler::toResource)
                .collect(Collectors.toList());
    }
}
