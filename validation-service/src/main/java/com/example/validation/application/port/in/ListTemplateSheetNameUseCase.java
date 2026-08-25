package com.example.validation.application.port.in;

import com.example.validation.application.shared.query.ListTemplateSheetNameQuery;

import java.util.List;

public interface ListTemplateSheetNameUseCase {
    List<String> list(ListTemplateSheetNameQuery query);
}
