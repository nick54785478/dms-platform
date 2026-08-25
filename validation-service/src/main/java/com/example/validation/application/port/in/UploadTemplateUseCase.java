package com.example.validation.application.port.in;

import com.example.validation.application.shared.command.UploadTemplateCommand;
import java.io.IOException;

public interface UploadTemplateUseCase {
    void upload(UploadTemplateCommand command) throws IOException;
}
