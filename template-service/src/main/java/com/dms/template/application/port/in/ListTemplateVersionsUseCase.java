package com.dms.template.application.port.in;

import com.dms.template.application.dto.PagedResult;
import com.dms.template.application.dto.TemplateVersionGottenResult;
import com.dms.template.application.query.ListTemplateVersionsQuery;

public interface ListTemplateVersionsUseCase {
    PagedResult<TemplateVersionGottenResult> listTemplateVersions(ListTemplateVersionsQuery query);
}
