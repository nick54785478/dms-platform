package com.example.demo.presentation.rest;

import com.example.demo.application.shared.command.MultipartUploadCommand;
import com.example.demo.application.port.in.MultipartUploadUseCase;
import com.example.demo.application.shared.dto.MultipartUploadInitiatedResult;
import com.example.demo.domain.file.aggregate.root.FileMetadata;
import com.example.demo.presentation.dto.CompleteMultipartRequest;
import com.example.demo.presentation.dto.FileResponse;
import com.example.demo.presentation.dto.InitiateMultipartRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 分段上傳控制器 (Presentation Layer - Inbound Adapter)。
 * <p>
 * 提供大檔案分段上傳 (Multipart Upload) 的完整生命週期 RESTful API。
 * </p>
 */
@RestController
@RequestMapping("/api/v1/files/multipart")
@Tag(name = "Multipart Upload", description = "大檔案分段上傳 API")
public class MultipartUploadController {

    private final MultipartUploadUseCase multipartUploadUseCase;

    public MultipartUploadController(MultipartUploadUseCase multipartUploadUseCase) {
        this.multipartUploadUseCase = multipartUploadUseCase;
    }

    @Operation(summary = "初始化分段上傳", description = "啟動大檔案分段上傳任務，回傳 FileId 與 UploadId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "初始化成功")
    })
    @PostMapping("/initiate")
    public ResponseEntity<MultipartUploadInitiatedResult> initiate(
            @Parameter(description = "租戶 ID") @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @RequestBody InitiateMultipartRequest request) throws Exception {

        MultipartUploadCommand.InitiateCommand command = new MultipartUploadCommand.InitiateCommand(
                tenantId,
                request.type(),
                request.originalFileName(),
                request.mimeType(),
                request.size(),
                request.checksum(),
                request.tags()
        );

        MultipartUploadInitiatedResult result = multipartUploadUseCase.initiateMultipartUpload(command);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "取得分段預先簽名網址", description = "為特定的片段(Part)取得直接上傳 Storage 的專屬網址")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功取得分段網址")
    })
    @GetMapping("/{fileId}/{uploadId}/presigned-part")
    public ResponseEntity<String> getPresignedPartUrl(
            @Parameter(description = "檔案 ID", required = true) @PathVariable String fileId,
            @Parameter(description = "上傳任務 ID (UploadId)", required = true) @PathVariable String uploadId,
            @Parameter(description = "分段編號 (Part Number)", required = true) @RequestParam("partNumber") int partNumber,
            @Parameter(description = "網址有效時長 (分鐘)") @RequestParam(value = "expiryMinutes", defaultValue = "15") int expiryMinutes) throws Exception {

        String url = multipartUploadUseCase.getPresignedPartUrl(fileId, uploadId, partNumber, expiryMinutes);
        return ResponseEntity.ok(url);
    }

    @Operation(summary = "完成分段上傳", description = "在所有分段都上傳後，通知伺服器進行合併並完成上傳")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "合併成功，回傳完整檔案資訊")
    })
    @PostMapping("/{fileId}/{uploadId}/complete")
    public ResponseEntity<FileResponse> complete(
            @Parameter(description = "檔案 ID", required = true) @PathVariable String fileId,
            @Parameter(description = "上傳任務 ID (UploadId)", required = true) @PathVariable String uploadId,
            @RequestBody CompleteMultipartRequest request) throws Exception {

        MultipartUploadCommand.CompleteCommand command = new MultipartUploadCommand.CompleteCommand(
                fileId,
                uploadId,
                request.partETags()
        );

        FileMetadata metadata = multipartUploadUseCase.completeMultipartUpload(command);
        return ResponseEntity.ok(FileResponse.fromDomain(metadata));
    }

    @Operation(summary = "放棄分段上傳", description = "中止分段上傳任務，並清理 Storage 上已佔用的分段空間")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "成功放棄任務與清理空間")
    })
    @DeleteMapping("/{fileId}/{uploadId}/abort")
    public ResponseEntity<Void> abort(
            @Parameter(description = "檔案 ID", required = true) @PathVariable String fileId,
            @Parameter(description = "上傳任務 ID (UploadId)", required = true) @PathVariable String uploadId) throws Exception {

        MultipartUploadCommand.AbortCommand command = new MultipartUploadCommand.AbortCommand(fileId, uploadId);
        multipartUploadUseCase.abortMultipartUpload(command);

        return ResponseEntity.noContent().build();
    }
}
