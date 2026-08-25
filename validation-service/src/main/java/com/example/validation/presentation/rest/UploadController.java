package com.example.validation.presentation.rest;

import com.example.validation.application.port.in.UploadTemplateUseCase;
import com.example.validation.application.shared.command.UploadTemplateCommand;
import com.example.validation.presentation.assembler.UploadTemplateResourceAssembler;
import com.example.validation.presentation.resource.in.UploadTemplateResource;
import com.example.validation.presentation.resource.out.TemplateUploadedResource;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.io.IOException;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/upload")
public class UploadController {

    private final UploadTemplateUseCase uploadTemplateUseCase;
    private final UploadTemplateResourceAssembler assembler;

    @Operation(summary = "上傳範本檔案與資訊")
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TemplateUploadedResource> upload(
            @Parameter(description = "上傳範本資源資訊 (JSON 格式)", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UploadTemplateResource.class)) 
            @RequestPart(name = "resource", required = true) UploadTemplateResource resource,
            @RequestPart(name = "file", required = true) MultipartFile file) throws IOException {
            
        UploadTemplateCommand command = assembler.toCommand(resource, file);
        uploadTemplateUseCase.upload(command);
        
        return new ResponseEntity<>(new TemplateUploadedResource("200", "上傳成功"), HttpStatus.OK);
    }
}
