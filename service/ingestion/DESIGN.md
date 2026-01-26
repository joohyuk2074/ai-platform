# Ingestion Service 설계문서

## 1. 서비스 개요

### 1.1 목적
Ingestion Service는 AI 플랫폼의 문서 전처리를 담당하는 마이크로서비스입니다. Markdown 파일을 업로드하여 검수, 청킹, 임베딩 과정을 거쳐 Agent Service에서 활용 가능한 형태로 데이터를 준비합니다.

### 1.2 주요 기능
- **문서 업로드**: Markdown 파일 업로드 및 메타데이터 관리
- **문서 검수**: 업로드된 문서의 유효성 검증 및 품질 검사
- **청킹**: 문서를 의미 단위로 분할하여 Chunk 생성
- **임베딩**: Chunk를 벡터화하여 Passage 생성
- **컬렉션 관리**: 관련 문서들을 그룹화하여 관리

### 1.3 서비스 정보
- **포트**: 8082
- **기술 스택**: Spring Boot 4.0.2, Java 25, MySQL, JPA, Kafka
- **의존성**: common-core 모듈
- **메시징**: Kafka (이벤트 기반 비동기 처리)

---

## 2. 아키텍처 설계

### 2.1 레이어드 아키텍처

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (Controller, API, DTO)                 │
└─────────────────────────────────────────┘
                   ↓
┌─────────────────────────────────────────┐
│         Application Layer               │
│  (Use Cases, Orchestration)             │
└─────────────────────────────────────────┘
                   ↓
┌─────────────────────────────────────────┐
│           Domain Layer                  │
│  (Entities, Value Objects, Services)    │
└─────────────────────────────────────────┘
                   ↓
┌─────────────────────────────────────────┐
│       Infrastructure Layer              │
│  (Repository Impl, External APIs)       │
└─────────────────────────────────────────┘
```

### 2.2 처리 파이프라인

```
Upload → Validation → Chunking → Embedding → Storage
  ↓          ↓           ↓           ↓          ↓
 File    Quality      Split      Vector    Database
Input    Check      Document   Creation   Persistence
```

### 2.3 이벤트 기반 아키텍처 (Kafka)

검수 → 청킹 → 임베딩 과정은 Kafka를 통한 이벤트 기반으로 비동기 처리됩니다.

#### 2.3.1 전체 이벤트 플로우

```
┌─────────────┐
│   Upload    │  (API 호출)
│   Service   │
└──────┬──────┘
       │ publish
       ↓
┌─────────────────────────────────────┐
│  document.uploaded (Kafka Topic)    │
└─────────────────────────────────────┘
       │ consume
       ↓
┌─────────────┐
│ Validation  │
│  Consumer   │ ──→ ValidationService
└──────┬──────┘
       │ publish
       ↓
┌─────────────────────────────────────┐
│  document.validated (Kafka Topic)   │
└─────────────────────────────────────┘
       │ consume
       ↓
┌─────────────┐
│  Chunking   │
│  Consumer   │ ──→ ChunkingService
└──────┬──────┘
       │ publish
       ↓
┌─────────────────────────────────────┐
│  document.chunked (Kafka Topic)     │
└─────────────────────────────────────┘
       │ consume
       ↓
┌─────────────┐
│  Embedding  │
│  Consumer   │ ──→ PassageEnrichmentService
└──────┬──────┘
       │ publish
       ↓
┌─────────────────────────────────────┐
│  document.completed (Kafka Topic)   │
└─────────────────────────────────────┘
       │ consume (optional)
       ↓
