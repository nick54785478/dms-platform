# 系統需求規格書 (SRS)：檔案微服務架構 (Phase 1)

本文件定義了基於領域驅動設計 (DDD) 與微服務架構下的檔案服務 (FileService) 系統需求與架構設計。目標是建立高可用、職責單一且具備防禦性設計的底層基礎設施。

## 1. 架構總覽與設計原則

### 1.1 核心設計原則
* **業務無知 (Business Agnostic)：** FileService 作為純粹的基礎設施，僅負責二進位檔案 (Blob) 的存取與傳輸，絕對不牽涉業務邏輯與關聯狀態。
* **職責反轉與解耦：** 附件的權限校驗、關聯綁定由各業務微服務自行負責。跨服務的狀態同步採用非同步事件驅動 (Event-Driven) 確保最終一致性。

## 2. FileService (檔案微服務) 核心規格
定位為 Infrastructure Layer，提供全公司微服務統一的檔案存儲介面。

### 2.1 核心功能需求
* **檔案上傳與下載：** 支援單檔、多檔上傳。提供基於檔案 ID 的下載與預覽串流。
* **分片上傳 (Multipart Upload)：** 深度整合 MinIO/S3 協議，支援大檔案的初始化、分片上傳、斷點續傳與最終合併。
* **Pre-signed URL 支援：** 支援簽發具有時效性的上傳與下載網址，讓前端直接與底層儲存 (如 MinIO/S3) 互動，卸載 FileService 的網路頻寬壓力。
* **中介資料管理 (Metadata)：** 儲存檔案的 MD5/SHA-256 (支援去重機制)、MIME Type、檔案大小等技術性資料。

### 2.2 附件與權限處理策略
* **無業務綁定：** FileService 資料庫 Schema 絕不包含 `user_id`、`order_id` 等業務欄位。
* **權限代理：** FileService 不執行業務權限驗證。前端須先向業務微服務取得授權憑證或 Pre-signed URL 後，才能存取實體檔案。

## 3. 分散式垃圾回收與資源清理機制 (Garbage Collection)
為處理業務綁定失敗產生的「孤兒檔案」以及「未完成的上傳碎片」，系統實作自動清理機制。

### 3.1 孤兒檔案清理 (Orphan Files)
採用 **雲端生命週期管理 (或雙 Bucket 架構)**：
1. 所有新上傳檔案預設進入 Temp Bucket (暫存區)。
2. 業務微服務完成資料庫寫入後，發送非同步事件。
3. FileService 收到事件，將實體檔案搬移至 Permanent Bucket (永久區)，並更新狀態為 `BOUND`。
4. 利用 MinIO/S3 原生的 Lifecycle Policy，設定 Temp Bucket 中的物件若超過設定時間（如 24 小時）未被搬移，則自動永久刪除。

### 3.2 分片上傳碎片清理
* **Abort Incomplete Multipart Uploads：** 於 MinIO/S3 設定 Bucket 規則，針對啟動超過 24 小時但未發送 Complete 合併請求的 Multipart Upload 任務，由底層儲存引擎自動終止並釋放佔用的磁碟空間。

## 4. 跨微服務事件整合 (Event-Driven Architecture)
狀態同步一律透過 Message Queue (如 Apache Kafka 或 RabbitMQ) 進行，確保系統的鬆耦合與容錯性。

* **檔案生效事件 (FileBoundEvent)：** 業務服務發佈。通知 FileService 該檔案已被業務正式綁定，執行搬移至永久區的操作。
* **附件刪除事件 (AttachmentDeletedEvent)：** 業務服務刪除領域實體 (如刪除一筆合約) 後發佈。FileService 訂閱此事件，並在背景非同步執行實體檔案刪除 (Soft/Hard delete)。

## 5. 非功能性需求 (NFRs)
* **可用性與容錯：** 業務服務需採用 Outbox Pattern 確保資料庫的 Transaction 與 Message Queue 的事件發佈具備最終一致性，避免網路中斷導致事件遺失。
* **安全性：** 檔案上傳入口必須校驗 Magic Numbers (避免僅依賴副檔名被惡意偽裝)，並限制單一檔案大小上限與 API 呼叫頻率 (Rate Limiting)。

## 6. 後續擴充藍圖 (Future Extensions)
本章節記錄未來預計引入的高階應用層服務，目前處於架構規劃階段，不列入第一階段開發範圍。

### 6.1 ValidationService (驗證微服務)
為解決大型資料表（如 Excel/CSV）批次匯入時的效能瓶頸與業務解耦，預計將「檔案內容解析與規則檢核」抽離為獨立的 ValidationService。
* **定位：** Application/Domain Layer。接收檔案串流，載入驗證規則 (Schema) 進行解析，並輸出結構化報告。
* **串流解析防護：** 針對大型 Excel，捨棄 DOM 解析模式，強制採用基於事件的 SAX 模式 (Event API) 逐行讀取，將記憶體消耗維持在常數級別 (O(1))，避免 OOM。
* **非同步與事件整合：** 面對巨量資料，由前端提交非同步驗證任務，ValidationService 在背景解析，成功後發佈 `ExcelValidatedEvent` 觸發後續業務服務的批次寫入流程。
