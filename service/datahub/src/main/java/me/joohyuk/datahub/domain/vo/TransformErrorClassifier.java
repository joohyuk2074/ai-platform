package me.joohyuk.datahub.domain.vo;

import java.util.Set;

/**
 * 문서 변환 오류를 분류하여 재시도 가능 여부를 판단합니다.
 */
public class TransformErrorClassifier {

  // 재시도 가능한 오류 코드
  private static final Set<String> RETRYABLE_ERROR_CODES = Set.of(
      "NETWORK_ERROR",
      "TIMEOUT",
      "CONNECTION_ERROR",
      "SERVICE_UNAVAILABLE",
      "TEMPORARY_ERROR",
      "RATE_LIMIT_EXCEEDED"
  );

  // DLQ로 전송해야 하는 영구적 오류 코드
  private static final Set<String> PERMANENT_ERROR_CODES = Set.of(
      "INVALID_FORMAT",
      "VALIDATION_ERROR",
      "UNSUPPORTED_FILE_TYPE",
      "FILE_TOO_LARGE",
      "CORRUPTED_FILE",
      "PARSING_ERROR",
      "BUSINESS_RULE_VIOLATION"
  );

  /**
   * 에러 코드가 재시도 가능한지 판단합니다.
   *
   * @param errorCode 에러 코드
   * @return 재시도 가능하면 true, 아니면 false
   */
  public static boolean isRetryable(String errorCode) {
    if (errorCode == null || errorCode.isBlank()) {
      return false;
    }
    return RETRYABLE_ERROR_CODES.contains(errorCode);
  }

  /**
   * 에러 코드가 영구적 오류인지 판단합니다. (DLQ 전송 대상)
   *
   * @param errorCode 에러 코드
   * @return 영구적 오류이면 true, 아니면 false
   */
  public static boolean isPermanent(String errorCode) {
    if (errorCode == null || errorCode.isBlank()) {
      return false;
    }
    return PERMANENT_ERROR_CODES.contains(errorCode);
  }

  /**
   * 에러 코드가 알 수 없는 오류인지 판단합니다. 알 수 없는 오류는 안전하게 DLQ로 전송합니다.
   *
   * @param errorCode 에러 코드
   * @return 알 수 없는 오류이면 true, 아니면 false
   */
  public static boolean isUnknown(String errorCode) {
    if (errorCode == null || errorCode.isBlank()) {
      return true;
    }
    return !isRetryable(errorCode) && !isPermanent(errorCode);
  }
}