┌─────────────┐
│   Agent     │
│  Service    │
└─────────────┘
```

#### 2.3.2 Kafka 토픽 구조

| 토픽명 | 파티션 | 설명 | Producer | Consumer |
|--------|--------|------|----------|----------|
| `document.uploaded` | 3 | 문서 업로드 완료 이벤트 | UploadService | ValidationConsumer |
| `document.validated` | 3 | 문서 검수 완료 이벤트 | ValidationConsumer | ChunkingConsumer |
| `document.chunked` | 3 | 청킹 완료 이벤트 | ChunkingConsumer | EmbeddingConsumer |
| `document.completed` | 3 | 전체 처리 완료 이벤트 | EmbeddingConsumer | Agent Service |
| `document.failed` | 1 | 처리 실패 이벤트 | All Consumers | MonitoringService |
| `document.failed.dlq` | 1 | Dead Letter Queue | Error Handler | Admin/Retry Service |

**파티셔닝 전략:**
- Key: `documentId`
- 동일 문서의 이벤트는 순서 보장을 위해 동일 파티션으로 라우팅
- 파티션 수: 3 (확장 가능)

#### 2.3.3 이벤트 스키마 정의

**공통 필드:**
```json
{
  "eventId": "uuid",
  "eventType": "DOCUMENT_UPLOADED | DOCUMENT_VALIDATED | DOCUMENT_CHUNKED | DOCUMENT_COMPLETED | DOCUMENT_FAILED",
  "documentId": "uuid",
  "collectionId": "uuid",
  "timestamp": "2026-01-26T10:30:00Z",
  "version": "1.0"
}
```

**DocumentUploadedEvent**
```json
{
  "eventId": "uuid",
  "eventType": "DOCUMENT_UPLOADED",
  "documentId": "uuid",
  "collectionId": "uuid",
  "timestamp": "2026-01-26T10:30:00Z",
  "version": "1.0",
  "payload": {
    "fileName": "example.md",
    "contentSize": 1024,
    "contentType": "text/markdown",
    "metadata": {
      "uploadedBy": "user-id",
      "tags": ["tag1", "tag2"]
    }
  }
}
```

**DocumentValidatedEvent**
```json
{
  "eventId": "uuid",
  "eventType": "DOCUMENT_VALIDATED",
  "documentId": "uuid",
  "collectionId": "uuid",
  "timestamp": "2026-01-26T10:30:05Z",
  "version": "1.0",
  "payload": {
    "validationResult": "PASSED",
    "qualityScore": 95.5,
    "warnings": [],
    "metadata": {}
  }
}
```

**DocumentChunkedEvent**
```json
{
  "eventId": "uuid",
  "eventType": "DOCUMENT_CHUNKED",
  "documentId": "uuid",
  "collectionId": "uuid",
  "timestamp": "2026-01-26T10:30:10Z",
  "version": "1.0",
  "payload": {
    "chunkIds": ["chunk-id-1", "chunk-id-2", "chunk-id-3"],
    "chunkCount": 3,
    "chunkingStrategy": "SEMANTIC_HEADER_BASED",
    "metadata": {}
  }
}
```

**DocumentCompletedEvent**
```json
{
  "eventId": "uuid",
  "eventType": "DOCUMENT_COMPLETED",
  "documentId": "uuid",
  "collectionId": "uuid",
  "timestamp": "2026-01-26T10:30:20Z",
  "version": "1.0",
  "payload": {
    "passageIds": ["passage-id-1", "passage-id-2", "passage-id-3"],
    "passageCount": 3,
    "embeddingModel": "text-embedding-3-large",
    "processingDuration": 15000,
    "metadata": {}
  }
}
```

**DocumentFailedEvent**
```json
{
  "eventId": "uuid",
  "eventType": "DOCUMENT_FAILED",
  "documentId": "uuid",
  "collectionId": "uuid",
  "timestamp": "2026-01-26T10:30:15Z",
  "version": "1.0",
  "payload": {
    "failedAt": "CHUNKING",
    "errorCode": "CHUNK_SIZE_EXCEEDED",
    "errorMessage": "Document size exceeds maximum allowed for chunking",
    "stackTrace": "...",
    "retryable": true,
    "retryCount": 0,
    "metadata": {}
  }
}
```

#### 2.3.4 Consumer 그룹 전략

| Consumer 그룹 | 역할 | 인스턴스 수 |
|---------------|------|-------------|
| `ingestion-validation-group` | 검수 처리 | 3 |
| `ingestion-chunking-group` | 청킹 처리 | 3 |
| `ingestion-embedding-group` | 임베딩 처리 | 3 |
| `agent-notification-group` | Agent 서비스 알림 | 1 |
| `monitoring-group` | 모니터링 및 로깅 | 1 |

**처리 보장:**
- **At-least-once 전달**: 중복 처리 가능성 있음 → 멱등성 보장 필요
- **수동 오프셋 커밋**: 처리 완료 후에만 커밋
- **트랜잭션 처리**: DB 저장 + Kafka Publish를 트랜잭션으로 묶음

#### 2.3.5 에러 처리 및 재시도 전략

**재시도 정책:**
```yaml
retry:
  max-attempts: 3
  backoff:
    initial-interval: 1000ms  # 1초
    multiplier: 2.0
    max-interval: 10000ms     # 10초
```

**처리 플로우:**
```
1. 메시지 수신
2. 처리 시도
   ├─ 성공 → 다음 이벤트 발행 + 오프셋 커밋
   └─ 실패
       ├─ Retryable 에러
       │   ├─ 재시도 횟수 < 3 → 재시도 (exponential backoff)
       │   └─ 재시도 횟수 >= 3 → DLQ로 이동
       └─ Non-retryable 에러
           └─ 즉시 DLQ로 이동 + document.failed 이벤트 발행
