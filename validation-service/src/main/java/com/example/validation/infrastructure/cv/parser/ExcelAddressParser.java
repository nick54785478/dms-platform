package com.example.validation.infrastructure.cv.parser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Excel 欄位位址轉換解析器 (ExcelAddressParser)
 * <p>
 * 用於在驗證邏輯發生錯誤時，將錯誤的內部索引與列數，反推轉換為人類可讀的 Excel 絕對位址
 * (例如：將第 1 列、第 1 欄轉換為 "A1")。這對於將客製化驗證 (CustomValidator) 所產生的
 * 錯誤訊息精確映射回使用者的 Excel 表格上至關重要。
 * </p>
 */
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExcelAddressParser {

	/**
	 * 將指定的 Excel 行號與欄號，轉換為標準的 Excel 儲存格位址字串 (例如："A1", "B2", "AA10")。
	 * 
	 * @param row Excel 實際資料列號 (1-based，從 1 開始計算)
	 * @param col Excel 實際資料欄號 (1-based，如 A=1, B=2)
	 * @return 轉換後的 Excel 儲存格位址 (例: "B2", "C3")。若傳入值無效，則回傳 "Invalid Cell"
	 */
	public static String convertNumToAddress(Integer row, Integer col) {
		if (row == null || col == null || row < 1 || col < 1) {
			return "Invalid Cell"; // 避免 null 或非法數值
		}
		return convertColumnToLetter(col) + row;
	}

	/**
	 * 將數值型的 Excel 欄號轉換為英文字母表示法 (例如: 1 -> "A", 2 -> "B", 27 -> "AA")。
	 * 內部使用 26 進位制的邏輯進行推算。
	 * 
	 * @param col 欄號 (1-based，從 1 開始計算)
	 * @return Excel 欄位英文字母表示 (如 "A", "B", "AA", "AB")
	 */
	private static String convertColumnToLetter(int col) {
		StringBuilder columnName = new StringBuilder();
		while (col > 0) {
			col--; // 調整索引 (因 Excel 的英文字母邏輯為 1-based，模除計算需轉為 0-based)
			columnName.insert(0, (char) ('A' + (col % 26)));
			col /= 26;
		}
		return columnName.toString();
	}

}
