package com.example.validation.application.port.in;

/**
 * 刪除範本欄位對應 UseCase (Inbound Port)
 * <p>
 * 負責處理刪除指定的 {@link com.example.validation.domain.mapping.aggregate.root.TemplateFieldMapping} 的應用邏輯。
 * </p>
 */
public interface DeleteTemplateFieldMappingUseCase {
    
    /**
     * 執行刪除範本欄位對應
     *
     * @param id 欲刪除的範本欄位對應 ID
     */
    void delete(Long id);
}
