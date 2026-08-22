# Document Management Service (dms-service)

`dms-service` 是 DMS (Document Management System) 的核心文件管理微服務，負責處理所有與文件 (Document) 本身相關的生命週期與商業邏輯，包含建立、修改、查詢以及版本控制等。

## 系統架構 (Architecture)

本服務嚴格遵循 **Domain-Driven Design (DDD, 領域驅動設計)** 與 **Clean Architecture (六角形架構)** 規範進行開發，並應用了 **CQRS (命令查詢職責分離)** 以及 **Outbox Pattern** 確保分散式系統中的事件發布一致性。

### 目錄結構與分層 (Package Structure)

- **`domain` (領域層)**：核心業務邏輯的所在處，包含 Aggregate Root (`Document`)、Entities (`DocumentVersion`)、Value Objects (`DocumentId`, `DocumentStatus`) 以及 Domain Events。本層不依賴任何外部框架 (如 Spring 或 JPA)，保持純 Java 的乾淨實作。
- **`application` (應用層)**：負責編排系統的使用案例 (Use Cases)，透過 Inbound Ports 接收外部指令。
  - `port/in`：Inbound Ports (Use Cases)，定義對外開放的業務功能 (e.g., `CreateDocumentUseCase`)。
  - `port/out`：Outbound Ports，定義應用層對外部設施的需求 (e.g., `DocumentRepositoryPort`, `MessagePublisherPort`)。
  - `service`：實作 Inbound Ports 的 Application Service，進行流程的協調並操作 Domain Aggregate。
  - CQRS 物件：`command`、`query` 以及回應的 `dto` 封裝。
- **`infrastructure` (基礎設施層)**：實作 Outbound Ports 的 Adapter，負責與真實的資料庫、Message Broker 等基礎設施互動。
  - `adapter`：Outbound Adapters，包含 `DocumentRepositoryAdapter`、`KafkaMessagePublisherAdapter` 以及 `EventOutboxAdapter`。
  - `persistence`：Spring Data JPA 的 Entity 與 Repository 定義。
- **`presentation` (表現層)**：系統的進入點。
  - `rest`：包含 Spring Web MVC 的 Controller，對外提供 RESTful API。
  - `resource`：與外部互動的 DTO 載體 (Request/Response payload)。
  - `assembler`：負責在 Resource 與 Command/Query/DTO 之間進行防腐與轉換。
- **`config` (配置層)**：Spring Bean 與環境相關的設定檔 (e.g., `CorsConfig`, `JacksonConfig`)。

## 核心設計與設計模式 (Core Design & Patterns)

1. **DDD 聚合根 (Aggregate Root)**
   `Document` 作為文件領域的聚合根，控制對 `DocumentVersion` 等內部狀態的改變，保護業務規則。

2. **Transaction Outbox Pattern**
   利用 `EventOutboxAdapter` 將 Domain Events (如 `AttachmentDeletedEvent`, `FileBoundEvent`) 與主要業務邏輯 (儲存 Document) 包裝在同一個資料庫交易中寫入 `DomainEventOutboxJpaEntity`。
   再透過獨立的 `OutboxMessageRelay` 排程器定期掃描 Outbox，將事件發佈到 Kafka 中，保證事件的「至少一次 (At-Least-Once) 發佈」。

3. **防腐層與 Assembler**
   Controller 收到的 `Resource` (例如 `CreateDocumentResource`) 不會直接傳入 `application` 層。所有資料皆透過 `DocumentResourceAssembler` 轉換成獨立的 `Command` 物件後再交給 UseCase 處理。

## 開發指南

- 請遵守工作區內 `.agents/AGENTS.md` 所定義的命名與分層規範。
- 新增功能時，請先從 `domain` 或 `application` 介面切入，再向外實作 Controller 與 Adapter。
- 所有非領域層的方法調用，須透過 Port 介面進行，確保核心業務不受技術變更影響。
