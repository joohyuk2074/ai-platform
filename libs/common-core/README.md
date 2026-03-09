# common-core

헥사고날 아키텍처 기반의 공통 도메인 핵심 모듈입니다. 모든 서비스 모듈이 공유하는 기반 클래스, 값 객체, 포트 인터페이스, 공통 응답 형식, 예외 처리 등을 제공합니다.

## 모듈 구조

```
libs/common-core/
├── build.gradle.kts
└── src/main/java/com/spartaecommerce/
    ├── api/
    │   ├── pagination/
    │   │   └── CustomPageable.java
    │   └── response/
    │       ├── CommonResponse.java
    │       ├── IdResponse.java
    │       └── PageResponse.java
    ├── domain/
    │   ├── entity/
    │   │   ├── AggregateRoot.java
    │   │   ├── BaseEntity.java
    │   │   └── Passport.java
    │   ├── event/
    │   │   ├── DomainEvent.java
    │   │   ├── EmptyEvent.java
    │   │   └── publisher/
    │   │       └── DomainEventPublisher.java
    │   ├── port/
    │   │   ├── IdGenerator.java
    │   │   ├── JsonSerializationException.java
    │   │   └── JsonSerializer.java
    │   └── vo/
    │       ├── BaseId.java
    │       ├── CollectionId.java
    │       ├── ContentHash.java
    │       ├── DocumentId.java
    │       ├── Metadata.java
    │       ├── TrackingId.java
    │       └── UserId.java
    ├── exception/
    │   ├── BusinessException.java
    │   ├── DomainErrorCode.java
    │   ├── DomainException.java
    │   └── ErrorCode.java
    └── util/
        ├── DateTimeHolder.java
        └── Snowflake.java
```

---

## 패키지별 상세 설명

### `api` - API 계층 공통 타입

API 요청/응답에서 공통으로 사용되는 타입을 제공합니다.

#### `api.pagination`

| 클래스 | 설명 |
|--------|------|
| `CustomPageable` | 페이지네이션 요청 파라미터를 담는 record. 페이지 번호, 크기, 정렬 기준, 방향, 커서 기반 조회를 위한 `lastId`를 포함합니다. |

**주요 기능:**
- 기본값: `page=0`, `size=20`, `sortBy=createdAt`, `direction=DESC`
- 유효성 검증: `page >= 0`, `1 <= size <= 100`, `direction`은 `ASC` 또는 `DESC`
- 정적 팩토리 메서드: `ofDefaults()`, `of(sortBy, direction)`, `of(page, size, sortBy, direction)`, `of(page, size, sortBy, direction, lastId)`

#### `api.response`

| 클래스 | 설명 |
|--------|------|
| `CommonResponse<T>` | 모든 API 응답의 공통 래퍼 클래스. `code`, `message`, `data` 필드를 포함합니다. |
| `IdResponse` | 생성 응답 시 반환되는 ID record. `Long id` 필드를 가집니다. |
| `PageResponse<T>` | 페이지 조회 응답 record. `contents`, `page`, `size`, `totalElements`, `totalPages`를 포함합니다. |

**`CommonResponse` 정적 팩토리 메서드:**

```java
CommonResponse.create(id)              // 201 Created
CommonResponse.success(data)           // 200 OK
CommonResponse.success(message, data)  // 200 OK (커스텀 메시지)
CommonResponse.error(errorCode, msg)   // 오류 응답
```

---

### `domain` - 도메인 계층

헥사고날 아키텍처의 도메인 계층 구성 요소입니다.

#### `domain.entity`

| 클래스 | 설명 |
|--------|------|
| `BaseEntity<ID>` | 모든 엔티티의 기반 추상 클래스. ID 필드와 `equals`/`hashCode`(ID 기반)를 제공합니다. |
| `AggregateRoot<ID>` | DDD Aggregate Root 추상 클래스. `BaseEntity`를 상속합니다. |
| `Passport` | Netflix Passport 패턴 구현체. API Gateway에서 `X-Passport` 헤더로 전달되는 인증된 사용자 정보를 담는 record입니다. |

**`Passport` 주요 기능:**
- `userId`: 인증된 사용자의 `UserId`
- `username`: 사용자명
- `roles`: 사용자 권한 목록
- `hasRole(role)`: 특정 역할 보유 여부 확인
- `isAdmin()`: ADMIN 역할 여부 확인
- Jackson `@JsonCreator`/`@JsonProperty`를 통한 JSON 직렬화/역직렬화 지원

#### `domain.event`

| 클래스/인터페이스 | 설명 |
|----------------|------|
| `DomainEvent<T>` | 도메인 이벤트 마커 인터페이스. |
| `EmptyEvent` | 페이로드가 없는 이벤트를 나타내는 싱글턴 클래스. `DomainEvent<Void>`를 구현합니다. |
| `DomainEventPublisher<T>` | 도메인 이벤트 발행을 위한 포트 인터페이스. `publish(T domainEvent)` 메서드를 정의합니다. |

#### `domain.port`

헥사고날 아키텍처의 포트(Port) 인터페이스를 정의합니다. 도메인 계층이 외부 기술에 의존하지 않도록 추상화합니다.

