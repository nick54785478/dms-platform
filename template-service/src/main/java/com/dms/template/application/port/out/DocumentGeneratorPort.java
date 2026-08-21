package com.dms.template.application.port.out;

import com.dms.template.application.dto.DocumentGeneratedResult;
import com.dms.template.domain.template.aggregate.root.Template;

import java.util.Map;

/**
 * 負責產生文件的 Outbound Port 介面 (Hexagonal Architecture).
 * 定義了根據範本與變數資料來產生實際文件（例如 PDF 或 Excel）的契約。
 * 實作類別應放置於 Infrastructure Layer 的 Adapter 中。
 */
public interface DocumentGeneratorPort {

    /**
     * 檢查此產生器是否支援特定的範本類型。
     *
     * @param type 範本類型 (例如 EXCEL, PDF)
     * @return 若支援該類型則回傳 true，否則回傳 false
     */
    boolean supports(com.dms.template.domain.template.aggregate.vo.TemplateType type);

    /**
     * 根據給定的範本結構與填寫的變數資料，產生最終的二進位文件。
     *
     * @param template 範本領域物件 (包含最新的版本設定與定義)
     * @param data     前端填寫或系統帶入的變數資料 (Key 為欄位/變數名稱，Value 為值)
     * @return 包含文件位元組與中繼資料 (檔名、ContentType) 的產生結果
     */
    DocumentGeneratedResult generate(Template template, Map<String, Object> data);
}