```

**Dead Letter Queue (DLQ) 처리:**
- 토픽: `document.failed.dlq`
- 실패한 메시지를 DLQ에 저장
- 별도 Admin/Retry Service에서 수동 재처리
- 알림 발송 (Slack, Email 등)

**Retryable vs Non-retryable:**

| 에러 타입 | Retryable | 예시 |
|-----------|-----------|------|
| 네트워크 타임아웃 | ✅ | 임베딩 API 호출 실패 |
| DB 연결 실패 | ✅ | Connection pool exhausted |
| 일시적 리소스 부족 | ✅ | Out of memory (temporary) |
| 잘못된 문서 형식 | ❌ | Invalid Markdown syntax |
| 비즈니스 규칙 위반 | ❌ | Document size exceeds limit |
| 데이터 무결성 오류 | ❌ | Foreign key constraint |

---

## 3. 도메인 모델

### 3.1 Entity 설계

#### DocumentCollection
문서들을 논리적으로 그룹화하는 컬렉션

```java
- CollectionId id
- String name
- String description
- List<Document> documents
- LocalDateTime createdAt
- LocalDateTime updatedAt
```

#### Document
업로드된 원본 Markdown 문서

```java
- DocumentId id
- CollectionId collectionId
- Content originalContent
- DocumentStatus status
- Metadata metadata
- List<Chunk> chunks
- LocalDateTime uploadedAt
- LocalDateTime processedAt
```

#### Chunk
문서를 의미 단위로 분할한 조각

```java
- ChunkId id
- DocumentId documentId
- Content chunkContent
- int sequenceNumber
- Metadata metadata
- List<Passage> passages
- LocalDateTime createdAt
```

#### Passage
임베딩된 벡터 데이터

```java
- PassageId id
- ChunkId chunkId
- float[] embedding
- Metadata metadata
- LocalDateTime createdAt
```

### 3.2 Value Objects

#### DocumentStatus (Enum)
```java
UPLOADED      // 업로드 완료
VALIDATING    // 검수 중
VALIDATED     // 검수 완료
CHUNKING      // 청킹 중
CHUNKED       // 청킹 완료
EMBEDDING     // 임베딩 중
COMPLETED     // 전체 처리 완료
FAILED        // 처리 실패
```

#### Content
```java
- String rawContent
- String contentType
- long size
```

#### Metadata
```java
- Map<String, Object> properties
- String version
- Map<String, String> tags
```

### 3.3 Domain Services

#### ValidationService
문서 검수 로직을 담당하는 도메인 서비스

**책임:**
- 문서 형식 검증 (Markdown 유효성)
- 문서 크기 제한 확인
- 콘텐츠 품질 검사 (인코딩, 특수문자 등)
- 메타데이터 유효성 검증

**메서드:**
```java
ValidationResult validate(Document document)
boolean isValidMarkdown(Content content)
boolean checkSizeLimit(Content content)
QualityReport analyzeQuality(Content content)
```

#### ChunkingService
문서를 의미 단위로 분할하는 도메인 서비스

**책임:**
- Markdown 구조 분석 (헤더, 섹션 인식)
- 의미 단위 청킹 전략 적용
- Chunk 크기 최적화
- Chunk 간 관계 유지

**메서드:**
```java
List<Chunk> chunkDocument(Document document)
List<Chunk> splitBySemanticUnits(Content content)
boolean isOptimalChunkSize(Chunk chunk)
void maintainChunkRelationships(List<Chunk> chunks)
```

#### PassageEnrichmentService
Chunk를 임베딩하여 Passage를 생성하는 도메인 서비스

**책임:**
- 임베딩 벡터 생성 (외부 임베딩 모델 호출)
- Passage 메타데이터 보강
- 벡터 정규화 및 최적화

**메서드:**
```java
Passage enrichChunk(Chunk chunk)
float[] generateEmbedding(Content content)
Metadata enrichMetadata(Chunk chunk, float[] embedding)
float[] normalizeVector(float[] embedding)
```

---

## 4. API 설계

### 4.1 REST API Endpoints

#### 컬렉션 관리

**POST /api/v1/collections**
- 새로운 컬렉션 생성
- Request Body:
  ```json
  {
    "name": "string",
    "description": "string"
  }
  ```
- Response: `201 Created`
  ```json
  {
    "collectionId": "uuid",
    "name": "string",
    "description": "string",
    "createdAt": "timestamp"
  }
  ```

**GET /api/v1/collections/{collectionId}**
- 컬렉션 상세 조회
- Response: `200 OK`

**GET /api/v1/collections**
- 컬렉션 목록 조회
- Query Parameters: `page`, `size`, `sort`
- Response: `200 OK`

#### 문서 관리

**POST /api/v1/collections/{collectionId}/documents**
- 문서 업로드
- Request: `multipart/form-data`
  - `file`: Markdown 파일
  - `metadata`: JSON 메타데이터 (optional)
- Response: `201 Created`
  ```json
  {
    "documentId": "uuid",
    "collectionId": "uuid",
    "status": "UPLOADED",
    "uploadedAt": "timestamp"
  }
  ```

**GET /api/v1/documents/{documentId}**
- 문서 상세 조회
- Response: `200 OK`
  ```json
  {
    "documentId": "uuid",
    "collectionId": "uuid",
    "status": "COMPLETED",
    "metadata": {},
    "chunkCount": 15,
    "uploadedAt": "timestamp",
    "processedAt": "timestamp"
  }
  ```

**GET /api/v1/documents/{documentId}/status**
- 문서 처리 상태 조회
- Response: `200 OK`
  ```json
  {
    "documentId": "uuid",
    "status": "CHUNKING",
    "progress": {
      "currentStep": "CHUNKING",
      "completedSteps": ["UPLOADED", "VALIDATED"],
      "percentage": 60
    }
  }
  ```

#### 처리 트리거

**POST /api/v1/documents/{documentId}/validate**
- 문서 검수 시작
- Response: `202 Accepted`

**POST /api/v1/documents/{documentId}/chunk**
- 문서 청킹 시작
- Response: `202 Accepted`

**POST /api/v1/documents/{documentId}/embed**
- 임베딩 처리 시작
- Response: `202 Accepted`

**POST /api/v1/documents/{documentId}/process**
- 전체 파이프라인 실행 (검수 → 청킹 → 임베딩)
- Response: `202 Accepted`

#### Chunk 조회

**GET /api/v1/documents/{documentId}/chunks**
- 문서의 Chunk 목록 조회
- Response: `200 OK`

**GET /api/v1/chunks/{chunkId}**
- Chunk 상세 조회
- Response: `200 OK`

#### Passage 조회

**GET /api/v1/chunks/{chunkId}/passages**
- Chunk의 Passage 조회
- Response: `200 OK`

**GET /api/v1/passages/{passageId}**
- Passage 상세 조회 (임베딩 벡터 포함)
- Response: `200 OK`

### 4.2 Error Response

```json
{
  "timestamp": "2026-01-26T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid Markdown format",
  "path": "/api/v1/documents"
}
```

---

## 5. 처리 프로세스 플로우 (Kafka 이벤트 기반)

### 5.1 전체 파이프라인

```
┌──────────────┐
│  문서 업로드   │  (REST API)
└──────┬───────┘
       │ publish: document.uploaded
       ↓
