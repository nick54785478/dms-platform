package com.example.dms.presentation.rest;

import com.example.dms.application.port.in.CreateDocumentUseCase;
import com.example.dms.application.port.in.DeleteDocumentCommand;
import com.example.dms.application.port.in.DeleteDocumentUseCase;
import com.example.dms.application.port.in.GetDocumentUseCase;
import com.example.dms.application.shared.command.GetDocumentQuery;
import com.example.dms.application.port.in.SearchDocumentQuery;
import com.example.dms.application.port.in.SearchDocumentUseCase;
import com.example.dms.application.port.in.UpdateDocumentUseCase;
import com.example.dms.application.port.in.UploadDocumentVersionUseCase;
import com.example.dms.application.shared.dto.DocumentGottenResult;
import com.example.dms.application.shared.dto.DocumentSearchedResult;
import com.example.dms.application.shared.dto.PageGottenResult;
import com.example.dms.presentation.assembler.DocumentResourceAssembler;
import com.example.dms.presentation.resource.in.CreateDocumentResource;
import com.example.dms.presentation.resource.in.UpdateDocumentResource;
import com.example.dms.presentation.resource.in.UploadDocumentVersionResource;
import com.example.dms.presentation.resource.out.DocumentCreatedResource;
import com.example.dms.presentation.resource.out.DocumentRetrievedResource;
import com.example.dms.presentation.resource.out.DocumentSearchedResource;
import com.example.dms.presentation.resource.out.PageRetrievedResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Document API", description = "文件管理相關 API")
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final CreateDocumentUseCase createDocumentUseCase;
    private final DeleteDocumentUseCase deleteDocumentUseCase;
    private final UpdateDocumentUseCase updateDocumentUseCase;
    private final GetDocumentUseCase getDocumentUseCase;
    private final SearchDocumentUseCase searchDocumentUseCase;
    private final UploadDocumentVersionUseCase uploadDocumentVersionUseCase;
    private final DocumentResourceAssembler assembler;

    @Operation(summary = "建立新文件", description = "建立一份新的文件元資料，並可選擇性關聯至 file-service 的實體檔案 (fileId)")
    @PostMapping
    public ResponseEntity<DocumentCreatedResource> createDocument(@RequestBody CreateDocumentResource resource) {
        DocumentGottenResult result = createDocumentUseCase.createDocument(assembler.toCommand(resource));
        return ResponseEntity.ok(assembler.toResource(result));
    }

    @Operation(summary = "更新文件", description = "更新文件的標題與描述")
    @PutMapping("/{id}")
    public ResponseEntity<DocumentCreatedResource> updateDocument(@PathVariable String id, @RequestBody UpdateDocumentResource resource) {
        DocumentGottenResult result = updateDocumentUseCase.updateDocument(assembler.toCommand(id, resource));
        return ResponseEntity.ok(assembler.toResource(result));
    }

    @Operation(summary = "上傳新版本文件", description = "綁定新檔案，並增加文件的主版本或次版本號")
    @PostMapping("/{id}/versions")
    public ResponseEntity<DocumentCreatedResource> uploadDocumentVersion(@PathVariable String id, @RequestBody UploadDocumentVersionResource resource) {
        DocumentGottenResult result = uploadDocumentVersionUseCase.uploadDocumentVersion(assembler.toCommand(id, resource));
        return ResponseEntity.ok(assembler.toResource(result));
    }

    @Operation(summary = "取得文件詳情與歷史版本", description = "根據 ID 取得文件，包含其歷代版本的詳細資訊")
    @GetMapping("/{id}")
    public ResponseEntity<DocumentRetrievedResource> getDocument(@PathVariable String id) {
        DocumentGottenResult result = getDocumentUseCase.getDocument(new GetDocumentQuery(id));
        return ResponseEntity.ok(assembler.toRetrievedResource(result));
    }

    @Operation(summary = "分頁搜尋文件", description = "取得所有文件並支援分頁")
    @GetMapping
    public ResponseEntity<PageRetrievedResource<DocumentSearchedResource>> searchDocuments(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageGottenResult<DocumentSearchedResult> result = searchDocumentUseCase.searchDocuments(new SearchDocumentQuery(page, size, title, status));
        return ResponseEntity.ok(assembler.toResource(result));
    }

    @Operation(summary = "刪除文件", description = "根據 ID 刪除文件元資料")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String id) {
        deleteDocumentUseCase.deleteDocument(new DeleteDocumentCommand(id));
        return ResponseEntity.noContent().build();
    }
}
