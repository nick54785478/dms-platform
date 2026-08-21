package com.dms.template.application.port.in;

import com.dms.template.application.dto.TemplateGottenResult;
import com.dms.template.application.query.GetTemplateQuery;

public interface GetTemplateUseCase {
    TemplateGottenResult getTemplate(GetTemplateQuery query);
}
