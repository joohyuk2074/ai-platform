package me.joohyuk.datahub.infrastructure.adapter.in.web.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/**
 * RFC 7807 Problem Details for HTTP APIs 표준을 따르는 에러 응답 객체
 *
 * <p>빅테크 기업에서 널리 사용하는 에러 응답 구조로, 다음을 제공합니다:
 * <ul>
 *   <li>표준화된 에러 형식으로 클라이언트의 일관된 에러 처리 지원</li>
 *   <li>추적 가능성을 위한 traceId</li>
 *   <li>보안을 위해 내부 구현 세부사항 숨김</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    /**
     * 에러 타입을 식별하는 URI
     */
    String type,

    /**
     * 사람이 읽을 수 있는 짧은 에러 제목
     */
    String title,

    /**
     * HTTP 상태 코드
     */
    int status,

    /**
     * 에러에 대한 상세 설명 (사용자에게 표시 가능)
     */
    String detail,

    /**
     * 에러가 발생한 요청 경로
     */
    String instance,

    /**
     * 에러 발생 시각
     */
    Instant timestamp,

    /**
     * 로그 추적을 위한 고유 ID (분산 추적 시스템과 연동 가능)
     */
    String traceId,

    /**
     * 도메인 에러 코드 (선택적, 비즈니스 로직 에러인 경우)
     */
    String errorCode
) {

  /**
   * 기본 에러 응답 생성 (errorCode 없음)
   */
  public static ErrorResponse of(
      String type,
      String title,
      int status,
      String detail,
      String instance,
      String traceId
  ) {
    return new ErrorResponse(
        type,
        title,
        status,
        detail,
        instance,
        Instant.now(),
        traceId,
        null
    );
  }

  /**
   * 도메인 에러 코드를 포함한 에러 응답 생성
   */
  public static ErrorResponse of(
      String type,
      String title,
      int status,
      String detail,
      String instance,
      String traceId,
      String errorCode
  ) {
    return new ErrorResponse(
        type,
        title,
        status,
        detail,
        instance,
        Instant.now(),
        traceId,
        errorCode
    );
  }
}
