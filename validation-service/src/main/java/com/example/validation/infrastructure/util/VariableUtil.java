package com.example.validation.infrastructure.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 客製驗證 Variable 工具類
 * 主要用於全域變數 (Variable) 的建立
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VariableUtil {

	/**
	 * 根據 Excel Sheet 資料與指定欄位名稱建立 Set 集合 (去除重複值並保持順序)
	 * 
	 * @param sheet            Excel 資料，格式為 List<Map<String, String>>
	 * @param mappingFieldName 欲提取資料的欄位名稱
	 * @return 包含該欄位所有不重複值的 LinkedHashSet
	 */
	public static Set<String> toSet(List<Map<String, String>> sheet, String mappingFieldName) {
		return sheet.stream().map(row -> row.get(mappingFieldName))
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * 建立判斷用的 Set 集合 (先過濾出另一指定欄位的值為 "Y" 的資料，再建立 Set)
	 * 	
	 * @param sheet               Excel 資料，格式為 List<Map<String, String>>
	 * @param mappingFieldName    欲提取資料的欄位名稱
	 * @param adjMappingFieldName 作為過濾條件的欄位名稱 (必須為 "Y" 才納入)
	 * @return 過濾後該欄位所有不重複值的 LinkedHashSet
	 */
	public static Set<String> toAdjSet(List<Map<String, String>> sheet, String mappingFieldName,
			String adjMappingFieldName) {
		return sheet.stream().filter(row -> StringUtils.equals("Y", row.get(adjMappingFieldName)))
				.map(row -> row.get(mappingFieldName)).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * 根據 Excel Sheet 資料與指定欄位名稱建立 List 集合
	 *
	 * @param sheet            Excel 資料，格式為 List<Map<String, String>>
	 * @param mappingFieldName 欲提取資料的欄位名稱
	 * @return 包含該欄位所有值的 List (不去除重複)
	 */
	public static List<String> toList(List<Map<String, String>> sheet, String mappingFieldName) {
		return sheet.stream().map(row -> row.get(mappingFieldName)).collect(Collectors.toList());
	}

	/**
	 * 計算兩個 Set 集合的差集 (Difference)
	 * 回傳存在於 s1 但不存在於 s2 的元素集合
	 *
	 * @param s1 基準 Set 集合
	 * @param s2 欲排除的 Set 集合
	 * @param <T> 元素型別
	 * @return 差集 (s1 - s2)
	 */
	public static <T> Set<T> diffSet(Set<T> s1, Set<T> s2) {
		Set<T> diff = new LinkedHashSet<>(s1);
		diff.removeAll(s2);
		return diff;
	}

	/**
	 * 計算兩個 Set 集合的交集 (Intersection)
	 * 回傳同時存在於 s1 與 s2 的元素集合
	 *
	 * @param s1 第一個 Set 集合
	 * @param s2 第二個 Set 集合
	 * @param <T> 元素型別
	 * @return 交集
	 */
	public static <T> Set<T> intersectionSet(Set<T> s1, Set<T> s2) {
		Set<T> intersection = new LinkedHashSet<>(s1);
		intersection.retainAll(s2);
		return intersection;
	}

	/**
	 * 將 Excel Sheet 資料轉換為 Map 結構
	 * 以指定的欄位作為 Key，另一個指定的欄位作為 Value
	 * 若有重複的 Key，後面的資料會覆蓋前面的資料
	 *
	 * @param sheet Excel 資料，格式為 List<Map<String, String>>
	 * @param key   作為 Map Key 的欄位名稱
	 * @param value 作為 Map Value 的欄位名稱
	 * @return 轉換後的 Map 集合
	 */
	public static Map<Object, Object> toMap(List<Map<String, String>> sheet, String key, String value) {
		return sheet.stream()
				.filter(row -> row.get(key) != null) // 防呆，避免 key 為 null
				.collect(Collectors.toMap(
						row -> row.get(key),
						row -> row.get(value) != null ? row.get(value) : "", // value 若為 null 替換為空字串
						(existing, replacement) -> replacement, // 發生 key 衝突時，使用新值覆蓋舊值
						LinkedHashMap::new // 保持原本順序
				));
	}
}
