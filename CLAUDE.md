# CLAUDE.md

이 파일은 이 저장소에서 작업할 때 Claude Code(claude.ai/code)에 대한 기술 가이드를 제공합니다.

> **협업 가이드**: 브랜치 전략, 커밋 규칙, PR 프로세스는 [CONTRIBUTING.md](./CONTRIBUTING.md)를 참조하세요.

## 프로젝트 개요

**ai-platform**은 Java 25와 Gradle로 빌드된 Spring Boot 4.0.2 마이크로서비스 플랫폼입니다. 이 Sparta MSA 최종 프로젝트는 헥사고날(포트 & 어댑터) 패턴을 사용하여 이벤트 주도 아키텍처로 문서 관리 및 AI 처리 파이프라인을 구현합니다.

## 빌드 및 실행 명령어

### 프로젝트 빌드
```bash
# 모든 모듈 빌드
./gradlew build

# 특정 서비스 빌드
./gradlew :service:datahub:build
./gradlew :service:datarex:build
./gradlew :service:vecdash:build

# 테스트 없이 빌드
./gradlew build -x test

# 클린 후 빌드
./gradlew clean build
```

### 서비스 실행
```bash
# 특정 서비스 실행
./gradlew :service:datahub:bootRun
./gradlew :service:datarex:bootRun
./gradlew :service:vecdash:bootRun
```

### 테스트 실행
```bash
# 모든 테스트 실행
./gradlew test

# 특정 서비스 테스트 실행
./gradlew :service:datahub:test

# 상세 출력과 함께 테스트 실행
./gradlew test --info

# 특정 테스트 클래스 실행
./gradlew :service:datahub:test --tests "me.joohyuk.datahub.application.service.DocumentCollectionCommandServiceTest"
```

### 컨테이너 이미지 빌드
```bash
# 특정 서비스의 OCI 이미지 생성
./gradlew :service:datahub:bootBuildImage
./gradlew :service:datarex:bootBuildImage
```

## 프로젝트 구조

```
sparta-msa-final-project/
├── libs/                          # 공유 라이브러리 (서비스 간 재사용)
│   ├── common-core/              # 도메인 추상화 (AggregateRoot, Value Objects, Events)
│   ├── common-infrastructure/    # 인프라 유틸리티 (ObjectMapper, JSON 직렬화)
│   ├── common-outbox/           # 트랜잭셔널 아웃박스 패턴 구현
│   ├── common-saga/             # 사가 패턴 지원 (보상 트랜잭션)
│   └── messaging-common/        # Kafka 토픽 및 공유 이벤트 스키마
│
└── service/                      # 마이크로서비스
    ├── datahub/                 # 문서 저장 및 오케스트레이션 서비스 (port 8082)
    ├── datarex/                 # 문서 변환 서비스 (Spring AI 통합)
    └── vecdash/                 # 벡터 임베딩 서비스
```

## 아키텍처 패턴

### 헥사고날 아키텍처 (포트 & 어댑터)

각 서비스는 다음과 같은 계층 구조를 따릅니다:

```
service/[service-name]/src/main/java/
├── domain/                        # 핵심 비즈니스 로직 (순수, 의존성 없음)
│   ├── entity/                   # 애그리거트 (AggregateRoot 확장)
│   ├── event/                    # 도메인 이벤트
│   ├── vo/                       # 값 객체 (불변)
│   └── exception/                # 도메인 특화 예외
│
├── application/                   # 유스케이스 오케스트레이션
│   ├── port/in/service/          # 입력 포트 (유스케이스 인터페이스)
│   ├── port/out/                 # 출력 포트 (추상화)
│   │   ├── persistence/          # 리포지토리 인터페이스
│   │   ├── message/              # 메시지 퍼블리셔 인터페이스
│   │   ├── storage/              # 파일 스토리지 인터페이스
│   │   └── external/             # 외부 API 클라이언트 인터페이스
│   ├── service/                  # 서비스 구현 (도메인 + 포트 조정)
│   └── dto/                      # Command/Result 객체
│
└── infrastructure/adapter/        # 구현 어댑터
    ├── in/                       # 인바운드 어댑터
    │   ├── web/                  # REST 컨트롤러
    │   └── listener/             # Kafka 이벤트 리스너
    └── out/                      # 아웃바운드 어댑터
        ├── persistence/          # JPA 리포지토리 구현
        ├── message/              # Kafka 퍼블리셔 구현
        └── storage/              # 파일 스토리지 구현
```

