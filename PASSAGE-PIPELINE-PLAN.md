# Passage 파이프라인 — 설계 평가 및 구현 작업 계획

작성일: 2026-01-31 | 기준 브랜치: `work/01027272074-joohyuk`

---

## 1. 현재 코드 상태 분석 (사실관계)

제안을 평가하는 기준으로, 실제 코드에서 확인된 갭을 정리합니다.

### 1.1 datahub

| 영역 | 파일 (경로 기준: `service/datahub/src/main/java/.../datahub/`) | 현재 상태 | 갭 |
|---|---|---|---|
| Document 엔티티 | `domain/entity/Document.java` | `status` 필드 **존재하지 않음** | 상태 관리 자체가 불가 |
| DocumentStatus enum | `domain/vo/DocumentStatus.java` | UPLOADING, UPLOADED, VALIDATING, VALIDATED, VALIDATION_FAILED, CHUNKED, PASSAGES_GENERATED, EMBEDDED, FAILED 정의 | **Document 엔티티에서 한 번도 참조되지 않음.** Validation 관련 상태는 DESIGN.md v3.0에서 제거된 단계 |
| DocumentCollection | `domain/entity/DocumentCollection.java` | name, description 필드만 존재 | 집계 상태 · 카운터 **전무** |
| DocumentJpaEntity | `infrastructure/.../entity/DocumentJpaEntity.java` | status 컬럼 없음 | DB에 상태를 저장할 수 없음 |
| DocumentCollectionJpaEntity | `infrastructure/.../entity/DocumentCollectionJpaEntity.java` | 집계 관련 컬럼 없음 | — |
| Kafka Publisher | `infrastructure/.../LoggingDocumentChunkMessagePublisher.java` | `log.info()`만 수행하는 stub | 실제 메시지 발행 없음 |
| Publisher **호출** | `application/DocumentPersistenceHelper.java` | event를 생성한 후 **반환만** | `DocumentChunkMessagePublisher.publish()`가 **어디서도 호출되지 않음** |
| Kafka Consumer | `domain/port/in/listener/IndexingResponseMessageListener.java` | 인터페이스만 존재 | 구현체 없음 |
| Kafka 의존성 | `build.gradle.kts` | `spring-kafka` **없음** | Kafka 통신 자체가 불가 |

### 1.2 datarex

| 영역 | 현재 상태 | 갭 |
|---|---|---|
| 앱 구조 | `ChunkingApplication.java` — Spring Boot 앱 클래스만 존재 | 비즈니스 로직 전무 |
| 의존성 | `spring-boot-starter-webmvc`, `common-core` | `spring-kafka` 없음 |
| 처리 로직 | 없음 | Kafka 수신, Passage 처리, 결과 publish 전부 구현 필요 |

> **DESIGN.md 불일치 주의**: DESIGN.md에서 datarex를 "FastAPI / Python"으로 정의했지만, 실제 코드는 **Spring Boot Java** 앱입니다.
> 이후 구현은 코드 기준(Spring Boot + Spring Kafka)으로 진행합니다.

---

## 2. 제안 평가

### 2.1 Document 상태 설계

**평가: 방향은 맞음 — 상태 수를 축소하고 현재 enum과 정합시키면 적용 가능**

| 제안 상태 | 평가 | 비고 |
|---|---|---|
| `UPLOADED` | ✅ 유지 | 파일 저장 + DB 저장 완료의 명확한 의미 |
| `PASSAGE_REQUESTED` | ✅ 유지 | Kafka 발행 완료 후의 상태로서 명확 |
| `PASSAGE_CREATING` | ⚠️ MVP에서 제거 권장 | datarex가 "작업 시작" 이벤트를 별도로 publish하지 않으면 datahub가 이 상태로 전이할 **트리거가 없음**. 필요하면 나중에 추가 가능 |
| `PASSAGE_CREATED` | ✅ 유지 | 완료 terminal 상태 |
| `PASSAGE_FAILED` | ✅ 유지 | 실패 terminal 상태. `attempt`와 결합하여 재시도 가능 여부 판단 |

현재 `DocumentStatus` enum에 남아있는 `VALIDATING / VALIDATED / VALIDATION_FAILED`는 DESIGN.md v3.0에서 제거된 단계이고, `CHUNKED / PASSAGES_GENERATED / EMBEDDED`는 Passage 통합(v4.0) 이후 더 이상 유효하지 않습니다. **전체 교체**가 필요합니다.

**권장 MVP 상태 전이:**

