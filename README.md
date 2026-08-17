# DMS Platform (Document Management System)

A modern, distributed Document Management System built with a microservices architecture. It provides robust document lifecycle management, large file handling via multipart uploads, and event-driven architecture to ensure data consistency and decoupling.

## 🌟 Key Features

- **Document Management**: Create, update, and manage document metadata.
- **Version Control**: Built-in support for historical document versions (Major/Minor versioning).
- **Multipart Upload Support**: Optimized for large files, allowing chunked uploads directly to the storage engine (S3/MinIO) via presigned URLs.
- **Fast Search & Filtering**: Debounced, partial-matching search for documents with status filtering.
- **Event-Driven File Binding**: Uses the Outbox pattern and Kafka to asynchronously bind uploaded files to business entities and handle soft-deletions safely.
- **Modern UI**: An elegant, responsive Angular frontend powered by PrimeNG.

## 📐 System Architecture

This system is built upon modern backend architectural principles to ensure maintainability, scalability, and loose coupling.

### Domain-Driven Design (DDD)
- **Rich Domain Model**: Core business logic is encapsulated within Entities, Value Objects, and Aggregate Roots (e.g., `Document`, `FileMetadata`) rather than scattered in services.
- **Pure Domain**: The `domain` layer has **zero dependencies** on external frameworks (Spring, JPA, HTTP), remaining entirely decoupled from technical details.
- **Vertical Slice Architecture**: Packages are organized by Aggregates (e.g., `domain/document`) instead of generic technical layers (`models`, `services`), keeping related logic highly cohesive.

### Hexagonal Architecture (Ports and Adapters)
- **Separation of Concerns**: The Application Layer acts as the orchestrator, defining Use Cases (Inbound Ports) and required infrastructure (Outbound Ports).
- **Inbound Adapters**: Controllers and Event Consumers in the Presentation Layer receive external requests and delegate to Application Services.
- **Outbound Adapters**: Repositories and Event Publishers in the Infrastructure Layer implement the Outbound Ports to interact with databases and message brokers.

### Event-Driven Architecture (EDA) & Outbox Pattern
- **Loose Coupling**: Microservices communicate asynchronously via **Kafka** events rather than synchronous HTTP calls, avoiding distributed monolith anti-patterns.
- **Transactional Outbox Pattern**: To prevent data inconsistency between local DB commits and Kafka message publishing, domain events are first saved to an `outbox_messages` table within the same transaction. A background scheduler then relays these messages to Kafka reliably, ensuring **At-Least-Once** delivery.
- **Event Consumers**: The `file-service` acts as a consumer for events like `FileBoundEvent` and `AttachmentDeletedEvent` to execute physical file manipulations securely in the background.

## 🏗️ Architecture & Technologies

This repository is structured as a mono-repository containing the following core modules:

### 1. `dms-frontend` (Angular 17+)
- Modern web interface built with **Angular** and **PrimeNG**.
- Implements Debounce logic for efficient API querying.
- Handles chunking logic for Multipart Uploads seamlessly within the browser.

### 2. `dms-service` (Spring Boot 3 + Java 21)
- The core business domain service managing documents, histories, and metadata.
- Built using **Domain-Driven Design (DDD)** and **Hexagonal Architecture (Ports and Adapters)**.
- Implements the **Outbox Pattern** to publish domain events reliably.

### 3. `file-service` (Spring Boot 3 + Java 21)
- A dedicated microservice for physical file lifecycle management.
- Integrates with **MinIO (S3 Compatible)** for object storage.
- Exposes `Initiate`, `Presigned-Part`, and `Complete` APIs to facilitate Multipart Uploads without streaming large files through the backend memory.
- Subscribes to Kafka events to finalize file binding and physical deletion.

### 4. `docker-compose` (Infrastructure)
A fully containerized local environment including:
- **PostgreSQL 15**: Relational database for `dms-service` and `file-service`.
- **Redis 7**: Distributed caching and locking.
- **Kafka (KRaft)**: High-throughput event bus for microservice communication.
- **Kafka-UI**: Web interface for monitoring Kafka topics.
- **MinIO**: S3-compatible object storage.

## 🚀 Getting Started

### Prerequisites
- [Docker & Docker Compose](https://www.docker.com/)
- [Java 21](https://jdk.java.net/21/)
- [Maven](https://maven.apache.org/)
- [Node.js 18+ & npm](https://nodejs.org/)
- [Angular CLI](https://angular.io/cli)

### 1. Start the Infrastructure
Navigate to the `docker-compose` directory and spin up the required databases and message brokers:
```bash
cd docker-compose
docker-compose up -d
```
*Tip: MinIO console is accessible at `http://localhost:9001` (admin / admin123).*
*Tip: Kafka-UI is accessible at `http://localhost:8080`.*

### 2. Run the File Service
Open a new terminal and start the File Service:
```bash
cd file-service
./mvnw spring-boot:run
```
*(Runs on port 8081)*

### 3. Run the DMS Core Service
Open another terminal and start the DMS Service:
```bash
cd dms-service
./mvnw spring-boot:run
```
*(Runs on port 8082)*

### 4. Run the Frontend
Finally, start the Angular frontend application:
```bash
cd dms-frontend
npm install
npm run start
```
*(Runs on port 4200)*

Navigate to `http://localhost:4200` in your browser to access the DMS Platform!

## 🧪 Testing Multipart Upload
If you want to test the multipart upload mechanism without the UI, you can run the provided Node.js test script located in the root directory:
```bash
node test-multipart.js
```
This script generates a dummy 12MB file and simulates the 3-step chunking process against the File Service.

## 📖 Architecture Guidelines
This project strictly follows predefined architectural rules:
- **Pure Domain**: The `domain/` layer has zero dependencies on Spring or any technical frameworks.
- **Vertical Slicing**: Organized by aggregates instead of technical layers.
- **Strict Naming Conventions**: Enforced `UseCase`, `Port`, `Adapter`, `Resource`, and `Command/Query` naming rules to maintain a ubiquitous language.
