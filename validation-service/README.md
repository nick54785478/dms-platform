# Validation Service (validation-service)

`validation-service` 是一個專為 DMS (文件管理系統) 設計的**配置化動態檢核引擎**。它負責在文件或範本 (如 Excel) 上傳時，依據事先定義好的規則動態地執行資料驗證。

## 核心功能特色 (Functional Highlights)

本服務跳脫了傳統將驗證邏輯寫死在 Java 程式碼內的作法，改採用高度彈性的架構設計，具備以下優勢：

### 1. 高度彈性的「動態規則引擎」(SpEL)
- **資料庫驅動 (Database-driven)**：驗證規則集中存放於資料庫的 `ValidationPolicy` 表中。這意味著未來新增或修改範本的檢核邏輯時，完全不需重新編譯或部署程式碼。
- **SpEL 核心**：利用 Spring Expression Language (SpEL) 來做為判斷核心，能輕易執行如 `#checkedValue > #minValue` 這樣的複雜邏輯。甚至連錯誤訊息 (`errorMessage`) 也能透過 SpEL 進行動態渲染 (自動代入錯誤數值或發生位置)。

### 2. 支援複雜的「變數定義」與「跨行/表驗證」
- **層級化驗證**：區分了 `ROW` (單行資料檢核) 與 `SHEET` (整表/跨行資料檢核) 兩種 Policy 類型。
- **動態變數 (VARIABLE)**：支援設定 `rule = "VARIABLE"`，允許先將 Excel 的部分資料透過表達式運算後(例如：匯總特定條件的 ID 成為一個 Set)，儲存到 `ContextRoot` 變成「全域變數」(如 `#mySet`)。後續的 `ROW` 驗證就能直接調用此變數進行關聯性檢查。

### 3. 可擴展的客製化方法庫 (Custom Function Registration)
- `CustomValidator` 實作了自動註冊機制 (`methodRegistration`)，會將 `ValidationUtil` 與 `VariableUtil` 內定義的所有靜態方法自動註冊進 SpEL 的執行上下文中。
- 開發人員可以將高度複雜的業務驗證邏輯 (例如：身分證字號驗證邏輯、跨微服務的 API 遠端查核) 封裝成 Java Util 方法，並在資料庫裡的 SpEL 字串中輕鬆呼叫，例如：`#isTaiwanId(#checkedValue)`。

### 4. 精確的錯誤定位 (Excel Address Mapping)
- 內建 `ExcelAddressParser`，能將資料結構中的抽象索引精確還原成使用者熟悉的 Excel 儲存格座標 (例如：`B5`, `C12`)。
- 在檔案匯入失敗時，能為使用者產出極具可讀性與指向性的錯誤報告。

---

> **注意：系統架構待重構**
> 目前專案雖然已具備優異的功能與業務邏輯設計，但在程式碼目錄與依賴關係上 (如 Domain Layer 的缺失、技術物件與 Port 的耦合) 尚未完全對齊工作區的 `AGENTS.md` (DDD 與 Clean Architecture 六角形架構) 規範。這部分的架構對齊將作為後續重構的重點。
