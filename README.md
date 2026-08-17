# DMS Platform (Document Management System)

一個基於微服務架構建立的現代化、分散式文件管理系統。提供強大的文件生命週期管理、透過分片上傳 (Multipart Upload) 處理超大檔案，並採用事件驅動架構 (Event-Driven Architecture) 來確保資料一致性與系統解耦。

## 核心特色

- **文件管理**：建立、更新與管理文件的中繼資料。
- **版本控制**：內建歷史版本支援 (主版本 / 次版本控制)。
- **分片上傳 (Multipart Upload)**：針對大檔案進行最佳化，允許透過預先簽名網址 (Presigned URLs) 直接將檔案分塊上傳至底層儲存引擎 (S3/MinIO)。
- **快速搜尋與過濾**：具備防抖 (Debounce) 機制、支援部分匹配的標題搜尋與狀態過濾。
- **事件驅動的檔案綁定**：使用 Outbox Pattern 與 Kafka 非同步地將上傳的檔案綁定至業務實體，並安全地處理軟刪除機制。
- **現代化 UI**：採用 Angular 與 PrimeNG 打造優雅、響應式的前端介面。

## 系統架構

本系統基於現代後端架構原則所建構，以確保高可維護性、可擴展性與鬆耦合。

### 領域驅動設計 (Domain-Driven Design, DDD)
- **充血模型 (Rich Domain Model)**：核心業務邏輯封裝於 Entity、Value Object 與 Aggregate Root (例如：`Document`、`FileMetadata`) 之中，避免邏輯散落於 Service 中。
- **純粹領域層 (Pure Domain)**：`domain` 層**零依賴**任何外部框架 (如 Spring、JPA、HTTP)，與技術細節完全解耦。
- **垂直切片架構 (Vertical Slice Architecture)**：依據 Aggregate (例如：`domain/document`) 組織套件，而非傳統的技術分層 (`models`、`services`)，讓相關邏輯高度內聚。

### 六角形架構 (Hexagonal Architecture / Ports and Adapters)
- **關注點分離**：Application 層擔任協調者角色，定義業務案例 (Inbound Ports) 與所需的基礎設施介面 (Outbound Ports)。
- **Inbound Adapters (入站適配器)**：位於 Presentation 層的 Controllers 與 Event Consumers 負責接收外部請求，並將其委派給 Application Services。
- **Outbound Adapters (出站適配器)**：位於 Infrastructure 層的 Repositories 與 Event Publishers 實作 Outbound Ports，負責與資料庫或訊息代理 (Message Broker) 進行互動。

### 事件驅動架構 (EDA) 與 Outbox Pattern
- **鬆耦合**：微服務之間透過 **Kafka** 事件進行非同步通訊，而非同步 HTTP 呼叫，避免分散式單體 (Distributed Monolith) 反模式。
- **Transactional Outbox Pattern**：為了防止本地資料庫與 Kafka 訊息發佈之間的資料不一致，領域事件會與業務資料在同一個 Transaction 內寫入 `outbox_messages` 資料表。隨後由背景排程器可靠地將這些訊息轉發至 Kafka，確保**至少一次 (At-Least-Once)** 的傳遞保證。
- **事件消費者 (Event Consumers)**：`file-service` 作為事件消費者，訂閱如 `FileBoundEvent` 與 `AttachmentDeletedEvent` 等事件，在背景安全地執行實體檔案的綁定與刪除操作。

## 專案結構與技術堆疊

此儲存庫為一個 Mono-repository，包含以下核心模組：

### 1. `dms-frontend` (Angular 17+)
- 現代化的 Web 介面，使用 **Angular** 與 **PrimeNG** 建構。
- 實作防抖 (Debounce) 邏輯，實現高效的 API 查詢。
- 在瀏覽器端完美處理分片上傳 (Multipart Upload) 的切片邏輯。

### 2. `dms-service` (Spring Boot 3 + Java 21)
- 負責管理文件、歷史記錄與中繼資料的核心業務領域服務。
- 使用 **領域驅動設計 (DDD)** 與 **六角形架構 (Ports and Adapters)** 建構。
- 實作 **Outbox Pattern** 來可靠地發佈領域事件。

### 3. `file-service` (Spring Boot 3 + Java 21)
- 專門處理實體檔案生命週期管理的微服務。
- 整合 **MinIO (S3 相容)** 作為物件儲存空間。
- 暴露 `Initiate`、`Presigned-Part` 與 `Complete` API，以實現分片上傳，避免大檔案串流耗盡後端記憶體。
- 訂閱 Kafka 事件以完成實體檔案綁定與刪除作業。

### 4. `docker-compose` (基礎設施)
完全容器化的本地開發環境，包含：
- **PostgreSQL 15**：作為 `dms-service` 與 `file-service` 的關聯式資料庫。
- **Redis 7**：用於分散式快取與鎖 (Distributed Locking)。
- **Kafka (KRaft)**：作為微服務間通訊的高吞吐量事件匯流排。
- **Kafka-UI**：用於監控 Kafka Topics 的網頁介面。
- **MinIO**：S3 相容的物件儲存服務。

## 快速開始

### 必備工具
- [Docker & Docker Compose](https://www.docker.com/)
- [Java 21](https://jdk.java.net/21/)
- [Maven](https://maven.apache.org/)
- [Node.js 18+ & npm](https://nodejs.org/)
- [Angular CLI](https://angular.io/cli)

### 1. 啟動基礎設施
導航至 `docker-compose` 目錄並啟動所需的資料庫與訊息代理：
```bash
cd docker-compose
docker-compose up -d
```
*提示：MinIO 控制台可於 `http://localhost:9001` 存取 (帳密：admin / admin123)。*
*提示：Kafka-UI 可於 `http://localhost:8080` 存取。*

### 2. 啟動 File Service
開啟一個新的終端機並啟動 File Service：
```bash
cd file-service
./mvnw spring-boot:run
```
*(運行於 Port 8081)*

### 3. 啟動 DMS Core Service
開啟另一個新的終端機並啟動 DMS Service：
```bash
cd dms-service
./mvnw spring-boot:run
```
*(運行於 Port 8082)*

### 4. 啟動 Frontend 前端應用
最後，啟動 Angular 前端應用：
```bash
cd dms-frontend
npm install
npm run start
```
*(運行於 Port 4200)*

在瀏覽器中前往 `http://localhost:4200` 即可使用 DMS Platform！

## 測試分片上傳 (Multipart Upload)
如果您想在沒有 UI 的情況下測試分片上傳機制，可以執行位於根目錄的 Node.js 測試腳本：
```bash
node test-multipart.js
```
此腳本會產生一個 12MB 的假檔案，並針對 File Service 模擬完整的三階段切片上傳流程。

## 架構開發規範
此專案嚴格遵守我們自定義的架構規範 (AGENTS.md)：
- **純粹領域 (Pure Domain)**：`domain/` 層對於 Spring 或任何技術框架具有零依賴。
- **垂直切片 (Vertical Slicing)**：依據 Aggregate 而非技術分層來組織架構。
- **嚴格的命名規範**：強制執行 `UseCase`、`Port`、`Adapter`、`Resource` 與 `Command/Query` 的命名規則，以維持無所不在的語言 (Ubiquitous Language)。
