package com.example.validation.application.port.in;

import com.example.validation.application.shared.dto.TemplateFieldMappingSearchedResult;
import com.example.validation.application.shared.query.ListTemplateFieldMappingQuery;

import java.util.List;

public interface ListTemplateFieldMappingUseCase {
    List<TemplateFieldMappingSearchedResult> list(ListTemplateFieldMappingQuery query);
}
