package com.example.validation.application.service;

import com.example.validation.application.port.in.UploadTemplateUseCase;
import com.example.validation.application.shared.command.UploadTemplateCommand;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
class UploadTemplateCommandService implements UploadTemplateUseCase {

	@Override
	public void upload(UploadTemplateCommand command) throws IOException {

		log.debug("command:{}", command);
        
		// TODO 後續上傳功能
	}
}
