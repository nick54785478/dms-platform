package com.dms.template.application.service;

import com.dms.template.application.assembler.TemplateDtoAssembler;
import com.dms.template.application.command.CreateTemplateCommand;
import com.dms.template.application.dto.TemplateGottenResult;
import com.dms.template.application.port.out.TemplateMessagePublisherPort;
import com.dms.template.application.port.out.TemplateRepositoryPort;
import com.dms.template.domain.template.aggregate.root.Template;
import com.dms.template.domain.template.event.TemplateCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CreateTemplateUseCaseTest {

    @Mock
    private TemplateRepositoryPort repositoryPort;

    @Mock
    private TemplateMessagePublisherPort messagePublisherPort;

    private TemplateApplicationService service;

    @BeforeEach
    void setUp() {
        TemplateDtoAssembler assembler = new TemplateDtoAssembler();
        service = new TemplateApplicationService(repositoryPort, messagePublisherPort, assembler);
    }

    @Test
    void shouldCreateTemplateSuccessfully() {
        // Arrange
        CreateTemplateCommand command = new CreateTemplateCommand("EXCEL", "TMPL_001", "Order Template", "Template for order document");

        // Act
        TemplateGottenResult result = service.createTemplate(command);

        // Assert
        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals("EXCEL", result.templateType());
        assertEquals("TMPL_001", result.templateCode());
        assertEquals("Order Template", result.name());
        assertEquals("Template for order document", result.description());

        // Verify Repository Interaction
        ArgumentCaptor<Template> templateCaptor = ArgumentCaptor.forClass(Template.class);
        verify(repositoryPort).save(templateCaptor.capture());
        Template savedTemplate = templateCaptor.getValue();
        assertEquals("EXCEL", savedTemplate.getTemplateType().name());
        assertEquals("TMPL_001", savedTemplate.getTemplateCode());

        // Verify Message Publisher Interaction
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagePublisherPort).publish(eventCaptor.capture());
        TemplateCreatedEvent event = (TemplateCreatedEvent) eventCaptor.getValue();
        assertEquals(savedTemplate.getId().getValue(), event.getTemplateId());
        assertEquals("EXCEL", event.getTemplateType());
        assertEquals("TMPL_001", event.getTemplateCode());
        assertEquals("Order Template", event.getName());
        
        // Verify Domain Events are cleared
        assertEquals(0, savedTemplate.getDomainEvents().size());
    }
}