**핵심 원칙**: 의존성은 내부로 흐릅니다 (Infrastructure → Application → Domain). 도메인 레이어는 외부 의존성이 전혀 없습니다.

### 아웃박스 패턴을 사용한 이벤트 주도 아키텍처

서비스들은 신뢰할 수 있는 메시징을 위해 **트랜잭셔널 아웃박스 패턴**을 사용하여 Kafka를 통해 비동기적으로 통신합니다:

1. **비즈니스 트랜잭션**이 도메인 변경사항과 아웃박스 메시지를 원자적으로 저장
2. **아웃박스 스케줄러**가 대기 중인 메시지를 폴링하여 Kafka에 발행
3. **컨슈머 서비스**가 이벤트를 처리하고 자체 상태를 업데이트
4. **멱등성**은 상관관계 ID를 통해 보장

**예시 흐름 (문서 변환)**:
```
Datahub: Document.transform() → TransformDocumentOutbox (PENDING)
  ↓ 스케줄러가 발행
Kafka: document.transform.requested
  ↓ 컨슈머
Datarex: 문서 변환 → TransformResultOutbox (PENDING)
  ↓ 스케줄러가 발행
Kafka: document.transform.result
  ↓ 컨슈머
Datahub: Document.completeTransform() → 상태 업데이트
```

### 도메인 주도 설계 패턴

- **애그리거트 루트**: `AggregateRoot<ID>`를 확장하는 도메인 엔티티로 비즈니스 규칙과 상태 전환을 캡슐화
  - 예시: datahub 서비스의 `Document`, `DocumentCollection`
  - public 메서드를 통해 불변성 강제 (예: `document.transform()`, `document.completeTransform()`)

- **값 객체**: `common-core/domain/vo/`에 있는 값 기반 동등성을 가진 불변 객체
  - ID: `DocumentId`, `CollectionId`, `UserId`, `TrackingId` (`BaseId<T>` 확장)
  - 기타: `ContentHash`, `Metadata`, `DocumentStatus`

- **도메인 이벤트**: `DomainEvent<T>` 인터페이스 구현
  - 상태 변경 시 애그리거트에서 발행
  - 신뢰할 수 있는 전달을 위해 아웃박스 테이블에 저장
  - 사가 추적을 위한 상관관계 ID 포함

### 분산 트랜잭션을 위한 사가 패턴

다단계 워크플로우는 보상 트랜잭션을 사용합니다:
- `SagaStep<T,S,U>` 인터페이스: `process()`와 `rollback()` 메서드
- 에러 분류가 재시도 vs. 보상을 결정
- 수동 개입이 필요한 영구 실패를 위한 데드 레터 큐(DLQ)

## 모듈 의존성

**의존성 규칙**:
- 서비스는 모든 `libs/*` 모듈에 의존할 수 있음
- `libs/*` 모듈은 절대 서비스에 의존하지 않음 (순수, 재사용 가능)
- 공통 라이브러리는 공유 도메인 추상화와 인프라 유틸리티를 제공

**서비스별 의존성**:
- `datahub`: 문서 저장, 오케스트레이션, Kafka 메시징, MySQL JPA
- `datarex`: 문서 변환, Spring AI (OpenAI 통합), 마크다운 파싱
- `vecdash`: 벡터 임베딩 관리

## 설정 관리

### 환경 변수

각 서비스는 `application.yml`을 통해 환경별 설정을 사용합니다:

