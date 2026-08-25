package com.example.validation.presentation.assembler;

import com.example.validation.application.shared.command.UploadTemplateCommand;
import com.example.validation.presentation.resource.in.UploadTemplateResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class UploadTemplateResourceAssembler {

    public UploadTemplateCommand toCommand(UploadTemplateResource resource, MultipartFile file) throws IOException {
        if (resource == null) {
            return null;
        }
        
        byte[] fileContent = file != null ? file.getBytes() : null;
        
        return new UploadTemplateCommand(
                resource.getName(),
                resource.getType(),
                resource.getFileType(),
                resource.getFilePath(),
                resource.getFileName(),
                fileContent
        );
    }
}