┌──────────────┐
│  검수 (검증)  │ ← ValidationConsumer
│  - 형식 검증  │    + ValidationService
│  - 크기 확인  │
│  - 품질 검사  │
└──────┬───────┘
       │ publish: document.validated
       ↓
┌──────────────┐
│  청킹 분할    │ ← ChunkingConsumer
│  - 구조 분석  │    + ChunkingService
│  - 의미 분할  │
│  - 크기 최적화│
└──────┬───────┘
       │ publish: document.chunked
       ↓
┌──────────────┐
│   임베딩     │ ← EmbeddingConsumer
│  - 벡터 생성  │    + PassageEnrichmentService
│  - 메타 보강  │
│  - 정규화    │
└──────┬───────┘
       │ publish: document.completed
       ↓
┌──────────────┐
│  데이터 저장  │
│  - DB 저장   │  (각 단계마다 수행)
│  - 상태 갱신  │
└──────┬───────┘
       ↓
┌──────────────┐
│ Agent 서비스  │  (이벤트 구독 or API 호출)
│   데이터 제공  │
└──────────────┘
```

### 5.2 상태 전이 다이어그램

```
UPLOADED → VALIDATING → VALIDATED → CHUNKING → CHUNKED → EMBEDDING → COMPLETED
    ↓           ↓            ↓           ↓          ↓          ↓
    └───────────┴────────────┴───────────┴──────────┴──────────┴──→ FAILED
                                                                       ↓
                                                              [DLQ + Retry Logic]
