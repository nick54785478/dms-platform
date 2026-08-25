package com.example.validation.presentation.rest;

import com.example.validation.application.port.in.ValidateExcelUseCase;
import com.example.validation.application.shared.command.ValidateExcelCommand;
import com.example.validation.presentation.assembler.ValidateExcelResourceAssembler;
import com.example.validation.presentation.resource.out.ExcelValidatedResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/validation")
public class ValidationController {

    private final ValidateExcelUseCase validateExcelUseCase;
    private final ValidateExcelResourceAssembler assembler;

    @Operation(summary = "驗證 Excel 檔案")
    @PostMapping(value = "/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExcelValidatedResource> validateExcel(
            @Parameter(description = "範本代碼", required = true)
            @RequestPart(name = "code") String code,
            @Parameter(description = "上傳檔案", required = true)
            @RequestPart(name = "file") MultipartFile file) throws IOException {

        ValidateExcelCommand command = assembler.toCommand(code, file);
        validateExcelUseCase.validate(command);

        return new ResponseEntity<>(new ExcelValidatedResource("200", "驗證成功"), HttpStatus.OK);
    }
}
