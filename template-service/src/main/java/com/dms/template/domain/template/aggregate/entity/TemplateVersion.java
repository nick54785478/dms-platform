package com.dms.template.domain.template.aggregate.entity;

import com.dms.template.domain.template.aggregate.vo.TemplateStatus;
import com.dms.template.domain.template.aggregate.vo.TemplateVariable;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * 範本的具體版本 (Entity)
 */
@Getter
public class TemplateVersion {
    private String version;
    private String contentDefinition;
    private TemplateStatus status;
    private List<TemplateVariable> variables;

    private TemplateVersion(String version, String contentDefinition, TemplateStatus status, List<TemplateVariable> variables) {
        this.version = version;
        this.contentDefinition = contentDefinition;
        this.status = status;
        this.variables = variables;
    }

    public static TemplateVersion create(String version, String contentDefinition, List<TemplateVariable> variables) {
        return new TemplateVersion(version, contentDefinition, TemplateStatus.DRAFT, variables);
    }
    
    public static TemplateVersion reconstitute(String version, String contentDefinition, TemplateStatus status, List<TemplateVariable> variables) {
        return new TemplateVersion(version, contentDefinition, status, variables);
    }
    
    public void updateContent(String contentDefinition, List<TemplateVariable> variables) {
        if (this.status != TemplateStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT version can be updated");
        }
        this.contentDefinition = contentDefinition;
        this.variables = variables;
    }

    public void publish() {
        this.status = TemplateStatus.PUBLISHED;
        if (this.version.endsWith("-DRAFT")) {
            this.version = this.version.replace("-DRAFT", "");
        }
    }

    public void archive() {
        this.status = TemplateStatus.ARCHIVED;
    }

    public List<TemplateVariable> getVariables() {
        return Collections.unmodifiableList(variables);
    }
}