| 클래스/인터페이스 | 설명 |
|----------------|------|
| `IdGenerator` | ID 생성 포트 인터페이스. `generateId(): long` 메서드를 정의합니다. |
| `JsonSerializer` | JSON 직렬화/역직렬화 포트 인터페이스. Jackson, Gson 등 다양한 구현체로 교체 가능합니다. |
| `JsonSerializationException` | JSON 직렬화/역직렬화 실패 시 발생하는 `RuntimeException`. |

**`JsonSerializer` 메서드:**

```java
<T> String serialize(T object)              // 객체 → JSON 문자열
<T> T deserialize(String json, Class<T> clazz)  // JSON 문자열 → 객체
```

#### `domain.vo` - 값 객체 (Value Objects)

| 클래스 | 타입 | 설명 |
|--------|------|------|
| `BaseId<T>` | 추상 클래스 | 모든 ID 값 객체의 기반 클래스. `@JsonValue`로 직렬화 시 원시 값을 반환합니다. |
| `UserId` | `BaseId<Long>` | 사용자 ID. `UserId.of(Long)` 팩토리 메서드 제공. |
| `DocumentId` | `BaseId<Long>` | 문서 ID. `DocumentId.from(String)`, `DocumentId.from(Long)` 팩토리 메서드 제공. |
| `CollectionId` | `BaseId<Long>` | 컬렉션 ID. `CollectionId.of(String)`, `CollectionId.of(Long)` 팩토리 메서드 제공. |
| `TrackingId` | `BaseId<UUID>` | 추적 ID (UUID). `TrackingId.of(String)` 팩토리 메서드로 UUID 문자열 파싱. |
| `ContentHash` | - | 파일 콘텐츠의 SHA-256 해시값을 나타내는 값 객체. 생성 시 64자의 유효한 hex 문자열인지 검증합니다. |
| `Metadata` | record | 파일 메타데이터(파일명, 크기, MIME 타입, 업로드 사용자, 출처, 작성자, 태그)를 담는 record. |

---

### `exception` - 예외 처리

| 클래스/인터페이스 | 설명 |
|----------------|------|
| `ErrorCode` | HTTP 상태코드, 오류 코드, 메시지를 포함하는 공통 오류 코드 열거형(enum). |
| `DomainErrorCode` | 도메인 계층의 오류 코드를 정의하기 위한 마커 인터페이스. `code()` 메서드를 정의합니다. |
| `BusinessException` | `ErrorCode` 기반의 비즈니스 예외 클래스 (`RuntimeException`). |
| `DomainException` | `DomainErrorCode` 기반의 도메인 예외 클래스 (`RuntimeException`). |

**`ErrorCode` 카테고리:**

| 카테고리 | 코드 | HTTP 상태 |
|----------|------|-----------|
| 공통 | `INVALID_REQUEST`, `INVALID_INPUT_VALUE`, `UNAUTHORIZED`, `INTERNAL_SERVER_ERROR` | 400, 401, 500 |
| JPA | `ENTITY_NOT_FOUND`, `ENTITY_ALREADY_EXISTS` | 404, 409 |
| 주문 | `ORDER_INVALID_STATE_TRANSITION` | 400 |
| 환불 | `REFUND_INVALID_STATE_TRANSITION` | 400 |
| 쿠폰 | `COUPON_NOT_FOUND`, `INVALID_COUPON_STATUS`, `COUPON_ALREADY_ISSUED` | 400, 404, 409 |
| 락 | `LOCK_ACQUISITION_FAILED` | 409 |
| 외부 API | `EXTERNAL_API_ERROR` | 500 |
| 메시지 큐 | `MESSAGE_CONSUME_ERROR` | 500 |

---

### `util` - 유틸리티

| 클래스/인터페이스 | 설명 |
|----------------|------|
| `DateTimeHolder` | 현재 시각을 추상화하는 인터페이스. 테스트 시 시간을 목(mock)할 수 있도록 설계되었습니다. `getCurrentDateTime(): LocalDateTime`, `now(): Instant` 메서드를 정의합니다. |
| `Snowflake` | Twitter Snowflake 알고리즘 기반의 분산 고유 ID 생성기. 41비트 타임스탬프 + 10비트 노드 ID + 12비트 시퀀스로 구성된 `long` 타입 ID를 생성합니다. |

**`Snowflake` ID 구조:**

```
| 1비트(미사용) | 41비트(타임스탬프) | 10비트(노드 ID) | 12비트(시퀀스) |
```

- 기준 시각: `2024-01-01T00:00:00Z`
- 노드 ID: 서버 시작 시 무작위 할당 (0 ~ 1023)
- 최대 초당 생성량: 노드당 4,096개

---

## 의존성

```kotlin
dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
}
```

Jackson Annotations만 의존하며, 도메인 계층의 순수성을 최대한 유지합니다.

---

## 아키텍처 원칙

이 모듈은 **헥사고날 아키텍처(Ports & Adapters)** 원칙을 따릅니다.

- **도메인 계층** (`domain.*`): 비즈니스 로직과 도메인 모델만 포함. 외부 기술에 의존하지 않습니다.
- **포트** (`domain.port.*`): 외부 시스템(DB, 메시지 큐, JSON 라이브러리 등)과의 통신을 위한 인터페이스만 정의합니다. 구현체는 각 서비스 모듈의 인프라 계층에 위치합니다.
- **API 타입** (`api.*`): 외부 인터페이스와의 통신 형식을 정의합니다.
