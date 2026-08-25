package com.example.validation.application.port.out;

/**
 * Excel 驗證輸出埠 (Outbound Port)
 * 負責定義對外部基礎設施要求進行 Excel 資料驗證的合約。
 */
public interface ExcelValidatorPort {

	/**
	 * 驗證 Excel 資料內容是否符合設定的政策規則
	 * 
	 * @param code 範本代碼 (Template Code)
	 * @param fileContent 上傳檔案的二進位位元組陣列
	 */
	void validateExcelData(String code, byte[] fileContent);
}