```

### 5.3 Use Case: 문서 업로드 및 처리 (이벤트 기반)

**시퀀스:**

1. **문서 업로드 (API 호출)**
   - 클라이언트가 `POST /api/v1/collections/{collectionId}/documents`로 Markdown 파일 업로드
   - Document 엔티티 생성 및 DB 저장 (상태: UPLOADED)
   - `document.uploaded` 이벤트를 Kafka에 발행
   - 클라이언트에게 `202 Accepted` 응답

2. **검수 처리 (비동기)**
   - ValidationConsumer가 `document.uploaded` 이벤트 수신
   - Document 상태를 VALIDATING으로 변경
   - ValidationService를 통한 검수 실행
     - 형식 검증 (Markdown 유효성)
     - 크기 제한 확인
     - 품질 검사
   - 검수 성공 시:
     - Document 상태를 VALIDATED로 변경 및 DB 저장
     - `document.validated` 이벤트 발행
     - 오프셋 커밋
   - 검수 실패 시:
     - Document 상태를 FAILED로 변경
     - `document.failed` 이벤트 발행
     - 에러 로깅 및 알림

3. **청킹 처리 (비동기)**
   - ChunkingConsumer가 `document.validated` 이벤트 수신
   - Document 상태를 CHUNKING으로 변경
   - ChunkingService를 통한 청킹 실행
     - Markdown 구조 분석
     - 의미 단위로 분할
     - Chunk 크기 최적화
   - 청킹 성공 시:
     - Chunk 엔티티들 생성 및 DB 저장
     - Document 상태를 CHUNKED로 변경
     - `document.chunked` 이벤트 발행 (chunkIds 포함)
     - 오프셋 커밋
   - 청킹 실패 시:
     - Document 상태를 FAILED로 변경
     - `document.failed` 이벤트 발행

4. **임베딩 처리 (비동기)**
   - EmbeddingConsumer가 `document.chunked` 이벤트 수신
   - Document 상태를 EMBEDDING으로 변경
   - 각 Chunk에 대해 PassageEnrichmentService 실행
     - 외부 임베딩 API 호출 (OpenAI, HuggingFace 등)
     - 벡터 정규화
     - 메타데이터 보강
   - 임베딩 성공 시:
     - Passage 엔티티들 생성 및 DB 저장
     - Document 상태를 COMPLETED로 변경
     - `document.completed` 이벤트 발행
     - 오프셋 커밋
   - 임베딩 실패 시:
     - Document 상태를 FAILED로 변경
     - `document.failed` 이벤트 발행

5. **Agent 서비스 알림 (선택)**
   - Agent Service가 `document.completed` 이벤트 구독
   - 새로운 Passage 데이터 사용 가능 알림
   - 내부 캐시 또는 인덱스 갱신

**실패 처리 및 재시도:**
- **Retryable 에러**: 최대 3회 재시도 (exponential backoff)
- **Non-retryable 에러**: 즉시 FAILED 상태로 변경 및 DLQ 이동
- **DLQ 처리**: Admin/Retry Service에서 수동 재처리
- **멱등성 보장**: 동일 이벤트 중복 처리 시에도 결과 일관성 유지

### 5.4 멱등성(Idempotency) 보장 전략

Kafka는 At-least-once 전달을 보장하므로 중복 이벤트 처리 가능성이 있습니다.

**멱등성 구현 방법:**

1. **Event ID 기반 중복 체크**
   ```java
   // 이미 처리된 이벤트인지 확인
   if (processedEventRepository.existsByEventId(eventId)) {
       log.info("Event already processed: {}", eventId);
       return; // 중복 처리 방지
   }

   // 처리 수행
   processDocument(event);

   // 처리 완료 기록
   processedEventRepository.save(new ProcessedEvent(eventId, timestamp));
   ```

2. **Document 상태 기반 처리**
   ```java
   // 현재 상태가 예상 상태와 일치하는지 확인
   Document document = documentRepository.findById(documentId);

   if (document.getStatus() != DocumentStatus.UPLOADED) {
       log.warn("Document already processed: {}", documentId);
       return; // 이미 다음 단계로 진행됨
   }

   // 낙관적 락을 사용한 상태 변경
   document.validateAndTransitionTo(DocumentStatus.VALIDATING);
   documentRepository.save(document);
   ```

3. **Database Unique Constraint**
   ```sql
   -- 중복 방지를 위한 유니크 제약
   CREATE TABLE processed_events (
       event_id VARCHAR(36) PRIMARY KEY,
       processed_at TIMESTAMP NOT NULL,
       INDEX idx_processed_at (processed_at)
   );
   ```

### 5.5 순서 보장 (Ordering Guarantee)

동일 문서의 이벤트는 반드시 순서대로 처리되어야 합니다.

**순서 보장 전략:**
- **파티션 키**: documentId를 파티션 키로 사용
  ```java
  ProducerRecord<String, DocumentEvent> record =
      new ProducerRecord<>(
          "document.validated",
          documentId.toString(),  // 파티션 키
          event
      );
  ```
- **동일 문서 → 동일 파티션**: 같은 documentId는 항상 같은 파티션으로 라우팅
- **순차 처리**: 각 파티션 내에서는 메시지 순서가 보장됨

---

## 6. 기술 스택

### 6.1 프레임워크 및 라이브러리
- **Spring Boot 4.0.2**: 애플리케이션 프레임워크
- **Spring Data JPA**: 데이터 접근 계층
- **Spring Validation**: 입력 검증
- **Spring Web MVC**: REST API 구현
- **Spring Kafka**: Kafka 메시징 통합
- **Lombok**: 보일러플레이트 코드 감소

### 6.2 메시징 시스템
- **Apache Kafka**: 이벤트 스트리밍 플랫폼
  - 비동기 이벤트 기반 처리
  - 파이프라인 단계 간 decoupling
  - 확장 가능한 메시지 큐
  - At-least-once 전달 보장

**Kafka 구성:**
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: ingestion-service
      auto-offset-reset: earliest
      enable-auto-commit: false  # 수동 커밋
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "me.joohyuk.ingestion.event"
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all  # 모든 replica 확인
      retries: 3
```

### 6.3 데이터베이스
- **MySQL**: 관계형 데이터 저장
  - Document, Chunk, Passage 영속화
  - 메타데이터 관리
  - 트랜잭션 보장
  - 이벤트 중복 체크 (processed_events 테이블)

