package com.dms.template.application.port.in;

import com.dms.template.application.command.CreateTemplateCommand;
import com.dms.template.application.dto.TemplateGottenResult;

/**
 * 建立範本 UseCase (Inbound Port)
 * <p>
 * 負責處理建立新的 {@link com.dms.template.domain.template.aggregate.root.Template} 的應用邏輯。
 * </p>
 */
public interface CreateTemplateUseCase {
    
    /**
     * 執行建立範本作業
     *
     * @param command 包含建立範本所需資訊的 Command 物件
     * @return 建立成功後，回傳包含範本詳細資訊的 {@link TemplateGottenResult} DTO
     */
    TemplateGottenResult createTemplate(CreateTemplateCommand command);
}
