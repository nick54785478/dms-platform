package com.dms.template.application.service;

import com.dms.template.application.assembler.TemplateDtoAssembler;
import com.dms.template.application.command.CreateTemplateCommand;
import com.dms.template.application.command.PublishTemplateCommand;
import com.dms.template.application.command.SaveTemplateDraftCommand;
import com.dms.template.application.dto.TemplateGottenResult;
import com.dms.template.application.port.in.CreateTemplateUseCase;
import com.dms.template.application.port.in.PublishTemplateUseCase;
import com.dms.template.application.port.in.SaveTemplateDraftUseCase;
import com.dms.template.application.port.out.TemplateMessagePublisherPort;
import com.dms.template.application.port.out.TemplateRepositoryPort;
import com.dms.template.domain.template.aggregate.root.Template;
import com.dms.template.domain.template.event.TemplatePublishedEvent;
import com.dms.template.domain.template.aggregate.vo.TemplateId;
import com.dms.template.domain.template.aggregate.vo.TemplateType;
import com.dms.template.domain.template.exception.TemplateNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 範本應用服務 (Inbound Adapter)
 * 宣告為 package-private，外部必須透過 UseCase 介面呼叫
 */
@Service
@RequiredArgsConstructor
class TemplateCommandService implements CreateTemplateUseCase, SaveTemplateDraftUseCase, PublishTemplateUseCase {

    private final TemplateRepositoryPort templateRepositoryPort;
    private final TemplateMessagePublisherPort templateMessagePublisherPort;
    private final TemplateDtoAssembler assembler;

    @Override
    @Transactional
    public TemplateGottenResult createTemplate(CreateTemplateCommand command) {
        // 1. 建立領域實體
        TemplateType type = null;
        if (command.templateType() != null) {
            type = TemplateType.valueOf(command.templateType());
        }

        Template template = Template.create(
                type,
                command.templateCode(),
                command.name(),
                command.description()
        );

        // 2. 持久化
        templateRepositoryPort.save(template);

        // 3. 回傳 DTO
        return assembler.toResult(template);
    }

    @Override
    @Transactional
    public void saveTemplateDraft(SaveTemplateDraftCommand command) {
        // 1. 透過 Port 取得聚合根
        Template template = templateRepositoryPort.findById(TemplateId.of(command.templateId()))
                .orElseThrow(() -> new TemplateNotFoundException(command.templateId()));

        // 2. 執行領域邏輯
        template.saveDraft(command.contentDefinition(), command.variables());

        // 3. 儲存狀態
        templateRepositoryPort.save(template);
    }

    @Override
    @Transactional
    public void publishTemplate(PublishTemplateCommand command) {
        // 1. 透過 Port 取得聚合根
        Template template = templateRepositoryPort.findById(TemplateId.of(command.templateId()))
                .orElseThrow(() -> new TemplateNotFoundException(command.templateId()));

        // 2. 執行領域邏輯
        template.publishVersion(command.version());

        // 3. 儲存狀態
        templateRepositoryPort.save(template);

        // 4. 發佈領域事件
        String publishedVersion = command.version().replace("-DRAFT", "");
        template.getVersions().stream()
                .filter(v -> v.getVersion().equals(publishedVersion))
                .findFirst()
                .ifPresent(v -> {
                    TemplatePublishedEvent event = new TemplatePublishedEvent(
                            template.getTemplateCode(),
                            template.getTemplateType().name(),
                            v.getContentDefinition()
                    );
                    templateMessagePublisherPort.publishTemplatePublishedEvent(event);
                });
    }
}