```
UPLOADED ──→ PASSAGE_REQUESTED ──→ PASSAGE_CREATED
                                 ↘
                                   PASSAGE_FAILED
```

### 2.2 Document 추가 필드

**평가: 운영 필드는 적극 권장. 한 가지만 방향 조정.**

| 제안 필드 | 평가 | 사유 |
|---|---|---|
| `attempt` (int) | ✅ 권장 | 재시도 횟수 추적은 운영에서 최소한의 필수 정보 |
| `lastErrorCode` (string) | ✅ 권장 | 실패 원인 파악의 핵심 |
| `lastErrorMessage` (string) | ✅ 권장 | 길이 제한(예: 500자)과 함께 적용 |
| `passageCount` (int) | ✅ 권장 | PASSAGE_CREATED 시 저장. 조회 시 즉시 활용 |
| `currentJobId` | ❌ 제거 권장 | datarex에서 작업 ID를 관리하는 개념이 현재 없고, 도메인 엔티티에 작업자 디테일을 넣는 것은 제안 자신의 포인트와 모순 |
| `lastResultEventId` | ✅ 권장 | DESIGN.md의 `processed_events` 테이블과 **이중 방어**로 멱등성 강건성 향상. 이벤트 수신 시 "이미 같은 eventId를 처리했으면 skip" 로직의 가장 빠른 체크 포인트 |

### 2.3 Collection 집계 상태

**평가: 구조 맞음. MVP에서 상태 수를 줄이고 조회시 계산으로 시작하면 안전.**

| 제안 상태 | 평가 | 사유 |
|---|---|---|
| `EMPTY` | ✅ 유지 | 문서 0개일 때 명확한 표현 |
| `READY` | ⚠️ MVP에서 제거 권장 | 현재 파이프라인에서 UPLOADED 후 즉시 PASSAGE_REQUESTED로 전이되므로 "요청 전" 상태가 존재하는 시간이 **밀리초 단위**. 외부에서 관찰할 수 있는 안정적인 상태가 아님 |
| `PROCESSING` | ✅ 유지 | 진행 중 표현 |
| `COMPLETED` | ✅ 유지 | 모든 문서 완료 |
| `FAILED` | ✅ 유지 | 실패 문서 존재 시 |
| `PARTIAL_COMPLETED` | ⚠️ MVP에서 제외 | "하나라도 실패면 FAILED"로 단일 정책으로 시작하면 충분. 정책이 유연해질 때 추가 |

**권장 MVP CollectionStatus:**

```
EMPTY ──→ PROCESSING ──→ COMPLETED
                       ↘
                         FAILED
```

### 2.4 집계 방식: "필드 저장 vs 조회 계산"

**평가: MVP에서는 조회시 계산. "이전 상태 → 새 상태" 증감 패턴은 필드 저장 단계에서 적용.**

제안의 "이전 상태 → 새 상태" 기반 증감 패턴은 올바른 패턴입니다. 그러나 이 패턴은 **필드 저장 방식의 안전장치**이고, 필드를 저장하지 않는 MVP에서는 적용 불필요합니다.

**MVP(조회시 계산)에서의 논리:**

```
Document 상태가 바뀜 → 그만큼 집계가 자동으로 바뀜 (SELECT COUNT GROUP BY)
→ 이벤트 중복/순서 뒤집힘에 강건
→ 구현 단순
→ 항상 정확
```

**전이 시점 기준:**
- Collection당 문서 수가 ~1000건을 넘거나
- 상태 조회 API의 트래픽이 초당 수십회 이상이 될 때

해당 시점에서 Section 4의 패턴으로 전이합니다.

---

## 3. 권장 설계 요약

### 3.1 DocumentStatus (정리된 Enum)

```java
public enum DocumentStatus {
  UPLOADED,              // 파일 저장 + DB 저장 완료
  PASSAGE_REQUESTED,     // Kafka 이벤트 publish 완료
  PASSAGE_CREATED,       // 결과 이벤트 수신 완료 (terminal)
  PASSAGE_FAILED;        // 처리 실패 (terminal, 재시도 가능)

  public boolean isTerminal() {
    return this == PASSAGE_CREATED || this == PASSAGE_FAILED;
  }
}
```

### 3.2 Document 엔티티 추가 필드

