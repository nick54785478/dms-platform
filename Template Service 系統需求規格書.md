# 系統需求規格書 (SRS)：Template Service (範本微服務)

本文件定義了基於領域驅動設計 (DDD) 與微服務架構下的 Template Service 系統需求與架構設計，為 DMS 系統提供文件範本的管理與動態文件生成能力。

## 1. 架構總覽與設計原則

### 1.1 服務定位
Template Service 隸屬於 Application/Domain Layer 的微服務，主要職責為**「管理文件範本生命週期」**與**「提供動態文件渲染生成 (Document Generation) 能力」**。
它將依賴基礎設施層的 File Service 來進行實體二進位檔案的儲存與提取。

### 1.2 核心設計原則
* **單一職責 (Single Responsibility)：** 專注於範本變數解析與文件合成，不管控業務流程狀態。
* **高內聚低耦合：** 與 File Service 解耦，範本的二進位檔案儲存在 File Service，Template Service 僅保存 File ID 與範本的中介資料 (Metadata)。
* **無狀態渲染 (Stateless Rendering)：** 文件渲染過程為無狀態操作，可隨時進行水平擴展 (Horizontal Scaling) 以應付大量文件生成的 CPU 密集型任務。

## 2. 核心功能需求

### 2.1 範本管理 (Template Management)
* **範本 CRUD 與版本控制 (Versioning)：** 支援範本的建立、更新與刪除。每次更新可選擇建立新版本，確保歷史業務調用渲染時的穩定性。
* **狀態流轉：** 支援狀態機管理，例如：`DRAFT` (草稿) -> `PUBLISHED` (已發佈) -> `ARCHIVED` (已封存)。
* **變數解析 (Variable Extraction)：** 上傳範本時，系統能自動解析範本內定義的變數標籤 (如 `${userName}`, `{{orderDate}}`)，並返回給前端進行表單綁定配置。

### 2.2 文件生成 (Document Generation)
* **資料合併渲染 (Data Merging)：** 接收外部業務服務傳遞的 JSON 格式 Payload 與指定的範本 ID/Version，執行變數替換與格式化。
* **多格式支援：** 支援生成 Excel (xlsx), PDF 等常見格式。

### 2.3 PDF 區塊化範本設計 (Block-based PDF Template Architecture)
針對 PDF 範本的設計與渲染，系統採用**區塊化 (Block-based) 與 HTML 轉印**的架構，以支援高靈活度的動態排版與擴展性：
* **前端區塊化編輯 (Block-based Editing)：** 捨棄傳統上傳固定實體 PDF (AcroForm) 的方式，改為由前端拖拉元件庫（如 `TitleBlock`, `TextBlock`, `TableBlock`, `SignatureBlock` 等）組合彈性版面。
* **中介 JSON 儲存 (Draft JSON)：** 範本設計內容序列化為 JSON 格式儲存於資料庫，結構包含頁面設定 (`pageSettings`) 與元件區塊陣列 (`blocks`)。
* **後端 HTML to PDF 渲染引擎 (Java)：** 
  * 接收 JSON 後，後端依照各 Block 定義與實際業務資料，透過樣板引擎 (如 Thymeleaf/FreeMarker) 動態組裝對應的 HTML 標籤結構。
  * 最終透過轉換引擎 (如 `OpenHTMLToPDF` 或透過 Headless Browser) 將 HTML 結合 CSS 完美渲染為包含動態表格與分頁機制的 PDF，並輸出給使用者。

## 3. 領域模型設計 (Domain Model)

依據 DDD 規範進行切片：
* **Aggregate Root - `Template`：** 代表一個範本主體，包含範本代碼 (TemplateCode)、名稱與描述。
* **Entity - `TemplateVersion`：** 範本的具體版本，包含該版本對應的 File ID、狀態 (Draft/Published)、變數 Schema 定義檔。
* **Value Object - `TemplateVariable`：** 定義範本內各變數的型別與預設值。
* **Domain Event：** 
  * `TemplatePublishedEvent`
  * `TemplateVersionCreatedEvent`

## 4. 跨服務整合與相依性

### 4.1 與 File Service 整合
* **範本上傳：** 前端或業務端先將實體範本上傳至 File Service 取得 `file_id`，再呼叫 Template Service 建立範本資料。
* **生成結果儲存：** Template Service 渲染出新文件後，背景直接對接 File Service 上傳生成的實體檔案，並將最終的 `file_id` 回傳給呼叫端。

### 4.2 事件驅動整合 (Event-Driven)
採用 Message Queue 進行狀態同步與通知：
* **`DocumentGenerateRequestedEvent`：** 業務端發送非同步渲染請求。
* **`DocumentGeneratedEvent`：** 渲染完成，攜帶生成文件的 `file_id` 通知原業務端。

## 5. 非功能性需求 (NFRs)

* **效能與資源隔離 (Resource Isolation)：** 由於文件渲染 (如轉 PDF) 高度消耗 CPU 與 Memory，建議未來部署時給予 Template Service 獨立且較高的 CPU 資源配額。
* **容錯處理：** 渲染過程中若發生 JSON 屬性缺漏，需依據策略決定是報錯中斷 (Fail-Fast) 還是以空白/預設值填入 (Lenient)。
