# Exception Handling Guide

## 📋 목차

1. [아키텍처 개요](#아키텍처-개요)
2. [에러 응답 형식](#에러-응답-형식)
3. [사용 방법](#사용-방법)
4. [로깅과 추적](#로깅과-추적)
5. [모범 사례](#모범-사례)

## 🏗️ 아키텍처 개요

### 핵심 컴포넌트

```
┌─────────────────────────────────────────────────────────┐
│                    HTTP Request                          │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│  TraceIdFilter (Infrastructure Layer)                    │
│  - X-Trace-Id 헤더 확인 또는 생성                          │
│  - MDC에 traceId, spanId 설정                            │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│  Controller (Presentation Layer)                         │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│  Service (Application Layer)                             │
│  - 비즈니스 로직 수행                                       │
│  - DomainException 발생 가능                              │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│  Domain (Domain Layer)                                   │
│  - 도메인 규칙 검증                                         │
│  - DomainException 발생 가능                              │
└──────────────────────┬──────────────────────────────────┘
                       │
                       │ Exception 발생
                       ▼
┌─────────────────────────────────────────────────────────┐
│  GlobalExceptionHandler (Infrastructure Layer)           │
│  - DomainException → HTTP Status 매핑                    │
│  - RFC 7807 ErrorResponse 생성                           │
│  - 상세 로깅 (MDC traceId 포함)                           │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│                 HTTP Response (JSON)                     │
│  {                                                       │
│    "type": "https://api.datahub.com/errors/...",        │
│    "title": "Error Title",                              │
│    "status": 404,                                       │
│    "detail": "Detailed error message",                  │
│    "instance": "/api/v1/documents/123",                 │
│    "timestamp": "2025-01-15T10:30:45.123Z",             │
│    "traceId": "a1b2c3d4-...",                           │
│    "errorCode": "DOCUMENT_NOT_FOUND"                    │
│  }                                                       │
└─────────────────────────────────────────────────────────┘
```

### 설계 원칙 (Google, Netflix, Amazon 기반)

1. **RFC 7807 Problem Details** - 표준화된 에러 응답 형식
2. **계층별 책임 분리** - Domain → Application → Infrastructure
3. **보안 우선** - 내부 상세정보는 로그에만, 클라이언트에는 안전한 메시지만
4. **추적 가능성** - traceId를 통한 분산 추적
5. **구조화된 로깅** - MDC와 JSON 로그 활용

## 📦 에러 응답 형식

### RFC 7807 Problem Details

모든 에러는 표준화된 JSON 형식으로 반환됩니다:

```json
{
  "type": "https://api.datahub.com/errors/domain/document-not-found",
  "title": "Business Rule Violation",
  "status": 404,
  "detail": "Document not found with id: doc-12345",
  "instance": "/api/v1/documents/doc-12345",
  "timestamp": "2025-01-15T10:30:45.123456Z",
  "traceId": "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8",
  "errorCode": "DOCUMENT_NOT_FOUND"
}
```

### 필드 설명

| 필드 | 타입 | 설명 |
|------|------|------|
| `type` | String | 에러 타입을 식별하는 URI |
| `title` | String | 사람이 읽을 수 있는 짧은 제목 |
| `status` | Integer | HTTP 상태 코드 |
| `detail` | String | 에러에 대한 상세 설명 (사용자에게 표시 가능) |
| `instance` | String | 에러가 발생한 요청 경로 |
| `timestamp` | ISO 8601 | 에러 발생 시각 |
| `traceId` | String | 로그 추적을 위한 고유 ID |
| `errorCode` | String (Optional) | 도메인 에러 코드 (비즈니스 로직 에러인 경우) |

## 🔨 사용 방법

### 1. 도메인 에러 코드 정의

```java
// DatahubDomainErrorCode.java
public enum DatahubDomainErrorCode implements DomainErrorCode {

  // 네이밍 컨벤션에 따라 자동으로 HTTP 상태 코드 매핑
  DOCUMENT_NOT_FOUND("DOCUMENT_NOT_FOUND"),              // → 404
  DOCUMENT_ALREADY_EXISTS("DOCUMENT_ALREADY_EXISTS"),    // → 409
  INVALID_DOCUMENT_FORMAT("INVALID_DOCUMENT_FORMAT"),    // → 400
  COLLECTION_ACCESS_FORBIDDEN("COLLECTION_ACCESS_FORBIDDEN"), // → 403
  DOCUMENT_PROCESSING_FAILED("DOCUMENT_PROCESSING_FAILED"),  // → 422

  // ...
}
```

### 2. 도메인 계층에서 예외 발생

```java
// DocumentDomainService.java
@Service
public class DocumentDomainService {

  public void validateDocumentExists(DocumentId documentId, Document document) {
    if (document == null) {
      throw new DomainException(
          "Document not found with id: " + documentId.value(),
          DatahubDomainErrorCode.DOCUMENT_NOT_FOUND
      );
    }
  }

  public void validateDocumentFormat(String fileType) {
    if (!SUPPORTED_FORMATS.contains(fileType)) {
      throw new DomainException(
          "Unsupported file format: " + fileType + ". Supported: " + SUPPORTED_FORMATS,
          DatahubDomainErrorCode.INVALID_DOCUMENT_FORMAT
      );
    }
  }
}
```

### 3. 애플리케이션 계층에서 예외 발생

```java
// UploadDocumentService.java
@Service
@RequiredArgsConstructor
public class UploadDocumentService implements UploadDocumentUseCase {

  private final DocumentRepository documentRepository;
  private final DocumentDomainService domainService;

  @Override
  public DocumentResponse uploadDocument(UploadDocumentCommand command) {
    // 중복 체크
    if (documentRepository.existsByName(command.fileName())) {
      throw new DomainException(
          "Document already exists: " + command.fileName(),
          DatahubDomainErrorCode.DOCUMENT_ALREADY_EXISTS
      );
    }

    // 도메인 서비스를 통한 검증
    domainService.validateDocumentFormat(command.fileType());

    // 비즈니스 로직...
  }
}
```

### 4. 자동 예외 처리

GlobalExceptionHandler가 자동으로 예외를 처리하고 적절한 HTTP 응답을 반환합니다:

```java
// Controller에서는 별도 예외 처리 불필요
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

  private final UploadDocumentUseCase uploadDocumentUseCase;

  @PostMapping
  public ResponseEntity<DocumentResponse> uploadDocument(
      @Valid @RequestBody UploadDocumentRequest request
  ) {
    // 예외가 발생하면 GlobalExceptionHandler가 자동 처리
    DocumentResponse response = uploadDocumentUseCase.uploadDocument(
        request.toCommand()
    );
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
```

## 📊 로깅과 추적

### TraceId 기반 분산 추적

모든 요청은 고유한 `traceId`를 가지며, 이를 통해 전체 요청 흐름을 추적할 수 있습니다.

#### 1. 자동 TraceId 생성

```
Client → Server: POST /api/v1/documents
                 (X-Trace-Id 헤더 없음)

TraceIdFilter: UUID 생성 → MDC에 설정
               traceId: a1b2c3d4-e5f6-7890
               spanId: x9y8z7w6-v5u4-t3s2

Server → Client: HTTP 201 Created
                 X-Trace-Id: a1b2c3d4-e5f6-7890
```

#### 2. 클라이언트가 제공하는 TraceId (마이크로서비스 간 전파)

```
Service A → Service B: POST /api/v1/documents
                       X-Trace-Id: existing-trace-id

TraceIdFilter: 기존 traceId 사용 → MDC에 설정
               traceId: existing-trace-id
               spanId: new-span-id (새로 생성)
```

### 로그 형식

#### 개발 환경 (컬러 콘솔)

```
2025-01-15 10:30:45.123 INFO [a1b2c3d4-e5f6-7890] [x9y8z7w6-v5u4] [http-nio-8082-exec-1] [c.s.d.i.a.i.w.DocumentController] - Document upload requested: doc-12345.pdf
2025-01-15 10:30:45.456 WARN [a1b2c3d4-e5f6-7890] [x9y8z7w6-v5u4] [http-nio-8082-exec-1] [c.s.d.i.a.i.w.e.GlobalExceptionHandler] - Domain exception - errorCode: DOCUMENT_NOT_FOUND, message: Document not found with id: doc-12345
```

#### 프로덕션 환경 (JSON)

```json
{
  "@timestamp": "2025-01-15T10:30:45.123Z",
  "level": "WARN",
  "traceId": "a1b2c3d4-e5f6-7890",
  "spanId": "x9y8z7w6-v5u4-t3s2",
  "thread": "http-nio-8082-exec-1",
  "logger": "c.s.d.i.a.i.w.e.GlobalExceptionHandler",
  "message": "Domain exception occurred",
  "errorCode": "DOCUMENT_NOT_FOUND",
  "status": 404,
  "service": "datahub",
  "environment": "prod"
}
```

### ELK Stack 연동 예시

Elasticsearch 쿼리로 특정 요청의 모든 로그 추적:

```json
GET /logs-*/_search
{
  "query": {
    "match": {
      "traceId": "a1b2c3d4-e5f6-7890"
    }
  },
  "sort": [
    { "@timestamp": "asc" }
  ]
}
```

## 🎯 모범 사례

### 1. 에러 코드 네이밍 컨벤션

HTTP 상태 코드 자동 매핑을 위해 다음 규칙을 따르세요:

| 네이밍 패턴 | HTTP 상태 | 예시 |
|------------|-----------|------|
| `*_NOT_FOUND` | 404 | `DOCUMENT_NOT_FOUND` |
| `*_ALREADY_EXISTS` | 409 | `DOCUMENT_ALREADY_EXISTS` |
| `*_UNAUTHORIZED` | 401 | `USER_UNAUTHORIZED` |
| `*_FORBIDDEN` | 403 | `COLLECTION_ACCESS_FORBIDDEN` |
| `INVALID_*` | 400 | `INVALID_DOCUMENT_FORMAT` |
| 기타 | 422 | `DOCUMENT_PROCESSING_FAILED` |

### 2. 예외 메시지 작성 가이드

```java
// ❌ 나쁜 예: 불충분한 정보
throw new DomainException(
    "Document not found",
    DatahubDomainErrorCode.DOCUMENT_NOT_FOUND
);

// ✅ 좋은 예: 충분한 컨텍스트 제공
throw new DomainException(
    String.format(
        "Document not found with id: %s for user: %s",
        documentId.value(),
        userId.value()
    ),
    DatahubDomainErrorCode.DOCUMENT_NOT_FOUND
);

// ✅ 좋은 예: 해결 방법 힌트 제공
throw new DomainException(
    String.format(
        "Unsupported file format: %s. Supported formats: %s",
        fileType,
        String.join(", ", SUPPORTED_FORMATS)
    ),
    DatahubDomainErrorCode.INVALID_DOCUMENT_FORMAT
);
```

### 3. 보안 고려사항

```java
// ❌ 나쁜 예: 민감한 정보 노출
throw new DomainException(
    "Database connection failed: username=admin, password=secret123",
    DatahubDomainErrorCode.DATABASE_ERROR
);

// ✅ 좋은 예: 안전한 메시지
throw new DomainException(
    "Database connection failed. Please contact support with trace ID",
    DatahubDomainErrorCode.DATABASE_ERROR
);
// 상세한 에러는 로그에만 기록 (GlobalExceptionHandler가 자동 처리)
```

### 4. 계층별 책임

```java
// Domain Layer: 도메인 규칙 검증
public class Document {
  public void validateContent() {
    if (content == null || content.isEmpty()) {
      throw new DomainException(
          "Document content cannot be empty",
          DatahubDomainErrorCode.INVALID_DOCUMENT_FORMAT
      );
    }
  }
}

// Application Layer: 비즈니스 로직 조율
@Service
public class UploadDocumentService {
  public DocumentResponse uploadDocument(UploadDocumentCommand command) {
    // 외부 시스템 연동 실패 시
    if (!storageService.isAvailable()) {
      throw new DomainException(
          "Storage service is temporarily unavailable",
          DatahubDomainErrorCode.STORAGE_UNAVAILABLE
      );
    }
  }
}

// Infrastructure Layer: 기술적 예외 변환
@Repository
public class DocumentRepositoryImpl implements DocumentRepository {
  public Document save(Document document) {
    try {
      return jpaRepository.save(toEntity(document));
    } catch (DataIntegrityViolationException e) {
      throw new DomainException(
          "Duplicate document name: " + document.getName(),
          DatahubDomainErrorCode.DOCUMENT_ALREADY_EXISTS,
          e
      );
    }
  }
}
```

### 5. 클라이언트 측 에러 처리

```typescript
// TypeScript 클라이언트 예시
interface ErrorResponse {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
  timestamp: string;
  traceId: string;
  errorCode?: string;
}

async function uploadDocument(file: File) {
  try {
    const response = await fetch('/api/v1/documents', {
      method: 'POST',
      body: formData,
      headers: {
        // 분산 추적을 위해 traceId 전파
        'X-Trace-Id': getCurrentTraceId(),
      },
    });

    if (!response.ok) {
      const error: ErrorResponse = await response.json();

      // errorCode 기반 처리
      switch (error.errorCode) {
        case 'DOCUMENT_ALREADY_EXISTS':
          showWarning('This document already exists.');
          break;
        case 'INVALID_DOCUMENT_FORMAT':
          showError('Please upload a supported file format.');
          break;
        default:
          showError(`Error: ${error.detail} (Trace ID: ${error.traceId})`);
      }
    }
  } catch (error) {
    // 네트워크 에러 등
    showError('Network error. Please try again.');
  }
}
```

## 📈 모니터링과 알림

### Prometheus Metrics (향후 확장)

```java
// 에러 발생 빈도 추적
@Component
public class ErrorMetrics {
  private final Counter errorCounter;

  public ErrorMetrics(MeterRegistry registry) {
    this.errorCounter = Counter.builder("datahub.errors")
        .tag("service", "datahub")
        .description("Total number of errors")
        .register(registry);
  }

  public void recordError(String errorCode, int httpStatus) {
    errorCounter.increment();
    // errorCode, httpStatus별 세분화된 메트릭 기록
  }
}
```

### 알림 설정 예시 (Slack, PagerDuty 등)

```java
// 특정 에러 발생 시 알림 (Critical 에러)
@Component
public class CriticalErrorNotifier {

  @Async
  public void notifyIfCritical(DomainException ex, String traceId) {
    if (isCritical(ex.getErrorCode())) {
      slackClient.sendAlert(
          "🚨 Critical Error in Datahub",
          "ErrorCode: " + ex.getErrorCode().code(),
          "TraceId: " + traceId,
          "Message: " + ex.getMessage()
      );
    }
  }
}
```

## 🔗 참고 자료

- [RFC 7807 - Problem Details for HTTP APIs](https://tools.ietf.org/html/rfc7807)
- [Google Cloud - Error Handling](https://cloud.google.com/apis/design/errors)
- [Microsoft REST API Guidelines - Error Handling](https://github.com/microsoft/api-guidelines/blob/vNext/Guidelines.md#7102-error-condition-responses)
- [Amazon API Gateway - Error Responses](https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-error-responses.html)
- [Netflix Hystrix - Fault Tolerance](https://github.com/Netflix/Hystrix/wiki)
