# Validation Service - SpEL 可用函式手冊

在設定 `ValidationPolicy` 的規則時，您可以直接在 SpEL (Spring Expression Language) 中呼叫以下內建的客製化方法。這些方法主要分為兩大類：**欄位驗證 (ValidationUtil)** 與 **全域變數處理 (VariableUtil)**。

系統在初始化 SpEL 執行環境時，已經將 `sheet` (當前的資料表) 等環境變數準備好，您可以直接傳入使用。

---

## 1. 欄位驗證方法 (ValidationUtil)
*主要用於驗證各個欄位 (Field) 的內容是否符合規範。*

### `validateDuplicate`
- **功能**：判斷該欄位的所有值是否在整張資料表中發生重複。
- **參數**：
  - `sheet` (List<Map<String, String>>)：整份資料表 (可直接帶入系統變數 `#sheet`)。
  - `mappingFieldName` (String)：欲檢查重複的欄位名稱。
- **回傳**：`Map<Integer, String>` (包含錯誤的列索引與對應錯誤訊息)
- **範例**：`#validateDuplicate(#sheet, 'empId')`

### `contains`
- **功能**：判斷該欄位的值，是否都存在於另一個指定好的「清單 (List)」之中。
- **參數**：
  - `checkList` (List<String>)：用來比對的合法清單 (通常是透過 `VariableUtil` 事先建立的變數)。
  - `sheet` (List<Map<String, String>>)：整份資料表。
  - `mappingFieldName` (String)：被檢查的欄位名稱。
- **回傳**：`Map<Integer, String>` (包含錯誤的列索引與對應錯誤訊息)
- **範例**：`#contains(#deptList, #sheet, 'deptCode')`

### `isNumeric`
- **功能**：判斷傳入的字串是否為數值 (包含正負數、整數與小數)。
- **參數**：`str` (String) 欲檢查的字串。
- **回傳**：`boolean` (若是數值回傳 true)
- **範例**：`#isNumeric(#checkedValue)`

### `isNotNumeric`
- **功能**：判斷傳入的字串是否 **不為** 數值。
- **參數**：`str` (String) 欲檢查的字串。
- **回傳**：`boolean` (若不是數值回傳 true)
- **範例**：`#isNotNumeric(#checkedValue)`

### `isNotNumericWithComma`
- **功能**：判斷傳入的字串在移除千分位逗號 (`,`) 後，是否 **不為** 數值。
- **參數**：`str` (String) 欲檢查的字串。
- **回傳**：`boolean` (若不是數值回傳 true)

### `isInteger`
- **功能**：判斷傳入的字串是否 **不為** 整數 (若無法被解析為整數則回傳 true)。
- **參數**：`data` (String) 欲檢查的字串。
- **回傳**：`boolean` (注意：無法被解析時回傳 true)

---

## 2. 全域變數處理方法 (VariableUtil)
*主要用於在執行 `SHEET` 級別的 `VARIABLE` 規則時，幫助您快速從資料中萃取並建立全域變數集合。*

### `toSet`
- **功能**：根據 Excel Sheet 資料與指定欄位名稱建立 Set 集合 (自動去除重複值並保持原本順序)。
- **參數**：
  - `sheet` (List<Map<String, String>>)：整份資料表。
  - `mappingFieldName` (String)：欲萃取的欄位名稱。
- **回傳**：`Set<String>`

### `toAdjSet`
- **功能**：建立判斷用的 Set 集合。會先過濾出另一個「指定欄位值為 "Y"」的資料列，再把目標欄位的值收集成 Set。
- **參數**：
  - `sheet`：整份資料表。
  - `mappingFieldName`：欲萃取的目標欄位。
  - `adjMappingFieldName`：作為過濾條件的欄位名稱 (該欄位值必須為 "Y" 才納入)。
- **回傳**：`Set<String>`

### `toList`
- **功能**：根據指定欄位名稱萃取出所有值並建立為 List 集合 (不去除重複)。
- **參數**：
  - `sheet`：整份資料表。
  - `mappingFieldName`：欲萃取的欄位名稱。
- **回傳**：`List<String>`

### `diffSet`
- **功能**：計算兩個 Set 集合的差集 (`s1 - s2`)，也就是存在於 `s1` 但不存在於 `s2` 的元素。
- **參數**：
  - `s1` (Set<T>)：第一個集合 (基準)。
  - `s2` (Set<T>)：第二個集合 (要排除的)。
- **回傳**：`Set<T>`

### `intersectionSet`
- **功能**：計算兩個 Set 集合的交集 (同時存在於 `s1` 與 `s2` 的元素)。
- **參數**：
  - `s1` (Set<T>)：第一個集合。
  - `s2` (Set<T>)：第二個集合。
- **回傳**：`Set<T>`

### `toMap`
- **功能**：將資料表轉化為 Map，以一個欄位作為 Key，另一個欄位作為 Value。(若有重複 Key，則保留最後一筆)。
- **參數**：
  - `sheet`：整份資料表。
  - `key` (String)：欲作為 Map Key 的欄位名稱。
  - `value` (String)：欲作為 Map Value 的欄位名稱。
- **回傳**：`Map<Object, Object>`
- **範例**：`#toMap(#sheet, 'empId', 'deptCode')`
