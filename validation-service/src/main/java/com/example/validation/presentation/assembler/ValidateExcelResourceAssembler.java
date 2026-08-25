package com.example.validation.presentation.assembler;

import com.example.validation.application.shared.command.ValidateExcelCommand;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class ValidateExcelResourceAssembler {

    public ValidateExcelCommand toCommand(String code, MultipartFile file) throws IOException {
        return new ValidateExcelCommand(code, file.getBytes());
    }
}
