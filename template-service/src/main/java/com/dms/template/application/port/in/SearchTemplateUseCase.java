package com.dms.template.application.port.in;

import com.dms.template.application.dto.PagedResult;
import com.dms.template.application.dto.TemplateSearchedResult;
import com.dms.template.application.query.SearchTemplateQuery;

public interface SearchTemplateUseCase {
    PagedResult<TemplateSearchedResult> searchTemplates(SearchTemplateQuery query);
}
