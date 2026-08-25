package com.example.validation.infrastructure.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 客製驗證 Validation 工具類
 * 用於驗證各個欄位 (Field) 的內容
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationUtil {

	/**
	 * 判斷該欄位的值是否重複，回傳包含錯誤訊息的 Map<data rowIndex, errorMessage>
	 *
	 * @param sheet            Excel 資料，List<Map<String, String>> 格式
	 * @param mappingFieldName 欲檢查重複的欄位名稱
	 * @return Map<data rowIndex, errorMessage>
	 */
	public static Map<Integer, String> validateDuplicate(List<Map<String, String>> sheet, String mappingFieldName) {
		Map<Integer, String> errorMap = new LinkedHashMap<>(); // 保持輸入順序
		Map<String, Integer> seenValues = new HashMap<>(); // 紀錄首次出現的值及其 rowIndex
		Set<String> recordedDuplicates = new HashSet<>(); // 紀錄已經加入 errorMap 的值

		for (int rowIndex = 0; rowIndex < sheet.size(); rowIndex++) {
			Map<String, String> row = sheet.get(rowIndex);
			String value = row.get(mappingFieldName);
			if (value != null) {
				if (seenValues.containsKey(value)) {
					// 第一次發現重複，將首次出現的索引也加入錯誤清單
					if (!recordedDuplicates.contains(value)) {
						int firstIndex = seenValues.get(value);
						errorMap.put(firstIndex + 1, String.format("欄位 %s 的值 '%s' 重複", mappingFieldName, value));
						recordedDuplicates.add(value); // 確保首筆數據只被記錄一次
					}
					// 當前索引也加入錯誤清單
					errorMap.put(rowIndex + 1, String.format("欄位 %s 的值 '%s' 重複", mappingFieldName, value));
				} else {
					// 記錄該值首次出現的索引
					seenValues.put(value, rowIndex);
				}
			}
		}
		return errorMap;
	}

	/**
	 * 判斷該欄位對應值是否存於指定清單中
	 * 
	 * @param checkList        檢查用清單(可使用 VARIABLE 定義)
	 * @param sheet            Excel 資料，List<Map<String, String>> 格式
	 * @param mappingFieldName 被檢查的欄位
	 * @return Map<data rowIndex, errorMessage>
	 */
	public static Map<Integer, String> contains(List<String> checkList, List<Map<String, String>> sheet,
			String mappingFieldName) {
		Map<Integer, String> result = new LinkedHashMap<>(); // 保持順序
		Set<String> checkSet = new HashSet<>(checkList); // 轉為 Set 加速查詢

		for (int rowIndex = 0; rowIndex < sheet.size(); rowIndex++) {
			Map<String, String> row = sheet.get(rowIndex);
			String value = row.get(mappingFieldName);

			if (value != null && !checkSet.contains(value)) {
				// 只存入 "不存在" 的值
				result.put(rowIndex + 1, String.format("欄位 %s 的值 '%s' 不存在", mappingFieldName, value));
			}
		}
		return result;
	}

	/**
	 * 判斷傳入的字串是否為數值 (包含正負數、整數與小數)
	 * 
	 * @param str 欲檢查的字串
	 * @return 若為數值則回傳 true，否則回傳 false
	 */
	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("^-?[\\d]+(\\.[\\d]+)?$");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

	/**
	 * 判斷傳入的字串是否「不為」數值
	 * 
	 * @param str 欲檢查的字串
	 * @return 若不為數值則回傳 true，否則回傳 false
	 */
	public static boolean isNotNumeric(String str) {
		return !isNumeric(str);
	}

	/**
	 * 判斷傳入的字串在移除逗號(,)後，是否「不為」數值
	 * 適用於檢查包含千分位符號的數值字串
	 * 
	 * @param str 欲檢查的字串 (可能包含逗號)
	 * @return 若移除逗號後不為數值則回傳 true，否則回傳 false
	 */
	public static boolean isNotNumericWithComma(String str) {
		return !isNumeric(str.replace(",", ""));
	}

	/**
	 * 判斷傳入的字串是否「不為」整數
	 * 注意：此方法在字串可成功轉型為 Integer 時回傳 false，發生例外時回傳 true
	 * 
	 * @param data 欲檢查的字串
	 * @return 若無法解析為整數則回傳 true，若可成功解析則回傳 false
	 */
	public static boolean isInteger(String data) {
		try {
			Integer.parseInt(data);
			return false;
		} catch (NumberFormatException e) {
			return true;
		}
	}
}