```
기존: id, collectionId, fileKey, contentHash, metadata, createdAt, updatedAt

추가:
  status              : DocumentStatus   (필수, 초기값 UPLOADED)
  attempt             : int              (기본값 0)
  lastErrorCode       : String           (nullable)
  lastErrorMessage    : String           (nullable, 최대 500자)
  passageCount        : int              (기본값 0)
  lastResultEventId   : String           (nullable)
```

### 3.3 CollectionStatus (조회시 계산 규칙)

우선순위 순으로 단일 규칙:

```
1. 문서 0개                         → EMPTY
2. 모든 문서가 PASSAGE_CREATED      → COMPLETED
3. PASSAGE_FAILED가 하나라도 존재    → FAILED
4. 그 외                            → PROCESSING
```

### 3.4 Kafka 이벤트 & 토픽

DESIGN.md의 Chunking / Embedding 두 단계를 **"Passage 생성" 단일 단계**로 합쳐 정리합니다.
(DESIGN.md v4.0에서 Chunk 엔티티가 제거되고 Passage로 통합된 방향과 일치)

| 토픽 | Producer | Consumer | 용도 |
|---|---|---|---|
| `passage.creation.requested` | datahub | datarex | Passage 생성 요청 |
| `passage.creation.completed` | datarex | datahub | 생성 완료 결과 |
| `passage.creation.failed` | datarex | datahub | 생성 실패 결과 |

**이벤트 스키마:**

`passage.creation.requested` (datahub → datarex)
```json
{
  "eventId": "uuid-string",
  "documentId": 1234,
  "collectionId": 56,
  "fileKey": "collections/56/1234_sample.md",
  "timestamp": "2026-01-31T10:00:00Z"
}
```

`passage.creation.completed` (datarex → datahub)
```json
{
  "eventId": "uuid-string",
  "documentId": 1234,
  "collectionId": 56,
  "passageCount": 5,
  "timestamp": "2026-01-31T10:00:05Z"
}
```

`passage.creation.failed` (datarex → datahub)
```json
{
  "eventId": "uuid-string",
  "documentId": 1234,
  "collectionId": 56,
  "errorCode": "CHUNKING_ERROR",
  "errorMessage": "...",
  "retryable": true,
  "timestamp": "2026-01-31T10:00:05Z"
}
```

> **파티셔닝 키**: `documentId` (동일 문서의 이벤트 순서 보장)
> **멱등성**: Consumer 측에서 `lastResultEventId`와 `processed_events` 테이블 이중 체크

---

## 4. 구현 작업 및 순서

작업은 **Phase 단위로 의존성이 있어 순차 진행**해야 합니다.
각 Phase 내의 작업 번호는 순서를 의미합니다.

---

### Phase 1 — datahub Domain 정합성 정리

> **prereq**: 없음 (시작점)
> **목적**: Document에 상태를 넣고, enum을 정리하여 도메인이 상태를 표현할 수 있게 함

| # | 작업 | 파일 | 세부 사항 |
|---|---|---|---|
| 1-1 | DocumentStatus enum 교체 | `domain/vo/DocumentStatus.java` | 기존 enum 전체를 권장 설계 3.1로 교체 |
| 1-2 | Document 엔티티에 필드 추가 | `domain/entity/Document.java` | status, attempt, lastErrorCode, lastErrorMessage, passageCount, lastResultEventId 추가. `create()`에서 status = UPLOADED로 초기화. `restore()`에 새 필드 포함 |
| 1-3 | Document 상태 전이 메서드 구현 | `domain/entity/Document.java` | `requestPassageCreation()` → PASSAGE_REQUESTED, `markPassageCreated(passageCount, eventId)` → PASSAGE_CREATED, `markPassageFailed(errorCode, errorMessage, eventId)` → PASSAGE_FAILED. 각 메서드 내에서 현재 상태 검증 |

---

### Phase 2 — datahub Infrastructure 준비

> **prereq**: Phase 1 완료
> **목적**: Kafka 의존성과 DB 컬럼을 준비하여 이벤트 통신과 상태 저장이 가능하게 함

| # | 작업 | 파일 | 세부 사항 |
|---|---|---|---|
| 2-1 | Kafka 의존성 추가 | `service/datahub/build.gradle.kts` | `implementation("org.springframework.kafka:spring-kafka")` 추가 |
| 2-2 | DocumentJpaEntity 컬럼 추가 | `infrastructure/.../entity/DocumentJpaEntity.java` | status, attempt, lastErrorCode, lastErrorMessage, passageCount, lastResultEventId 컬럼. `from()` / `toDomain()` 매핑 업데이트 |
| 2-3 | application.yml Kafka 설정 | `resources/application.yml` | bootstrap-servers, producer (key/value serializer, acks), consumer (group-id, auto-offset-reset: earliest, enable-auto-commit: false, deserializer, trusted.packages) |
| 2-4 | DocumentRepository 메서드 추가 | `port/out/persistence/DocumentRepository.java` + `DocumentRepositoryImpl` + `DocumentJpaRepository` | `findByCollectionId(CollectionId)`, `countByCollectionIdAndStatus(Long collectionId, String status)` |