**데이터베이스** (datahub, datarex):
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`

**Kafka**:
- `KAFKA_BOOTSTRAP_SERVERS` (기본값: `localhost:9094`)
- `KAFKA_CONSUMER_GROUP_ID`

**JPA**:
- `DDL_AUTO` (기본값: `update`)
- `SHOW_SQL` (기본값: `false`)

### 로컬 서비스 실행

1. 인프라 의존성 시작 (MySQL, Kafka):
   ```bash
   # 일반적으로 docker-compose를 통해 (etc/ 디렉토리 확인)
   ```

2. 환경 변수 설정 또는 `application.yml`의 기본값 사용

3. 서비스 실행:
   ```bash
   ./gradlew :service:datahub:bootRun
   ```

## 주요 아키텍처 파일

| 컴포넌트 | 위치 | 목적 |
|-----------|----------|---------|
| 애그리거트 루트 기본 클래스 | `libs/common-core/domain/entity/AggregateRoot.java` | 도메인 애그리거트의 기본 클래스 |
| 값 객체 기본 클래스 | `libs/common-core/domain/vo/BaseId.java` | 타입화된 ID의 기본 클래스 |
| 도메인 이벤트 인터페이스 | `libs/common-core/domain/event/DomainEvent.java` | 도메인 이벤트 계약 |
| 아웃박스 엔티티 | `libs/common-outbox/OutboxMessageJpaEntity.java` | 범용 아웃박스 테이블 구조 |
| 아웃박스 스케줄러 | `libs/common-outbox/OutboxScheduler.java` | 폴링 기반 메시지 전달 |
| 사가 스텝 | `libs/common-saga/SagaStep.java` | 사가 보상 인터페이스 |
| Kafka 토픽 | `libs/messaging-common/topics/KafkaTopics.java` | 중앙 집중식 토픽 정의 |

## 개발 워크플로우

> 브랜치 전략, 커밋 규칙, PR 프로세스는 [CONTRIBUTING.md](./CONTRIBUTING.md)를 참조하세요.

### 기능 개발 시 아키텍처 준수

기능 추가 시 헥사고날 아키텍처를 준수합니다:

1. **Domain Layer**: `domain/` 레이어에 도메인 로직 정의 (외부 의존성 없음)
2. **Input Port**: `application/port/in/service/`에 유스케이스 인터페이스 생성
3. **Output Port**: `application/port/out/`에 출력 포트 정의 (추상화)
4. **Adapter**: `infrastructure/adapter/`에 어댑터 구현 (기술 세부사항)

### 새로운 도메인 엔티티 추가

1. `domain/entity/`에서 `AggregateRoot<YourId>` 확장
2. `domain/vo/`에서 `BaseId<YourId>`를 확장하는 강타입 ID 생성
3. `domain/event/`에 도메인 이벤트 정의
4. `application/port/out/persistence/`에 리포지토리 포트 생성
5. `infrastructure/adapter/out/persistence/`에 JPA 어댑터 구현

### 새로운 이벤트 추가

1. `domain/event/`에 `DomainEvent<T>`를 구현하는 이벤트 클래스 정의
2. 서비스 간 이벤트라면 `messaging-common/events/`에 추가
3. `messaging-common/topics/KafkaTopics.java`에 Kafka 토픽 정의
4. 아웃박스 테이블 및 리포지토리 생성 (아웃박스 패턴 확장)
5. 아웃박스 폴링을 위한 스케줄러 추가
6. 컨슈머 서비스에 Kafka 리스너 구현

### 테스트 전략

- **도메인 테스트**: 애그리거트와 값 객체에 대한 순수 단위 테스트
- **서비스 테스트**: 포트 모킹으로 유스케이스 테스트
- **통합 테스트**: 실제 데이터베이스/Kafka로 테스트 (Testcontainers 사용)
- **계약 테스트**: 서비스 간 Kafka 이벤트 스키마 검증

## 협업 및 코드 리뷰

**핵심 규칙**:
- `main` 브랜치로 직접 푸시 금지 (PR을 통해서만)
- Conventional Commits 준수 (`feat:`, `fix:`, `refactor:` 등)
- PR 제목: `feat(datahub): Add document versioning`
- 모든 테스트 통과 필수: `./gradlew test`

**상세 문서**:
- 브랜치 전략, 커밋 컨벤션: [CONTRIBUTING.md](./CONTRIBUTING.md)
- PR 템플릿: [.github/pull_request_template.md](./.github/pull_request_template.md)

## 자주 사용하는 Gradle 작업

```bash
# 모든 프로젝트 목록 조회
./gradlew projects

# 의존성 확인
./gradlew :service:datahub:dependencies

# 특정 라이브러리 빌드
./gradlew :libs:common-core:build

# 특정 프로파일로 실행
./gradlew :service:datahub:bootRun --args='--spring.profiles.active=dev'
```

## 코드 스타일

- **주석**: 과도한 주석 지양, 복잡한 비즈니스 로직만 주석 작성
- **네이밍**: camelCase (변수/메서드), PascalCase (클래스)
- 상세한 코드 스타일 가이드는 [CONTRIBUTING.md](./CONTRIBUTING.md) 참조