### 6.4 외부 연동
- **임베딩 모델 API**: Chunk를 벡터로 변환
  - OpenAI Embeddings (text-embedding-3-large)
  - HuggingFace Transformers (BAAI/bge-large)
  - 또는 자체 임베딩 서비스
  - WebClient를 통한 비동기 HTTP 호출

### 6.5 빌드 도구
- **Gradle (Kotlin DSL)**: 빌드 관리
- **Java 25**: 프로그래밍 언어

### 6.6 의존성 추가 (build.gradle.kts)

```kotlin
dependencies {
    // 기존 의존성
    implementation(project(":libs:common-core"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")

    // Kafka 의존성 추가
    implementation("org.springframework.kafka:spring-kafka")

    // JSON 처리
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // 비동기 HTTP 클라이언트 (임베딩 API 호출용)
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    compileOnly("org.projectlombok:lombok")
    runtimeOnly("com.mysql:mysql-connector-j")
    annotationProcessor("org.projectlombok:lombok")

    // 테스트
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.testcontainers:kafka")
}

---

## 7. Agent 서비스와의 연동

### 7.1 연동 방식

#### 이벤트 기반 연동 (권장)
Kafka 이벤트를 통한 비동기 알림 방식을 사용합니다.

**처리 플로우:**
```
┌─────────────────┐
│   Ingestion     │
│    Service      │
└────────┬────────┘
         │ publish: document.completed
         ↓
┌─────────────────────────────────────┐
│  document.completed (Kafka Topic)   │
└─────────────────────────────────────┘
         │ consume
         ↓