---

### Phase 3 — datahub 이벤트 Publish 구현

> **prereq**: Phase 2 완료
> **목적**: 문서 업로드 완료 후 Kafka로 Passage 생성 요청을 전송

| # | 작업 | 파일 | 세부 사항 |
|---|---|---|---|
| 3-1 | PassageCreationRequestedEvent 정의 | `domain/event/PassageCreationRequestedEvent.java` | `record` — eventId(UUID.randomUUID()), documentId, collectionId, fileKey, timestamp |
| 3-2 | PassageCreationRequestPublisher 포트 | `domain/port/out/message/publisher/PassageCreationRequestPublisher.java` | `void publish(PassageCreationRequestedEvent)` |
| 3-3 | KafkaPassageCreationRequestPublisher 구현 | `infrastructure/adapter/out/message/publisher/KafkaPassageCreationRequestPublisher.java` | KafkaTemplate로 토픽 `passage.creation.requested`에 publish. 키 = `documentId.toString()` |
| 3-4 | 업로드 후 publish 연결 | `application/DocumentPersistenceHelper.java` | `@Transactional` 내에서 save 후, 트랜잭션 커밋 후 이벤트 publish. `document.requestPassageCreation()` 호출 → save → publish 순서. **기존 LoggingDocumentChunkMessagePublisher는 제거 가능** |

> **트랜잭션 경계 주의**: save와 Kafka publish는 같은 트랜잭션을 공유할 수 없습니다(분산 트랜잭션).
> MVP에서는 **save 후 publish** 순서로 진행하고, publish 실패 시 Document는 PASSAGE_REQUESTED 상태로 남습니다.
> 이후 Outbox Pattern 도입으로 강건성을 높일 수 있습니다.

---

### Phase 4 — datarex Kafka Consumer + 처리 로직 + Producer

> **prereq**: Phase 3 완료 (토픽이 존재해야 Consumer가 수신 가능)
> **목적**: datarex에서 요청을 수신하고, 처리 후 결과를 돌려보냄

| # | 작업 | 파일 | 세부 사항 |
|---|---|---|---|
| 4-1 | Kafka 의존성 추가 | `service/datarex/build.gradle.kts` | `implementation("org.springframework.kafka:spring-kafka")` |
| 4-2 | application.properties Kafka 설정 | `resources/application.properties` | producer + consumer 설정 (datarex 그룹명: `datarex-passage-creation-group`) |
| 4-3 | 이벤트 DTO 정의 | `event/` 패키지 | `PassageCreationRequestedEvent`, `PassageCreationCompletedEvent`, `PassageCreationFailedEvent` — datahub와 동일한 필드 구조 |
| 4-4 | PassageCreationConsumer 구현 | `infrastructure/adapter/in/kafka/PassageCreationConsumer.java` | `@KafkaListener(topics = "passage.creation.requested", groupId = "...")`. try-catch로 성공/실패 분리 |
| 4-5 | Passage 생성 로직 (MVP stub) | `application/PassageCreationService.java` | MVP: FileStorage에서 파일 읽기 → 고정 크기 단순 분할 → passageCount 계산. 실제 Chunking(Langchain) / Embedding은 후속 단계 |
| 4-6 | 결과 Producer 구현 | `infrastructure/adapter/out/kafka/PassageCreationResultProducer.java` | KafkaTemplate로 `passage.creation.completed` 또는 `passage.creation.failed` publish. 키 = `documentId.toString()` |

> **datarex에서 파일에 접근하는 방법**: datahub와 동일한 FileStorage 구현을 공유하거나(예: MinIO/S3), `fileKey`를 기반으로 원격 저장소에서 읽습니다. MVP에서는 공유 파일 시스템 또는 MinIO로 진행합니다.

---

### Phase 5 — datahub 결과 수신 및 Collection 집계

> **prereq**: Phase 4 완료 (datarex에서 결과 이벤트가 발행되어야 수신 가능)
> **목적**: 결과 이벤트를 수신하여 Document 상태를 업데이트하고, Collection 집계를 조회 가능하게 함

