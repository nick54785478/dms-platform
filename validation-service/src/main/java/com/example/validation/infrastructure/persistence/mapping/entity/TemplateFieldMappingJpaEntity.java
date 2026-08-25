package com.example.validation.infrastructure.persistence.mapping.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "template_field_mapping")
public class TemplateFieldMappingJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_code", nullable = false)
    private String templateCode;

    @Column(name = "template_sheet_name")
    private String templateSheetName;

    @Column(name = "header_name", nullable = false)
    private String headerName;

    @Column(name = "mapping_field_name", nullable = false)
    private String mappingFieldName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getTemplateSheetName() {
        return templateSheetName;
    }

    public void setTemplateSheetName(String templateSheetName) {
        this.templateSheetName = templateSheetName;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public String getMappingFieldName() {
        return mappingFieldName;
    }

    public void setMappingFieldName(String mappingFieldName) {
        this.mappingFieldName = mappingFieldName;
    }
}
