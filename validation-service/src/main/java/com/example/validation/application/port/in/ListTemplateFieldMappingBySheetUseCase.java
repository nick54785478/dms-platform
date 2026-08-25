package com.example.validation.application.port.in;

import com.example.validation.application.shared.dto.TemplateFieldMappingSearchedResult;
import com.example.validation.application.shared.query.ListTemplateFieldMappingBySheetQuery;

import java.util.List;

public interface ListTemplateFieldMappingBySheetUseCase {
    List<TemplateFieldMappingSearchedResult> list(ListTemplateFieldMappingBySheetQuery query);
}