| # | 작업 | 파일 | 세부 사항 |
|---|---|---|---|
| 5-1 | 수신 이벤트 DTO 정의 | `domain/event/` | `PassageCreationCompletedEvent`, `PassageCreationFailedEvent` record 정의 |
| 5-2 | PassageResultConsumer 구현 | `infrastructure/adapter/in/kafka/PassageResultConsumer.java` | `@KafkaListener` — `passage.creation.completed`, `passage.creation.failed` 수신. **기존 `IndexingResponseMessageListener`는 제거 가능** |
| 5-3 | 결과 수신 핸들러 로직 | Consumer 내부 (또는 별도 Application Service) | ① `documentRepository.getById()` ② `lastResultEventId` 체크 (동일이면 skip) ③ `document.markPassageCreated()` 또는 `markPassageFailed()` ④ `documentRepository.save()` |
| 5-4 | CollectionStatus enum 정의 | `domain/vo/CollectionStatus.java` | EMPTY, PROCESSING, COMPLETED, FAILED |
| 5-5 | Collection 집계 조회 로직 | `application/DocumentQueryServiceImpl.java` | `countByCollectionIdAndStatus`를 호출하여 카운트 수집 → Section 3.3 규칙으로 CollectionStatus 결정 → DTO 반환 |
| 5-6 | Collection 상태 조회 API 업데이트 | `infrastructure/adapter/in/web/DocumentCollectionController.java` | GET 응답에 `collectionStatus`, `totalDocuments`, `createdDocuments`, `failedDocuments`, `processingDocuments` 포함 |

---

### Phase 6 — 테스트

> **prereq**: Phase 1 ~ 5 완료

| # | 작업 | 대상 |
|---|---|---|
| 6-1 | Document 상태 전이 단위 테스트 | 유효한 전이(UPLOADED→PASSAGE_REQUESTED)와 무효한 전이(PASSAGE_CREATED→PASSAGE_REQUESTED 등) 모두 커버 |
| 6-2 | CollectionStatus 집계 규칙 단위 테스트 | 경계 케이스: 문서 0개, 혼재 상태(일부 CREATED + 일부 FAILED), 전원 CREATED 등 |
| 6-3 | Consumer 멱등성 테스트 | 동일 eventId의 completed 이벤트를 2번 수신하면 Document 상태가 한 번만 변경되는지 확인 |
| 6-4 | E2E 통합 테스트 | `@EmbeddedKafka` 또는 Testcontainers(Kafka)로 datahub publish → datarex consume → datarex publish → datahub consume 전체 플로우 검증 |

---

## 5. 작업 의존성 다이어그램

```
Phase 1 (Domain 정합성)
    │
    ▼
Phase 2 (Infrastructure 준비)
    │
    ▼
Phase 3 (datahub Publish)
    │
    ▼
Phase 4 (datarex Consumer + Producer)   ← datarex 파일 접근 방식 확인 필요
    │
    ▼
Phase 5 (datahub Consumer + 집계)
    │
    ▼
Phase 6 (테스트)
```

Phase 4에서 datarex가 파일을 읽어야 하므로, **파일 저장소 공유 방식**(예: MinIO, S3, 공유 볼륨)을 Phase 3 이전에 확인해야 합니다.

---

## 6. 후속 단계 (MVP 이후)

| 항목 | 내용 |
|---|---|
| Outbox Pattern | Phase 3의 "save 후 publish" 패턴을 Outbox로 교체하여 분산 트랜잭션 없이 정확히 한 번 전달 보장 |
| 실제 Chunking 로직 | datarex의 Phase 4-5 stub을 Langchain 기반 Semantic Chunking으로 교체 |
| Embedding | Passage 생성 단계에 Embedding (OpenAI text-embedding-3-large) 추가 |
| Collection 필드 저장 | 조회 트래픽 증가 시 Section 2.4의 "이전 상태 → 새 상태" 기반 증감 패턴 적용 |
| PASSAGE_CREATING 상태 | datarex에서 "작업 시작" 이벤트를 publish하는 기능 추가 시 활성화 |
| DLQ + 재시도 | `attempt` 기반 재시도 정책과 Dead Letter Queue 처리 구현 (DESIGN.md Section 2.3.5) |
| processed_events 테이블 | 멱등성 이중 방어의 나머지 절반. `lastResultEventId` 체크가 안정적이면 이후 단계에서 추가 |
