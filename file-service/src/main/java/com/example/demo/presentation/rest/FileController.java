package com.example.demo.presentation.rest;

import com.example.demo.application.shared.command.PresignedUrlCommand;
import com.example.demo.application.shared.command.UploadFileCommand;
import com.example.demo.application.port.in.ManageFileUseCase;
import com.example.demo.domain.file.aggregate.root.FileMetadata;
import com.example.demo.presentation.dto.FileResponse;
import com.example.demo.presentation.dto.PresignedUploadRequest;
import com.example.demo.application.shared.dto.PresignedUrlGeneratedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 檔案管理控制器 (Presentation Layer - Inbound Adapter)。
 * <p>
 * 提供小檔案上傳與預先簽名網址等一般檔案操作的 RESTful API。
 * </p>
 */
@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "File Management", description = "檔案管理 API (上傳、下載預先簽名網址等)")
public class FileController {

    private final ManageFileUseCase manageFileUseCase;

    public FileController(ManageFileUseCase manageFileUseCase) {
        this.manageFileUseCase = manageFileUseCase;
    }

    @Operation(summary = "上傳小檔案", description = "直接上傳小檔案，檔案會先進入暫存區，並建立領域資料")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "上傳成功，回傳檔案資訊"),
            @ApiResponse(responseCode = "400", description = "請求參數錯誤")
    })
    @PostMapping("/upload")
    public ResponseEntity<FileResponse> uploadFile(
            @Parameter(description = "租戶 ID") @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @Parameter(description = "要上傳的檔案", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "檔案業務類型") @RequestParam(value = "type", required = false) String type,
            @Parameter(description = "檔案檢查碼 (如 MD5/SHA)") @RequestParam(value = "checksum", required = false) String checksum) throws Exception {

        UploadFileCommand command = new UploadFileCommand(
                tenantId,
                type,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                checksum,
                null, // tags can be parsed from a separate JSON part if needed, passing null for simplicity
                file
        );

        FileMetadata metadata = manageFileUseCase.uploadFile(command);
        return ResponseEntity.ok(FileResponse.fromDomain(metadata));
    }

    @Operation(summary = "取得預先簽名上傳網址", description = "客戶端取得直接上傳至 Storage 的 URL，避免通過伺服器中轉")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功取得網址")
    })
    @PostMapping("/presigned-upload")
    public ResponseEntity<PresignedUrlGeneratedResult> getPresignedUploadUrl(
            @Parameter(description = "租戶 ID") @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @RequestBody PresignedUploadRequest request) throws Exception {

        PresignedUrlCommand command = new PresignedUrlCommand(
                tenantId,
                request.type(),
                request.originalFileName(),
                request.mimeType(),
                request.size(),
                request.checksum(),
                request.tags(),
                request.expiryMinutes() > 0 ? request.expiryMinutes() : 15 // Default 15 minutes
        );

        PresignedUrlGeneratedResult result = manageFileUseCase.generatePresignedUploadUrl(command);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "取得預先簽名下載網址", description = "取得供客戶端直接下載檔案的專屬安全網址")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功取得網址"),
            @ApiResponse(responseCode = "404", description = "檔案不存在")
    })
    @GetMapping("/{fileId}/presigned-download")
    public ResponseEntity<String> getPresignedDownloadUrl(
            @Parameter(description = "檔案 ID", required = true) @PathVariable String fileId,
            @Parameter(description = "是否為下載模式 (true: 下載, false: 預覽)") @RequestParam(value = "isDownload", defaultValue = "true") boolean isDownload,
            @Parameter(description = "網址有效時長 (分鐘)") @RequestParam(value = "expiryMinutes", defaultValue = "60") int expiryMinutes) throws Exception {

        String url = manageFileUseCase.generatePresignedDownloadUrl(fileId, isDownload, expiryMinutes);
        return ResponseEntity.ok(url);
    }
}