┌─────────────────┐
│     Agent       │
│    Service      │
│  1. 이벤트 수신  │
│  2. API 호출    │ ──→ GET /api/v1/passages?documentId={id}
│  3. 데이터 동기화│
└─────────────────┘
```

**장점:**
- 느슨한 결합 (Loose Coupling)
- Agent Service가 다운되어도 Ingestion은 정상 동작
- 여러 Consumer(Agent Service, Monitoring 등)가 동시 구독 가능
- 이벤트 히스토리 추적 가능

#### 하이브리드 방식: 이벤트 알림 + REST API 조회

Agent Service는 두 가지 방식으로 데이터를 가져옵니다:

1. **이벤트 구독**: `document.completed` 토픽 구독
2. **REST API 호출**: 이벤트 수신 시 Ingestion API를 호출하여 실제 데이터 조회

**이유:**
- Kafka 메시지 크기 제한 (벡터 데이터는 크기가 큼)
- 이벤트는 알림 용도, 실제 데이터는 API로 조회
- 데이터 일관성 보장 (DB에서 직접 조회)

### 7.2 데이터 포맷

Agent Service에 제공하는 데이터 구조:

```json
{
  "passageId": "uuid",
  "chunkId": "uuid",
  "documentId": "uuid",
  "collectionId": "uuid",
  "content": "chunk text content",
  "embedding": [0.123, 0.456, ..., 0.789],
  "metadata": {
    "documentTitle": "string",
    "chunkSequence": 3,
    "documentStatus": "COMPLETED",
    "tags": ["tag1", "tag2"]
  },
  "createdAt": "timestamp"
}
```

### 7.3 검색 지원

Agent Service의 RAG(Retrieval-Augmented Generation) 지원을 위한 API:

**POST /api/v1/passages/search**
- 벡터 유사도 기반 검색
- Request:
  ```json
  {
    "queryEmbedding": [0.111, 0.222, ...],
    "collectionId": "uuid",
    "topK": 10,
    "minSimilarity": 0.7
  }
  ```
- Response:
  ```json
  {
    "results": [
      {
        "passage": { /* Passage 객체 */ },
        "similarity": 0.95
      }
    ]
  }
  ```

---

## 8. 데이터베이스 스키마

### 8.1 테이블 구조

#### document_collections
```sql
CREATE TABLE document_collections (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name)
);
```

#### documents
```sql
CREATE TABLE documents (
    id VARCHAR(36) PRIMARY KEY,
    collection_id VARCHAR(36) NOT NULL,
    original_content LONGTEXT NOT NULL,
    content_type VARCHAR(50),
    content_size BIGINT,
    status VARCHAR(20) NOT NULL,
    metadata JSON,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    FOREIGN KEY (collection_id) REFERENCES document_collections(id),
    INDEX idx_collection_id (collection_id),
    INDEX idx_status (status)
);
```

#### chunks
```sql
CREATE TABLE chunks (
    id VARCHAR(36) PRIMARY KEY,
    document_id VARCHAR(36) NOT NULL,
    chunk_content TEXT NOT NULL,
    sequence_number INT NOT NULL,
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (document_id) REFERENCES documents(id),
    INDEX idx_document_id (document_id),
    INDEX idx_sequence (document_id, sequence_number)
);
```

#### passages
```sql
CREATE TABLE passages (
    id VARCHAR(36) PRIMARY KEY,
    chunk_id VARCHAR(36) NOT NULL,
    embedding BLOB NOT NULL,  -- 벡터 데이터 (또는 별도 벡터 DB 사용)
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chunk_id) REFERENCES chunks(id),
    INDEX idx_chunk_id (chunk_id)
);
```

#### processed_events (멱등성 보장용)
```sql
CREATE TABLE processed_events (
    event_id VARCHAR(36) PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    document_id VARCHAR(36) NOT NULL,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_document_id (document_id),
    INDEX idx_processed_at (processed_at)
);
```

### 8.2 인덱스 전략
- `collection_id`, `document_id`, `chunk_id`: 외래 키 조회 최적화
- `status`: 상태별 문서 필터링
- `sequence_number`: Chunk 순서 보장
- `event_id`: 중복 이벤트 체크 (processed_events)

### 8.3 트랜잭션 경계

이벤트 기반 처리에서 트랜잭션은 각 Consumer에서 관리됩니다:

```java
@Transactional
public void handleDocumentValidatedEvent(DocumentValidatedEvent event) {
    // 1. 멱등성 체크
    if (processedEventRepository.existsByEventId(event.getEventId())) {
        return;
    }

    // 2. 비즈니스 로직 수행 (DB 저장)
    Document document = documentRepository.findById(event.getDocumentId());
    // ... validation logic ...
    documentRepository.save(document);

    // 3. 다음 이벤트 발행
    kafkaTemplate.send("document.validated", event.getDocumentId(), event);

    // 4. 처리 완료 기록
    processedEventRepository.save(new ProcessedEvent(event.getEventId()));

    // 5. 모든 작업이 성공하면 커밋 → 오프셋 커밋
}
```

**트랜잭션 범위:**
- DB 저장 + Kafka 이벤트 발행 + 멱등성 기록을 하나의 트랜잭션으로 처리
- 실패 시 전체 롤백 → 재처리 가능

---

## 9. 확장 고려사항

### 9.1 성능 최적화
- **비동기 이벤트 처리**: Kafka를 통한 비동기 처리 (이미 적용됨)
- **배치 처리**: 여러 문서를 일괄 업로드하는 배치 API 제공
- **병렬 처리**:
  - Kafka Consumer 인스턴스 수 증가 (파티션 수만큼 확장 가능)
  - Chunk 임베딩을 병렬로 처리 (CompletableFuture, Parallel Stream)
- **캐싱**:
  - 자주 조회되는 Passage에 대한 캐싱 (Redis)
  - 임베딩 API 응답 캐싱 (동일 content 중복 호출 방지)
- **Kafka 성능 튜닝**:
  - Producer: `batch.size`, `linger.ms` 조정
  - Consumer: `max.poll.records`, `fetch.min.bytes` 조정
  - Partition 수 증가로 처리량 향상

### 9.2 확장성
- **벡터 DB 도입**: MySQL 대신 Pinecone, Weaviate, Milvus 등 벡터 전용 DB 사용
- **Kafka 클러스터 확장**:
  - Broker 수 증가
  - Partition 수 증가 (리파티셔닝 전략 필요)
  - Replication Factor 조정 (데이터 안정성)
- **Consumer 그룹 스케일링**:
  - Kubernetes HPA(Horizontal Pod Autoscaler)로 Consumer 인스턴스 자동 확장
  - Kafka lag 기반 스케일링
- **멀티 테넌시**: 사용자별 컬렉션 격리, 토픽 분리 고려

### 9.3 모니터링
- **이벤트 처리 메트릭**:
  - 각 단계별 소요 시간 (Validation, Chunking, Embedding)
  - 이벤트 처리 성공/실패율
  - 시간당 처리된 문서 수
- **Kafka 메트릭**:
  - Consumer Lag: 처리 지연 모니터링
  - 초당 메시지 처리량 (messages/sec)
  - 파티션별 오프셋 추적
  - DLQ 메시지 수
- **리소스 사용량**:
  - CPU, 메모리 사용률
  - DB 연결 풀 상태
  - Kafka connection 상태
- **알림 설정**:
  - Consumer lag > 1000: 경고 알림
  - DLQ 메시지 발생: 즉시 알림
  - 실패율 > 5%: 경고 알림
  - 처리 시간 > 임계값: 성능 저하 알림

### 9.4 보안
- **파일 업로드 검증**: 악성 파일 필터링
- **크기 제한**: DoS 방지를 위한 파일 크기 제한
- **인증/인가**: API 접근 제어 (JWT, OAuth2)

---

## 10. 개발 우선순위

### Phase 1: Core Domain (완료)
- ✅ Entity 모델링 (Document, Chunk, Passage, DocumentCollection)
- ✅ Value Object 정의 (DocumentStatus, Content, Metadata 등)
- ✅ Repository 인터페이스 정의
- ✅ Domain Service 뼈대 구현

### Phase 2: Event & Kafka Infrastructure
- Event 클래스 정의 (DocumentUploadedEvent, DocumentValidatedEvent 등)
- Kafka Producer 설정 및 구현
- Kafka Consumer 설정 (ValidationConsumer, ChunkingConsumer, EmbeddingConsumer)
- Kafka 토픽 생성 스크립트
- 멱등성 처리를 위한 ProcessedEvent 엔티티 및 Repository
- 에러 처리 및 재시도 로직 구현 (RetryTemplate, ErrorHandler)

### Phase 3: Application Layer
- DocumentUploadService 구현 (파일 업로드 + document.uploaded 이벤트 발행)
- ValidationEventHandler 구현 (ValidationConsumer + ValidationService)
- ChunkingEventHandler 구현 (ChunkingConsumer + ChunkingService)
- EmbeddingEventHandler 구현 (EmbeddingConsumer + PassageEnrichmentService)
- DTO 정의 및 매핑 (Event ↔ Entity)

### Phase 4: Presentation Layer
- REST Controller 구현
  - 컬렉션 관리 API
  - 문서 업로드 API
  - 문서 상태 조회 API
  - Chunk/Passage 조회 API
- 파일 업로드 처리 (MultipartFile)
- Exception Handling (Global Exception Handler)
- Validation (@Valid, @Validated)

### Phase 5: Infrastructure
- JPA Repository 구현
- 외부 임베딩 API 연동 (WebClient)
- Kafka 설정 관리 (application.yml)
- Database 스키마 마이그레이션 (Flyway 또는 Liquibase)
- Dead Letter Queue 처리 로직

### Phase 6: Testing
- 단위 테스트
  - Domain Service 테스트
  - Event Handler 테스트
- 통합 테스트
  - Kafka 통합 테스트 (EmbeddedKafka, Testcontainers)
  - API 통합 테스트 (MockMvc)
- 멱등성 테스트 (중복 이벤트 처리)
- Agent Service 연동 테스트

### Phase 7: Production Ready
- 로깅 및 모니터링
  - 이벤트 처리 시간 메트릭
  - Kafka lag 모니터링
  - 실패율 추적
- 에러 처리 강화
  - DLQ 알림 (Slack, Email)
  - 재처리 Admin UI
- 성능 최적화
  - Chunk 임베딩 병렬 처리
  - Kafka Consumer 스레드 튜닝
- 문서화 완성
- Docker Compose 구성 (Kafka, Zookeeper, MySQL)

---

## 11. 참고 사항

### 11.1 Markdown 청킹 전략
- **헤더 기반 분할**: # 헤더를 기준으로 섹션 분리
- **크기 제한**: 각 Chunk는 512~1024 토큰 이내
- **문맥 유지**: 헤더 정보를 Chunk 메타데이터에 포함
- **오버랩**: 인접 Chunk 간 일부 내용 중복으로 문맥 연결

### 11.2 임베딩 모델 선택
- **OpenAI text-embedding-3-large**: 3072 차원
- **HuggingFace BAAI/bge-large**: 1024 차원
- **Cohere embed-multilingual**: 다국어 지원

### 11.3 벡터 저장 방식
- **MySQL BLOB**: 소규모 데이터, 간단한 구현
- **Vector DB**: 대규모 데이터, 빠른 유사도 검색
  - Pinecone, Weaviate, Milvus, Qdrant 등

---

## 12. Kafka 로컬 환경 구성

### 12.1 Docker Compose로 Kafka 실행

프로젝트 루트에 `docker-compose.yml` 생성:

```yaml
version: '3.8'

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: ingestion_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

