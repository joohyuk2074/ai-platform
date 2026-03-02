package me.joohyuk.datahub.infrastructure.adapter.in.web.exception;

import com.spartaecommerce.exception.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 전역 예외 처리 핸들러
 *
 * <p>빅테크 기업의 모범 사례를 적용한 예외 처리 전략:
 * <ul>
 *   <li>RFC 7807 Problem Details 표준 준수</li>
 *   <li>계층별 예외 매핑 (Domain → Application → HTTP)</li>
 *   <li>보안을 위한 정보 제어 (내부 상세정보는 로그에만 기록)</li>
 *   <li>추적 가능성 (traceId를 통한 로그 연관)</li>
 *   <li>구조화된 로깅 (MDC 활용)</li>
 * </ul>
 *
 * <p>참고: Google, Netflix, Amazon 등 빅테크 기업의 API 가이드라인 기반
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private static final String TRACE_ID_KEY = "traceId";
  private static final String ERROR_TYPE_PREFIX = "https://api.datahub.com/errors/";

  /**
   * 도메인 예외 처리
   *
   * <p>비즈니스 로직에서 발생하는 예외를 처리합니다.
   * 도메인 에러 코드에 따라 적절한 HTTP 상태 코드로 매핑됩니다.
   */
  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> handleDomainException(
      DomainException ex,
      HttpServletRequest request
  ) {
    String traceId = getOrCreateTraceId();
    String errorCode = ex.getErrorCode().code();

    // HTTP 상태 코드 매핑 (도메인 에러 코드 기반)
    HttpStatus status = mapDomainErrorToHttpStatus(errorCode);

    // 상세 로깅 (서버 로그에만 기록)
    log.warn(
        "[traceId={}] Domain exception occurred - errorCode: {}, message: {}, status: {}",
        traceId,
        errorCode,
        ex.getMessage(),
        status.value(),
        ex
    );

    // 클라이언트 응답 (안전한 메시지만 포함)
    ErrorResponse response = ErrorResponse.of(
        ERROR_TYPE_PREFIX + "domain/" + errorCode.toLowerCase().replace('_', '-'),
        "Business Rule Violation",
        status.value(),
        ex.getMessage() != null ? ex.getMessage() : "A business rule was violated",
        request.getRequestURI(),
        traceId,
        errorCode
    );

    return ResponseEntity.status(status).body(response);
  }

  /**
   * Bean Validation 예외 처리 (@Valid, @Validated)
   *
   * <p>요청 본문의 유효성 검증 실패 시 발생
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpServletRequest request
  ) {
    String traceId = getOrCreateTraceId();

    // 모든 필드 에러를 수집
    String detail = ex.getBindingResult()
        .getAllErrors()
        .stream()
        .map(error -> {
          if (error instanceof FieldError fieldError) {
            return String.format("%s: %s", fieldError.getField(), error.getDefaultMessage());
          }
          return error.getDefaultMessage();
        })
        .collect(Collectors.joining(", "));

    log.warn(
        "[traceId={}] Validation failed - {} field errors: {}",
        traceId,
        ex.getErrorCount(),
        detail
    );

    ErrorResponse response = ErrorResponse.of(
        ERROR_TYPE_PREFIX + "validation/request-body",
        "Validation Failed",
        HttpStatus.BAD_REQUEST.value(),
        "Request validation failed: " + detail,
        request.getRequestURI(),
        traceId
    );

    return ResponseEntity.badRequest().body(response);
  }

  /**
   * Constraint Violation 예외 처리 (@PathVariable, @RequestParam 검증)
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      ConstraintViolationException ex,
      HttpServletRequest request
  ) {
    String traceId = getOrCreateTraceId();

    String detail = ex.getConstraintViolations()
        .stream()
        .map(violation -> {
          String propertyPath = violation.getPropertyPath().toString();
          // 메서드명 제거하고 파라미터명만 추출
          String paramName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
          return String.format("%s: %s", paramName, violation.getMessage());
        })
        .collect(Collectors.joining(", "));

    log.warn("[traceId={}] Constraint violation: {}", traceId, detail);

    ErrorResponse response = ErrorResponse.of(
        ERROR_TYPE_PREFIX + "validation/constraint",
        "Constraint Violation",
        HttpStatus.BAD_REQUEST.value(),
        "Constraint validation failed: " + detail,
        request.getRequestURI(),
        traceId
    );

    return ResponseEntity.badRequest().body(response);
  }

  /**
   * 잘못된 요청 본문 처리 (JSON 파싱 실패 등)
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpServletRequest request
  ) {
    String traceId = getOrCreateTraceId();

    log.warn("[traceId={}] Malformed request body: {}", traceId, ex.getMessage());

    ErrorResponse response = ErrorResponse.of(
        ERROR_TYPE_PREFIX + "validation/malformed-request",
        "Malformed Request",
        HttpStatus.BAD_REQUEST.value(),
        "Request body is not readable or malformed",
        request.getRequestURI(),
        traceId
    );

    return ResponseEntity.badRequest().body(response);
  }

  /**
   * 필수 파라미터 누락 처리
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex,
      HttpServletRequest request
  ) {
    String traceId = getOrCreateTraceId();

    log.warn(
        "[traceId={}] Missing required parameter: {} ({})",
        traceId,
        ex.getParameterName(),
        ex.getParameterType()
    );

    ErrorResponse response = ErrorResponse.of(
        ERROR_TYPE_PREFIX + "validation/missing-parameter",
        "Missing Required Parameter",
        HttpStatus.BAD_REQUEST.value(),
        String.format("Required parameter '%s' is missing", ex.getParameterName()),
        request.getRequestURI(),
        traceId
    );

    return ResponseEntity.badRequest().body(response);
  }

  /**
   * 파라미터 타입 불일치 처리
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex,
      HttpServletRequest request
  ) {
    String traceId = getOrCreateTraceId();

    log.warn(
        "[traceId={}] Type mismatch for parameter '{}': expected {}, got {}",
        traceId,
        ex.getName(),
        ex.getRequiredType(),
        ex.getValue()
    );

    ErrorResponse response = ErrorResponse.of(
        ERROR_TYPE_PREFIX + "validation/type-mismatch",
        "Type Mismatch",
        HttpStatus.BAD_REQUEST.value(),
        String.format(
            "Parameter '%s' should be of type %s",
            ex.getName(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        ),
        request.getRequestURI(),
        traceId
    );

    return ResponseEntity.badRequest().body(response);
  }

  /**
   * 예상하지 못한 모든 예외 처리 (Fallback)
   *
   * <p>보안을 위해 상세한 에러 정보는 로그에만 기록하고,
   * 클라이언트에는 일반적인 메시지만 반환합니다.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
      Exception ex,
      HttpServletRequest request
  ) {
    String traceId = getOrCreateTraceId();

    // 상세한 스택 트레이스를 포함하여 로깅 (보안상 클라이언트에는 노출하지 않음)
    log.error(
        "[traceId={}] Unexpected error occurred - type: {}, message: {}",
        traceId,
        ex.getClass().getName(),
        ex.getMessage(),
        ex
    );

    // 클라이언트에는 일반적인 메시지만 반환 (보안)
    ErrorResponse response = ErrorResponse.of(
        ERROR_TYPE_PREFIX + "internal-error",
        "Internal Server Error",
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "An unexpected error occurred. Please contact support with trace ID: " + traceId,
        request.getRequestURI(),
        traceId
    );

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  /**
   * 도메인 에러 코드를 HTTP 상태 코드로 매핑
   *
   * <p>컨벤션 기반 매핑:
   * <ul>
   *   <li>NOT_FOUND로 끝나는 경우 → 404</li>
   *   <li>ALREADY_EXISTS로 끝나는 경우 → 409</li>
   *   <li>UNAUTHORIZED로 끝나는 경우 → 401</li>
   *   <li>FORBIDDEN으로 끝나는 경우 → 403</li>
   *   <li>INVALID로 시작하는 경우 → 400</li>
   *   <li>그 외 → 422 (Unprocessable Entity)</li>
   * </ul>
   */
  private HttpStatus mapDomainErrorToHttpStatus(String errorCode) {
    if (errorCode.endsWith("NOT_FOUND")) {
      return HttpStatus.NOT_FOUND;
    } else if (errorCode.endsWith("ALREADY_EXISTS")) {
      return HttpStatus.CONFLICT;
    } else if (errorCode.endsWith("UNAUTHORIZED")) {
      return HttpStatus.UNAUTHORIZED;
    } else if (errorCode.endsWith("FORBIDDEN")) {
      return HttpStatus.FORBIDDEN;
    } else if (errorCode.startsWith("INVALID")) {
      return HttpStatus.BAD_REQUEST;
    }
    // 기본값: 비즈니스 로직 에러는 422 Unprocessable Entity
    return HttpStatus.UNPROCESSABLE_ENTITY;
  }

  /**
   * Trace ID 획득 또는 생성
   *
   * <p>MDC(Mapped Diagnostic Context)에서 기존 traceId를 가져오거나,
   * 없으면 새로 생성합니다. 분산 추적 시스템(Zipkin, Jaeger 등)과 연동 가능합니다.
   */
  private String getOrCreateTraceId() {
    String traceId = MDC.get(TRACE_ID_KEY);
    if (traceId == null || traceId.isBlank()) {
      traceId = UUID.randomUUID().toString();
      MDC.put(TRACE_ID_KEY, traceId);
    }
    return traceId;
  }
}