**실행:**
```bash
docker-compose up -d
```

### 12.2 Kafka 토픽 생성

```bash
# 컨테이너 접속
docker exec -it <kafka-container-id> bash

# 토픽 생성
kafka-topics --create --topic document.uploaded --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics --create --topic document.validated --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics --create --topic document.chunked --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics --create --topic document.completed --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics --create --topic document.failed --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
kafka-topics --create --topic document.failed.dlq --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

# 토픽 목록 확인
kafka-topics --list --bootstrap-server localhost:9092
```

### 12.3 Kafka 메시지 모니터링

```bash
# Consumer로 메시지 확인
kafka-console-consumer --bootstrap-server localhost:9092 --topic document.uploaded --from-beginning

# Consumer 그룹 확인
kafka-consumer-groups --bootstrap-server localhost:9092 --list

# Consumer lag 확인
kafka-consumer-groups --bootstrap-server localhost:9092 --group ingestion-validation-group --describe
```

---

## 변경 이력

| 버전 | 날짜       | 작성자  | 변경 내용           |
|------|------------|---------|---------------------|
| 1.0  | 2026-01-26 | joohyuk | 초안 작성           |
| 2.0  | 2026-01-26 | joohyuk | Kafka 이벤트 기반 아키텍처 추가 |
